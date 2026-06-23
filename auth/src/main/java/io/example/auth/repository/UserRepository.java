package io.example.auth.repository;

import io.example.auth.domain.requests.CreateUserRequest;
import io.example.auth.domain.requests.UpdateUserVerifiedRequest;
import io.example.auth.domain.requests.UpdateUserPasswordRequest;
import io.example.auth.model.AuthUser;
import io.vertx.core.Future;

public interface UserRepository {
    Future<AuthUser> findByEmail(String email);
    Future<AuthUser> findByEmailAndVerify(String email);
    Future<AuthUser> findById(Integer userId);
    Future<AuthUser> createUser(CreateUserRequest request);
    Future<AuthUser> updateUserIsVerified(UpdateUserVerifiedRequest request);
    Future<AuthUser> updateUserPassword(UpdateUserPasswordRequest request);
    Future<AuthUser> findByVerificationCode(String verificationCode);
}
