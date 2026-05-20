package io.example.cart.service;

import java.util.List;
import io.example.common.model.ApiResponsePagination;
import io.example.cart.model.CartResponse;
import io.vertx.core.Future;
import pb.cart.CartQuery.FindAllCartRequest;

public interface CartQueryService {
    Future<ApiResponsePagination<List<CartResponse>>> findAll(FindAllCartRequest req);
}
