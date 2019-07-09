package com.att.ingestion.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.CharMatcher;
import com.att.ingestion.exceptions.MandatoryValueNotFoundException;
import com.att.ingestion.model.Resource;
import com.att.ingestion.model.checksum.CheckSumCalculator;
import com.att.ingestion.service.ResourceLoader;


public class Utils {

    private static Logger log = LoggerFactory.getLogger(Utils.class);
    private static final CharMatcher legalCharacters = CharMatcher.inRange('a', 'z').or(CharMatcher.inRange('A', 'Z')).or(CharMatcher.inRange('0', '9')); // either case

    private static final Pattern boolCheckPat = Pattern.compile("true|yes|t|y|1", Pattern.CASE_INSENSITIVE);

    public static String convertToCamelCase(String prefix, String undescoreSeparatedValue)
    {
        StringBuilder sb = new StringBuilder(prefix);
        String[] parts = undescoreSeparatedValue.split("_");
        for (String part : parts)
        {

            String cleanValue = legalCharacters.retainFrom(part).trim();
            if (cleanValue.length() > 0)
            {
                sb.append(cleanValue.substring(0, 1).toUpperCase());
                if (cleanValue.length() > 1)
                {
                    sb.append(StringUtils.substring(cleanValue, 1).toLowerCase());
                }
            }
        }
        return sb.toString();
    }

    public static boolean toBoolean(String str)
    {
        return boolCheckPat.matcher(str).matches();
    }

    public static void addIfNotFound(List<Object> list, List<Object> elements) {
    	for (Object element: elements) {
			addIfNotFound(list, element);
		}
	}

    public static void addIfNotFound(List<Object> list, Object element) {
    	boolean found = false;
    	for (Object o: list) {
    		if (o != null && element != null && o.toString().equalsIgnoreCase(element.toString())) {
				found = true;
				break;
			}
		}
		if (!found) {
			list.add(element);
		}
	}
    

    
    public static int getIngestionChecksum(Resource r, Set<String> propertyNames) throws Exception {
		StringBuilder checkSum = new StringBuilder();
		List <String> properties = new ArrayList<String>();

		Collections.sort(properties, (String s1, String s2) -> s1.compareTo(s2));
		for (String property: properties) {
			checkSum.append(property).append("|");
		}
        CheckSumCalculator calc = new CheckSumCalculator(true);
        if (log.isDebugEnabled()) {
            log.debug(">>>>>>>>>>>>>>>> jStr: everything=" + checkSum.toString());
        }
        calc.add(checkSum.toString(), "checksum");
        return calc.getChecksum();
    }
    
  
    public static void upsertResource(ResourceLoader resourceLoader, Resource newResource) throws Exception {
    	newResource.setLastCheckedTime(System.currentTimeMillis());
		log.info("Upserting resource " + newResource.getUrn());
		resourceLoader.addUpdatedResource(newResource);

    }
    
    public static Resource createOrUpdateResource(ResourceLoader resourceLoader, Resource newResource, Resource existingResource, long generationTimeMs) throws Exception {
    	
        // This field can only be set after checksum is calculated so it does not affect checksum calculation
    	newResource.setLastCheckedTime(generationTimeMs);
    	
    	Set<String> propertyNamesInOrder = new TreeSet<String>();
    	if(newResource.getProperties() != null) {
	    	for (Map.Entry<String, Object> entry : newResource.getProperties().entrySet()) {
	        	propertyNamesInOrder.add(entry.getKey());
	        }
    	}
        
        removeBlankProperties(newResource);
        removeBlankProperties(existingResource);

        //check if its and existing resource
    	if (existingResource != null) {
            if (log.isDebugEnabled()) {
	    		log.debug("----------------------------------------- Calculating checksum for NEW resource");
            }
        	int newResChecksum = Utils.getIngestionChecksum(newResource, propertyNamesInOrder);
            if (log.isDebugEnabled()) {
	    		log.debug("----------------------------------------- Calculating checksum for OLD resource");
            }
        	int oldResChecksum = Utils.getIngestionChecksum(existingResource, propertyNamesInOrder);
            if (log.isDebugEnabled()) {
	    		log.debug("Checksum results. OLD: " + oldResChecksum + " NEW: " + newResChecksum);
            }
    		Resource returnResource = existingResource;
        	if (newResChecksum == oldResChecksum) {
        		log.info(existingResource.getUrn() + " existing resource, no change, needs lastChecked update");
        		resourceLoader.updateLastChecked(existingResource.getUrn(), generationTimeMs);
        	} else {
        		log.info(existingResource.getUrn() + " existing resource, needs full update");
        		returnResource = mergeResources(newResource, existingResource);
        		resourceLoader.addUpdatedResource(returnResource);
        	}
        	return returnResource;
        } else {
        	log.info("new resource for BP " + newResource.getBpName());
        	Resource res = resourceLoader.getServer().createResource(newResource);
			if (res != null) {
				newResource.setId(res.getId());
				newResource.setUrn(res.getUrn());
			}
        }
		return newResource;
    }
    
