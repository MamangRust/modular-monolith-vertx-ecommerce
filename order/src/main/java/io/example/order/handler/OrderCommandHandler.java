package io.example.order.handler;

import com.google.protobuf.Empty;
import io.example.order.service.OrderCommandService;
import io.vertx.core.Future;
import pb.order.OrderCommon.ApiResponseOrder;
import pb.order.OrderCommon.ApiResponseOrderDeleteAt;
import pb.order.OrderCommon.ApiResponseOrderDelete;
import pb.order.OrderCommon.ApiResponseOrderAll;
import pb.order.OrderCommon.FindByIdOrderRequest;
import pb.order.OrderCommand.CreateOrderRequest;
import pb.order.OrderCommand.UpdateOrderRequest;
import pb.order.VertxOrderCommandServiceGrpcServer.OrderCommandServiceApi;

public class OrderCommandHandler implements OrderCommandServiceApi {
    private final OrderCommandService service;

    public OrderCommandHandler(OrderCommandService service) {
        this.service = service;
    }

    @Override
    public Future<ApiResponseOrder> create(CreateOrderRequest req) {
        return service.createOrder(req)
                .map(resp -> {
                    var builder = ApiResponseOrder.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.toOrderResponse(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrder> update(UpdateOrderRequest req) {
        return service.updateOrder(req)
                .map(resp -> {
                    var builder = ApiResponseOrder.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.toOrderResponse(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderDeleteAt> trashedOrder(FindByIdOrderRequest req) {
        return service.trash((long) req.getId())
                .map(resp -> {
                    var builder = ApiResponseOrderDeleteAt.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.toOrderResponseDeleteAt(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderDeleteAt> restoreOrder(FindByIdOrderRequest req) {
        return service.restore((long) req.getId())
                .map(resp -> {
                    var builder = ApiResponseOrderDeleteAt.newBuilder()
                            .setStatus(resp.status())
                            .setMessage(resp.message());
                    if (resp.data() != null) {
                        builder.setData(ProtoConverter.toOrderResponseDeleteAt(resp.data()));
                    }
                    return builder.build();
                });
    }

    @Override
    public Future<ApiResponseOrderDelete> deleteOrderPermanent(FindByIdOrderRequest req) {
        return service.deletePermanent((long) req.getId())
                .map(resp -> ApiResponseOrderDelete.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .build());
    }

    @Override
    public Future<ApiResponseOrderAll> restoreAllOrder(Empty req) {
        return service.restoreAll()
                .map(resp -> ApiResponseOrderAll.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .build());
    }

    @Override
    public Future<ApiResponseOrderAll> deleteAllOrderPermanent(Empty req) {
        return service.deleteAllPermanent()
                .map(resp -> ApiResponseOrderAll.newBuilder()
                        .setStatus(resp.status())
                        .setMessage(resp.message())
                        .build());
    }
}
