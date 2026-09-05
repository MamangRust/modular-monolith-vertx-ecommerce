package io.example.apigateway.handler;

import static io.example.apigateway.utils.GrpcGatewayUtils.sendResponse;

import io.example.apigateway.utils.GrpcGatewayUtils;
import io.example.common.exception.api.BadRequestException;
import io.example.common.exception.api.UnauthorizedException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.RequiredArgsConstructor;
import pb.Auth;
import pb.VertxAuthServiceGrpcClient;

@RequiredArgsConstructor
public class AuthProxyHandler {
  private final VertxAuthServiceGrpcClient client;

  public void register(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();

    String email = GrpcGatewayUtils.getJsonString(body, "email", "");
    String password = GrpcGatewayUtils.getJsonString(body, "password", "");

    if (email.isBlank() || password.isBlank()) {
      ctx.fail(new BadRequestException("Email and Password are required"));
      return;
    }

    var req = Auth.RegisterRequest.newBuilder()
        .setFirstname(GrpcGatewayUtils.getJsonString(body, "firstname", ""))
        .setLastname(GrpcGatewayUtils.getJsonString(body, "lastname", ""))
        .setEmail(email)
        .setPassword(password)
        .setConfirmPassword(GrpcGatewayUtils.getJsonString(body, "confirm_password", ""))
        .build();

    client.registerUser(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 201))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void login(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();

    String email = GrpcGatewayUtils.getJsonString(body, "email", "");
    String password = GrpcGatewayUtils.getJsonString(body, "password", "");

    if (email.isBlank() || password.isBlank()) {
      ctx.fail(new BadRequestException("Email and Password are required"));
      return;
    }

    var req = Auth.LoginRequest.newBuilder()
        .setEmail(email)
        .setPassword(password)
        .build();

    client.loginUser(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void refreshToken(RoutingContext ctx) {
    JsonObject body = ctx.body().asJsonObject();
    String refreshToken = GrpcGatewayUtils.getJsonString(body, "refresh_token", "");

    if (refreshToken.isBlank()) {
      ctx.fail(new BadRequestException("Refresh Token is required"));
      return;
    }

    var req = Auth.RefreshTokenRequest.newBuilder()
        .setRefreshToken(refreshToken)
        .build();

    client.refreshToken(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void getMe(RoutingContext ctx) {
    if (ctx.user() == null || ctx.user().principal() == null) {
      ctx.fail(new UnauthorizedException("Unauthorized"));
      return;
    }

    int userId = GrpcGatewayUtils.getUserId(ctx);
    if (userId == 0) {
      ctx.fail(new UnauthorizedException("Invalid user token payload"));
      return;
    }

    var req = Auth.GetMeRequest.newBuilder()
        .setUserId(userId)
        .build();

    client.getMe(req)
        .onSuccess(resp -> sendResponse(ctx, resp, 200))
        .onFailure(err -> GrpcGatewayUtils.handleError(ctx, err));
  }

  public void logout(RoutingContext ctx) {
    if (ctx.user() != null) {
      ctx.user().authorizations().clear();
    }

    ctx.response()
        .setStatusCode(200)
        .putHeader("Content-Type", "application/json")
        .end(new JsonObject().put("message", "Successfully logged out").encode());
  }
}