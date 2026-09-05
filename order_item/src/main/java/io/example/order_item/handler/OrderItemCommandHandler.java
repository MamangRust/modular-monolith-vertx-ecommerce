package io.example.order_item.handler;

import com.google.protobuf.Empty;

import io.example.common.grpc.GrpcExceptionMapper;
import io.example.order_item.domain.requests.CreateOrderItemRecordRequest;
import io.example.order_item.domain.requests.UpdateOrderItemRecordRequest;
import io.example.order_item.service.OrderItemCommandService;
import io.vertx.core.Future;
import lombok.RequiredArgsConstructor;
import pb.order_item.OrderItemCommand.CalculateTotalPriceRequest;
import pb.order_item.OrderItemCommand.CalculateTotalPriceResponse;
import pb.order_item.OrderItemCommon.FindByIdOrderItemRequest;
import pb.order_item.OrderItemCommon.ApiResponseOrderItem;
import pb.order_item.OrderItemCommon.ApiResponseOrderItemDelete;
import pb.order_item.OrderItemCommon.ApiResponseOrderItemAll;
import pb.order_item.VertxOrderItemCommandServiceGrpcServer.OrderItemCommandServiceApi;
import io.example.common.grpc.GrpcServerBinder;

@RequiredArgsConstructor
public class OrderItemCommandHandler implements OrderItemCommandServiceApi {
    private final OrderItemCommandService service;

    @Override
    public Future<ApiResponseOrderItem> createOrderItem(
            pb.order_item.OrderItemCommand.CreateOrderItemRecordRequest req) {
        var domainReq = CreateOrderItemRecordRequest.builder()
                .orderId(req.getOrderId())
                .productId(req.getProductId())
                .quantity(req.getQuantity())
                .price(req.getPrice())
                .build();
        return service.create(domainReq)
                .map(data -> ApiResponseOrderItem.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.fromModel(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseOrderItem> updateOrderItem(
            pb.order_item.OrderItemCommand.UpdateOrderItemRecordRequest req) {
        var domainReq = UpdateOrderItemRecordRequest.builder()
                .orderItemId(req.getOrderItemId())
                .quantity(req.getQuantity())
                .price(req.getPrice())
                .build();
        return service.update(domainReq)
                .map(data -> ApiResponseOrderItem.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setData(ProtoConverter.fromModel(data))
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseOrderItem> trashOrderItem(FindByIdOrderItemRequest req) {
        return service.trash((long) req.getId())
                .map(data -> {
                    ApiResponseOrderItem.Builder builder = ApiResponseOrderItem.newBuilder()
                            .setStatus("success")
                            .setMessage("OK");
                    if (data != null && !data.isEmpty()) {
                        builder.setData(ProtoConverter.fromModelDeleteAtToResponse(data.get(0)));
                    }
                    return builder.build();
                })
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseOrderItem> restoreOrderItem(FindByIdOrderItemRequest req) {
        return service.restore((long) req.getId())
                .map(data -> {
                    ApiResponseOrderItem.Builder builder = ApiResponseOrderItem.newBuilder()
                            .setStatus("success")
                            .setMessage("OK");
                    if (data != null && !data.isEmpty()) {
                        builder.setData(ProtoConverter.fromModelDeleteAtToResponse(data.get(0)));
                    }
                    return builder.build();
                })
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseOrderItemDelete> deleteOrderItemPermanent(FindByIdOrderItemRequest req) {
        return service.deletePermanent((long) req.getId())
                .map(v -> ApiResponseOrderItemDelete.newBuilder()
                        .setStatus("success")
                        .setMessage("Order item deleted permanently")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseOrderItemAll> restoreAllOrdersItem(Empty req) {
        return service.restoreAll()
                .map(v -> ApiResponseOrderItemAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All order items restored successfully")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseOrderItemAll> deleteAllPermanentOrdersItem(Empty req) {
        return service.deleteAll()
                .map(v -> ApiResponseOrderItemAll.newBuilder()
                        .setStatus("success")
                        .setMessage("All order items permanently deleted")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<ApiResponseOrderItemDelete> deleteOrderItemByOrderPermanent(FindByIdOrderItemRequest req) {
        return service.deleteByOrderPermanent((long) req.getId())
                .map(v -> ApiResponseOrderItemDelete.newBuilder()
                        .setStatus("success")
                        .setMessage("Order items deleted permanently")
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

    @Override
    public Future<CalculateTotalPriceResponse> calculateTotalPrice(CalculateTotalPriceRequest req) {
        return service.calculateTotalPrice((long) req.getOrderId())
                .map(data -> CalculateTotalPriceResponse.newBuilder()
                        .setStatus("success")
                        .setMessage("OK")
                        .setTotalPrice(data != null ? data.intValue() : 0)
                        .build())
                .recover(GrpcExceptionMapper::toFailedFuture);
    }

  @Override
  public pb.order_item.VertxOrderItemCommandServiceGrpcServer.OrderItemCommandServiceApi bindAll(io.vertx.grpc.server.GrpcServer server) {
    GrpcServerBinder.bind(server, pb.order_item.VertxOrderItemCommandServiceGrpcServer.CreateOrderItem, this::createOrderItem);
    GrpcServerBinder.bind(server, pb.order_item.VertxOrderItemCommandServiceGrpcServer.UpdateOrderItem, this::updateOrderItem);
    GrpcServerBinder.bind(server, pb.order_item.VertxOrderItemCommandServiceGrpcServer.TrashOrderItem, this::trashOrderItem);
    GrpcServerBinder.bind(server, pb.order_item.VertxOrderItemCommandServiceGrpcServer.RestoreOrderItem, this::restoreOrderItem);
    GrpcServerBinder.bind(server, pb.order_item.VertxOrderItemCommandServiceGrpcServer.DeleteOrderItemPermanent, this::deleteOrderItemPermanent);
    GrpcServerBinder.bind(server, pb.order_item.VertxOrderItemCommandServiceGrpcServer.RestoreAllOrdersItem, this::restoreAllOrdersItem);
    GrpcServerBinder.bind(server, pb.order_item.VertxOrderItemCommandServiceGrpcServer.DeleteAllPermanentOrdersItem, this::deleteAllPermanentOrdersItem);
    GrpcServerBinder.bind(server, pb.order_item.VertxOrderItemCommandServiceGrpcServer.DeleteOrderItemByOrderPermanent, this::deleteOrderItemByOrderPermanent);
    GrpcServerBinder.bind(server, pb.order_item.VertxOrderItemCommandServiceGrpcServer.CalculateTotalPrice, this::calculateTotalPrice);
    return this;
  }
}