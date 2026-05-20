package io.example.apigateway.middleware;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public final class ApiKeyMiddleware {
  private ApiKeyMiddleware() {}

  // NOTE: This middleware is currently a placeholder and disabled because FindByApiKeyRequest
  // is not defined in the merchant gRPC schemas.
  /*
  public static Handler<RoutingContext> requireApiKey(Object merchantClient) {
    return ctx -> {
      ctx.next();
    };
  }
  */
}
