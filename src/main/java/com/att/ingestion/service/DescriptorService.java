package com.att.ingestion.service;


import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.ingestion.descriptor.Blueprint;
import com.att.ingestion.descriptor.Blueprints;
import com.att.ingestion.descriptor.Entry;
import com.att.ingestion.descriptor.Field;
import com.att.ingestion.descriptor.Foreach;
import com.att.ingestion.descriptor.Foreachkey;
import com.att.ingestion.descriptor.Group;
import com.att.ingestion.descriptor.Include;
import com.att.ingestion.descriptor.Var;
import com.att.ingestion.exceptions.MandatoryValueNotFoundException;
import com.att.ingestion.model.Resource;
import com.att.ingestion.utils.ParserUtil;
import com.att.ingestion.utils.StringUtils;
import com.att.ingestion.utils.Utils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

public class DescriptorService {

	private static final Logger logger = LoggerFactory.getLogger(DescriptorService.class);
	
	static Gson gson = new Gson();
	InputStream descriptorFile;
	ResourceLoader resourceLoader;
	Map<String,List<Entry>> internalMaps;
	Map<String,Group> internalGroups;
	Blueprints bps;
	
	Map<String,Map<String,Resource>> existingResources = new ConcurrentHashMap<String,Map<String,Resource>>(1000);

    List<Resource> preloadedResources = new ArrayList<>();
    Resource preloadedResource;


	public DescriptorService(ResourceLoader resourceLoader, InputStream descriptorFile) {
		this.descriptorFile = descriptorFile;
		this.resourceLoader = resourceLoader;
	}

	public void setExistingResources(Map<String,Map<String,Resource>> existingResources) {
		this.existingResources = existingResources;
	}

	public Map<String,Map<String,Resource>> getExistingResources() {
		return this.existingResources;
	}

