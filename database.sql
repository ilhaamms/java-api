DROP DATABASE belajar_spring_restful_api;

CREATE DATABASE belajar_spring_restful_api;

USE belajar_spring_restful_api;

DROP TABLE users;

CREATE Table users(
    username VARCHAR(100) NOT NULL ,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(100) NOT NULL,
    token VARCHAR(100),
    token_expired_at BIGINT,
    PRIMARY KEY (username),
    UNIQUE (token)
) ENGINE InnoDB;

SELECT * FROM users;

TRUNCATE TABLE users;

DESC users;

CREATE TABLE contacts(
    id VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    phone VARCHAR(100),
    email VARCHAR(100),
    PRIMARY KEY (id),
# artinya username di table contacts adalah foreign key yang merujuk ke username di table users
# dengan nama foreign key fk_users_contact
    FOREIGN KEY fk_users_contact (username) REFERENCES users(username)
)ENGINE InnoDB;

SELECT * FROM contacts;

TRUNCATE TABLE contacts;

DESC contacts;

