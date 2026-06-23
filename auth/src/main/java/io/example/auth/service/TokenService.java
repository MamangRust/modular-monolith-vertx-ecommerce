package io.example.auth.service;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TokenService {
    private final JWTAuth jwtProvider;

    public String createAccessToken(Integer userId) {
        return jwtProvider.generateToken(
                new JsonObject().put("sub", userId.toString()),
                new JWTOptions().setExpiresInMinutes(15));
    }

    public String createRefreshToken(Integer userId) {
        return jwtProvider.generateToken(
                new JsonObject().put("sub", userId.toString()),
                new JWTOptions().setExpiresInMinutes(1440) // 24 hours
        );
    }
}
