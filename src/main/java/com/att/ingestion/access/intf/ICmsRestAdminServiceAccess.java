package com.att.ingestion.access.intf;

import java.util.List;
import java.util.Map;

import com.att.ingestion.model.Resource;

public interface ICmsRestAdminServiceAccess
{

	public Resource createResource(Resource res) throws Exception;
 	
 	public Resource deleteResource(Resource res) throws Exception;
 	
 	public List<Resource> deleteResourceBatch(List<Resource> res) throws Exception;
 	
 	public List<Resource> createResourceBatch(List<Resource> list) throws Exception;
 	
 	public List<Resource> updateResourceBatch(List<Resource> list) throws Exception;
 	
 	public void updateLastCheckedResource(Map<String, Long> checkMap) throws Exception;
 	
 	public List<Resource> getResourcesByEntityType(String entityType) throws Exception;
 	
 	public Resource getResourcesByEntityTypeAndId(String entityType, String id) throws Exception;
}