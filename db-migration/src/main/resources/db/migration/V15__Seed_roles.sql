-- Seed default roles used by the application (RegisterService assigns ROLE_ADMIN by default)
INSERT INTO roles (role_name)
VALUES ('ROLE_ADMIN')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO roles (role_name)
VALUES ('ROLE_USER')
ON CONFLICT (role_name) DO NOTHING;

INSERT INTO roles (role_name)
VALUES ('ROLE_MERCHANT')
ON CONFLICT (role_name) DO NOTHING;
