package io.example.apigateway.utils;

import com.google.protobuf.MessageOrBuilder;

import io.example.common.exception.api.ApiException;
import io.example.common.exception.api.BadRequestException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.opentelemetry.api.trace.Span;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.RoutingContext;

public class GrpcGatewayUtils {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(GrpcGatewayUtils.class);

    public static void sendResponse(RoutingContext ctx, MessageOrBuilder proto, int httpStatus) {
        JsonObject json = ProtoMapper.toJson(proto);
        ctx.response()
                .setStatusCode(httpStatus)
                .putHeader("Content-Type", "application/json")
                .end(json.encode());
    }

    private static final String INVALID_GRPC_STATUS_PREFIX = "Invalid gRPC status ";

    public static void handleError(RoutingContext ctx, Throwable err) {
        if (err instanceof StatusRuntimeException sre) {
            String description = sre.getStatus().getDescription();
            respondError(ctx,
                    grpcToHttpStatus(sre.getStatus().getCode()),
                    description != null ? description : sre.getStatus().getCode().name());
        } else if (isInvalidGrpcStatus(err)) {
            respondInvalidGrpcStatus(ctx, err);
        } else {
            ctx.fail(500, err);
        }
    }

    /**
     * The vertx-grpc 4.5.x client fails a non-OK response with a plain
     * {@code "Invalid gRPC status <code>"} failure instead of a
     * {@link StatusRuntimeException}. Parse the embedded code so the gateway
     * can still translate it to the correct HTTP status.
     */
    private static boolean isInvalidGrpcStatus(Throwable err) {
        return err != null && err.getMessage() != null
                && err.getMessage().startsWith(INVALID_GRPC_STATUS_PREFIX);
    }

    private static void respondInvalidGrpcStatus(RoutingContext ctx, Throwable err) {
        try {
            String codeStr = err.getMessage().substring(INVALID_GRPC_STATUS_PREFIX.length()).trim();
            int code = Integer.parseInt(codeStr);
            Status.Code statusCode = Status.fromCodeValue(code).getCode();
            respondError(ctx, grpcToHttpStatus(statusCode), statusCode.name());
        } catch (RuntimeException e) {
            ctx.fail(500, err);
        }
    }

    /**
     * Single source of truth for the global failure handler: maps any Throwable
     * reaching {@code ctx.fail(...)} to a JSON error response.
     */
    public static void handleFailure(RoutingContext ctx, Throwable failure) {
        if (failure instanceof StatusRuntimeException sre) {
            handleError(ctx, sre);
        } else if (isInvalidGrpcStatus(failure)) {
            respondInvalidGrpcStatus(ctx, failure);
        } else if (failure instanceof ApiException apiEx) {
            respondError(ctx, apiEx.getStatusCode(), apiEx.getMessage());
        } else if (failure instanceof IllegalArgumentException iae) {
            respondError(ctx, 400, iae.getMessage());
        } else {
            // Jangan bocorkan detail internal/database ke client.
            log.error("Unhandled gateway failure", failure);
            respondError(ctx, 500, "Internal Server Error");
        }
    }

    public static int grpcToHttpStatus(Status.Code code) {
        return switch (code) {
            case NOT_FOUND -> 404;
            case ALREADY_EXISTS -> 409;
            case INVALID_ARGUMENT, FAILED_PRECONDITION -> 400;
            case UNAUTHENTICATED -> 401;
            case PERMISSION_DENIED -> 403;
            case DEADLINE_EXCEEDED -> 504;
            case RESOURCE_EXHAUSTED -> 429;
            case UNAVAILABLE -> 503;
            default -> 500;
        };
    }

    private static void respondError(RoutingContext ctx, int httpStatus, String message) {
        JsonObject body = new JsonObject()
                .put("status", "error")
                .put("message", message != null ? message : "Error");

        String traceId = currentTraceId();
        if (traceId != null) {
            body.put("trace_id", traceId);
        }

        ctx.response()
                .setStatusCode(httpStatus)
                .putHeader("Content-Type", "application/json")
                .end(body.encode());
    }

    private static String currentTraceId() {
        var spanContext = Span.current().getSpanContext();
        return spanContext.isValid() ? spanContext.getTraceId() : null;
    }

    /**
     * Reads the numeric user id from the JWT "sub" claim. The auth service signs
     * tokens with the user id under "sub" (see TokenService), not "userId".
     * Returns 0 when the token is missing or the claim is not a valid integer.
     */
    public static int getUserId(RoutingContext ctx) {
        if (ctx.user() == null || ctx.user().principal() == null) {
            return 0;
        }
        try {
            String sub = ctx.user().principal().getString("sub");
            return sub != null ? Integer.parseInt(sub) : 0;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int getSafePathInt(RoutingContext ctx, String param) {
        try {
            return Integer.parseInt(ctx.pathParam(param));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid path parameter: " + param + " must be an integer");
        }
    }

    public static String getJsonString(JsonObject json, String key, String defaultValue) {
        String value = json.getString(key);
        return value != null ? value : defaultValue;
    }

    public static Integer getJsonInteger(JsonObject json, String key, Integer defaultValue) {
        Integer value = json.getInteger(key);
        return value != null ? value : defaultValue;
    }

    public static int getJsonInteger(JsonObject json, String key, int defaultValue) {
        try {
            return json.getInteger(key, defaultValue);
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public static int getQueryInt(RoutingContext ctx, String key, int defaultValue) {
        try {
            return ctx.queryParams().contains(key) ? Integer.parseInt(ctx.queryParams().get(key)) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getQueryString(RoutingContext ctx, String key, String defaultValue) {
        return ctx.queryParams().contains(key) ? ctx.queryParams().get(key) : defaultValue;
    }

    public static String getFormString(RoutingContext ctx, String key, String defaultValue) {
        String value = ctx.request().getFormAttribute(key);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    public static int getFormInteger(RoutingContext ctx, String key, int defaultValue) {
        String value = ctx.request().getFormAttribute(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getRequiredFormString(RoutingContext ctx, String key) {
        String value = ctx.request().getFormAttribute(key);
        if (value == null || value.isBlank()) {
            throw new BadRequestException(key + " is required");
        }
        return value;
    }

    public static int getRequiredFormInteger(RoutingContext ctx, String key) {
        String value = ctx.request().getFormAttribute(key);
        if (value == null || value.isBlank()) {
            throw new BadRequestException(key + " is required");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException(key + " must be a valid integer");
        }
    }

    public static FileUpload getFileUpload(RoutingContext ctx, String fieldName) {
        return ctx.fileUploads().stream()
                .filter(f -> fieldName.equals(f.name()))
                .findFirst()
                .orElse(null);
    }
}
