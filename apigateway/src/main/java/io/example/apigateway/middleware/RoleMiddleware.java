package io.example.apigateway.middleware;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.RoutingContext;

public final class RoleMiddleware {
  private RoleMiddleware() {}

  public static Handler<RoutingContext> requireRole(String role) {
    return ctx -> {
      if (ctx.user() == null || ctx.user().principal() == null) {
        ctx.response().setStatusCode(401).end("Unauthorized");
        return;
      }

      JsonArray roles = ctx.user().principal().getJsonArray("roleNames");

      if (roles == null || !hasRole(roles, role)) {
        ctx.response().setStatusCode(403).end("Forbidden");
        return;
      }

      ctx.next();
    };
  }

  /**
   * Matches either the exact role name ("ADMIN") or the prefixed form
   * ("ROLE_ADMIN") used by the auth service.
   */
  private static boolean hasRole(JsonArray roles, String required) {
    if (roles == null) {
      return false;
    }
    String requiredPrefixed = required.startsWith("ROLE_") ? required : "ROLE_" + required;
    for (Object candidate : roles) {
      String name = String.valueOf(candidate);
      if (name.equals(required) || name.equals(requiredPrefixed)) {
        return true;
      }
    }
    return false;
  }
}
