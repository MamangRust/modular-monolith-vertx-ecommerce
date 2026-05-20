package io.example.order_item.handler;

import com.google.protobuf.Empty;
import io.example.order_item.service.OrderItemCommandService;
import io.vertx.core.Future;
import pb.order_item.OrderItemCommand.CreateOrderItemRecordRequest;
import pb.order_item.OrderItemCommand.UpdateOrderItemRecordRequest;
import pb.order_item.OrderItemCommand.CalculateTotalPriceRequest;
import pb.order_item.OrderItemCommand.CalculateTotalPriceResponse;
import pb.order_item.OrderItemCommon.FindByIdOrderItemRequest;
import pb.order_item.OrderItemCommon.ApiResponseOrderItem;
import pb.order_item.OrderItemCommon.ApiResponseOrderItemDelete;
import pb.order_item.OrderItemCommon.ApiResponseOrderItemAll;
import pb.order_item.VertxOrderItemCommandServiceGrpcServer.OrderItemCommandServiceApi;

public class OrderItemCommandHandler implements OrderItemCommandServiceApi {
    private final OrderItemCommandService service;

    public OrderItemCommandHandler(OrderItemCommandService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponseOrderItem> createOrderItem(CreateOrderItemRecordRequest req) {
        return service.create(req)
                .map(resp -> {
                    ApiResponseOrderItem.Builder builder = ApiResponseOrderItem.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.fromModel(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderItem> updateOrderItem(UpdateOrderItemRecordRequest req) {
        return service.update(req)
                .map(resp -> {
                    ApiResponseOrderItem.Builder builder = ApiResponseOrderItem.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.fromModel(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderItem> trashOrderItem(FindByIdOrderItemRequest req) {
        return service.trash(req.getId())
                .map(resp -> {
                    ApiResponseOrderItem.Builder builder = ApiResponseOrderItem.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null && !resp.data().isEmpty()) {
                        builder.setData(ProtoConverter.fromModelDeleteAtToResponse(resp.data().get(0)));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderItem> restoreOrderItem(FindByIdOrderItemRequest req) {
        return service.restore(req.getId())
                .map(resp -> {
                    ApiResponseOrderItem.Builder builder = ApiResponseOrderItem.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null && !resp.data().isEmpty()) {
                        builder.setData(ProtoConverter.fromModelDeleteAtToResponse(resp.data().get(0)));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderItemDelete> deleteOrderItemPermanent(FindByIdOrderItemRequest req) {
        return service.deletePermanent(req.getId())
                .map(resp -> ApiResponseOrderItemDelete.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .build());
    }

    @Override
    public Future<ApiResponseOrderItemAll> restoreAllOrdersItem(Empty req) {
        return service.restoreAll()
                .map(resp -> ApiResponseOrderItemAll.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .build());
    }

    @Override
    public Future<ApiResponseOrderItemAll> deleteAllPermanentOrdersItem(Empty req) {
        return service.deleteAll()
                .map(resp -> ApiResponseOrderItemAll.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .build());
    }

    @Override
    public Future<ApiResponseOrderItemDelete> deleteOrderItemByOrderPermanent(FindByIdOrderItemRequest req) {
        return service.deleteByOrderPermanent(req.getId())
                .map(resp -> ApiResponseOrderItemDelete.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .build());
    }

    @Override
    public Future<CalculateTotalPriceResponse> calculateTotalPrice(CalculateTotalPriceRequest req) {
        return service.calculateTotalPrice(req.getOrderId())
                .map(resp -> CalculateTotalPriceResponse.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .setTotalPrice(resp.data() != null ? resp.data() : 0)
                        .build());
    }
}
