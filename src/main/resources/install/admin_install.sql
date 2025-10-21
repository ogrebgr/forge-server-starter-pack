CREATE TABLE IF NOT EXISTS forge_starter_pack.admin_users
(
    id serial NOT NULL,
    is_disabled boolean NOT NULL,
    is_superadmin boolean NOT NULL,
    name character varying COLLATE pg_catalog."default",
    CONSTRAINT admin_users_pkey PRIMARY KEY (id)
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS forge_starter_pack.admin_users
    OWNER to luckinvasion_master_admin;

REVOKE ALL ON TABLE forge_starter_pack.admin_users FROM luckinvasion_master;

GRANT INSERT, DELETE, SELECT, UPDATE ON TABLE forge_starter_pack.admin_users TO luckinvasion_master;

GRANT ALL ON TABLE forge_starter_pack.admin_users TO luckinvasion_master_admin;


CREATE TABLE IF NOT EXISTS forge_starter_pack.admin_user_blowfish
(
    "user" integer NOT NULL,
    username character varying(255) COLLATE pg_catalog."default" NOT NULL,
    password character varying(255) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT admin_user_blowfish_pkey PRIMARY KEY ("user"),
    CONSTRAINT fk_admin_user_blowfish FOREIGN KEY ("user")
        REFERENCES forge_starter_pack.admin_users (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS forge_starter_pack.admin_user_blowfish
    OWNER to luckinvasion_master_admin;

REVOKE ALL ON TABLE forge_starter_pack.admin_user_blowfish FROM luckinvasion_master;

GRANT INSERT, DELETE, SELECT, UPDATE ON TABLE forge_starter_pack.admin_user_blowfish TO luckinvasion_master;

GRANT ALL ON TABLE forge_starter_pack.admin_user_blowfish TO luckinvasion_master_admin;




CREATE TABLE IF NOT EXISTS forge_starter_pack.properties
(
    name character varying(100) COLLATE pg_catalog."default" NOT NULL,
    value character varying(1000) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT "Properties_pkey" PRIMARY KEY (name)
)

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS forge_starter_pack.properties
    OWNER to luckinvasion_master_admin;

REVOKE ALL ON TABLE forge_starter_pack.properties FROM luckinvasion_master;

GRANT INSERT, DELETE, SELECT, UPDATE ON TABLE forge_starter_pack.properties TO luckinvasion_master;

GRANT ALL ON TABLE forge_starter_pack.properties TO luckinvasion_master_admin;