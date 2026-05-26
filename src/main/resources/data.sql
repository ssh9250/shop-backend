SET FOREIGN_KEY_CHECKS = 0;

DELETE FROM order_item;
DELETE FROM orders;
DELETE FROM post_file;
DELETE FROM comment;
DELETE FROM post;
DELETE FROM item;
DELETE FROM member;

SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO member (
    created_at,
    updated_at,
    deleted_at,
    email,
    password,
    nickname,
    phone,
    address,
    role
)
VALUES (
           NOW(),
           NOW(),
           NULL,
           'admin@test.com',
           '1234',
           '테스트판매자',
           '010-0000-0000',
           'Busan',
           'ADMIN'
       );

INSERT INTO item (
    created_at,
    updated_at,
    deleted_at,
    name,
    description,
    stock,
    price,
    used,
    item_status,
    version,
    member_id
)
VALUES (
           NOW(),
           NOW(),
           NULL,
           '동시성 테스트 아이템',
           '재고 100만개 테스트용',
           1000000,
           10000,
           false,
           0,
           0,
           (
               SELECT id
               FROM member
               WHERE email = 'admin@test.com'
           )
       );