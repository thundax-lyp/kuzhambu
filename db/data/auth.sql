SET NAMES utf8mb4;

-- Initial admin account:
--   login name: admin
--   password credential value is a placeholder and must be rotated before production use.

INSERT INTO `auth_principal_identity` (
    `identity_id`, `principal_type`, `principal_id`, `identity_type`,
    `identity_value`, `status`
) VALUES (
    '01KUZHAMBU00000000000007',
    'USER',
    '01KUZHAMBU00000000000001',
    'USER_ACCOUNT',
    'admin',
    'ENABLED'
) ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `identity_value` = VALUES(`identity_value`),
    `status` = VALUES(`status`);

INSERT INTO `auth_principal_credential` (
    `credential_id`, `principal_type`, `principal_id`, `identity_id`,
    `credential_type`, `credential_value`, `status`, `need_change_password`,
    `failed_count`, `failed_limit`
) VALUES (
    '01KUZHAMBU00000000000008',
    'USER',
    '01KUZHAMBU00000000000001',
    '01KUZHAMBU00000000000007',
    'USER_PASSWORD',
    '{noop}admin',
    'ACTIVE',
    1,
    0,
    5
) ON DUPLICATE KEY UPDATE
    `principal_type` = VALUES(`principal_type`),
    `principal_id` = VALUES(`principal_id`),
    `credential_value` = VALUES(`credential_value`),
    `status` = VALUES(`status`),
    `need_change_password` = VALUES(`need_change_password`),
    `failed_count` = VALUES(`failed_count`),
    `failed_limit` = VALUES(`failed_limit`);
