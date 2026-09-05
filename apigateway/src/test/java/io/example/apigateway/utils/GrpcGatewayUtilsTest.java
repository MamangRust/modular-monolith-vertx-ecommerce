package io.example.apigateway.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.example.common.exception.api.BadRequestException;
import io.example.common.exception.api.ForbiddenException;
import io.example.common.exception.api.NotFoundException;
import io.example.common.exception.api.UnauthorizedException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class GrpcGatewayUtilsTest {

  private RoutingContext ctx;
  private HttpServerResponse response;

  @BeforeEach
  void setUp() {
    ctx = mock(RoutingContext.class);
    response = mock(HttpServerResponse.class);
    when(ctx.response()).thenReturn(response);
    when(response.setStatusCode(anyInt())).thenReturn(response);
    when(response.putHeader(anyString(), anyString())).thenReturn(response);
    when(response.end(anyString())).thenReturn(Future.succeededFuture());
  }

  private static StatusRuntimeException grpc(Status.Code code, String message) {
    return Status.fromCode(code).withDescription(message).asRuntimeException();
  }

  @Test
  void mapsNotFoundTo404() {
    GrpcGatewayUtils.handleError(ctx, grpc(Status.Code.NOT_FOUND, "User not found"));
    verify(response).setStatusCode(404);
  }

  @Test
  void mapsInvalidArgumentTo400() {
    GrpcGatewayUtils.handleError(ctx, grpc(Status.Code.INVALID_ARGUMENT, "bad input"));
    verify(response).setStatusCode(400);
  }

  @Test
  void mapsAlreadyExistsTo409() {
    GrpcGatewayUtils.handleError(ctx, grpc(Status.Code.ALREADY_EXISTS, "User already exists"));
    verify(response).setStatusCode(409);
  }

  @Test
  void mapsUnauthenticatedTo401() {
    GrpcGatewayUtils.handleError(ctx, grpc(Status.Code.UNAUTHENTICATED, "Invalid credentials"));
    verify(response).setStatusCode(401);
  }

  @Test
  void mapsPermissionDeniedTo403() {
    GrpcGatewayUtils.handleError(ctx, grpc(Status.Code.PERMISSION_DENIED, "Forbidden"));
    verify(response).setStatusCode(403);
  }

  @Test
  void mapsUnavailableTo503() {
    GrpcGatewayUtils.handleError(ctx, grpc(Status.Code.UNAVAILABLE, "service down"));
    verify(response).setStatusCode(503);
  }

  @Test
  void mapsDeadlineExceededTo504() {
    GrpcGatewayUtils.handleError(ctx, grpc(Status.Code.DEADLINE_EXCEEDED, "timeout"));
    verify(response).setStatusCode(504);
  }

  @Test
  void mapsResourceExhaustedTo429() {
    GrpcGatewayUtils.handleError(ctx, grpc(Status.Code.RESOURCE_EXHAUSTED, "rate limited"));
    verify(response).setStatusCode(429);
  }

  @Test
  void mapsUnknownCodeTo500() {
    GrpcGatewayUtils.handleError(ctx, grpc(Status.Code.INTERNAL, "boom"));
    verify(response).setStatusCode(500);
  }

  @Test
  void errorBodyContainsStatusAndMessage() {
    GrpcGatewayUtils.handleError(ctx, grpc(Status.Code.NOT_FOUND, "User not found"));

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(response).end(captor.capture());
    assertThat(captor.getValue())
        .contains("\"status\":\"error\"")
        .contains("\"message\":\"User not found\"");
  }

  @Test
  void errorBodyOmitsTraceIdWhenNoSpanActive() {
    GrpcGatewayUtils.handleError(ctx, grpc(Status.Code.NOT_FOUND, "User not found"));

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(response).end(captor.capture());
    assertThat(captor.getValue()).doesNotContain("trace_id");
  }

  @Test
  void handleFailureUnknownErrorDoesNotLeakInternalMessage() {
    GrpcGatewayUtils.handleFailure(ctx, new IllegalStateException("connection refused: secret db detail"));

    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(response).end(captor.capture());
    assertThat(captor.getValue())
        .contains("\"message\":\"Internal Server Error\"")
        .doesNotContain("secret db detail");
  }

  @Test
  void handleFailureMapsApiBadRequestTo400() {
    GrpcGatewayUtils.handleFailure(ctx, new BadRequestException("Email is required"));
    verify(response).setStatusCode(400);
  }

  @Test
  void handleFailureMapsApiNotFoundTo404() {
    GrpcGatewayUtils.handleFailure(ctx, new NotFoundException("Route not found"));
    verify(response).setStatusCode(404);
  }

  @Test
  void handleFailureMapsApiUnauthorizedTo401() {
    GrpcGatewayUtils.handleFailure(ctx, new UnauthorizedException("Unauthorized"));
    verify(response).setStatusCode(401);
  }

  @Test
  void handleFailureMapsApiForbiddenTo403() {
    GrpcGatewayUtils.handleFailure(ctx, new ForbiddenException("Forbidden"));
    verify(response).setStatusCode(403);
  }

  @Test
  void handleFailureMapsIllegalArgumentTo400() {
    GrpcGatewayUtils.handleFailure(ctx, new IllegalArgumentException("Invalid path parameter"));
    verify(response).setStatusCode(400);
  }

  @Test
  void handleFailureMapsGrpcErrorThroughHandleError() {
    GrpcGatewayUtils.handleFailure(ctx, grpc(Status.Code.ALREADY_EXISTS, "conflict"));
    verify(response).setStatusCode(409);
  }

  @Test
  void handleFailureMapsUnknownTo500() {
    GrpcGatewayUtils.handleFailure(ctx, new IllegalStateException("weird"));
    verify(response).setStatusCode(500);
  }

  @Test
  void handleFailureMapsNullTo500() {
    GrpcGatewayUtils.handleFailure(ctx, null);
    verify(response).setStatusCode(500);
  }
}
