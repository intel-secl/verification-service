/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 * Author:  hmgowda
 * Created: Jul 11, 2018
 */
ALTER TABLE mw_audit_log_entry DROP data;
ALTER TABLE mw_audit_log_entry ADD COLUMN data json;

INSERT INTO changelog (ID, APPLIED_AT, DESCRIPTION) VALUES (20180711102000,NOW(),'Updated the data attribute datatype to json');