package io.example.banner.repository.impl;

import java.time.LocalDate;
import java.time.LocalTime;

import io.example.banner.domain.requests.CreateBannerRequest;
import io.example.banner.domain.requests.UpdateBannerRequest;
import io.example.banner.model.Banner;
import io.example.banner.repository.BannerCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BannerCommandRepositoryImpl implements BannerCommandRepository {
        private final Pool client;

        @Override
        public Future<Banner> createBanner(CreateBannerRequest req) {
                LocalDate startDate = LocalDate.parse(req.getStartDate());
                LocalDate endDate = LocalDate.parse(req.getEndDate());
                LocalTime startTime = LocalTime.parse(req.getStartTime());
                LocalTime endTime = LocalTime.parse(req.getEndTime());

                return client
                                .preparedQuery("""
                                                INSERT INTO banners (name, start_date, end_date, start_time, end_time, is_active)
                                                VALUES ($1, $2, $3, $4, $5, $6)
                                                RETURNING banner_id, name, start_date, end_date, start_time, end_time, is_active, created_at, updated_at, deleted_at
                                                """)
                                .execute(Tuple.of(
                                                req.getName(),
                                                startDate,
                                                endDate,
                                                startTime,
                                                endTime,
                                                req.getIsActive()))
                                .map(this::mapSingleOrNull);
        }

        @Override
        public Future<Banner> updateBanner(UpdateBannerRequest req) {
                LocalDate startDate = req.getStartDate() != null && !req.getStartDate().isBlank() ? LocalDate.parse(req.getStartDate()) : null;
                LocalDate endDate = req.getEndDate() != null && !req.getEndDate().isBlank() ? LocalDate.parse(req.getEndDate()) : null;
                LocalTime startTime = req.getStartTime() != null && !req.getStartTime().isBlank() ? LocalTime.parse(req.getStartTime()) : null;
                LocalTime endTime = req.getEndTime() != null && !req.getEndTime().isBlank() ? LocalTime.parse(req.getEndTime()) : null;

                return client
                                .preparedQuery("""
                                                UPDATE banners
                                                SET name = COALESCE(NULLIF($1, ''), name),
                                                    start_date = COALESCE(NULLIF($2::TEXT, ''), start_date),
                                                    end_date = COALESCE(NULLIF($3::TEXT, ''), end_date),
                                                    start_time = COALESCE(NULLIF($4::TEXT, ''), start_time),
                                                    end_time = COALESCE(NULLIF($5::TEXT, ''), end_time),
                                                    is_active = COALESCE(NULLIF($6::TEXT, ''), is_active),
                                                    updated_at = CURRENT_TIMESTAMP
                                                WHERE banner_id = $7 AND deleted_at IS NULL
                                                RETURNING banner_id, name, start_date, end_date, start_time, end_time, is_active, created_at, updated_at, deleted_at
                                                """)
                                .execute(Tuple.of(
                                                req.getName() != null ? req.getName() : "",
                                                req.getStartDate() != null ? req.getStartDate() : "",
                                                req.getEndDate() != null ? req.getEndDate() : "",
                                                req.getStartTime() != null ? req.getStartTime() : "",
                                                req.getEndTime() != null ? req.getEndTime() : "",
                                                req.getIsActive() != null ? req.getIsActive() : "",
                                                (long) req.getBannerId()))
                                .map(this::mapSingleOrNull);
        }

        @Override
        public Future<Banner> trashed(Long bannerId) {
                return client
                                .preparedQuery("""
                                                UPDATE banners
                                                SET deleted_at = CURRENT_TIMESTAMP
                                                WHERE banner_id = $1 AND deleted_at IS NULL
                                                RETURNING banner_id, name, start_date, end_date, start_time, end_time, is_active, created_at, updated_at, deleted_at
                                                """)
                                .execute(Tuple.of(bannerId))
                                .map(this::mapSingleOrNull);
        }

        @Override
        public Future<Banner> restore(Long bannerId) {
                return client
                                .preparedQuery("""
                                                UPDATE banners
                                                SET deleted_at = NULL, updated_at = CURRENT_TIMESTAMP
                                                WHERE banner_id = $1 AND deleted_at IS NOT NULL
                                                RETURNING banner_id, name, start_date, end_date, start_time, end_time, is_active, created_at, updated_at, deleted_at
                                                """)
                                .execute(Tuple.of(bannerId))
                                .map(this::mapSingleOrNull);
        }

        @Override
        public Future<Void> deletePermanent(Long bannerId) {
                return client
                                .preparedQuery("DELETE FROM banners WHERE banner_id = $1")
                                .execute(Tuple.of(bannerId))
                                .mapEmpty();
        }

        @Override
        public Future<Void> restoreAll() {
                return client
                                .query("UPDATE banners SET deleted_at = NULL, updated_at = CURRENT_TIMESTAMP WHERE deleted_at IS NOT NULL")
                                .execute()
                                .mapEmpty();
        }

        @Override
        public Future<Void> deleteAll() {
                return client
                                .query("DELETE FROM banners WHERE deleted_at IS NOT NULL")
                                .execute()
                                .mapEmpty();
        }

        private Banner mapSingleOrNull(RowSet<Row> rows) {
                return rows.iterator().hasNext() ? Banner.fromRow(rows.iterator().next()) : null;
        }
}
