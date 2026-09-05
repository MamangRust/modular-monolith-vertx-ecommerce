package io.example.common.grpc;

import static org.assertj.core.api.Assertions.assertThat;

import io.example.common.exception.grpc.BadRequestException;
import io.example.common.exception.grpc.ConflictException;
import io.example.common.exception.grpc.ForbiddenException;
import io.example.common.exception.grpc.InsufficientBalanceException;
import io.example.common.exception.grpc.InternalServerErrorException;
import io.example.common.exception.grpc.NotFoundException;
import io.example.common.exception.grpc.UnauthorizedException;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.vertx.core.Future;
import org.junit.jupiter.api.Test;

class GrpcExceptionMapperTest {

  @Test
  void mapsNotFoundToNotFoundStatus() {
    assertGrpcStatus(new NotFoundException("User not found"), Status.Code.NOT_FOUND);
  }

  @Test
  void mapsBadRequestToInvalidArgument() {
    assertGrpcStatus(new BadRequestException("ID is required"), Status.Code.INVALID_ARGUMENT);
  }

  @Test
  void mapsConflictToAlreadyExists() {
    assertGrpcStatus(new ConflictException("User already exists"), Status.Code.ALREADY_EXISTS);
  }

  @Test
  void mapsInsufficientBalanceToFailedPrecondition() {
    assertGrpcStatus(new InsufficientBalanceException(100, 250), Status.Code.FAILED_PRECONDITION);
  }

  @Test
  void mapsUnauthorizedToUnauthenticated() {
    assertGrpcStatus(new UnauthorizedException("Invalid credentials"), Status.Code.UNAUTHENTICATED);
  }

  @Test
  void mapsForbiddenToPermissionDenied() {
    assertGrpcStatus(new ForbiddenException("Permission denied"), Status.Code.PERMISSION_DENIED);
  }

  @Test
  void mapsInternalServerErrorToInternal() {
    assertGrpcStatus(
        new InternalServerErrorException("Boom", new RuntimeException("cause")),
        Status.Code.INTERNAL);
  }

  @Test
  void passesThroughExistingStatusRuntimeException() {
    StatusRuntimeException original = Status.UNAVAILABLE
        .withDescription("service down")
        .asRuntimeException();

    StatusRuntimeException mapped = GrpcExceptionMapper.toStatusRuntimeException(original);

    assertThat(mapped).isSameAs(original);
    assertThat(mapped.getStatus().getCode()).isEqualTo(Status.Code.UNAVAILABLE);
  }

  @Test
  void mapsUnknownThrowableToInternal() {
    StatusRuntimeException mapped =
        GrpcExceptionMapper.toStatusRuntimeException(new IllegalStateException("weird"));

    assertThat(mapped.getStatus().getCode()).isEqualTo(Status.Code.INTERNAL);
  }

  @Test
  void attachesErrorResponseDetailToStatus() {
    StatusRuntimeException mapped = GrpcExceptionMapper.toStatusRuntimeException(
        new UnauthorizedException("Invalid credentials"));

    assertThat(mapped.getStatus().getCode()).isEqualTo(Status.Code.UNAUTHENTICATED);
    assertThat(mapped.getStatus().getDescription()).isEqualTo("Invalid credentials");
  }

  @Test
  void toFailedFutureReturnsFailedFutureWithStatusException() {
    Future<Object> future = GrpcExceptionMapper.toFailedFuture(new NotFoundException("nope"));

    assertThat(future.failed()).isTrue();
    assertThat(future.cause())
        .isInstanceOf(StatusRuntimeException.class)
        .satisfies(err -> {
          StatusRuntimeException sre = (StatusRuntimeException) err;
          assertThat(sre.getStatus().getCode()).isEqualTo(Status.Code.NOT_FOUND);
        });
  }

  private static void assertGrpcStatus(Throwable domainException, Status.Code expectedCode) {
    StatusRuntimeException mapped = GrpcExceptionMapper.toStatusRuntimeException(domainException);
    assertThat(mapped.getStatus().getCode()).isEqualTo(expectedCode);
    assertThat(mapped.getStatus().getDescription())
        .isEqualTo(domainException.getMessage());
  }
}
