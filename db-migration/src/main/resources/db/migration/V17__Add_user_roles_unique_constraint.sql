-- UserRoleRepositoryImpl uses ON CONFLICT (user_id, role_id) DO NOTHING
-- which requires a unique constraint on those columns.
ALTER TABLE user_roles
    ADD CONSTRAINT uq_user_roles_user_role UNIQUE (user_id, role_id);
