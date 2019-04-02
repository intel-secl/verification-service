/**
 * Author:  nkgadepa
 * Created: Aug 02, 2018
 */


-- Function which creates the primary partition if it does not exist.
CREATE OR REPLACE FUNCTION create_partition_and_insert() 
  RETURNS trigger AS
  '
    BEGIN
      IF NOT EXISTS(SELECT relname FROM pg_class WHERE relname=''mw_audit_log_entry_0'') THEN
        EXECUTE ''CREATE TABLE mw_audit_log_entry_0 (check(0=0)) INHERITS ('' || TG_RELNAME || '');'';
      END IF; 
      INSERT INTO mw_audit_log_entry_0 Values (NEW.*); 
      RETURN NULL; 
    END; 
  '
LANGUAGE plpgsql VOLATILE
COST 100;

-- Register a trigger to intercept the inserts on the main mw_audit_log_entry table
CREATE TRIGGER insert_partition_trigger
BEFORE INSERT ON mw_audit_log_entry
FOR EACH ROW EXECUTE PROCEDURE create_partition_and_insert();

CREATE OR REPLACE FUNCTION public.rotate_audit_log_partitions(
    max_row_count integer,
    num_rotations integer)

    RETURNS integer AS 

    'DECLARE count_in_primary_partition integer;
    counter INTEGER := num_rotations;

    BEGIN

       -- Acquire a lock to make sure that there are no concurrent rotation calls
       LOCK TABLE mw_audit_log_entry IN ACCESS EXCLUSIVE MODE;

       IF NOT EXISTS(SELECT relname FROM pg_class WHERE relname=''mw_audit_log_entry_0'') THEN
          EXECUTE ''CREATE TABLE mw_audit_log_entry_0 (check(0=0)) INHERITS (mw_audit_log_entry);'';
       END IF;

       -- Find the number of rows in the primary partition to kick off the rotation
       SELECT INTO count_in_primary_partition n_live_tup FROM pg_stat_all_tables WHERE relname = ''mw_audit_log_entry_0'';
       RAISE NOTICE ''Number of rows in the primary partition: %'', count_in_primary_partition;

       IF count_in_primary_partition >= max_row_count THEN
          -- Rotate audit log table
          RAISE NOTICE ''Log count threshold hit'';
          RAISE NOTICE ''Number of max rotations: %'', num_rotations-1;
          
          DECLARE
              table_name_row RECORD;
              table_name varchar(25);
              table_num INT;
          BEGIN
              -- looping through each partition table
              FOR table_name_row IN
                  -- select audit log partitions
                  SELECT tablename FROM pg_catalog.pg_tables WHERE tablename LIKE ''mw_audit_log_entry_%'' ORDER BY tablename DESC
              LOOP
                  table_name := table_name_row.tablename;
                  table_num := RIGHT(table_name, 1);

                  -- if partition number greater than number rotations, drop the table
                  IF table_num >= num_rotations-1 THEN
                    EXECUTE ''DROP TABLE '' || table_name;
              
                   -- rotate the table
                  ELSE
                      BEGIN
                          RAISE NOTICE ''Table Name: %'', table_name;
         
                          EXECUTE ''ALTER TABLE mw_audit_log_entry_'' || table_num || '' RENAME TO mw_audit_log_entry_'' || table_num+1;
                      EXCEPTION WHEN OTHERS THEN
                          RAISE NOTICE ''% %'', SQLERRM, SQLSTATE;
                          CONTINUE;
                      END;
                  END IF;
              END LOOP;
          END;
          
          IF NOT EXISTS(SELECT relname FROM pg_class WHERE relname=''mw_audit_log_entry_0'') THEN
            EXECUTE ''CREATE TABLE mw_audit_log_entry_0 (check(0=0)) INHERITS (mw_audit_log_entry);'';
          END IF; 
       END IF;
       RETURN 0;

    END;'
LANGUAGE 'plpgsql' VOLATILE 
COST 100;
