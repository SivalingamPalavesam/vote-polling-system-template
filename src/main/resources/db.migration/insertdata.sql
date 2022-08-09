--liquibase formatted sql
--changeset sivalingam:7
INSERT  INTO roles(name) VALUES('ROLE_USER');
INSERT  INTO roles(name) VALUES('ROLE_ADMIN');