/**
 * Author:
 * Created: Apr 09, 2019
 */
ALTER TABLE mw_user_login_certificate ADD COLUMN sha384_hash bytea NOT NULL;

INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20190409152200,NOW(),'Added a column for sha384 digest algorithm in mw_user_login_certificate table');