    public static String getExistingPropertyAsString(Resource r, String propName, Map<String, String> selectors) throws MandatoryValueNotFoundException {
    	String propertyResult = null;
    	if(r.getProperties() != null) {
	    	for (Map.Entry<String, Object> entry : r.getProperties().entrySet()) {
	    		if (entry.getKey().equals(propName)) {
	    				propertyResult = propName;
	    				break;
	    		}
	    	}
    	}
    	if (propertyResult != null) {
    		StringBuffer result = new StringBuffer();
    		result.append(r.getProperties().get(propertyResult));
    		return result.toString();
    	}
    	throw new MandatoryValueNotFoundException();
    }
    
    public static boolean isPropertyEmpty(Object rp) {
    	if (rp == null) {
    		return true;
    	}
    	return false;
    }
    
    /**
     * Copy all resource properties from newResource to existingResource and return the merged existingResource
     * @param newResource
     * @param existingResource
     * @return
     */
    public static Resource mergeResources(Resource newResource, Resource existingResource) {
    	Map<String, Object> existingProperties = existingResource.getProperties();
    	Map<String,Object> existingPropertiesMap = new HashMap<String,Object>();
    	
    	if(existingProperties != null) {
	    	for (Map.Entry<String, Object> entry : existingProperties.entrySet()) {
	        	if (!isPropertyEmpty(entry.getValue())) {
	            	existingPropertiesMap.put(entry.getKey(), entry.getValue());
	        	}
	        }
    	}
    	
    	if(newResource.getProperties() != null) {
	    	for (Map.Entry<String, Object> entry : newResource.getProperties().entrySet()) {
	        	//Remove old property or properties with no value
	        	if (existingPropertiesMap.get(entry.getKey()) != null) {
	        		existingPropertiesMap.remove(entry.getKey());
	            	Set<String> toDelete = new HashSet<String>();
	            	if(existingResource.getProperties() != null) {
		            	for (Map.Entry<String, Object> entry1 : existingResource.getProperties().entrySet()) {
		                	if (entry1.getKey().equals(entry.getKey())) {
			                		toDelete.add(entry1.getKey());
		                	}
		                }
		            	existingProperties.keySet().removeAll(toDelete);
	            	}
	        	}
	        	
	        	//Add new property
	        	existingResource.getProperties().put(entry.getKey(), entry.getValue());
	        	existingPropertiesMap.put(entry.getKey(), entry.getValue());
	        }
    	}
    	
    	if(existingResource.getProperties() != null) {
	    	for (Map.Entry<String, Object> entry : existingResource.getProperties().entrySet()) {
	        	if (!isPropertyEmpty(entry.getValue())) {
	        		existingPropertiesMap.put(entry.getKey(), entry.getValue());
	        	}
	        }
    	}
        existingResource.setBpVersionNum(newResource.getBpVersionNum());
        return existingResource;
    }
    
    public static void removeBlankProperties(Resource resource) {
    	if (resource == null) {
    		return;
    	}
	}
    
    @SuppressWarnings("unchecked")
	public static void addResourceProperty(Resource r, String propName, Map<String, String> selectors, 
    		Object value, Boolean allowsNull, Boolean mandatory) throws IOException, MandatoryValueNotFoundException {
    	// Check if mandatory
    	if (Boolean.TRUE.equals(mandatory)) {
        	if (value == null || StringUtils.isBlank(value.toString())) {
        		throw new MandatoryValueNotFoundException(String.format("Value is null or empty for mandatory property %s", propName));
	    	}
    	}
    	// Check if allows null value
    	if (!Boolean.TRUE.equals(allowsNull)) {// default does not allow null
        	if (value == null || StringUtils.isBlank(value.toString())) {
	    		return;
	    	}
    	}
    	Object existingProp = getExistingProperty(r, propName, selectors);
    	if (existingProp != null) {
        	if (value == null || StringUtils.isBlank(value.toString())) {
        		return;
        	}
    		if (existingProp == value) {
    			log.warn(">>>>>>> property " + propName + " already contains the value " + value);
    			return;
    		}
        	if (value instanceof List) {
				addIfNotFound((List<Object>)existingProp, (List<Object>) value);
        	} else {
        		addIfNotFound(Collections.singletonList(existingProp), Collections.singletonList(value));
        	}
    	} else {
        	r.getProperties().put(propName, value);
    	}
    }
    
    public static Object getExistingProperty(Resource r, String propName, Map<String, String> selectors) {
    	if(r.getProperties() != null) {
	    	for (Map.Entry<String, Object> entry : r.getProperties().entrySet()) {
	    		if (entry.getKey().equals(propName)) {
	    			return entry.getValue();
	    		}
	    	}
    	}
    	return null;
    }
}
