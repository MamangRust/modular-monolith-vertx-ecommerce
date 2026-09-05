package io.example.slider.repository.impl;

import io.example.slider.domain.requests.CreateSliderRequest;
import io.example.slider.model.Slider;
import io.example.slider.domain.requests.UpdateSliderRequest;
import io.example.slider.repository.SliderCommandRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SliderCommandRepositoryImpl implements SliderCommandRepository {
        private final Pool client;

        @Override
        public Future<Slider> createSlider(CreateSliderRequest req) {
                return client
                                .preparedQuery("""
                                                INSERT INTO sliders (name, image)
                                                VALUES ($1, $2)
                                                RETURNING *
                                                """)
                                .execute(Tuple.of(req.getName(), req.getImage()))
                                .map(rows -> Slider.fromRow(rows.iterator().next()));
        }

        @Override
        public Future<Slider> updateSlider(UpdateSliderRequest req) {
                return client
                                .preparedQuery("""
                                                UPDATE sliders
                                                SET name = COALESCE(NULLIF($1, ''), name),
                                                    image = COALESCE(NULLIF($2, ''), image),
                                                    updated_at = CURRENT_TIMESTAMP
                                                WHERE slider_id = $3 AND deleted_at IS NULL
                                                RETURNING *
                                                """)
                                .execute(Tuple.of(req.getName() != null ? req.getName() : "", req.getImage() != null ? req.getImage() : "", req.getSliderId()))
                                .map(this::mapSingleOrNull);
        }

        @Override
        public Future<Slider> trash(Long sliderId) {
                return client
                                .preparedQuery("""
                                                UPDATE sliders
                                                SET deleted_at = CURRENT_TIMESTAMP
                                                WHERE slider_id = $1 AND deleted_at IS NULL
                                                RETURNING *
                                                """)
                                .execute(Tuple.of(sliderId))
                                .map(this::mapSingleOrNull);
        }

        @Override
        public Future<Slider> restore(Long sliderId) {
                return client
                                .preparedQuery("""
                                                UPDATE sliders
                                                SET deleted_at = NULL, updated_at = CURRENT_TIMESTAMP
                                                WHERE slider_id = $1 AND deleted_at IS NOT NULL
                                                RETURNING *
                                                """)
                                .execute(Tuple.of(sliderId))
                                .map(this::mapSingleOrNull);
        }

        @Override
        public Future<Boolean> deletePermanent(Long sliderId) {
                return client
                                .preparedQuery("DELETE FROM sliders WHERE slider_id = $1")
                                .execute(Tuple.of(sliderId))
                                .map(rows -> rows.rowCount() > 0);
        }

        @Override
        public Future<Integer> restoreAll() {
                return client
                                .preparedQuery(
                                                "UPDATE sliders SET deleted_at = NULL, updated_at = CURRENT_TIMESTAMP WHERE deleted_at IS NOT NULL")
                                .execute()
                                .map(RowSet::rowCount);
        }

        @Override
        public Future<Integer> deleteAll() {
                return client
                                .preparedQuery("DELETE FROM sliders WHERE deleted_at IS NOT NULL")
                                .execute()
                                .map(RowSet::rowCount);
        }

        private Slider mapSingleOrNull(RowSet<Row> rows) {
                return rows.iterator().hasNext() ? Slider.fromRow(rows.iterator().next()) : null;
        }
}
