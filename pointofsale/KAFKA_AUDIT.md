# Kafka Audit — Panduan Lengkap Event & Notifikasi Email (Vert.x Point of Sale)

Dokumen ini adalah **audit menyeluruh** pemakaian **Apache Kafka** pada project
**Vert.x Point of Sale** (Java 21, Vert.x 4.5.24, gRPC, PostgreSQL, Redis,
Kafka). Berbeda dari dokumen alur service (`ORDER_TRANSACTION.md`) yang fokus
pada order/transaction, dokumen ini fokus pada **lapisan event-driven**:
infrastruktur broker, producer, katalog topik, format payload, consumer,
semantik pengiriman, deduplikasi, chaos engineering, observability, dan
langkah operasional.

> **Rangkuman singkat:** Kafka di project ini dipakai untuk **satu tujuan** —
> notifikasi email asinkron. Tiga service mempublikasikan event
> (**auth**, **merchant**, **transaction**) ke **8 topik** bernama
> `email-service-topic-*`, dan satu service (**email**) menjadi satu-satunya
> consumer yang mengirim email via SMTP. Tidak ada pola transactional outbox,
> tidak ada event bus domain, tidak ada CDC, dan tidak ada konsumen lain.

> **Pemutakhiran dokumen:** diselaraskan dengan implementasi aktual:
> vertx-kafka-client 4.5.24, broker KRaft single-node `apache/kafka:3.9.0`,
> producer `acks=1`, consumer `group.id=email-service-group` dengan guard
> dedup Redis (`EmailDedupGuard`, TTL 24 jam, fail-open), serta semua 8 topik
> yang di-subscribe `EmailVerticle`.

---

## Daftar Isi

