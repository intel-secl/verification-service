/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  purvades
 * Created: Feb 20, 2018
 */

-- first thing we create is the changelog
CREATE TABLE changelog (
  ID decimal(20,0) NOT NULL,
  APPLIED_AT timestamp NOT NULL,
  DESCRIPTION varchar(255) NOT NULL,
  PRIMARY KEY (ID)
);

CREATE SEQUENCE audit_log_entry_serial;
CREATE TABLE mw_audit_log_entry (
  id CHAR(36) DEFAULT nextval('audit_log_entry_serial'),
  transaction_id varchar(50) NULL,
  entity_id varchar(150) NULL,
  entity_type varchar(150) NULL,
  finger_print varchar(200) NULL,
  created timestamp NOT NULL,
  action varchar(50) NULL,
  data text NULL,
  PRIMARY KEY (id)
);

CREATE TABLE mw_rpc (
  ID char(36) DEFAULT NULL,
  Name character varying(200) DEFAULT NULL,
  Input bytea DEFAULT NULL,
  Output bytea DEFAULT NULL,
  Status character varying(200) DEFAULT NULL,
  ProgressCurrent integer DEFAULT NULL,
  ProgressMax integer DEFAULT NULL,
  PRIMARY KEY (ID)
); 

-- Since uuid is a contrib module, it is not loaded by default. So, we need to load it first.
CREATE EXTENSION "uuid-ossp";

-- example insert: insert into mw_role  (id,role_name,description) values ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11','test_role','just for testing');
CREATE TABLE mw_role (
  id CHAR(36) NOT NULL,
  role_name character varying(200) NOT NULL,
  description text DEFAULT NULL,
  PRIMARY KEY (id)
); 

CREATE INDEX idx_role_name on mw_role (role_name ASC);

-- describes which permissions are granted to users having a role
-- this is a many-to-many table; there is no primary key but each record must be unique (see constraint)
CREATE TABLE mw_role_permission (
  role_id CHAR(36) NOT NULL,
  permit_domain character varying(200) DEFAULT NULL,
  permit_action character varying(200) DEFAULT NULL,
  permit_selection character varying(200) DEFAULT NULL,
  CONSTRAINT mw_role_permission_ukey UNIQUE (role_id,permit_domain,permit_action,permit_selection)
); 

CREATE INDEX idx_role_id_domain on mw_role_permission (role_id ASC, permit_domain ASC) ;
CREATE INDEX idx_role_id_domain_permit_action on mw_role_permission (role_id ASC, permit_domain ASC, permit_action ASC) ;
CREATE INDEX idx_role_id_permit_action on mw_role_permission (role_id ASC, permit_action ASC) ;
CREATE INDEX idx_role_id_domain_permit_action_selection on mw_role_permission (role_id ASC, permit_domain ASC, permit_action ASC, permit_selection ASC) ;

-- replaces mw_portal_user 
CREATE TABLE mw_user (
  id CHAR(36) NOT NULL,
  username character varying(255) NOT NULL,
  locale character varying(8) NULL,
  comment text DEFAULT NULL,
  PRIMARY KEY (id)
); 

CREATE INDEX idx_user_name on mw_user (username ASC);

