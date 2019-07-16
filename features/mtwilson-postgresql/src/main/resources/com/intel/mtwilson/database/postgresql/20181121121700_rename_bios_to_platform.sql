/**
 * Author:  ddhawal
 * Created: Nov 21, 2018
 * TODO: Remove following post CCB
 */
UPDATE mw_flavor SET content = (regexp_replace(content::text, '"flavor_part":"BIOS"', '"flavor_part":"PLATFORM"'))::json;
UPDATE mw_flavorgroup SET flavor_type_match_policy = (regexp_replace(flavor_type_match_policy::text, '"flavor_part":"BIOS"', '"flavor_part":"PLATFORM"'))::json;
UPDATE mw_report SET saml = regexp_replace(saml, 'TRUST_BIOS', 'TRUST_PLATFORM', 'gi') WHERE NOT saml IS NULL;
UPDATE mw_report SET trust_report = (replace(trust_report::text, '"BIOS"', '"PLATFORM"'))::json WHERE NOT trust_report IS NULL;