1. [Arsitektur Event-Driven](#1-arsitektur-event-driven)
2. [Infrastruktur Kafka](#2-infrastruktur-kafka)
3. [Dependensi Maven](#3-dependensi-maven)
4. [Producer — KafkaConfig & KafkaService](#4-producer--kafkaconfig--kafkaservice)
5. [Katalog Topik](#5-katalog-topik)
6. [Detail Event per Topik](#6-detail-event-per-topik)
7. [Consumer — Email Service](#7-consumer--email-service)
8. [Semantik Pengiriman & Guarantees](#8-semantik-pengiriman--guarantees)
9. [Deduplikasi (EmailDedupGuard)](#9-deduplikasi-emaildedupguard)
10. [Chaos Engineering pada Kafka](#10-chaos-engineering-pada-kafka)
11. [Observability](#11-observability)
12. [Operasional (CLI)](#12-operasional-cli)
13. [Skenario Gagal & Mitigasi](#13-skenario-gagal--mitigasi)
14. [Kandidat Peningkatan (Roadmap)](#14-kandidat-peningkatan-roadmap)
15. [Referensi Silang](#15-referensi-silang)

---

## 1. Arsitektur Event-Driven

```mermaid
graph LR
    subgraph Producers["Producer (3 service)"]
        AUTH["auth service<br/>(RegisterService · PasswordResetService)"]
        MER["merchant service<br/>(MerchantCommandService · MerchantDocumentCommandService)"]
        TS["transaction service<br/>(TransactionCommandService)"]
    end

    subgraph Kafka["Apache Kafka (KRaft, single-node)<br/>apache/kafka:3.9.0 · :9092"]
        T1["email-service-topic-auth-register"]
        T2["email-service-topic-auth-forgot-password"]
        T3["email-service-topic-auth-verify-code-success"]
        T4["email-service-topic-merchant-create"]
        T5["email-service-topic-merchant-update-status"]
        T6["email-service-topic-merchant-document-create"]
        T7["email-service-topic-merchant-document-update-status"]
        T8["email-service-topic-transaction-create"]
    end

    subgraph Consumer["Consumer (1 service)"]
        EMAIL["email service<br/>(EmailVerticle · group email-service-group)"]
        SMTP["SMTP Server<br/>(SMTP_SERVER/SMTP_PORT/USER/PASS · STARTTLS)"]
        RD["Redis<br/>(email:dedup:… TTL 24h)"]
    end

    AUTH -->|produce · acks=1| T1
    AUTH -->|produce| T2
    AUTH -->|produce| T3
    MER -->|produce| T4
    MER -->|produce| T5
    MER -->|produce| T6
    MER -->|produce| T7
    TS -->|produce| T8

    T1 -->|subscribe| EMAIL
    T2 -->|subscribe| EMAIL
    T3 -->|subscribe| EMAIL
    T4 -->|subscribe| EMAIL
    T5 -->|subscribe| EMAIL
    T6 -->|subscribe| EMAIL
    T7 -->|subscribe| EMAIL
    T8 -->|subscribe| EMAIL

    EMAIL -->|cek/klaim key| RD
    EMAIL -->|MailClient · setHtml(body)| SMTP
```

**Pola:** **fan-out sederhana ke satu consumer group.** Semua topik dibuat
otomatis oleh broker (`KAFKA_AUTO_CREATE_TOPICS_ENABLE=true`) saat pertama
kali diproduksi/di-subscribe — tidak ada setup topik manual.

---

## 2. Infrastruktur Kafka

### 2.1 Docker Compose (`deployments/local/docker-compose.yml`)

| Aspek | Nilai |
|---|---|
| Image | `apache/kafka:3.9.0` |
| Container | `my-kafka` |
| Mode | **KRaft** single-node (broker + controller dalam satu proses) |
| Listener | `PLAINTEXT://:9092` + `CONTROLLER://:9093` |
| Advertised | `PLAINTEXT://kafka:9092` (hostname service compose) |
| Auto-create topik | `KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"` |
| Replication factor | 1 (topik internal: offsets, transaction state) |
| Cluster ID | `kraft-cluster-01` |
| Volume | `kafka_data` (persisten) |

```yaml
kafka:
  image: apache/kafka:3.9.0
  container_name: my-kafka
  environment:
    KAFKA_NODE_ID: 1
    KAFKA_PROCESS_ROLES: broker,controller
    KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
    KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
    KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
    KAFKA_AUTO_CREATE_TOPICS_ENABLE: "true"
    KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
    KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,CONTROLLER:PLAINTEXT
    KAFKA_LOG_DIRS: /tmp/kraft-combined-logs
    KAFKA_CLUSTER_ID: kraft-cluster-01
    KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
    KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
    KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
  healthcheck:
    test: ["CMD-SHELL", "/opt/kafka/bin/kafka-broker-api-versions.sh --bootstrap-server localhost:9092 || exit 1"]
```

> **Catatan keamanan:** listener memakai `PLAINTEXT` (tanpa TLS) dan tidak ada
> autentikasi/SASL. Ini wajar untuk dev lokal, tetapi untuk produksi sebaiknya
> minimal memakai SASL/SCRAM atau network policy (lihat kandidat peningkatan).

### 2.2 Environment Variable

Semua service producer/consumer membaca address broker dari satu env:

| Env | Default | Lokasi |
|---|---|---|
| `KAFKA_BROKERS` | `localhost:9092` | `docker.env.example` → `docker.env` (compose: `kafka:9092`); K8s: `kafka.point-of-sale.svc.cluster.local:9092` (`deployments/kubernetes/base/common/configmaps.yaml`) |

Service yang menerima env ini di compose: **auth, merchant, transaction,
email** (service lain — user, role, cashier, category, product, order,
order_item, apigateway, db-migration — **tidak** memakai Kafka).

### 2.3 Kubernetes (`deployments/kubernetes/`)

- Broker di-deploy sebagai **Deployment** `apache/kafka:3.7.0` (mode KRaft,
  `deployments/kubernetes/base/kafka/kafka-deployment.yaml`) + `Service`
  (`kafka-service.yaml`) + `PersistentVolumeClaim` (`kafka-pvc.yaml`) +
  `kafka-exporter` (deployment & service), semuanya masuk ke base
  `kustomization.yaml` (entri `- kafka/`).
- Service diakses via DNS `kafka.point-of-sale.svc.cluster.local:9092` yang
  dimasukkan ke ConfigMap `common/configmaps.yaml` (env `KAFKA_BROKERS`).

---

## 3. Dependensi Maven

Vert.x Kafka client di-deklarasi di **parent POM** (`pom.xml`) dengan versi
mengikuti `vertx.version` (saat ini **4.5.24**):

```xml
<!-- pom.xml (dependencyManagement) -->
<dependency>
  <groupId>io.vertx</groupId>
  <artifactId>vertx-kafka-client</artifactId>
  <version>${vertx.version}</version>
</dependency>
```

Modul yang memakai (dependency langsung di pom modul):

| Modul | Pemakaian |
|---|---|
| `common/` | `KafkaService` (producer wrapper) + `KafkaConfig` (factory) |
| `auth/` | Producer → topik auth-* |
| `merchant/` | Producer → topik merchant-* |
| `transaction/` | Producer → topik transaction-create |
| `email/` | **Consumer** (satu-satunya) |

> Tidak ada modul lain yang men-declare `vertx-kafka-client`.

---

## 4. Producer — KafkaConfig & KafkaService

### 4.1 `KafkaConfig.createProducer(Vertx)` — `common/.../config/KafkaConfig.java`

Factory producer standar:

```java
config.put("bootstrap.servers", System.getenv().getOrDefault("KAFKA_BROKERS", "localhost:9092"));
config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
config.put("acks", "1");
```

> Catatan: `KafkaConfig` ini ada, namun tiga verticle producer
> (Auth/Merchant/Transaction) saat ini **membangun konfigurasinya sendiri**
> (identik: StringSerializer + `acks=1`) daripada memakai factory ini.

### 4.2 `KafkaService.sendMessage(topic, key, value)` — `common/.../service/KafkaService.java`

Wrapper producer tunggal yang dipakai semua service:

```java
public Future<Void> sendMessage(String topic, String key, JsonObject value) {
    KafkaProducerRecord<String, String> record = KafkaProducerRecord.create(topic, key, value.encode());
    return producer.send(record)
        .onSuccess(metadata -> log.info("📤 Message sent to topic {}: {}", topic, value.encode()))
        .onFailure(err -> log.error("❌ Failed to send message to topic {}", topic, err))
        .mapEmpty();
}
```

Karakteristik:
- **Key & value berupa String.** Value adalah `JsonObject` yang di-encode
  (`value.encode()`) → JSON string.
- Kembalian `Future<Void>` — pemanggil (service) dapat `compose`/`recover`.
- Log sukses/gagal otomatis (inilah "audit trail" pertama di log service).
- `close()` menutup producer (dipanggil di `stop()` tiap verticle).

### 4.3 Inisialisasi di tiap Verticle

| Verticle | Snippet kunci |
|---|---|
| `AuthVerticle` | `KafkaProducer.create(vertx, kafkaConfig)` → `new KafkaService(producer)` → di-inject ke `RegisterService` & `PasswordResetService`; `stop()` → `kafkaService.close()` |
| `MerchantVerticle` | Sama; di-inject ke `MerchantCommandServiceImpl` & `MerchantDocumentCommandServiceImpl` |
| `TransactionVerticle` | Sama; di-inject ke `TransactionCommandServiceImpl` |

Semua memakai `acks=1` (hanya menunggu ack leader, tanpa wait semua replica) —
kompromi latency vs durability.

---

## 5. Katalog Topik

Delapan topik, semuanya dengan **1 partition** (default broker) dan pola nama
`email-service-topic-<domain>-<aksi>`:

| # | Topik | Producer (service & method) | Dipicu saat | Key | Konsumen |
|---|---|---|---|---|---|
| 1 | `email-service-topic-auth-register` | auth · `RegisterService.sendWelcomeEmail` | User berhasil register (role `ROLE_ADMIN` di-assign + kode verifikasi di-cache Redis TTL 15m) | `userId` | email |
| 2 | `email-service-topic-auth-forgot-password` | auth · `PasswordResetService.sendForgotPasswordEmail` | User minta reset password (reset token dibuat + di-cache Redis TTL 5m) | `userId` | email |
| 3 | `email-service-topic-auth-verify-code-success` | auth · `PasswordResetService.sendVerificationSuccessEmail` | Kode verifikasi user berhasil diverifikasi | `userId` | email |
| 4 | `email-service-topic-merchant-create` | merchant · `MerchantCommandServiceImpl.sendMerchantCreateEvent` | Merchant baru berhasil dibuat | `merchantId` | email |
| 5 | `email-service-topic-merchant-update-status` | merchant · `MerchantCommandServiceImpl.sendMerchantStatusUpdateEvent` | Status merchant di-update | `merchantId` | email |
| 6 | `email-service-topic-merchant-document-create` | merchant · `MerchantDocumentCommandServiceImpl.sendMerchantDocumentCreateEvent` | Dokumen merchant dibuat | `documentId` | email |
| 7 | `email-service-topic-merchant-document-update-status` | merchant · `MerchantDocumentCommandServiceImpl.sendMerchantDocumentStatusUpdateEvent` | Status dokumen merchant di-update | `documentId` | email |
| 8 | `email-service-topic-transaction-create` | transaction · `TransactionCommandServiceImpl.sendTransactionCreateEvent` | Transaksi baru tercatat (insert sukses + cache evict) | `transactionId` | email |

**Matriks pemakaian per service:**

| Service | Produce | Consume |
|---|---|---|
| auth | 3 topik | — |
| merchant | 4 topik | — |
| transaction | 1 topik | — |
| email | — | 8 topik |
| user, role, cashier, category, product, order, order_item, apigateway, db-migration | — | — |

> Order Service & Order Item Service **tidak** mengirim event Kafka — alurnya
> sinkron via gRPC (lihat `ORDER_TRANSACTION.md`).

---

## 6. Detail Event per Topik

Semua payload berformat JSON `{ email, subject, body }` — kontrak minimal yang
dipahami consumer. Key selalu ID entitas terkait (untuk partitioning).

### 6.1 `email-service-topic-auth-register`

Diproduksi `RegisterService.sendWelcomeEmail` setelah:
1. User baru di-insert (BCrypt cost 12)
2. Role default `ROLE_ADMIN` di-assign via `user_roles`
3. Kode verifikasi di-cache Redis `verification:<email>` (TTL 15 menit) —
   `UUID.randomUUID().toString().substring(0, 10)` → 10 karakter (termasuk
   tanda hubung: `xxxxxxxx-x`)

```json
{
  "email": "user@example.com",
  "subject": "Welcome to SanEdge",
  "body": "Your account has been successfully created. Link: https://sanedge.example.com/login?verify_code=abc123xyz0"
}
```

Key: `userId.toString()`. Gagal → `recover` (warn + lanjut; registrasi tetap sukses).

### 6.2 `email-service-topic-auth-forgot-password`

Diproduksi `PasswordResetService.sendForgotPasswordEmail` setelah reset token
10-karakter dibuat (expiry 24 jam di tabel `reset_token`) dan di-cache Redis
`resetToken:<token>` (TTL 5 menit):

```json
{
  "email": "user@example.com",
  "subject": "Password Reset Request",
  "body": "Click to reset your password: https://sanedge.example.com/reset-password?token=abc123xyz0"
}
```

Key: `userId`. Gagal → recover + lanjut (flow forgot-password tetap `true`).

### 6.3 `email-service-topic-auth-verify-code-success`

Diproduksi `PasswordResetService.sendVerificationSuccessEmail` setelah
`userRepository.updateUserIsVerified(userId, true)` dan Redis
`verification:<email>` dihapus:

```json
{
  "email": "user@example.com",
  "subject": "Verification Success",
  "body": "Your account has been successfully verified."
}
```

Key: `userId`.

### 6.4 `email-service-topic-merchant-create`

Diproduksi `MerchantCommandServiceImpl.sendMerchantCreateEvent` setelah
`createMerchant` (user owner dicek exists via gRPC user service):

```json
{
  "email": "merchant@example.com",
  "subject": "Merchant Created",
  "body": "Merchant <b>Toko Bahagia</b> has been created successfully."
}
```

Key: `merchantId`. Email diambil dari `merchant.getContactEmail()`.

### 6.5 `email-service-topic-merchant-update-status`

Diproduksi setelah `updateMerchantStatus` (status merchant berganti):

```json
{
  "email": "merchant@example.com",
  "subject": "Merchant Status Updated",
  "body": "Merchant <b>Toko Bahagia</b> status has been updated to <b>ACTIVE</b>."
}
```

Key: `merchantId`.

### 6.6 `email-service-topic-merchant-document-create`

Diproduksi `MerchantDocumentCommandServiceImpl.sendMerchantDocumentCreateEvent`
setelah `createMerchantDocument` (merchant owner dicek exists dulu):

```json
{
  "email": "merchant@example.com",
  "subject": "Merchant Document Created",
  "body": "Document <b>KTP</b> has been created for merchant <b>Toko Bahagia</b>."
}
```

Key: `documentId`.

### 6.7 `email-service-topic-merchant-document-update-status`

Diproduksi setelah `updateMerchantDocumentStatus`; merchant di-fetch ulang
untuk email tujuan (bila merchant hilang → warn + skip event):

```json
{
  "email": "merchant@example.com",
  "subject": "Merchant Document Status Updated",
  "body": "Document <b>KTP</b> for merchant <b>Toko Bahagia</b> status has been updated to <b>VERIFIED</b>."
}
```

Key: `documentId`.

### 6.8 `email-service-topic-transaction-create`

Diproduksi `TransactionCommandServiceImpl.sendTransactionCreateEvent` — satu
satunya event yang berkaitan langsung dengan dokumen `ORDER_TRANSACTION.md`.

Urutan di `createTransaction`:
1. Validasi eksplisit (`order_id > 0`, `payment_method` tidak blank, `amount > 0`)
2. `INSERT transactions` (SQL langsung, `payment_status` default `"pending"`)
3. Evict cache `transaction:list:*`
4. `sendTransactionCreateEvent`:
   - `merchantQueryRepository.findContactEmailByMerchantId(merchantId)` (gRPC → merchant service)
   - Bila `contact_email` null/blank → warn + **skip** (tanpa publish)
   - `kafkaService.sendMessage("email-service-topic-transaction-create", transactionId, payload)`

```json
{
  "email": "merchant@example.com",
  "subject": "New Transaction Created",
  "body": "A new transaction of <b>50000</b> using <b>CASH</b> has been created. Status: <b>PENDING</b>."
}
```

Key: `transactionId`.

> `amount` dan `payment_method` diambil dari entitas `Transaction` yang baru
> dibuat; `status` adalah enum `PaymentStatus.name()`.

---

## 7. Consumer — Email Service

Satu-satunya consumer: **`email/` module** → `EmailVerticle`.

### 7.1 Konfigurasi Consumer

```java
kafkaConfig.put("bootstrap.servers", System.getenv().getOrDefault("KAFKA_BROKERS", "localhost:9092"));
kafkaConfig.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
kafkaConfig.put("value.deserializer", "io.vertx.kafka.client.serialization.JsonObjectDeserializer");
kafkaConfig.put("group.id", "email-service-group");
kafkaConfig.put("auto.offset.reset", "earliest");
```

| Aspek | Nilai | Catatan |
|---|---|---|
| Group | `email-service-group` | Skala horizontal: tambah instance email → partition dibagi (saat ini 1 partition) |
| Offset reset | `earliest` | Topik baru di-consume dari awal (semua event historis akan dikirim ulang bila topik sudah ada isinya) |
| Deserializer value | `JsonObjectDeserializer` | Value JSON string → `JsonObject` langsung |
| Auto commit offset | default Kafka client (`enable.auto.commit=true`) | Lihat §8 |

### 7.2 Topik yang di-subscribe

Delapan topik persis seperti di §5 (hardcoded `Arrays.asList(...)` di
`EmailVerticle.start`). Tambahan topik baru = ubah daftar ini.

### 7.3 Alur pemrosesan record

```text
record (topic, partition, offset, value={email,subject,body})
  → log "📥 Received message from topic ..."
  → EmailDedupGuard.claim(topic, partition, offset)   [Redis]
      ├─ true  → sendEmail(value)                      [MailClient → SMTP]
      └─ false → skip (duplikat, log "⏭️")
      └─ error → fail-open: tetap sendEmail (log "❌ Dedup guard failed")
```

`sendEmail`:
- Membaca `email`, `subject`, `body`; bila ada yang null → warn + return (payload cacat di-skip).
- `MailMessage.setFrom("no-reply@payment-gateway.com").setTo(email).setSubject(subject).setHtml(body)`.
- `MailClient.createShared(vertx, mailConfig)` dengan config:
  `SMTP_SERVER` (default `localhost`), `SMTP_PORT` (default `587`),
  `SMTP_USER`, `SMTP_PASS`, `StartTLSOptions.REQUIRED`.
- Gagal kirim → log error (offset sudah/bisa ter-commit → email hilang; lihat §13).

### 7.4 Health check

| Endpoint | Status |
|---|---|
| `/health/live` | 200 selalu (proses hidup) |
| `/health/ready` | 200 bila `ready=true` (setelah subscribe sukses), 503 sebelum itu |

Port: `METRIC_EMAIL_ADDR` atau `HEALTH_PORT` (default 8080).

---

## 8. Semantik Pengiriman & Guarantees

### 8.1 Producer → broker

- **Durability dari perspektif aplikasi:** event dikirim **setelah** operasi
  DB commit (tanpa outbox). Jika Kafka down saat `send` → future gagal →
  service hanya log warning dan **request tetap sukses**. Konsekuensi: email
  tidak terkirim, dan tidak ada retry/backfill otomatis (event bisa hilang —
  bukan jaminan at-most-once dari sisi producer, melainkan keputusan desain
  "swallow failure").
- `acks=1` → ack leader saja; kemungkinan kecil event hilang bila leader crash
  sebelum replikasi.
- **Tanpa idempotent producer** (`enable.idempotence` tidak disetel).

### 8.2 Broker → consumer

- Default consumer (auto-commit) memberi **at-least-once**: crash antara proses
  dan commit offset → record dibaca ulang → email bisa terkirim dua kali.
  Ditutup oleh **dedup Redis** (§9) yang mengidentifikasi ulang record via
  `(topic, partition, offset)`.

### 8.3 Graceful degradation (pola konsisten di semua producer)

| Kondisi | Perilaku |
|---|---|
| `kafkaService == null` (Kafka tidak diinisialisasi) | Warn + skip event, operasi utama tetap sukses |
| Email tujuan tidak ditemukan (mis. merchant tanpa `contact_email`) | Warn + skip event |
| `sendMessage` gagal (broker down, timeout) | Error di-log di `KafkaService`, caller `.recover` → operasi tetap sukses |

### 8.4 Ringkasan

```text
DB commit ──(tanpa outbox)──► Kafka send ──(acks=1)──► broker ──► consumer (at-least-once)
                                 │                                              │
                          gagal → warn & sukses                          dedup Redis → email sekali
```

---

## 9. Deduplikasi (EmailDedupGuard)

`email/.../service/EmailDedupGuard.java` — guard idempotensi sisi consumer.

- **Key:** `email:dedup:<topic>:<partition>:<offset>` (prefix `email:dedup:`,
  TTL **24 jam**, konstanta `DEDUP_TTL`).
- **Alur `claim(topic, partition, offset)`:**
  1. `redisService.exists(key)` → ada → `false` (duplikat, skip).
  2. Tidak ada → `redisService.set(key, "1", 24h)` → `true` (proses).
  3. **Fail-open:** bila Redis error di langkah mana pun → warn + return `true`
     (email tetap dikirim) — prioritas: ketersediaan notifikasi > exactly-once.
- Mengapa cukup `(topic, partition, offset)`: sumber duplikat hanyalah
  replay/retry record yang sama (offset sama). Dua event berbeda untuk entitas
  yang sama punya offset berbeda → keduanya dikirim.

> Diuji di `EmailDedupGuardTest` (claim pertama `true`, duplikat `false`,
> key sesuai format).

---

## 10. Chaos Engineering pada Kafka

`common/.../chaos/ChaosKafkaInterceptor.java` menyediakan wrapper JDK proxy
untuk `KafkaProducer` dengan tiga aksi injeksi:

| Aksi | Efek |
|---|---|
| `dropMessage` | Record dibuang diam-diam, future sukses (simulasi loss) |
| `rejectMessage` | Future gagal dengan `errorMessage` (simulasi kegagalan publish) |
| `latencyMs` | Pengiriman ditunda N ms (simulasi slow broker) |

Pencocokan policy: `ChaosManager.evaluate("kafka", topic)` — exact match target
topik, fallback `topic.contains(target)`.

**Status integrasi saat ini:**
- Class tersedia di modul `common/` dan diuji secara unit, **namun belum
  di-wire** ke verticle producer manapun — `AuthVerticle`, `MerchantVerticle`,
  dan `TransactionVerticle` masih memakai `KafkaProducer.create(...)` polos
  (bukan `ChaosKafkaInterceptor.wrap(...)`).
- `chaos.yaml` saat ini **tidak** memuat policy tipe `kafka` (yang ada: http,
  sql, cpu, memory).

Contoh policy yang siap dipakai bila di-wire (tambahkan ke `chaos.yaml`):

```yaml
- name: "kafka-drop-transaction-event"
  type: "kafka"
  target: "email-service-topic-transaction-create"
  enabled: false
  errorChance: 0.2
  dropMessage: true

- name: "kafka-delay-email-topics"
  type: "kafka"
  target: "email-service-topic-"
  enabled: false
  errorChance: 1.0
  latencyMs: 1500
```

---

## 11. Observability

### 11.1 Kafka Exporter (compose lokal)

`kafka-exporter` (`danielqsj/kafka-exporter:v1.9.0`) dengan arg
`--kafka.server=my-kafka:9092` → metrik Prometheus (kafka_broker_*, kafka_topic_*,
lag consumer, dll.). Scrape target ada di `observability/prometheus.yml` dan
alert rule di `observability/rules/kafka-exporter-alerts.yaml`.

### 11.2 Log (audit trail langsung)

Semua titik penting sudah log dengan emoji khas:

| Sisi | Log |
|---|---|
| Producer | `📤 Message sent to topic <t>: <payload>` / `❌ Failed to send message to topic <t>` |
| Consumer | `📥 Received message from topic <t> (partition <p>, offset <o>): <payload>` |
| Dedup | `⏭️ Duplicate Kafka record skipped (dedup key: <k>)` |
| Fail-open | `⚠️ Failed to set dedup key ... proceeding to send (fail-open)` |
| Kirim email | `✅ Email successfully sent to <email>` / `❌ Failed to send email to <email>` |

> Kombinasi log producer + consumer dengan `(topic, partition, offset)` di
> consumer memungkinkan **trace manual end-to-end** sebuah event.

### 11.3 Tracing (OpenTelemetry)

- Service memakai OpenTelemetry untuk tracing gRPC/HTTP/SQL, namun **tidak ada
  instrumentasi khusus Kafka** (tidak ada span produce/consume, tidak ada
  propagasi traceparent lewat header Kafka record). Span aktivitas bisnis
  (mis. `TransactionCommandService.createTransaction`) tetap mencatat fase
  publish sebagai bagian dari future chain.

---

## 12. Operasional (CLI)

Masuk ke container broker lalu jalankan perintah Kafka bawaan:

```sh
docker compose -f deployments/local/docker-compose.yml exec kafka bash

# List semua topik
/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list

# Deskripsi satu topik (partition, replica, offset)
/opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --describe --topic email-service-topic-transaction-create

# Konsumsi dari awal (lihat payload asli)
/opt/kafka/bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --topic email-service-topic-auth-register --from-beginning

# Produksi manual dengan key (uji coba end-to-end tanpa service)
/opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 \
  --topic email-service-topic-transaction-create \
  --property parse.key=true --property key.separator=: \
  --property key.serializer=org.apache.kafka.common.serialization.StringSerializer
> 42:{"email":"test@example.com","subject":"Test","body":"<b>Hello</b>"}
```

Cek lag consumer group:

```sh
/opt/kafka/bin/kafka-consumer-groups.sh --bootstrap-server localhost:9092 \
  --group email-service-group --describe
```

---

## 13. Skenario Gagal & Mitigasi

| Skenario | Dampak | Mitigasi saat ini |
|---|---|---|
| Broker Kafka down saat publish | Event hilang; email tidak terkirim | Log warning + request tetap sukses (graceful). **Tidak ada retry/queue** — terima konsekuensi atau implementasikan outbox (§14) |
| Publish sukses tapi consumer belum aktif (`auto.offset.reset=earliest`) | Event lama dikirim ulang saat consumer pertama connect | Dedup Redis (§9) mencegah email ganda |
| Consumer crash antara proses & commit offset | Record dibaca ulang → potensi email ganda | Dedup Redis (key `(topic,partition,offset)`) |
| Redis down di sisi consumer | Dedup gagal | **Fail-open**: email tetap dikirim (bisa duplikat pada replay) |
| SMTP down / kredensial salah | Email gagal terkirim | Log error saja; offset tetap maju → email hilang permanen (tidak ada retry) |
| Merchant tanpa `contact_email` | Event di-skip | Warn + skip; operasi bisnis tetap sukses |
| Payload tidak lengkap (`email`/`subject`/`body` null) | Record di-skip | Warn + return (tidak crash) |
| Kafka topik tidak ada | Auto-create broker (`true`) membuat otomatis | — |

---

## 14. Kandidat Peningkatan (Roadmap)

Urut berdasarkan nilai vs biaya:

1. **Transactional outbox** (paling disarankan) — insert entity + `outbox` dalam
   satu transaksi DB, publisher periodik mengirim ke Kafka dengan idempotent
   producer. Menutup celah "event hilang saat Kafka down" tanpa mengorbankan
   konsistensi. (Versi e-commerce project ini pernah memakai pola ini.)
2. **Retry & dead-letter** — consumer gagal SMTP → pindah ke topik `-retry`
   (delay) lalu `-dlq`; bukan sekadar log error.
3. **Idempotency key transaksi** — mencegah baris transaction ganda dari
   request duplikat (kaitannya dengan topik #8).
4. **Wrap producer dengan `ChaosKafkaInterceptor`** — mengaktifkan policy
   tipe `kafka` yang sudah tersedia di `common/`.
5. **Instrumentasi OpenTelemetry Kafka** — span produce/consume + propagasi
   traceparent via header record untuk trace end-to-end.
6. **SASL/SCRAM + TLS** di broker, dan set `acks=all` untuk produksi.
7. **Idempotent producer** (`enable.idempotence=true`) + lebih dari 1 partition
   agar consumer email bisa diskalakan.

---

## 15. Referensi Silang

- [`ORDER_TRANSACTION.md`](ORDER_TRANSACTION.md) — alur lengkap Order, Order
  Item, Transaction & Product; §12 berisi ringkasan event transaction (silakan
  baca bersama dokumen ini).
- [`README.md`](README.md) — arsitektur umum & service catalog.
- [`chaos.yaml`](chaos.yaml) — policy chaos (belum ada tipe `kafka`).
- `common/src/main/java/io/example/common/service/KafkaService.java` &
  `.../config/KafkaConfig.java` — wrapper producer.
- `email/src/main/java/io/example/email/EmailVerticle.java` &
  `.../service/EmailDedupGuard.java` — consumer + dedup.
- `auth/.../service/RegisterService.java`, `PasswordResetService.java` —
  producer auth.
- `merchant/.../service/impl/MerchantCommandServiceImpl.java`,
  `MerchantDocumentCommandServiceImpl.java` — producer merchant.
- `transaction/.../service/impl/TransactionCommandServiceImpl.java` — producer
  transaction.
- `deployments/local/docker-compose.yml` & `docker.env.example` — infra broker
  & env.
