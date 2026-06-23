package io.example.banner.repository.impl;

import java.sql.Date;
import java.sql.Time;

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
                Date startDate = Date.valueOf(req.getStartDate());
                Date endDate = Date.valueOf(req.getEndDate());
                Time startTime = Time.valueOf(req.getStartTime());
                Time endTime = Time.valueOf(req.getEndTime());

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
                Date startDate = Date.valueOf(req.getStartDate());
                Date endDate = Date.valueOf(req.getEndDate());
                Time startTime = Time.valueOf(req.getStartTime());
                Time endTime = Time.valueOf(req.getEndTime());

                return client
                                .preparedQuery("""
                                                UPDATE banners
                                                SET name = $1,
                                                    start_date = $2,
                                                    end_date = $3,
                                                    start_time = $4,
                                                    end_time = $5,
                                                    is_active = $6,
                                                    updated_at = CURRENT_TIMESTAMP
                                                WHERE banner_id = $7 AND deleted_at IS NULL
                                                RETURNING banner_id, name, start_date, end_date, start_time, end_time, is_active, created_at, updated_at, deleted_at
                                                """)
                                .execute(Tuple.of(
                                                req.getName(),
                                                startDate,
                                                endDate,
                                                startTime,
                                                endTime,
                                                req.getIsActive(),
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
