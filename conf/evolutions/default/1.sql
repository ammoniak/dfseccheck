# Users schema

# --- !Ups

CREATE TABLE "useraccount" (
    "id" varchar(255) NOT NULL,
    "email" varchar(255) NOT NULL,
    "fullname" varchar(255) NOT NULL
);
create unique index i_useraccount_id on "useraccount" ("id");

# --- !Downs

DROP TABLE "useraccount";