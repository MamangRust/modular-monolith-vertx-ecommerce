package io.example.cart.service;

import io.example.common.model.ApiResponse;
import io.example.cart.model.CartResponse;
import io.vertx.core.Future;
import pb.cart.CartCommand.CreateCartRequest;
import pb.cart.CartCommand.DeleteCartRequest;
import pb.cart.CartCommand.DeleteAllCartRequest;

public interface CartCommandService {
    Future<ApiResponse<CartResponse>> create(CreateCartRequest req);
    Future<ApiResponse<Boolean>> deletePermanent(DeleteCartRequest req);
    Future<ApiResponse<Boolean>> deleteAll(DeleteAllCartRequest req);
}
