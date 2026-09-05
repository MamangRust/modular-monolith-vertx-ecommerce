package io.example.banner.handler;

import io.example.common.domain.PagedResult;
import io.example.banner.model.BannerResponse;
import io.example.banner.model.BannerResponseDeleteAt;
import io.example.banner.service.BannerQueryService;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, VertxExtension.class})
class BannerQueryHandlerTest {

  @Mock
  private BannerQueryService service;

  private BannerQueryHandler handler;

  @BeforeEach
  void setUp() {
    handler = new BannerQueryHandler(service);
  }

  private static BannerResponse aResp(Long id, String name) {
    return BannerResponse.builder()
        .id(id)
        .name(name)
        .startDate("2024-01-01")
        .endDate("2024-12-31")
        .startTime("00:00")
        .endTime("23:59")
        .isActive(true)
        .createdAt("2026-06-26T10:00:00Z")
        .updatedAt("2026-06-26T10:00:00Z")
        .build();
  }

  private static BannerResponseDeleteAt aRespDeleteAt(Long id, String name) {
    return BannerResponseDeleteAt.builder()
        .id(id)
        .name(name)
        .startDate("2024-01-01")
        .endDate("2024-12-31")
        .startTime("00:00")
        .endTime("23:59")
        .isActive(true)
        .createdAt("2026-06-26T10:00:00Z")
        .updatedAt("2026-06-26T10:00:00Z")
        .deletedAt("2026-06-25T10:00:00Z")
        .build();
  }

  /* ─── findAll ─── */

  @Test
  @DisplayName("findAll returns paginated response")
  void findAll(VertxTestContext ctx) {
    var data = List.of(aResp(1L, "Summer Sale"), aResp(2L, "Winter Promo"));
    when(service.getBanners(any())).thenReturn(Future.succeededFuture(new PagedResult<>(data, 2)));

    var req = pb.banner.BannerQuery.FindAllBannerRequest.newBuilder().setPage(1).setPageSize(10).build();

    handler.findAll(req)
        .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
          assertThat(resp.getStatus()).isEqualTo("success");
          assertThat(resp.getMessage()).isEqualTo("OK");
          assertThat(resp.getDataCount()).isEqualTo(2);
          assertThat(resp.getData(0).getName()).isEqualTo("Summer Sale");
          assertThat(resp.getData(1).getName()).isEqualTo("Winter Promo");
          assertThat(resp.getPagination().getTotalRecords()).isEqualTo(2);
          assertThat(resp.getPagination().getCurrentPage()).isEqualTo(1);
          ctx.completeNow();
        })));
  }


  @Test
  @DisplayName("findById returns banner response")
  void findById(VertxTestContext ctx) {
    when(service.getBannerById(1L)).thenReturn(Future.succeededFuture(aResp(1L, "Flash Sale")));

    var req = pb.banner.BannerCommon.FindByIdBannerRequest.newBuilder().setId(1).build();

    handler.findById(req)
        .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
          assertThat(resp.getStatus()).isEqualTo("success");
          assertThat(resp.getData().getBannerId()).isEqualTo(1);
          assertThat(resp.getData().getName()).isEqualTo("Flash Sale");
          ctx.completeNow();
        })));
  }

  /* ─── findByActive ─── */

  @Test
  @DisplayName("findByActive returns paginated active banners")
  void findByActive(VertxTestContext ctx) {
    var data = List.of(aRespDeleteAt(1L, "Active Banner"));
    when(service.getActiveBanners(any())).thenReturn(Future.succeededFuture(new PagedResult<>(data, 1)));

    var req = pb.banner.BannerQuery.FindAllBannerRequest.newBuilder().setPage(1).setPageSize(10).build();

    handler.findByActive(req)
        .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
          assertThat(resp.getStatus()).isEqualTo("success");
          assertThat(resp.getDataCount()).isEqualTo(1);
          assertThat(resp.getData(0).getName()).isEqualTo("Active Banner");
          assertThat(resp.getPagination().getTotalRecords()).isEqualTo(1);
          ctx.completeNow();
        })));
  }

  /* ─── findByTrashed ─── */

  @Test
  @DisplayName("findByTrashed returns paginated trashed banners")
  void findByTrashed(VertxTestContext ctx) {
    var data = List.of(aRespDeleteAt(1L, "Trashed Banner"));
    when(service.getTrashedBanners(any())).thenReturn(Future.succeededFuture(new PagedResult<>(data, 1)));

    var req = pb.banner.BannerQuery.FindAllBannerRequest.newBuilder().setPage(1).setPageSize(10).build();

    handler.findByTrashed(req)
        .onComplete(ctx.succeeding(resp -> ctx.verify(() -> {
          assertThat(resp.getStatus()).isEqualTo("success");
          assertThat(resp.getDataCount()).isEqualTo(1);
          assertThat(resp.getData(0).getName()).isEqualTo("Trashed Banner");
          assertThat(resp.getPagination().getTotalRecords()).isEqualTo(1);
          ctx.completeNow();
        })));
  }
}