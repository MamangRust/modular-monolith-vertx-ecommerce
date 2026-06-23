package io.example.role.repository;

import java.util.List;
import io.example.common.domain.PagedResult;
import io.example.role.domain.requests.FindAllRolesRequest;
import io.example.role.model.Role;
import io.vertx.core.Future;

public interface RoleQueryRepository {
    Future<PagedResult<Role>> getRoles(FindAllRolesRequest request);

    Future<PagedResult<Role>> getActiveRoles(FindAllRolesRequest request);

    Future<PagedResult<Role>> getTrashedRoles(FindAllRolesRequest request);

    Future<Role> getRoleById(Long roleId);

    Future<Role> findByTrashedId(Long roleId);

    Future<Role> getRoleByName(String roleName);

    Future<List<Role>> getRolesByUserId(Long userId);
}
