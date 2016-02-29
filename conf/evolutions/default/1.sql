# Users schema

# --- !Ups

CREATE TABLE "useraccount" (
    "id" bigint(20) NOT NULL AUTO_INCREMENT,
    "email" varchar(255) NOT NULL,
    "password" varchar(255) NOT NULL,
    "fullname" varchar(255) NOT NULL,
    "isAdmin" boolean NOT NULL,
    PRIMARY KEY ("id")
);

# --- !Downs

DROP TABLE "useraccount";