	protected void init(){
		if (bps == null) {// Do not load the BPs again (stream will fail)
			try {
				bps = getBlueprints(descriptorFile);
				processTagsMap(bps);
				processTagsGroup(bps);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			
		}
	}

	public InputStream getDescriptorFile() {
		return descriptorFile;
	}

	public void setDescriptorFile(InputStream descriptorFile) {
		this.descriptorFile = descriptorFile;
	}

	public static Object getJSONpathValue(String json, String jsonPath, Map<String,String> variables) {
		if (json == null || jsonPath == null) {
			return "";
		}
		if (jsonPath.startsWith("${")) {//Variable, not jsonpath
			return replaceVariablesWithContent(jsonPath, variables);
		}
		String jsonPathUpdated = replaceVariablesWithContent(jsonPath, variables);
		log(logger,"get value from jsonpath " + jsonPathUpdated);
		
		// Support for multiple jsonpaths separated by comma
		if (jsonPathUpdated.indexOf(",") > 0) {
			StringBuffer result = new StringBuffer();
			try {
				List<String> jsonPaths = ParserUtil.splitByComma(jsonPathUpdated);
				for (String jsonPathItem: jsonPaths) {
					Object o = JsonPath.read(json, jsonPathItem);
					if (o != null) {
						if (result.length() != 0) {
							result.append(",");
						}
						result.append(o.toString());
					}
				}
			} catch (Exception e) {
				log(logger,"Error processing jsonPath " + jsonPath);
			}
			log(logger,String.format("jsonpath=%s returned value=%s", jsonPath, result.toString()));
	    	return result.toString();
		} else {
			Object result;
			try {
				result = JsonPath.read(json, jsonPathUpdated);
			} catch (Exception e) {
				log(logger,String.format("Could not read jsonpath %s. Error: %s", jsonPathUpdated, e.getMessage()));
				result = "";
			}
			log(logger,String.format("jsonpath=%s returned value=%s", jsonPath, result));
	    	return result;
		}
	}
	
	public static List<String> toList(JSONArray array) {
		List<String> returnList = new ArrayList<String>(); 
		for (Object o: array) {
			if (o instanceof String) {
				returnList.add((String)o);
			} else {
				returnList.add(gson.toJson(o));
			}
		}
		return returnList;
	}
	
	/**
	 * Replace variables with its content based on variables map passed with params. 
	 * Example: Map contains "var1"="Value 1".
	 * "something${var1}something" will be replaced with "somethingValue 1something"
	 * @param string Jsonpath String to replace the variables content.
	 * @param variables map of variables: varName as key. Var value as result. Used to replace the variables content
	 * @return
	 */
	public static String replaceVariablesWithContent(String string, Map<String,String> variables) {
		if (string == null) {
			return null;
		}
		// Support for variables: something${variable}something else
		int previousIndex = 0;
		while (true) {
			int currentIndex = string.indexOf("$", previousIndex);
			if (currentIndex == -1) {
				break;
			}
			if (currentIndex < string.length() - 2) { // Not at the end of the line
				if (string.charAt(currentIndex + 1) == '{') { // Has "{" after $
					int endBracketIndex = string.indexOf("}", currentIndex + 2);
					if (endBracketIndex != -1) { // Has ending "}" after
						String varName = string.substring(currentIndex + 2, endBracketIndex).trim();
						String varValue = "";
						if (variables.containsKey(varName)) {
							varValue = variables.get(varName);
						}
						string = string.substring(0, currentIndex) + varValue + string.substring(endBracketIndex + 1);
						previousIndex = currentIndex + varValue.length();// set index to continue after it

						continue;
					}
				}
			}
			previousIndex = currentIndex + 1;
		}
		return string;
	}
	
	public Map<String,Resource> exec(String contentOfferJSON) throws Exception {
		return execAndCollectResources(contentOfferJSON, true, null);
	}
	

	/**
	 * Executes the descriptor file and adds/updates resources to CMS accordingly
	 * @param contentOfferJSON
	 * @return Map with key=BP name value=urn added/updated
	 * @throws Exception 
	 */
	public Map<String,Resource> execAndCollectResources(String contentOfferJSON, boolean emptyQueue, Map<String,Resource> collectedResources) throws Exception {
		Map<String,Resource> urnsResult = new HashMap<String,Resource>();
		
		if (bps == null) {// Do not load the BPs again (stream will fail)
			bps = getBlueprints(descriptorFile);
			processTagsMap(bps);
			processTagsGroup(bps);
		}

		// Processing outer foreach tags
		for (Foreach foreach: bps.getForeach()) {
			processTagForeach(contentOfferJSON, null, foreach, new LinkedHashMap<String,String>(),collectedResources);
		}
		
		// Processing outer variables
		Map<String, String> variables = new LinkedHashMap<String, String>();
		for (Var var: bps.getVar()) {
			Object varValue = getVarContent(contentOfferJSON, var, variables);
			if (varValue != null) {
				variables.put(var.getName(), String.valueOf(varValue));
			}
		}
		
		// Processing outer BP tags
		for (Blueprint bp: bps.getBlueprint()) {
			String bpType = "";
			if (StringUtils.isNotBlank(bp.getJsonpathmatch())) {
				Object o = getJSONpathValue(contentOfferJSON, bp.getJsonpathmatch(), new LinkedHashMap<String,String>());
				if (o instanceof JSONArray && !((JSONArray)o).isEmpty()) {
					bpType = String.valueOf(((JSONArray) o).get(0));
				} else {
					bpType = String.valueOf(o);
				}
			}
			String jsonmatchvalue = replaceVariablesWithContent(bp.getJsonmatchvalue(), variables);
			if (StringUtils.isBlank(bp.getJsonpathmatch()) || StringUtils.equals(jsonmatchvalue, bpType)) {
				Resource resource = processBP(contentOfferJSON, bp, variables, collectedResources);
				if (resource != null) {
					urnsResult.put(resource.getUrn() == null ? resource.getId() : resource.getUrn(), resource);
				}
			} else {
				log(logger,String.format("Not processing BP %s. Evaluation of jsonpathmatch=%s does NOT match jsonmatchvalue=%s", bp.getName(), bp.getJsonpathmatch(), bp.getJsonmatchvalue()));
			}
		}
		return urnsResult;
	}
	
	public Blueprints getBlueprints(InputStream inputStream) throws Exception {
		try {
	    	Blueprints blueprints = parseFeedXml(inputStream);
	    	return blueprints;
		} finally {
			if (inputStream != null) {
		    	inputStream.close();
			}
		}
	}
	
	public Blueprints parseFeedXml(InputStream feedStream) throws Exception {
		
        JAXBContext jaxbContext = JAXBContext.newInstance(Blueprints.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Blueprints blueprints = (Blueprints) unmarshaller.unmarshal(feedStream);

		return blueprints;
	}
	

	/**
	 * process blueprint definition. If updateRequired == true, nothing will be returned and the update will be done in batches
	 * @param bp
	 * @param variables
	 * @param collectedResources
	 * @return
	 * @throws Exception
	 */
	protected Resource processBP(String json, Blueprint bp, Map<String,String> variables, Map<String,Resource> collectedResources) throws Exception {
		log(logger,"---> Processing BP " + bp.getName());
		
        // Adding tags from the includes
		addIncludes(bp);

		log(logger,"Processing BluePrint Descriptor: " + bp.getName());
		
		Resource r = new Resource();
		r.setBpName(bp.getName());
		r.setEntityType(bp.getType());
		if (bp.getVersion() > 0) {
            r.setBpVersionNum(bp.getVersion());
        }
        r.setLastCheckedTime(System.currentTimeMillis());
        r.setCreatedTime(System.currentTimeMillis());
        r.setUpdatedTime(System.currentTimeMillis());

        preloadedResource = null;
		if (Boolean.TRUE.equals(bp.isPreload())) {
            preloadedResource = preloadExistingResource(json, r, bp, variables, null);
            if (preloadedResource != null) {
                preloadedResources.add(preloadedResource);
            }
		}

    	processInnerTags(json, r, bp.getForeach(), bp.getForeachkey(), bp.getField(), bp.getVar(), null, variables, collectedResources);

		if (preloadedResource != null && preloadedResources.size() > 0) {
            preloadedResources.remove(preloadedResources.size() - 1);
        }

		// Loading existing resources where primaryKey=true and type=blueprint. It needs that for the URN and resource lookup
		Map<String,String> existingURNS = new HashMap<>();
		for (Field f: bp.getField()) {
			if (Boolean.TRUE.equals(f.isPrimaryKey()) && "blueprint".equals(f.getType()) && Boolean.FALSE.equals(f.getBlueprint().isCreate())) {
				Resource res = getExistingResource(json, r, f.getBlueprint(), variables, null);
				if (res != null) {
					existingURNS.put(f.getName(), res.getUrn());
				}
			}
        }
    	
		// If skipCms=true just generate the URN and return it. Don't send to CMS.
    	if (Boolean.TRUE.equals(bp.isSkipCms())) {
            r.setUrn(generateUrn(json, bp, r, variables, existingURNS));
			return r;
		}

        if (Boolean.FALSE.equals(bp.isCreate())) {
        	// create=false lookup resource. Will fail and throw an Exception if not found
    		Resource existingRes = getExistingResource(json, r, bp, variables, existingURNS);
    		if (existingRes != null) {
    			r.setId(existingRes.getId());
                r.setUrn(existingRes.getUrn());
        		log(logger,String.format("Loaded resource for BP=%s (because create=false). Resource found: %s", bp.getName(), existingRes.getUrn()));
    		} else {
                r.setUrn(generateUrn(json, bp, r, variables, existingURNS));
    		}
    		if (Boolean.FALSE.equals(bp.isUpdate())) {
            	// create=false and update=false -> just return the urn without updating the resource
        		Resource loadedResource = new Resource();
        		loadedResource.setId(existingRes.getId());
        		loadedResource.setUrn(existingRes.getUrn());
        		return loadedResource;
    		}
        } else {
        	if (Boolean.FALSE.equals(bp.isUpdate())) {
            	// create=true and update=false -> Load resource and if it is already there, just return the urn without updating it
        		Resource existingRes = getExistingResource(json, r, bp, variables, existingURNS);
        		if (existingRes != null) {
            		log(logger,String.format("Loaded resource for BP=%s (because update=false). Resource found: %s. Not to update this resource.", bp.getName(), existingRes.getUrn()));
            		r.setId(existingRes.getId());
                    r.setUrn(existingRes.getUrn());
                    return r;
        		}
        	}
        	if (Boolean.TRUE.equals(bp.isLoadExistingURN())) {
        		Resource existingRes = getExistingResource(json, r, bp, variables, existingURNS);
        		if (existingRes != null) {
            		r.setId(existingRes.getId());
                    r.setUrn(existingRes.getUrn());
        		} else {
                    r.setUrn(generateUrn(json, bp, r, variables, existingURNS));
        		}
        	} else {
	            r.setId(r.getId());
	            r.setUrn(generateUrn(json, bp, r, variables, existingURNS));
        	}
        }

        ObjectMapper mapper = new ObjectMapper();
        System.out.println(mapper.writeValueAsString(r));
        
        createOrUpdateResource(resourceLoader, r, json, bp, variables, collectedResources);
        
		return r;
	}

	protected String generateUrn(String json, Blueprint bp, Resource resource, Map<String,String> variables, Map<String,String> existingURNS) throws Exception {
		if (StringUtils.isNotBlank(bp.getUrn())) {
			return String.valueOf(getJSONpathValue(json, bp.getUrn(), variables)).replace("urn:cms:resource:", "");
		}
		String key = bp.getName() + getPKKey(json, resource, bp, variables, existingURNS);
		key = key.replaceAll("_", "zz").replaceAll("urn.cms.resource", "zz").replaceAll("[^0-9a-zA-Z]", "");
		log(logger,String.format("Generated URN %s", key));
		return key;
	}
	
	protected void createOrUpdateResource(ResourceLoader resourceLoader, Resource r, String json, Blueprint bp, Map<String,String> variables, Map<String,Resource> urnsResult) throws Exception {
		Utils.upsertResource(resourceLoader, r);
	}

	protected Map<String, List<Entry>> getInternalMaps() {
		return internalMaps;
	}

	protected void setInternalMaps(Map<String, List<Entry>> internalMaps) {
		this.internalMaps = internalMaps;
	}

	/**
	 * Updates the internalMaps property based on the available maps defined. internalMaps contains the key-value pair for every map defined in the descriptor file.
	 * @param blueprints
	 * @throws Exception
	 */
	protected void processTagsMap(Blueprints blueprints) {
		List<com.att.ingestion.descriptor.Map> maps = blueprints.getMap();
		setInternalMaps(new HashMap<String,List<Entry>>());
		for (com.att.ingestion.descriptor.Map m: maps) {
			internalMaps.put(m.getName(), m.getEntry());
		}
	}

	protected void processTagsGroup(Blueprints blueprints) {
		internalGroups = new HashMap<String,Group>();
		for (Group g: blueprints.getGroup()) {
			internalGroups.put(g.getName(), g);
		}
		for (Group g: blueprints.getGroup()) {
			addIncludes(g);
		}
	}
	
	protected void addIncludes(Object targetObj) {
		List<Foreach> foreachs;
		List<Foreachkey> foreachkeys;
		List<Var> vars;
		List<Field> fields;
		List<Include> includes;
		if (targetObj instanceof Blueprint) {
			foreachs = ((Blueprint)targetObj).getForeach();
			foreachkeys = ((Blueprint)targetObj).getForeachkey();
			vars = ((Blueprint)targetObj).getVar();
			fields = ((Blueprint)targetObj).getField();
			includes = ((Blueprint)targetObj).getInclude();
		} else if (targetObj instanceof Group) {
			foreachs = ((Group)targetObj).getForeach();
			foreachkeys = ((Group)targetObj).getForeachkey();
			vars = ((Group)targetObj).getVar();
			fields = ((Group)targetObj).getField();
			includes = ((Group)targetObj).getInclude();
		} else if (targetObj instanceof Foreach) {
			foreachs = ((Foreach)targetObj).getForeach();
			foreachkeys = ((Foreach)targetObj).getForeachkey();
			vars = ((Foreach)targetObj).getVar();
			fields = ((Foreach)targetObj).getField();
			includes = ((Foreach)targetObj).getInclude();
		} else if (targetObj instanceof Foreachkey) {
			foreachs = ((Foreachkey)targetObj).getForeach();
			foreachkeys = ((Foreachkey)targetObj).getForeachkey();
			vars = ((Foreachkey)targetObj).getVar();
			fields = ((Foreachkey)targetObj).getField();
			includes = ((Foreachkey)targetObj).getInclude();
		} else {
			// nothing to do
			return;
		}
		for (Include includeTag: includes) {
			if (!internalGroups.containsKey(includeTag.getGroup())) {
				log(logger,String.format("Trying to include an invalid group=%s", includeTag.getGroup()));
			}
			Group g = internalGroups.get(includeTag.getGroup());
			if (g == null) {
				throw new RuntimeException("Group named " + includeTag.getGroup() + " NOT FOUND!");
			}
			foreachs.addAll(g.getForeach());
			foreachkeys.addAll(g.getForeachkey());
			vars.addAll(g.getVar());
			fields.addAll(g.getField());
		}
		// Remove include so it won't mess up the next run
		includes.clear();
	}

	protected Resource preloadExistingResource(String doc, Resource r, Blueprint bp, Map<String,String> variables, Map<String,String> existingURNS) throws Exception {
		Map<String,Map<String,String>> selectors = new LinkedHashMap<String,Map<String,String>>();
		Map<String, Object> filterCondition = new HashMap<String, Object>();
		for (Field f: bp.getField()) {
			if (Boolean.TRUE.equals(f.isPrimaryKey())) {
				String value;
				if ("blueprint".equals(f.getType())) {
					if (existingURNS != null && existingURNS.containsKey(f.getName())) {
						value = existingURNS.get(f.getName());
					} else {
						value = Utils.getExistingPropertyAsString(r, f.getName(), null);
					}
				} else {
					value = String.valueOf(getFieldValue(doc, f, variables));
				}
				filterCondition.put(f.getName(), value);
				if (f.getSelector() != null) {
					selectors.put(f.getName(), generateSelector(f, variables));
				}
			}
		}
		if (filterCondition.isEmpty()) {
			 return null;
		}

		return resourceLoader.pullResource(bp.getName(), filterCondition, selectors);
	}

	protected Resource getExistingResource(String doc, Resource r, Blueprint bp, Map<String,String> variables, Map<String,String> existingURNS) throws Exception {
		String keyString = null;
		synchronized(existingResources) {
			if (existingResources.get(bp.getName()) == null) {
				existingResources.put(bp.getName(), new ConcurrentHashMap<String, Resource>());
			}
			keyString = getPKKey(doc, r, bp, variables, existingURNS);
			if (existingResources.get(bp.getName()).containsKey(keyString)) {
				return existingResources.get(bp.getName()).get(keyString);
			}
		}
		//NOT cached. Loading...
		Map<String,Map<String,String>> selectors = new LinkedHashMap<String,Map<String,String>>();
		Map<String, Object> filterCondition = new HashMap<String, Object>();
		for (Field f: bp.getField()) {
			if (Boolean.TRUE.equals(f.isPrimaryKey())) {
				String value;
				if ("blueprint".equals(f.getType())) {
					if (existingURNS != null && existingURNS.containsKey(f.getName())) {
						value = existingURNS.get(f.getName());
					} else {
						value = Utils.getExistingPropertyAsString(r, f.getName(), null);
					}
				} else {
					value = String.valueOf(getFieldValue(doc, f, variables));
				}
				filterCondition.put(f.getName(), value);
				if (f.getSelector() != null) {
					selectors.put(f.getName(), generateSelector(f, variables));
				}
			}
        }
		if (filterCondition.isEmpty()) {
			return null;
		}

		Resource res = resourceLoader.pullResource(bp.getName(), filterCondition, selectors);
		
		if (res == null && Boolean.FALSE.equals(bp.isCreate())) {
			throw new Exception(String.format("Blueprint %s set as create=false and resource ref could not be found. Resource will NOT be created. Failing ingestion. Filter condition: %s Selectors: %s ", bp.getName(), filterCondition, selectors));
		}
		if (res == null) {
			log(logger,"Search returned null");
			return null;
		}
		log(logger,String.format("Returned URN %s", res.getUrn()));
		existingResources.get(bp.getName()).put(keyString, res);
		return res;
	}

	protected String getPKKey(String json, Resource r, Blueprint bp, Map<String,String> variables, Map<String,String> existingURNS) throws IOException, MandatoryValueNotFoundException {
		Map<String,Field> fieldsInAlphabeticalOrder = new TreeMap<String,Field>();
		Field selectorField = null;
		for (Field f: bp.getField()) {
			if (Boolean.TRUE.equals(f.isPrimaryKey())) {
				fieldsInAlphabeticalOrder.put(f.getName(), f);
				if (f.getSelector() != null) {
					selectorField = f;
				}
			}
        }

		StringBuffer key = new StringBuffer();
		//"[%s]_[%s]..."  sample: [Gina]_[Torres](_[selectorname=selectorvalue]).
		
		for (String keyObj: fieldsInAlphabeticalOrder.keySet()) {
			if (key.length() != 0) {
				key.append("_");
			}
			Field field = fieldsInAlphabeticalOrder.get(keyObj);
			if ("blueprint".equals(field.getType())) {
				if (existingURNS != null && existingURNS.containsKey(field.getName())) {
					key.append(String.format("[%s]", existingURNS.get(field.getName())));
				} else {
					key.append(String.format("[%s]", Utils.getExistingPropertyAsString(r, field.getName(), null)));
				}
			} else {
				key.append(String.format("[%s]", getFieldValue(json, field, variables)));
			}
		}
		// Add selector if any
		if (selectorField != null) {
			key.append("_");
			key.append(String.format("[%s]", generateSelector(selectorField, variables)));
			log(logger,"Key with selector: " + key.toString());
		}
		return key.toString();
	}


	private void processInnerTags(String json, Resource r, List<Foreach> foreachs, List<Foreachkey> foreachkeys,
								  List<Field> fields, List<Var> vars, List<Blueprint> blueprints,
								  Map<String,String> variables,Map<String,Resource> collectedResources) throws Exception {

		for (Var oneVar: vars) {
			Object varValue = getVarContent(json, oneVar, variables);
			if (varValue != null) {
				variables.put(oneVar.getName(), String.valueOf(varValue));
			}
		}

        for (Foreach f: foreachs) {
        	processTagForeach(json, r, f, variables, collectedResources);
        }

        for (Foreachkey f: foreachkeys) {
        	processTagForeachkey(json, r, f, variables, collectedResources);
        }

        for (Field f: fields) {
        	processTagField(json, r, f, variables,collectedResources);
        }
        
        if (blueprints != null) { 
        	for (Blueprint blueprint: blueprints) {
	        	processBP(json, blueprint, variables,collectedResources);
        	}
        }
	}
	
	@SuppressWarnings("unchecked")
	private void processTagForeach(String json, Resource r, Foreach foreach, Map<String,String> variables,Map<String,Resource> urnsResult) throws Exception {
		addIncludes(foreach);
		
		Object valueObj = null;
		if (foreach.getValue() != null) {
			valueObj = replaceVariablesWithContent(foreach.getValue(), variables);
		} else {
			Field tempField = new Field();
			tempField.setJsonpath(foreach.getJsonpath());
			valueObj = getFieldValue(json, tempField, variables);
		}
		if (!Boolean.TRUE.equals(foreach.isForceloop()) && (valueObj == null || "".equals(valueObj))) {
			// Nothing to process here
			return;
		}
		List<String> list = null;
		if (foreach.getSplitter() != null) {
//			list = (List<String>)ParserUtil.execute(foreach.getSplitter(), (String)valueObj, resourceLoader);
			if (valueObj instanceof JSONArray) {
				log(logger,"Splitter " + foreach.getSplitter() + " parsing JSONArray size " + ((JSONArray)valueObj).size());
			}
			if (valueObj instanceof JSONArray && ((JSONArray)valueObj).size() > 0) {
				log(logger,">>>>>>>>>>> Splitter value for " + ((JSONArray)valueObj).get(0).toString());
				list = (List<String>)ParserUtil.execute(foreach.getSplitter(), ((JSONArray)valueObj).get(0).toString(), resourceLoader);
			} else {
				list = (List<String>)ParserUtil.execute(foreach.getSplitter(), String.valueOf(valueObj), resourceLoader);
			}
		} else if (valueObj instanceof JSONArray) {
			list = DescriptorService.toList((JSONArray)valueObj);
		} else {
			list = new ArrayList<String>();
			String valueString = String.valueOf(valueObj);
			if (valueString != null && valueString.startsWith("[") && valueString.endsWith("]") && !valueString.startsWith("[{") ) {//This is a simple array. Parsing it.
				valueString = valueString.substring(1, valueString.length() - 1);
				List<String> values = ParserUtil.splitByComma(valueString);
				for (String oneValue: values) {
					if (oneValue != null && oneValue.startsWith("\"") && oneValue.endsWith("\"") ) {
						oneValue = oneValue.substring(1, valueString.length() - 1);
					}
					list.add(oneValue);
				}
			} else {
				list.add(String.valueOf(valueObj));
			}
		}
		
		// Remove Duplicates if required
		removeDuplicates(foreach.isRemoveDuplicates(), list);
		
		//Save var context value
		String previousValue = variables.get(foreach.getName());
		
		if (list != null) {
			for (String item: list) {
				variables.put(foreach.getName(), item);
				String innerJson = item;
				if (foreach.getSplitter() != null) { // If splitter used, don't change innerJson
					innerJson = json;
				}
				processInnerTags(innerJson, r, foreach.getForeach(), foreach.getForeachkey(), foreach.getField(), foreach.getVar(), foreach.getBlueprint(),variables,urnsResult);
			}
		}

		//Restore var context value
		if (previousValue != null) {
			variables.put(foreach.getName(), previousValue);
		} else {
			variables.remove(foreach.getName());
		}
		
	}
	
	protected static void removeDuplicates(Boolean removeDuplicates, List<String> list) {
		if (Boolean.TRUE.equals(removeDuplicates)) {
			Set<String> s = new HashSet<>();
			s.addAll(list);
			list.clear();
			list.addAll(s);
		}
	}

	protected void processTagForeachkey(String json, Resource r, Foreachkey foreachkey, Map<String,String> variables,Map<String,Resource> urnsResult) throws Exception {
		addIncludes(foreachkey);

		Object value = getJSONpathValue(json, foreachkey.getJsonpath(), variables);
		
		//Save var context value
		String previousValue = variables.get(foreachkey.getName());
		
		if (value instanceof Map) {
			
			// Process: Assuming the key of the map is always a String
			@SuppressWarnings("unchecked")
			Map<String,Object> map = (Map<String,Object>)value;
			for (String key: map.keySet()) {
				variables.put(foreachkey.getName(), key);
				processInnerTags(json, r, foreachkey.getForeach(), foreachkey.getForeachkey(), foreachkey.getField(), foreachkey.getVar(), null,variables,urnsResult);
			}
			
		} else if (value instanceof JSONArray) {
			
			// Process: key is an Integer in case of array
			for (int i = 0; i < ((JSONArray)value).size(); i++) {
				variables.put(foreachkey.getName(), String.valueOf(i));
				processInnerTags(json, r, foreachkey.getForeach(), foreachkey.getForeachkey(), foreachkey.getField(), foreachkey.getVar(),null, variables,urnsResult);
			}
			
		}
		
		//Restore var context value
		if (previousValue != null) {
			variables.put(foreachkey.getName(), previousValue);
		} else {
			variables.remove(foreachkey.getName());
		}
	}

	protected void processTagField(String json, Resource r, Field f, Map<String,String> variables, Map<String,Resource> urnsResult) throws Exception {
		if (StringUtils.isBlank(f.getType())) {
	    	processField(json, r, f, variables);
		} else if (f.getType().equals("kvp")) {
			processFieldKVP(json, r, f, variables,urnsResult);
		} else if (f.getType().equals("blueprint")) {
			log(logger,"--> Processing inner BP " + f.getBlueprint().getName() + " ");
    		if (f.getBlueprint() != null) {
    	        try {
                	Resource resource = processBP(json, f.getBlueprint(), variables,urnsResult);
                	Map<String,String> selectorObj = generateSelector(f, variables);

                	// Set value
            		Utils.addResourceProperty(r, f.getName(), selectorObj, resource.getUrn(), f.isAllowsNull(), f.isMandatory());
    	        } catch (MandatoryValueNotFoundException e) {
    	        	log(logger,"Mandatory BP could not be added for blueprint " + f.getBlueprint().getName());
    	        }
    		} else {
    			log(logger,"No blueprint inside field tag " + f.getName());
    		}
		}
	}
	
	private void processField(String json, Resource r, Field f, Map<String,String> variables) throws IOException, MandatoryValueNotFoundException {
		Object value = getFieldValue(json, f, variables);
		Map<String,String> selectorObj = generateSelector(f, variables);

		// Processing preloaded values in the CMS resource if any
		if (Boolean.TRUE.equals(f.isPreload()) && preloadedResource != null) {
			Map<String, String> selectors = null;
			if (f.getSelector() != null) {
				selectors = new HashMap<>();
				selectors.put(f.getSelector(), f.getSelectorValue());
			}
			Object preloadedValue = Utils.getExistingProperty(preloadedResource, f.getName(), selectors);
			if (preloadedValue != null ) {
                    Utils.addResourceProperty(r, f.getName(), selectorObj, preloadedValue, f.isAllowsNull(), f.isMandatory());
            }
		}

		Utils.addResourceProperty(r, f.getName(), selectorObj, value, f.isAllowsNull(), f.isMandatory());
	}

	private void processFieldKVP(String json, Resource r, Field f, Map<String,String> variables,Map<String,Resource> urnsResult) throws Exception {
		Map<String,Object> kvp = new HashMap<String,Object>();
		//Processing var
		for (Var oneVar: f.getVar()) {
			Object varValue = getVarContent(json, oneVar, variables);
			if (varValue != null) {
				variables.put(oneVar.getName(), String.valueOf(varValue));
			}
		}
		for (Field innerField: f.getField()) {
			if (StringUtils.isBlank(innerField.getType())) {
				Object value = getFieldValue(json, innerField, variables);
				if (value != null && !StringUtils.isBlank(String.valueOf(value))) {
					kvp.put(innerField.getName(), value);
				}
			} else if (innerField.getType().equals("blueprint")) {
	    		if (innerField.getBlueprint() != null) {
	    	        try {
		            	Resource resource = processBP(json, innerField.getBlueprint(), variables,urnsResult);
		            	kvp.put(innerField.getName(), resource.getUrn());
	    	        } catch (MandatoryValueNotFoundException e) {
	    	        	log(logger,"Mandatory BP could not be added for blueprint " + innerField.getBlueprint().getName());
	    	        }
	    		} else {
	    			log(logger,"No blueprint inside field tag " + f.getName());
	    		}
			}
		}
		
		Map<String,String> selectorObj = generateSelector(f, variables);
		
		// Set value
		if (!kvp.isEmpty()) {
			Utils.addResourceProperty(r, f.getName(), selectorObj, kvp, f.isAllowsNull(), f.isMandatory());
		}
	}

	public Map<String,String> generateSelector(Field f, Map<String,String> variables) {
		String selector = f.getSelector();
		String selectorValue = f.getSelectorValue();
		if (selector == null) {
			return null;
		}
		Map<String,String> selectorObj = null;

		String[] selectors = selector.split(",");
		String[] selectorValues = selectorValue.split(",");
		
		if (selectors.length != selectorValues.length) {
			log(logger,String.format("Invalid selector configuration for field %s", f.getName()));
			return null;
		}

		for (int i = 0; i < selectors.length; i++) {
			String selectorItem = selectors[i];
			String selectorValueItem = selectorValues[i]; 
			if (selectorItem != null && selectorValueItem != null) {
				selectorValueItem = replaceVariablesWithContent(selectorValueItem, variables);
				if (StringUtils.isBlank(selectorValueItem)) {
					selectorValueItem = "Default";
				}
				if (selectorObj == null) {
					selectorObj = new HashMap<String,String>();
				}
				selectorObj.putAll(Collections.singletonMap(selectorItem, selectorValueItem));
			}
		}
		
		return selectorObj;
	}

	protected Object getVarContent(String json, Var oneVar, Map<String,String> variables) throws NoSuchMethodException,
			SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException, MandatoryValueNotFoundException {
		String value = "";
		if (oneVar.getValue() != null) {
			value = replaceVariablesWithContent(oneVar.getValue(), variables);
		} else if (oneVar.getJsonpath() != null) {
			value = replaceVariablesWithContent(oneVar.getJsonpath(), variables);
			value = String.valueOf(getJSONpathValue(json, value, variables));
		}

		// Process split using splitter+index attributes from the tag
		if (oneVar.getIndex() != null && oneVar.getSplitter() != null) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>)ParserUtil.execute(oneVar.getSplitter(), value, resourceLoader);
			if (list != null && list.size() > (oneVar.getIndex())) {
				value = list.get(oneVar.getIndex());
			}
		}
		
		// Process map
		if (oneVar.getMap() != null && internalMaps.containsKey(oneVar.getMap())) {
			List<Entry> entries = internalMaps.get(oneVar.getMap());
			for (Entry entry: entries) {
				if (value == null) {
					continue;
				}
				if ("regex".equals(entry.getType()) && value.matches(entry.getKey())) {
					value = entry.getValue();
					break;
				} else if ("case-insensitive".equals(entry.getType()) && value.equalsIgnoreCase(entry.getKey())) {
					value = entry.getValue();
					break;
				} else if ("*".equals(entry.getKey())) {
					value = entry.getValue();
					break;
				} else if (value.equals(entry.getKey())) {
					value = entry.getValue();
					break;
				}
			}
		}
		
		// Process converter
		Object resultConverted = processConverter("<var name:" + oneVar.getName() + "/>", oneVar.getConverter(), (String)value);

		return resultConverted;
	}
	
