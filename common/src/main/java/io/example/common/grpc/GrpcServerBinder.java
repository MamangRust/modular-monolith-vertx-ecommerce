package io.example.common.grpc;

import io.vertx.core.Future;
import io.vertx.grpc.common.GrpcStatus;
import io.vertx.grpc.common.ServiceMethod;
import io.vertx.grpc.server.GrpcServer;
import io.vertx.grpc.server.GrpcServerResponse;

import java.util.function.Function;

/**
 * Binds unary gRPC service methods with correct error propagation.
 *
 * <p>The vertx-grpc 4.5.x generated {@code bind_*} default methods swallow the
 * failure and unconditionally send {@code GrpcStatus.INTERNAL}, which turns
 * every domain error (NotFound, BadRequest, Conflict, ...) into HTTP 500 at the
 * gateway. This binder maps failures through
 * {@link GrpcExceptionMapper#toGrpcStatus(Throwable)} and attaches the error
 * message, so the API gateway can translate the gRPC code into the right HTTP
 * status (NotFound -&gt; 404, InvalidArgument -&gt; 400, AlreadyExists -&gt; 409, ...).
 *
 * <p>Handlers should override the generated {@code bindAll(GrpcServer)} and
 * bind each implemented method with this class instead of the generated
 * defaults.
 */
public final class GrpcServerBinder {

    private GrpcServerBinder() {
    }

    public static <Req, Resp> void bind(
            GrpcServer server,
            ServiceMethod<Req, Resp> method,
            Function<Req, Future<Resp>> handler) {
        server.callHandler(method, request -> {
            request.handler(req -> {
                try {
                    handler.apply(req)
                            .onSuccess(resp -> request.response().end(resp).onFailure(t -> { }))
                            .onFailure(err -> fail(request.response(), err));
                } catch (RuntimeException e) {
                    fail(request.response(), e);
                }
            });
        });
    }

    private static void fail(GrpcServerResponse<?, ?> response, Throwable err) {
        GrpcStatus status = GrpcExceptionMapper.toGrpcStatus(err);
        if (status == null) {
            status = GrpcStatus.INTERNAL;
        }
        String message = err.getMessage();
        if (message != null && !message.isBlank()) {
            response.status(status).statusMessage(message).end().onFailure(t -> { });
        } else {
            response.status(status).end().onFailure(t -> { });
        }
    }
}
