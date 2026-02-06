-- Seed initial roles
INSERT INTO
  roles (role_name, created_at)
VALUES
  ('STUDENT', NOW()),
  ('TEACHER', NOW()),
  ('ADMIN', NOW());