	private Object processConverter(String tagName, String converter, String value) throws IOException, MandatoryValueNotFoundException {
		if (converter == null) {
			return value;
		}
		Object result = null;
		try {
			result = ParserUtil.execute(converter, (String)value, resourceLoader);
		} catch (Exception e) {
			value = null;
			log(logger,String.format("Property [%s] has an invalid converter(%s) or value(%s)", tagName, converter, value));
		}
		return result;
	}

	private Object getFieldValue(final String json, final Field f, Map<String,String> variables) throws IOException, MandatoryValueNotFoundException {
		Object value = replaceVariablesWithContent(f.getValue(), variables);
		if (f.getJsonpath() != null) {
			value = getJSONpathValue(json, f.getJsonpath(), variables);
		}
		
		// Process converter
		String converter = f.getConverter();
		if (converter != null) {
			try {
				value = ParserUtil.execute(converter, String.valueOf(value), resourceLoader);
			} catch (Exception e) {
				value = null;
				log(logger,String.format("Property [%s] has an invalid converter(%s) or value(%s)", f.getName(), converter, value));
			}
		}
		
		if (Boolean.TRUE.equals(f.isPrimaryKey()) && (value == null || "".equals(value))) {
			log(logger, String.format("Mandatory [PK] field=%s with empty value=%s", f.getName(), value));
			throw new MandatoryValueNotFoundException();
		}
		
		return value;
	}


	private static void log(Logger logger, String msg){
		if(logger!= null){
			logger.info(msg);
		}
	}
}
