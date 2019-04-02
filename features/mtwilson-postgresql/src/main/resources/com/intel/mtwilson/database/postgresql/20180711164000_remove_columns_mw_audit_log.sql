/**
 * Author:  srege
 * Created: Jul 11, 2018
 */

ALTER TABLE mw_audit_log_entry DROP COLUMN transaction_id;
ALTER TABLE mw_audit_log_entry DROP COLUMN finger_print;

INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20180711164000,NOW(),'Remove transaction_id and finger_print columns');