-- expires may be replaced with notAfter and notBefore
CREATE TABLE mw_user_login_password (
  id CHAR(36) DEFAULT NULL,
  user_id CHAR(36) DEFAULT NULL,
  password_hash bytea NOT NULL,
  salt bytea NOT NULL,
  iterations integer DEFAULT 1,
  algorithm character varying(128) NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
  status varchar(128) NOT NULL DEFAULT 'PENDING',
  comment text,
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_login_password_role (
  login_password_id CHAR(36) NOT NULL,
  role_id CHAR(36) NOT NULL
); 

-- expires may be replaced with notAfter and notBefore
CREATE TABLE mw_user_login_certificate (
  id CHAR(36) DEFAULT NULL,
  user_id CHAR(36) DEFAULT NULL,
  certificate bytea NOT NULL,
  sha1_hash bytea NOT NULL,
  sha256_hash bytea NOT NULL,
  expires timestamp DEFAULT NULL,
  enabled boolean NOT NULL DEFAULT '0',
  status varchar(128) NOT NULL DEFAULT 'PENDING',
  comment text,
  PRIMARY KEY (id)
); 

CREATE TABLE mw_user_login_certificate_role (
  login_certificate_id CHAR(36) NOT NULL,
  role_id CHAR(36) NOT NULL
);

CREATE  TABLE mw_host_tpm_password (
  id CHAR(36) NOT NULL,
  password TEXT NOT NULL ,
  modifiedOn timestamp without time zone NOT NULL ,
  PRIMARY KEY (id) 
);

CREATE  TABLE mw_tag_certificate (
  id uuid,
  hardware_uuid uuid,
  certificate BYTEA NOT NULL ,
  subject VARCHAR(255) NOT NULL ,
  issuer VARCHAR(255) NOT NULL ,
  notBefore timestamp without time zone NOT NULL ,
  notAfter timestamp without time zone NOT NULL ,
  CONSTRAINT mw_tag_certificate_pkey PRIMARY KEY (id)
);

CREATE  TABLE mw_tag_certificate_request (
  id CHAR(36) NOT NULL,
  subject VARCHAR(255) NOT NULL ,
  status VARCHAR(255) NOT NULL ,
  content bytea NOT NULL,
  contentType VARCHAR(255) NOT NULL,
  PRIMARY KEY (id) 
);

CREATE  TABLE mw_file (
  id CHAR(36) NOT NULL ,
  name VARCHAR(255) NULL ,
  contentType VARCHAR(255) NULL ,
  content BYTEA NULL ,
  PRIMARY KEY (id) 
);

-- fix sql problem from 20130427142800 that created only a sequence and no table
CREATE TABLE mw_request_log (
  instance varchar(255) NOT NULL,
  received timestamp NOT NULL,
  source varchar(255) NOT NULL,
  content TEXT NOT NULL,
  digest varchar(128) NOT NULL
);

CREATE UNIQUE INDEX mw_request_log_unique_constraint ON mw_request_log(LOWER(digest));

CREATE TABLE mw_tls_policy (
  id char(36) NOT NULL,
  name varchar(255) NOT NULL,
  private boolean NOT NULL,
  content_type varchar(255) NOT NULL,
  content bytea NOT NULL,
  comment text NULL,
  PRIMARY KEY (id)
);

CREATE TABLE mw_tpm_endorsement (
  id char(36) NOT NULL,
  hardware_uuid char(36) NOT NULL,
  issuer varchar(255) NOT NULL,
  revoked boolean NOT NULL DEFAULT false,
  certificate bytea NOT NULL,
  comment text NULL,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX mw_tpm_endorsement_hardware_uuid_unique_constraint ON mw_tpm_endorsement(LOWER(hardware_uuid));

CREATE TABLE mw_esxi_cluster (
  id char(36) NOT NULL,
  connection_string varchar(255) NOT NULL,
  cluster_name varchar(255) NOT NULL,
  tls_policy_id char(36) NULL,
  PRIMARY KEY (id)
);

CREATE TABLE mw_link_esxi_cluster_host (
  ID char(36) NOT NULL,
  Cluster_ID char(36) NOT NULL,
  Hostname varchar(255) NOT NULL,
  PRIMARY KEY (ID),
  CONSTRAINT Cluster_ID FOREIGN KEY (Cluster_ID) REFERENCES mw_esxi_cluster (ID) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION 
);

CREATE TABLE mw_flavor (
  id char(36) NOT NULL,
  content json NOT NULL,
  created timestamp NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX idx_flavor_label ON mw_flavor ((content -> 'meta' -> 'description' ->> 'label'));
CREATE INDEX idx_flavor_bios_name ON mw_flavor ((content -> 'meta' -> 'description' ->> 'bios_name'));
CREATE INDEX idx_flavor_bios_version ON mw_flavor ((content -> 'meta' -> 'description' ->> 'bios_version'));
CREATE INDEX idx_flavor_os_name ON mw_flavor ((content -> 'meta' -> 'description' ->> 'os_name'));
CREATE INDEX idx_flavor_os_version ON mw_flavor ((content -> 'meta' -> 'description' ->> 'os_version'));
CREATE INDEX idx_flavor_vmm_name ON mw_flavor ((content -> 'meta' -> 'description' ->> 'vmm_name'));
CREATE INDEX idx_flavor_vmm_version ON mw_flavor ((content -> 'meta' -> 'description' ->> 'vmm_version'));
CREATE INDEX idx_flavor_hardware_uuid ON mw_flavor ((content -> 'meta' -> 'description' ->> 'hardware_uuid'));

CREATE TABLE mw_flavorgroup (
  id char(36) NOT NULL,
  name varchar(255) NOT NULL,
  flavor_type_match_policy json NULL,
  PRIMARY KEY (id)
);

CREATE INDEX idx_flavorgroup_name ON mw_flavorgroup (name ASC);

CREATE TABLE mw_link_flavor_flavorgroup (
  id char(36) NOT NULL,
  flavor_id char(36) NOT NULL,
  flavorgroup_id char(36) NOT NULL,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_flavor_flavorgroup ON mw_link_flavor_flavorgroup (flavor_id ASC, flavorgroup_id ASC);

CREATE TABLE mw_host (
  id char(36) NOT NULL,
  name varchar(255) NOT NULL,
  description text NULL,
  connection_string text NOT NULL,
  hardware_uuid char(36) NULL,
  tls_policy_id char(36) NULL,
  PRIMARY KEY (id)
);

CREATE INDEX idx_host_hostname ON mw_host (name ASC);
CREATE INDEX idx_host_hardware_uuid ON mw_host (LOWER(hardware_uuid));

CREATE TABLE mw_host_status (
  id char(36) NOT NULL,
  host_id char(36) NOT NULL,
  status json NULL,
  created timestamp NOT NULL,
  host_report json NULL,
  PRIMARY KEY (id)
);

CREATE INDEX idx_host_status_host_id ON mw_host_status (host_id ASC);
CREATE INDEX idx_host_status_status ON mw_host_status ((status->>'host_state'));

CREATE TABLE mw_link_flavor_host (
  id char(36) NOT NULL,
  flavor_id char(36) NOT NULL,
  host_id char(36) NOT NULL,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_flavor_host ON mw_link_flavor_host (flavor_id ASC, host_id ASC);

CREATE TABLE mw_link_flavorgroup_host (
  id char(36) NOT NULL,
  flavorgroup_id char(36) NOT NULL,
  host_id char(36) NOT NULL,
  PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_flavorgroup_host ON mw_link_flavorgroup_host (flavorgroup_id ASC, host_id ASC);

CREATE TABLE mw_report (
  id char(36) NOT NULL,
  host_id char(36) NOT NULL,
  trust_report json NOT NULL,
  created timestamp NOT NULL,
  expiration timestamp NOT NULL,
  saml text NOT NULL,
  PRIMARY KEY (id)
);

CREATE INDEX idx_report_host_id ON mw_report (host_id ASC);

CREATE TABLE mw_queue (
  id char(36) NOT NULL,
  queue_action text NOT NULL,
  action_parameters json NULL,
  created timestamp NOT NULL,
  updated timestamp NOT NULL,
  status varchar(128) NOT NULL DEFAULT 'NEW',
  message text NULL,
  PRIMARY KEY (id)
);

CREATE TABLE mw_telemetry (
  id char(36) NOT NULL,
  create_date TIMESTAMP DEFAULT current_timestamp,
  host_num integer NOT NULL,
  PRIMARY KEY (id)
);

CREATE  TABLE mw_host_credential (
  id CHAR(36) NOT NULL,
  host_id VARCHAR(255) ,
  hardware_uuid VARCHAR(255),
  host_name VARCHAR(255),
  credential TEXT DEFAULT NULL ,
  created_ts timestamp NULL DEFAULT now(),
  PRIMARY KEY (id)
);

CREATE INDEX idx_host_credential_host_id ON mw_host_credential (host_id ASC);
CREATE INDEX idx_host_credential_hardware_uuid ON mw_host_credential (hardware_uuid ASC);
CREATE INDEX idx_host_credential_hostname ON mw_host_credential (host_name ASC);

INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20180220103000,NOW(),'initial database tables created');