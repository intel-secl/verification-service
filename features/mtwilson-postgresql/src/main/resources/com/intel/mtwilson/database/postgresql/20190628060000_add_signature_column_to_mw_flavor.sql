/**
 * Author:  ddhawal
 * Created: Oct 26, 2018
 */

ALTER TABLE mw_flavor ADD COLUMN signature text UNIQUE;
CREATE INDEX idx_mw_flavor_signature ON mw_flavor (signature);