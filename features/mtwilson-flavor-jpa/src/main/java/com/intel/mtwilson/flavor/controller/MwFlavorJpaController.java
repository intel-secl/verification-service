package com.intel.mtwilson.flavor.controller;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.core.common.model.HardwareFeature;
import com.intel.mtwilson.core.common.model.AttestationExemptFeature;
import com.intel.mtwilson.core.common.model.HardwareFeatureDetails;
import com.intel.mtwilson.core.flavor.common.FlavorPart;
import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.controller.exceptions.PreexistingEntityException;
import com.intel.mtwilson.flavor.data.MwFlavor;
import com.intel.mtwilson.core.common.model.HostManifest;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.intel.mtwilson.core.flavor.common.FlavorPart.*;
import static java.util.Optional.ofNullable;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.intel.mtwilson.flavor.utils.HostMeasurementUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author rksavino
 */
public class MwFlavorJpaController implements Serializable {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MwFlavorJpaController.class);
    
    public MwFlavorJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MwFlavor mwFlavor) throws PreexistingEntityException, Exception {
        EntityManager em = null;
        try {
            mwFlavor.setCreated(Calendar.getInstance().getTime());
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(mwFlavor);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (findMwFlavor(mwFlavor.getId()) != null) {
                throw new PreexistingEntityException("The flavor " + mwFlavor + " already exists.", ex);
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MwFlavor mwFlavor) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.merge(mwFlavor);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                String id = mwFlavor.getId();
                if (findMwFlavor(id) == null) {
                    throw new NonexistentEntityException("The flavor with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(String id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            MwFlavor mwFlavor;
            try {
                mwFlavor = em.getReference(MwFlavor.class, id);
                mwFlavor.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The flavor with id " + id + " no longer exists.", enfe);
            }
            em.remove(mwFlavor);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MwFlavor> findMwFlavorEntities() {
        return findMwFlavorEntities(true, -1, -1);
    }

    public List<MwFlavor> findMwFlavorEntities(int maxResults, int firstResult) {
        return findMwFlavorEntities(false, maxResults, firstResult);
    }

    private List<MwFlavor> findMwFlavorEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MwFlavor.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public MwFlavor findMwFlavor(String id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MwFlavor.class, id);
        } finally {
            em.close();
        }
    }

    public int getMwFlavorCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MwFlavor> rt = cq.from(MwFlavor.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

    public MwFlavor findMwFlavorByName(String name) {
        String jsonLikeLabelQueryText = String.format("label = '%s'", name);
        EntityManager em = getEntityManager();
        MwFlavor mwFlavor = null;
        try {
            Query query = em.createNativeQuery("SELECT * FROM mw_flavor WHERE " + jsonLikeLabelQueryText, MwFlavor.class);
            mwFlavor = (MwFlavor) query.getSingleResult();
        } catch (NoResultException ex){
            log.debug("No flavor found with label {}.", name);
        } finally {
            em.close();
        }
        return mwFlavor;
    }

    public List<MwFlavor> findMwFlavorByNameLike(String name) {
        String jsonLikeLabelQueryText = String.format("label LIKE '%%%s%%'", name);
        List<MwFlavor> mwFlavorList = null;
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNativeQuery("SELECT * FROM mw_flavor WHERE " + jsonLikeLabelQueryText, MwFlavor.class);
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwFlavorList = query.getResultList();
            }
            return mwFlavorList;
        } finally {
            em.close();
        }
    }
    
    public List<MwFlavor> findMwFlavorByKeyValue(String key, String value) {
        List<MwFlavor> flavorList = null;
        EntityManager em = getEntityManager();
        try {
            // For some reason either named parameters or if we specify the position of the parameter, it is not working.
            Query query = em.createNativeQuery("SELECT * FROM mw_flavor WHERE content->'meta'->'description'->> ? = ?", MwFlavor.class);
            query.setParameter(1, key);
            query.setParameter(2, value);
            
            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                flavorList = query.getResultList();
            }
            return flavorList;
        } finally {
            em.close();
        }
    }
    
    private String buildFlavorPartQueryStringWithFlavorParts(String flavorType, String flavorgroupId) {
        return String.format("%s AND f.content -> 'meta' -> 'description' ->> 'flavor_part'='%s'", buildFlavorPartQueryStringWithFlavorgroup(flavorgroupId), flavorType);
    }

    private String buildFlavorPartQueryStringWithFlavorgroup(String flavorgroupId) {
        return String.format("SELECT f.id FROM mw_flavor AS f\n"
                + "INNER JOIN mw_link_flavor_flavorgroup AS l ON f.id = l.flavor_id \n"
                + "INNER JOIN mw_flavorgroup AS fg ON l.flavorgroup_id = fg.id \n"
                + "WHERE fg.id = '%s'\n", flavorgroupId);
    }
    
    private String buildMultipleFlavorPartQueryString(String flavorgroupId,HostManifest hostManifest, HashMap<String, Boolean> flavorTypesWithLatestStatus) {        

        String jsonDescriptionQueryTemplate = "f.content -> 'meta' -> 'description' ->>";
        String jsonBiosQueryTemplate = "f.content -> 'bios' ->>";
        String jsonHardwareQueryTemplate = "(f.content -> 'hardware' -> 'feature' ->";
        String jsonBiosQueryText = null;
        String jsonOsQueryText = null;
        String jsonSoftwareQueryText = null;
        String jsonAssetTagQueryText = null;
        String jsonHostUniqueQueryText = null;        

        if(!flavorTypesWithLatestStatus.isEmpty()){
            for(String flavorType : flavorTypesWithLatestStatus.keySet()){
                switch (FlavorPart.valueOf(flavorType)) {
                    case PLATFORM:
                        jsonBiosQueryText = buildFlavorPartQueryStringWithFlavorParts(flavorType, flavorgroupId);
                        if (hostManifest != null && hostManifest.getHostInfo() != null && hostManifest.getHostInfo().getTbootInstalled() != null) {
                            jsonBiosQueryText = String.format("%s\nAND ( %s 'tboot_installed' = '%s'",
                                    jsonBiosQueryText, jsonDescriptionQueryTemplate, hostManifest.getHostInfo().getTbootInstalled());
                            if(Boolean.valueOf(hostManifest.getHostInfo().getTbootInstalled())) { // If tboot is enabled and flavor is of old format tboot_installed field wont be there
                                jsonBiosQueryText = String.format("%s\nOR ( %s 'tboot_installed' ) is null )",
                                        jsonBiosQueryText, jsonDescriptionQueryTemplate);
                            } else {
                                jsonBiosQueryText = String.format("%s )", jsonBiosQueryText);
                            }
                        }
                        if (hostManifest != null && hostManifest.getHostInfo() != null && hostManifest.getHostInfo().getBiosName() != null && !hostManifest.getHostInfo().getBiosName().isEmpty()) {
                            jsonBiosQueryText = String.format("%s\nAND ( %s 'bios_name' = '%s'",
                                    jsonBiosQueryText, jsonBiosQueryTemplate, hostManifest.getHostInfo().getBiosName());
                            // TODO: Remove following post CCB
                            jsonBiosQueryText = String.format("%s\nOR %s 'bios_name' = '%s')",
                                    jsonBiosQueryText, jsonDescriptionQueryTemplate, hostManifest.getHostInfo().getBiosName());
                        }
                        if (hostManifest != null && hostManifest.getHostInfo() != null && hostManifest.getHostInfo().getBiosVersion() != null && !hostManifest.getHostInfo().getBiosVersion().isEmpty()) {
                            jsonBiosQueryText = String.format("%s\nAND ( %s 'bios_version' = '%s'",
                                    jsonBiosQueryText, jsonBiosQueryTemplate, hostManifest.getHostInfo().getBiosVersion());
                            // TODO: Remove following post CCB
                            jsonBiosQueryText = String.format("%s\nOR %s 'bios_version' = '%s')",
                                    jsonBiosQueryText, jsonDescriptionQueryTemplate, hostManifest.getHostInfo().getBiosVersion());
                        }
                        if (hostManifest != null && hostManifest.getHostInfo() != null) {
                            for(HardwareFeature feature : HardwareFeature.values()) {
                                /*
                                Check whether the hardware feature is a type of AttestationExemptFeature,
                                if it is not then include it in flavor retrieval query
                                 */
                                if (!EnumUtils.isValidEnum(AttestationExemptFeature.class, feature.getValue())) {
                                    HardwareFeatureDetails featureDetails = getHardwareFeatureDetails(hostManifest, feature);
                                    if (featureDetails != null) {
                                        String jsonHardwareFeatureQueryTemplate = jsonHardwareQueryTemplate.concat(" '" + feature.getValue().toLowerCase() + "' ->>");
                                        jsonBiosQueryText = String.format("%s\nAND %s 'enabled' = '%s')",
                                                jsonBiosQueryText, jsonHardwareFeatureQueryTemplate, featureDetails.getEnabled());
                                        if (feature == HardwareFeature.CBNT && featureDetails.getEnabled() && !StringUtils.isEmpty(featureDetails.getMeta().get("profile"))) {
                                            jsonBiosQueryText = String.format("%s\nAND %s 'profile' = '%s')",
                                                    jsonBiosQueryText, jsonHardwareFeatureQueryTemplate, featureDetails.getMeta().get("profile"));
                                        }
                                    } else {
                                        String jsonHardwareFeatureQueryTemplate = jsonHardwareQueryTemplate.concat("> '" + feature.getValue().toLowerCase() + "' ");
                                        jsonBiosQueryText = String.format("%s\nAND %s) is null", jsonBiosQueryText, jsonHardwareFeatureQueryTemplate);
                                    }
                                }
                            }
                        }
                        if(flavorTypesWithLatestStatus.get(PLATFORM.getValue())){
                           jsonBiosQueryText = String.format("%s\nORDER BY f.created desc LIMIT 1",jsonBiosQueryText); 
                        }
                        break;
                    case OS:
                        jsonOsQueryText = buildFlavorPartQueryStringWithFlavorParts(flavorType, flavorgroupId);
                        if (hostManifest != null && hostManifest.getHostInfo() != null && hostManifest.getHostInfo().getTbootInstalled() != null) {
                            jsonOsQueryText = String.format("%s\nAND ( %s 'tboot_installed' = '%s'",
                                    jsonOsQueryText, jsonDescriptionQueryTemplate, hostManifest.getHostInfo().getTbootInstalled());
                            if(Boolean.valueOf(hostManifest.getHostInfo().getTbootInstalled())) {
                                jsonOsQueryText = String.format("%s\nOR ( %s 'tboot_installed' ) is null )",
                                        jsonOsQueryText, jsonDescriptionQueryTemplate);
                            } else {
                                jsonOsQueryText = String.format("%s )", jsonOsQueryText);
                            }
                        }
                        if (hostManifest != null && hostManifest.getHostInfo() != null && hostManifest.getHostInfo().getOsName() != null && !hostManifest.getHostInfo().getOsName().isEmpty()) {
                            jsonOsQueryText = String.format("%s\nAND %s 'os_name' = '%s'",
                                    jsonOsQueryText, jsonDescriptionQueryTemplate, hostManifest.getHostInfo().getOsName());
                        }
                        if (hostManifest != null && hostManifest.getHostInfo() != null && hostManifest.getHostInfo().getOsVersion() != null && !hostManifest.getHostInfo().getOsVersion().isEmpty()) {
                            jsonOsQueryText = String.format("%s\nAND %s 'os_version' = '%s'",
                                    jsonOsQueryText, jsonDescriptionQueryTemplate, hostManifest.getHostInfo().getOsVersion());
                        }
                        if (hostManifest != null && hostManifest.getHostInfo() != null && hostManifest.getHostInfo().getVmmName() != null && !hostManifest.getHostInfo().getVmmName().isEmpty()) {
                            jsonOsQueryText = String.format("%s\nAND %s 'vmm_name' = '%s'",
                                    jsonOsQueryText, jsonDescriptionQueryTemplate, hostManifest.getHostInfo().getVmmName());
                        }
                        if (hostManifest != null && hostManifest.getHostInfo() != null && hostManifest.getHostInfo().getVmmVersion() != null && !hostManifest.getHostInfo().getVmmVersion().isEmpty()) {
                            jsonOsQueryText = String.format("%s\nAND %s 'vmm_version' = '%s'",
                                    jsonOsQueryText, jsonDescriptionQueryTemplate, hostManifest.getHostInfo().getVmmVersion());
                        }
                        if(flavorTypesWithLatestStatus.get(OS.getValue())){
                           jsonOsQueryText = String.format("%s\nORDER BY f.created desc LIMIT 1", jsonOsQueryText); 
                        }
                        break;
                    case SOFTWARE:
                        jsonSoftwareQueryText = buildFlavorPartQueryStringWithFlavorParts(flavorType, flavorgroupId);
                        if (hostManifest != null && hostManifest.getPcrManifest() != null && hostManifest.getPcrManifest().getMeasurementXmls() != null && !HostMeasurementUtils.getMeasurementLabels(hostManifest).isEmpty()) {
                            jsonSoftwareQueryText = String.format("%s\nAND f.label in ('%s')",
                                    jsonSoftwareQueryText, StringUtils.join(HostMeasurementUtils.getMeasurementLabels(hostManifest), "','"));
                        }
                        break;
                    case ASSET_TAG:
                        jsonAssetTagQueryText = String.format("SELECT f.id FROM mw_flavor AS f\nWHERE %s ",buildFlavorPartQueryString(FlavorPart.ASSET_TAG.getValue()));
                        if (hostManifest != null && hostManifest.getHostInfo() != null && hostManifest.getHostInfo().getHardwareUuid() != null && !hostManifest.getHostInfo().getHardwareUuid().isEmpty()) {
                            jsonAssetTagQueryText = String.format("%s\nAND LOWER(%s 'hardware_uuid') = '%s'",
                                    jsonAssetTagQueryText, jsonDescriptionQueryTemplate, hostManifest.getHostInfo().getHardwareUuid().toLowerCase());
                        }
                        if(flavorTypesWithLatestStatus.get(FlavorPart.ASSET_TAG.getValue())){
                           jsonAssetTagQueryText = String.format("%s\nORDER BY f.created desc LIMIT 1", jsonAssetTagQueryText); 
                        }
                        break;
                    case HOST_UNIQUE:
                        jsonHostUniqueQueryText = String.format("SELECT f.id FROM mw_flavor AS f\nWHERE %s ",buildFlavorPartQueryString(FlavorPart.HOST_UNIQUE.getValue()));
                        if (hostManifest != null && hostManifest.getHostInfo() != null && hostManifest.getHostInfo().getTbootInstalled() != null) {
                            jsonHostUniqueQueryText = String.format("%s\nAND ( %s 'tboot_installed' = '%s'",
                                    jsonHostUniqueQueryText, jsonDescriptionQueryTemplate, hostManifest.getHostInfo().getTbootInstalled());
                            if(Boolean.valueOf(hostManifest.getHostInfo().getTbootInstalled())) {
                                jsonHostUniqueQueryText = String.format("%s\nOR ( %s 'tboot_installed' ) is null )",
                                        jsonHostUniqueQueryText, jsonDescriptionQueryTemplate);
                            } else {
                                jsonHostUniqueQueryText = String.format("%s )", jsonHostUniqueQueryText);
                            }
                        }
                        if (hostManifest != null && hostManifest.getHostInfo() != null && hostManifest.getHostInfo().getHardwareUuid() != null && !hostManifest.getHostInfo().getHardwareUuid().isEmpty()) {
                            jsonHostUniqueQueryText = String.format("%s\nAND LOWER(%s 'hardware_uuid') = '%s'",
                                    jsonHostUniqueQueryText, jsonDescriptionQueryTemplate, hostManifest.getHostInfo().getHardwareUuid().toLowerCase());
                        }
                        if(flavorTypesWithLatestStatus.get(FlavorPart.HOST_UNIQUE.getValue())){
                           jsonHostUniqueQueryText = String.format("%s\nORDER BY f.created desc LIMIT 1", jsonHostUniqueQueryText); 
                        }
                        break;
                    default:
                        throw new IllegalArgumentException(String.format("Invalid partial flavor type: %s", flavorType));
                }
            }
        }
        
        String jsonFlavorTypeQueryText = null;
        String jsonQueryText = null;
        
        // add automatic flavor group types to query string
        if (jsonBiosQueryText != null && !jsonBiosQueryText.isEmpty()) {
            jsonFlavorTypeQueryText = String.format("%s f.id IN (%s)\nOR ", ofNullable(jsonFlavorTypeQueryText).orElse(""), jsonBiosQueryText);
        }
        if (jsonOsQueryText != null && !jsonOsQueryText.isEmpty()) {
            jsonFlavorTypeQueryText = String.format("%s f.id IN (%s)\nOR ", ofNullable(jsonFlavorTypeQueryText).orElse(""), jsonOsQueryText);
        }
        if (jsonSoftwareQueryText != null && !jsonSoftwareQueryText.isEmpty()) {
            jsonFlavorTypeQueryText = String.format("%s f.id IN (%s)\nOR ", ofNullable(jsonFlavorTypeQueryText).orElse(""), jsonSoftwareQueryText);
        }
        if (jsonAssetTagQueryText != null && !jsonAssetTagQueryText.isEmpty()) {
            jsonFlavorTypeQueryText = String.format("%s f.id IN (%s)\nOR", ofNullable(jsonFlavorTypeQueryText).orElse(""), jsonAssetTagQueryText);
        }
        if (jsonHostUniqueQueryText != null && !jsonHostUniqueQueryText.isEmpty()) {
            jsonFlavorTypeQueryText = String.format("%s f.id IN (%s)\nOR", ofNullable(jsonFlavorTypeQueryText).orElse(""), jsonHostUniqueQueryText);
        }
        
        // strip off trailing spaces and "OR", and 
        jsonFlavorTypeQueryText = StringUtils.stripEnd(StringUtils.stripEnd(jsonFlavorTypeQueryText, " "), "OR");
        
        if (jsonFlavorTypeQueryText != null && !jsonFlavorTypeQueryText.isEmpty()) {
            jsonQueryText = String.format(" %s ", jsonFlavorTypeQueryText);
        } else if (flavorgroupId != null && !flavorgroupId.isEmpty()) {
            jsonQueryText = String.format("f.id IN (%s)", buildFlavorPartQueryStringWithFlavorgroup(flavorgroupId));
        }
        // add WHERE statement
        if (jsonQueryText != null && !jsonQueryText.isEmpty()) {
            jsonQueryText = String.format("WHERE (%s)", jsonQueryText);
        }
        
        return jsonQueryText;
    }

    public List<MwFlavor> findMwFlavorEntities(UUID flavorgroupId, HostManifest hostManifest, HashMap<String, Boolean> flavorTypeswithLatestStatus) {
        List<MwFlavor> mwFlavorList = null;
        EntityManager em = getEntityManager();
        String flavorgroupIdString = null;
        if (flavorgroupId != null && flavorgroupId.toString() != null && !flavorgroupId.toString().isEmpty()) {
            flavorgroupIdString = flavorgroupId.toString();
        }
        try {
            String queryString = String.format("SELECT f.id, f.content FROM mw_flavor AS f %s",
                    ofNullable(buildMultipleFlavorPartQueryString(flavorgroupIdString, hostManifest, 
                            flavorTypeswithLatestStatus)).orElse(""));
            log.debug("The Flavor search query string is:\n{}", queryString);
            Query query = em.createNativeQuery(queryString, MwFlavor.class);

            if (query.getResultList() != null && !query.getResultList().isEmpty()) {
                mwFlavorList = query.getResultList();
            }
            return mwFlavorList;
        } finally {
            em.close();
        }
    }
    
    private String buildFlavorPartQueryString(String flavorType) {
        return String.format("f.content -> 'meta' -> 'description' ->> 'flavor_part' = '%s'", flavorType);
    }
    
    public boolean hostHasUniqueFlavor(String hardwareUuid, String flavorType) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNativeQuery("SELECT COUNT(*) " +
                    "FROM mw_flavor as f " +
                    "INNER JOIN mw_link_flavor_flavorgroup as l ON f.id = l.flavor_id " +
                    "INNER JOIN mw_flavorgroup as fg ON l.flavorgroup_id = fg.id " +
                    "WHERE fg.name = 'host_unique' " +
                    "AND (" + buildFlavorPartQueryString(flavorType) + " " +
                    "AND LOWER(f.content -> 'meta' -> 'description' ->> 'hardware_uuid') = ?)");
            query.setParameter(1, hardwareUuid.toLowerCase());
            Long uniqueFlavorsForHostCount = (Long) query.getResultList().get(0);
            if (uniqueFlavorsForHostCount > 0) {
                return true;
            }
            return false;
        } finally {
            em.close();
        }
    }
    
    public boolean flavorgroupContainsFlavorType(UUID flavorgroupId, String flavorType) {
        EntityManager em = getEntityManager();
        try {
            Query query = em.createNativeQuery("SELECT COUNT(*) " +
                    "FROM mw_flavor as f " +
                    "INNER JOIN mw_link_flavor_flavorgroup as l ON f.id = l.flavor_id " +
                    "INNER JOIN mw_flavorgroup as fg ON l.flavorgroup_id = fg.id, json_array_elements(fg.flavor_type_match_policy ->'flavor_match_policies') policies " +
                    "WHERE fg.id = ? AND policies ->> 'flavor_part' = ? AND " + buildFlavorPartQueryString(flavorType));
            query.setParameter(1, flavorgroupId.toString());
            query.setParameter(2, flavorType);
            Long flavorCount = (Long) query.getResultList().get(0);
            if (flavorCount > 0) {
                return true;
            }
            return false;
        } finally {
            em.close();
        }
    }
    
    private HardwareFeatureDetails getHardwareFeatureDetails(HostManifest hostManifest, HardwareFeature feature) {
        HardwareFeatureDetails featureDetails = null;
        if(hostManifest.getHostInfo().getHardwareFeatures() != null && hostManifest.getHostInfo().getHardwareFeatures().size() != 0) {
            featureDetails = hostManifest.getHostInfo().getHardwareFeatures().get(feature);
        } else if(feature.equals(HardwareFeature.TPM)) {
            featureDetails = new HardwareFeatureDetails();
            featureDetails.setEnabled(Boolean.valueOf(hostManifest.getHostInfo().getTpmEnabled()));
        } else if(feature.equals(HardwareFeature.TXT)) {
            featureDetails = new HardwareFeatureDetails();
            featureDetails.setEnabled(Boolean.valueOf(hostManifest.getHostInfo().getTxtEnabled()));
        }
        return featureDetails;
    }
}
