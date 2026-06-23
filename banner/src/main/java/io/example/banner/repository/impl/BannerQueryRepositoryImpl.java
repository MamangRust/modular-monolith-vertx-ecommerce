package io.example.banner.repository.impl;

import java.util.ArrayList;
import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.banner.domain.requests.FindAllBannerRequest;
import io.example.banner.model.Banner;
import io.example.banner.repository.BannerQueryRepository;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BannerQueryRepositoryImpl implements BannerQueryRepository {
  private final Pool client;

  @Override
  public Future<PagedResult<Banner>> getBanners(FindAllBannerRequest req) {
    int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

    return client
        .preparedQuery(
            """
                SELECT
                    banner_id, name, start_date, end_date, start_time, end_time, is_active, created_at, updated_at, deleted_at,
                    COUNT(*) OVER() AS total_count
                FROM banners
                WHERE deleted_at IS NULL
                  AND (
                    $1::TEXT IS NULL
                    OR name ILIKE '%' || $1 || '%'
                  )
                ORDER BY created_at DESC
                LIMIT $2 OFFSET $3
                """)
        .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getPageSize(), offset))
        .map(this::mapPagedBanners);
  }

  @Override
  public Future<PagedResult<Banner>> getActiveBanners(FindAllBannerRequest req) {
    int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

    return client
        .preparedQuery(
            """
                SELECT
                    banner_id, name, start_date, end_date, start_time, end_time, is_active, created_at, updated_at, deleted_at,
                    COUNT(*) OVER() AS total_count
                FROM banners
                WHERE deleted_at IS NULL
                  AND is_active = TRUE
                  AND (
                    $1::TEXT IS NULL
                    OR name ILIKE '%' || $1 || '%'
                  )
                ORDER BY created_at DESC
                LIMIT $2 OFFSET $3
                """)
        .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getPageSize(), offset))
        .map(this::mapPagedBanners);
  }

  @Override
  public Future<PagedResult<Banner>> getTrashedBanners(FindAllBannerRequest req) {
    int offset = (req.getPage() > 0 ? req.getPage() - 1 : 0) * req.getPageSize();

    return client
        .preparedQuery(
            """
                SELECT
                    banner_id, name, start_date, end_date, start_time, end_time, is_active, created_at, updated_at, deleted_at,
                    COUNT(*) OVER() AS total_count
                FROM banners
                WHERE deleted_at IS NOT NULL
                  AND (
                    $1::TEXT IS NULL
                    OR name ILIKE '%' || $1 || '%'
                  )
                ORDER BY created_at DESC
                LIMIT $2 OFFSET $3
                """)
        .execute(Tuple.of(normalizeSearch(req.getSearch()), req.getPageSize(), offset))
        .map(this::mapPagedBanners);
  }

  @Override
  public Future<Banner> getBannerById(Long bannerId) {
    return client
        .preparedQuery(
            """
                SELECT banner_id, name, start_date, end_date, start_time, end_time, is_active, created_at, updated_at, deleted_at
                FROM banners
                WHERE banner_id = $1 AND deleted_at IS NULL
                """)
        .execute(Tuple.of(bannerId))
        .map(rows -> rows.iterator().hasNext() ? Banner.fromRow(rows.iterator().next()) : null);
  }

  private String normalizeSearch(String search) {
    return (search == null || search.isBlank()) ? null : search;
  }

  private PagedResult<Banner> mapPagedBanners(RowSet<Row> rows) {
    List<Banner> banners = new ArrayList<>();
    int total = 0;

    for (Row row : rows) {
      banners.add(Banner.fromRow(row));
      if (total == 0) {
        Integer tc = row.getInteger("total_count");
        if (tc != null) {
          total = tc;
        }
      }
    }

    return new PagedResult<>(banners, total);
  }
}
