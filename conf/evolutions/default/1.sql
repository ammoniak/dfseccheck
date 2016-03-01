# Users schema

# --- !Ups

CREATE TABLE "useraccount" (
    "id" bigint NOT NULL,
    "email" varchar(255) NOT NULL,
    "pw" varchar(255) NOT NULL,
    "fullname" varchar(255) NOT NULL
);

# --- !Downs

DROP TABLE "useraccount";