package io.example.role.service;

import java.util.List;

import io.example.common.domain.PagedResult;
import io.example.role.domain.requests.FindAllRolesRequest;
import io.example.role.model.RoleResponse;
import io.example.role.model.RoleResponseDeleteAt;
import io.vertx.core.Future;

public interface RoleQueryService {
    Future<PagedResult<RoleResponse>> getAllRoles(FindAllRolesRequest req);

    Future<PagedResult<RoleResponseDeleteAt>> getActiveRoles(FindAllRolesRequest req);

    Future<PagedResult<RoleResponseDeleteAt>> getTrashedRoles(FindAllRolesRequest req);

    Future<RoleResponse> getRoleById(Long roleId);

    Future<List<RoleResponse>> getRolesByUserId(Long userId);
}