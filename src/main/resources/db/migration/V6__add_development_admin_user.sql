INSERT INTO users (
    email,
    password_hash,
    first_name,
    last_name,
    role,
    active
)
VALUES (
           'admin@example.com',
           'NOT_YET_AUTHENTICATABLE',
           'Development',
           'Admin',
           'ADMIN',
           TRUE
       );