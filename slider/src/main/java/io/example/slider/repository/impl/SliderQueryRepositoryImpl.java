package io.example.slider.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.slider.model.FindAllSlider;
import io.example.slider.model.Slider;
import io.example.slider.repository.SliderQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

public class SliderQueryRepositoryImpl implements SliderQueryRepository {
    private final Pool client;

    public SliderQueryRepositoryImpl(Pool client) {
        this.client = client;
    }

    @Override
    public Future<PagedResult<Slider>> getSliders(FindAllSlider req) {
        int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

        return client
                .preparedQuery("""
                        SELECT
                            *,
                            COUNT(*) OVER() AS total_count
                        FROM sliders
                        WHERE deleted_at IS NULL
                          AND (
                            $1::TEXT IS NULL
                            OR name ILIKE '%' || $1 || '%'
                          )
                        ORDER BY created_at DESC
                        LIMIT $2 OFFSET $3
                        """)
                .execute(Tuple.of(
                        normalizeSearch(req.getSearch()),
                        req.getPageSize(),
                        offset))
                .map(this::mapPagedSliders);
    }

    @Override
    public Future<PagedResult<Slider>> getSlidersActive(FindAllSlider req) {
        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize();
        int offset = (page - 1) * pageSize;

        String search = normalizeSearch(req.getSearch());

        return client
                .preparedQuery("""
                        SELECT
                            slider_id,
                            name,
                            image,
                            created_at,
                            updated_at,
                            deleted_at,
                            COUNT(*) OVER() AS total_count
                        FROM sliders
                        WHERE deleted_at IS NULL
                          AND (
                            $1::TEXT IS NULL
                            OR name ILIKE '%' || $1 || '%'
                          )
                        ORDER BY created_at DESC
                        LIMIT $2 OFFSET $3
                        """)
                .execute(Tuple.of(search, pageSize, offset))
                .map(this::mapPagedSliders);
    }

    @Override
    public Future<PagedResult<Slider>> getSlidersTrashed(FindAllSlider req) {
        int page = req.getPage() > 0 ? req.getPage() : 1;
        int pageSize = req.getPageSize();
        int offset = (page - 1) * pageSize;

        String search = normalizeSearch(req.getSearch());

        return client
                .preparedQuery("""
                        SELECT
                            slider_id,
                            name,
                            image,
                            created_at,
                            updated_at,
                            deleted_at,
                            COUNT(*) OVER() AS total_count
                        FROM sliders
                        WHERE deleted_at IS NOT NULL
                          AND (
                            $1::TEXT IS NULL
                            OR name ILIKE '%' || $1 || '%'
                          )
                        ORDER BY created_at DESC
                        LIMIT $2 OFFSET $3
                        """)
                .execute(Tuple.of(search, pageSize, offset))
                .map(this::mapPagedSliders);
    }

    @Override
    public Future<Slider> getSliderById(Long sliderId) {
        return client
                .preparedQuery("""
                        SELECT *
                        FROM sliders
                        WHERE slider_id = $1 AND deleted_at IS NULL
                        """)
                .execute(Tuple.of(sliderId))
                .map(rows -> rows.iterator().hasNext() ? Slider.fromRow(rows.iterator().next()) : null);
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search;
    }

    private PagedResult<Slider> mapPagedSliders(RowSet<Row> rows) {
        List<Slider> sliders = new ArrayList<>();
        int total = 0;

        for (Row row : rows) {
            sliders.add(Slider.fromRow(row));
            if (total == 0) {
                total = row.getInteger("total_count");
            }
        }

        return new PagedResult<>(sliders, total);
    }
}
