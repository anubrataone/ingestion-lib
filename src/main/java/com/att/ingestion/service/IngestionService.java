package com.att.ingestion.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.att.ingestion.access.CmsRestAdminServiceAccess;
import com.att.ingestion.access.CmsRestClientServiceAccess;
import com.att.ingestion.config.CmsConfiguration;
import com.att.ingestion.model.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;

public class IngestionService {

	private static final Logger log = LoggerFactory.getLogger(IngestionService.class);
	
    CmsRestAdminServiceAccess cmsAdminAccess;
    CmsRestClientServiceAccess cmsClientAccess;
    
	CmsConfiguration config;
	ObjectMapper mapper = new ObjectMapper();

    public IngestionService(CmsConfiguration config) throws Exception {

    	RestTemplate restTemplateAdmin = new RestTemplate();
        RestTemplate restTemplateClient = new RestTemplate();
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
		factory.setReadTimeout(config.getTimeout());

		this.config = config;
		cmsAdminAccess = new CmsRestAdminServiceAccess(restTemplateAdmin, config);
		cmsClientAccess = new CmsRestClientServiceAccess(restTemplateClient, config);
   }

    /**
     * Creates a single resource and returns the URN associated to it
     * @param r
     * @return
     * @throws Exception
     */
    public Resource createResource(Resource r) throws Exception {
		Resource entity = cmsAdminAccess.createResource(r);
    	return entity;
    }

	public void createIngestionResourceBatch(List<Resource> list) throws Exception {
    	cmsAdminAccess.updateResourceBatch(list);
    }    

    public void updateIngestionResourceBatch(List<Resource> list) throws Exception {
    	List<Resource> inList = new ArrayList<Resource>();
    	Map<String, Resource> resources = new HashMap<String, Resource>();
		for (Resource r : list) {
    		// Prevent duplication //TODO
			if (resources.get(r.getId()) == null) {
	    		inList.add(r);
				log.debug("[INGESTION-LIB] Add to queue list " + r.getId());
	    		resources.put(r.getId(), r);
			} else {
				if (mapper.writeValueAsString(r.getProperties()).equals( mapper.writeValueAsString(resources.get(r.getId()).getProperties()))) {
					// Duplicate. Ignore
					log.debug("[INGESTION-LIB] Preventing dup for resource " + r.getId());
					log.debug(mapper.writeValueAsString(r.getProperties()) + " [EQUALS] " + mapper.writeValueAsString(resources.get(r.getId()).getProperties()));
				} else {
					log.debug(mapper.writeValueAsString(r.getProperties()) + " [NOT EQUALS] " + mapper.writeValueAsString(resources.get(r.getId()).getProperties()));
		    		inList.add(r);
				}
			}
    	}
    	log.debug("[CMS] [BATCH UPDATE TO CMS] " + mapper.writeValueAsString(inList));
    	cmsAdminAccess.updateResourceBatch(inList);
    }

	public Resource getResourceForEntityType(String index, String entityType) throws Exception {
    	log.debug("[CMS] [CMS getResourceForEntityType] " + entityType);
		return cmsClientAccess.getResourcesByType(index, entityType);
	}
	
	public Resource getResourceForURN(String index, String urn) throws Exception {
    	log.debug("[CMS] [CMS getResourceForURN] " + urn);
		return cmsClientAccess.getResourceByUrn(index, urn);
	}
    
	public Resource getResourceForId(String index, String id) throws Exception {
    	log.debug("[CMS] [CMS getResourceForId] " + id);
		return cmsClientAccess.getResourceById(index, id);
	}
	
	public List<Resource> advancedQueryForResource(String index, Map<String, Object> requestParams) throws Exception {
    	log.debug("[CMS] [CMS searchResources] " + requestParams.toString());
		return cmsClientAccess.searchResources(index, requestParams);
	}
	
    public void updateLastCheckedResource(Map<String, Long> map) throws Exception {
		cmsAdminAccess.updateLastCheckedResource(map);
	}
}
