package io.example.category.repository.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.example.category.domain.requests.FindAllCategoriesRequest;
import io.vertx.core.Future;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

@ExtendWith({ MockitoExtension.class, VertxExtension.class })
class CategoryQueryRepositoryImplTest {

    @Mock
    private Pool pool;

    @Mock
    private PreparedQuery<RowSet<Row>> preparedQuery;

    @Mock
    private RowSet<Row> rowSet;

    @Mock
    private RowIterator<Row> iterator;

    private CategoryQueryRepositoryImpl repo;

    @BeforeEach
    void setUp() {
        repo = new CategoryQueryRepositoryImpl(pool);
        when(pool.preparedQuery(anyString())).thenReturn(preparedQuery);
        when(preparedQuery.execute(any(Tuple.class))).thenReturn(Future.succeededFuture(rowSet));
        when(rowSet.iterator()).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(false);
    }

    private static FindAllCategoriesRequest aRequest(int page, int pageSize) {
        return FindAllCategoriesRequest.builder().page(page).pageSize(pageSize).build();
    }

    /** Tuple layout is: [0] search, [1] pageSize, [2] offset. */
    private static void assertOffset(Tuple tuple, int expectedPageSize, int expectedOffset) {
        assertThat(tuple.getInteger(1)).isEqualTo(expectedPageSize);
        assertThat(tuple.getInteger(2)).isEqualTo(expectedOffset);
    }

    private static Tuple capturedTuple(PreparedQuery<RowSet<Row>> preparedQuery) {
        ArgumentCaptor<Tuple> captor = ArgumentCaptor.forClass(Tuple.class);
        verify(preparedQuery).execute(captor.capture());
        return captor.getValue();
    }

    /* ─── getCategories pagination ─── */

    @Test
    @DisplayName("getCategories page 1 uses offset 0")
    void getCategoriesFirstPage(VertxTestContext ctx) {
        repo.getCategories(aRequest(1, 10))
                .onComplete(ctx.succeeding(result -> ctx.verify(() -> {
                    assertOffset(capturedTuple(preparedQuery), 10, 0);
                    assertThat(result.getData()).isEmpty();
                    assertThat(result.getTotalRecords()).isZero();
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("getCategories page 2 uses offset = pageSize")
    void getCategoriesSecondPage(VertxTestContext ctx) {
        repo.getCategories(aRequest(2, 10))
                .onComplete(ctx.succeeding(result -> ctx.verify(() -> {
                    assertOffset(capturedTuple(preparedQuery), 10, 10);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("getCategories page 3 with custom page size uses (page-1) * pageSize")
    void getCategoriesThirdPageCustomPageSize(VertxTestContext ctx) {
        repo.getCategories(aRequest(3, 5))
                .onComplete(ctx.succeeding(result -> ctx.verify(() -> {
                    assertOffset(capturedTuple(preparedQuery), 5, 10);
                    ctx.completeNow();
                })));
    }

    @Test
    @DisplayName("getCategories defaults page 0 to first page and page size 0 to 10")
    void getCategoriesInvalidPagingFallsBackToDefaults(VertxTestContext ctx) {
        repo.getCategories(aRequest(0, 0))
                .onComplete(ctx.succeeding(result -> ctx.verify(() -> {
                    assertOffset(capturedTuple(preparedQuery), 10, 0);
                    ctx.completeNow();
                })));
    }

    /* ─── getCategoriesActive pagination ─── */

    @Test
    @DisplayName("getCategoriesActive applies the same (page-1) * pageSize offset")
    void getCategoriesActiveOffset(VertxTestContext ctx) {
        repo.getCategoriesActive(aRequest(2, 25))
                .onComplete(ctx.succeeding(result -> ctx.verify(() -> {
                    assertOffset(capturedTuple(preparedQuery), 25, 25);
                    ctx.completeNow();
                })));
    }

    /* ─── getCategoriesTrashed pagination ─── */

    @Test
    @DisplayName("getCategoriesTrashed applies the same (page-1) * pageSize offset")
    void getCategoriesTrashedOffset(VertxTestContext ctx) {
        repo.getCategoriesTrashed(aRequest(4, 3))
                .onComplete(ctx.succeeding(result -> ctx.verify(() -> {
                    assertOffset(capturedTuple(preparedQuery), 3, 9);
                    ctx.completeNow();
                })));
    }
}
