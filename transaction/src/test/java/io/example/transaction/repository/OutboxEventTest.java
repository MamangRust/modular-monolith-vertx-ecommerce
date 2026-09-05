package io.example.transaction.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import io.vertx.sqlclient.Row;

class OutboxEventTest {
    @Test
    void parsesObjectPayload() {
        Row row = rowWithPayload("{\"title\":\"Transaction Successful\"}");

        OutboxEvent event = OutboxEvent.fromRow(row);

        assertThat(event.getPayload().getString("title")).isEqualTo("Transaction Successful");
    }

    @Test
    void parsesLegacyStringEncodedPayload() {
        Row row = rowWithPayload("\"{\\\"title\\\":\\\"Transaction Successful\\\"}\"");

        OutboxEvent event = OutboxEvent.fromRow(row);

        assertThat(event.getPayload().getString("title")).isEqualTo("Transaction Successful");
    }

    private Row rowWithPayload(String payload) {
        Row row = mock(Row.class);
        when(row.getLong("id")).thenReturn(1L);
        when(row.getString("aggregate_type")).thenReturn("transaction");
        when(row.getString("aggregate_id")).thenReturn("1");
        when(row.getString("event_type")).thenReturn("transaction.created");
        when(row.getString("payload")).thenReturn(payload);
        when(row.getString("topic")).thenReturn("test-topic");
        when(row.getString("key")).thenReturn("1");
        when(row.getLocalDateTime("created_at")).thenReturn(LocalDateTime.now());
        when(row.getLocalDateTime("published_at")).thenReturn(null);
        return row;
    }
}
