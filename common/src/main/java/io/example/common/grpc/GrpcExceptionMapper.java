package io.example.common.grpc;

import io.example.common.exception.grpc.GrpcException;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.vertx.core.Future;

public final class GrpcExceptionMapper {

    private GrpcExceptionMapper() {
    }

    /**
     * Maps any Throwable to a failed Future with proper gRPC Status.
     * This is the single place where domain exceptions → gRPC status mapping
     * happens. The status code is derived from
     * {@link GrpcException#getGrpcStatusCode()} — the single source of truth
     * per exception class — so adding a new domain exception automatically
     * gets the correct mapping without touching this class.
     *
     * <p>Note: protobuf {@code pb.Api.ErrorResponse} detail is intentionally
     * not attached as gRPC status trailers yet — grpc-api 1.65.0 has no
     * {@code Status.withDetails(...)}; the API Gateway already round-trips the
     * correct HTTP status purely from the gRPC status code.
     */
    public static <T> Future<T> toFailedFuture(Throwable throwable) {
        return Future.failedFuture(toStatusRuntimeException(throwable));
    }

    public static StatusRuntimeException toStatusRuntimeException(Throwable throwable) {
        if (throwable instanceof StatusRuntimeException sre) {
            return sre;
        }

        if (throwable instanceof GrpcException de) {
            return Status.fromCode(de.getGrpcStatusCode())
                    .withDescription(de.getMessage())
                    .withCause(de)
                    .asRuntimeException();
        }

        return Status.INTERNAL
                .withDescription("An unexpected error occurred")
                .withCause(throwable)
                .asRuntimeException();
    }

    /**
     * Maps any Throwable to a Vert.x {@link io.vertx.grpc.common.GrpcStatus}.
     * Used by {@link GrpcServerBinder} so the service responds with the correct
     * gRPC status code. The vertx-grpc 4.5.x generated {@code bind_*} default
     * methods swallow the failure and always send INTERNAL, so the binder needs
     * this explicit mapping.
     */
    public static io.vertx.grpc.common.GrpcStatus toGrpcStatus(Throwable throwable) {
        if (throwable instanceof GrpcException de) {
            return io.vertx.grpc.common.GrpcStatus.valueOf(de.getGrpcStatusCode().value());
        }
        if (throwable instanceof StatusRuntimeException sre) {
            return io.vertx.grpc.common.GrpcStatus.valueOf(sre.getStatus().getCode().value());
        }
        if (throwable instanceof StatusException se) {
            return io.vertx.grpc.common.GrpcStatus.valueOf(se.getStatus().getCode().value());
        }
        return io.vertx.grpc.common.GrpcStatus.INTERNAL;
    }
}
