package io.example.role.repository;

import io.example.role.domain.requests.CreateRoleRequest;
import io.example.role.domain.requests.UpdateRoleRequest;
import io.example.role.model.Role;
import io.vertx.core.Future;

public interface RoleCommandRepository {
    Future<Role> createRole(CreateRoleRequest request);

    Future<Role> updateRole(UpdateRoleRequest request);

    Future<Role> trashed(Long roleId);

    Future<Role> restore(Long roleId);

    Future<Boolean> deletePermanent(Long roleId);

    Future<Integer> restoreAllRoles();

    Future<Integer> deleteAllPermanentRoles();
}
