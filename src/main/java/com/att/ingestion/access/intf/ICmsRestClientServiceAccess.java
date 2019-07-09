package com.att.ingestion.access.intf;

import java.util.List;
import java.util.Map;

import com.att.ingestion.model.Resource;

public interface ICmsRestClientServiceAccess
{

	public Resource getResourceByUrn(String index, String urn) throws Exception;
	
	public Resource getResourceById(String index, String id) throws Exception;
	
	public Resource getResourcesByType(String index, String type) throws Exception;
	
 	public List<Resource> searchResources(String index, Map<String, Object> requestParams) throws Exception;
}