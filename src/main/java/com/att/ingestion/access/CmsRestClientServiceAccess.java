package com.att.ingestion.access;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.att.ingestion.access.errorHandler.CmsResponseErrorHandler;
import com.att.ingestion.access.exception.CmsRestServiceAccessException;
import com.att.ingestion.access.intf.ICmsRestClientServiceAccess;
import com.att.ingestion.access.model.CmsAccessToken;
import com.att.ingestion.config.CmsConfiguration;
import com.att.ingestion.model.Resource;
import com.att.ingestion.model.rest.ResponseData;
import com.att.ingestion.model.rest.ResponseHeader;

/**
 * Class that defines API for accessing CMS REST services. As the different methods signatures define
 * input parameters required by the relevant CMS services, a {@link CmsRestServiceAccessInvocationContext}
 * object should be used to define optional header parameters to be passed to CMS services.
 * <code>Content-type</code> is the only header set by default to {@link MediaType#APPLICATION_JSON}.
 * @see CmsRestServiceAccessInvocationContext
 * @see #setInvocationContext(CmsRestServiceAccessInvocationContext)
 */
public class CmsRestClientServiceAccess implements ICmsRestClientServiceAccess
{
    private final static Logger logger = LoggerFactory.getLogger(CmsRestClientServiceAccess.class);
    final RestTemplate restTemplate;
    final String contentMicroserviceUrl;

    public CmsRestClientServiceAccess(RestTemplate restTemplate, CmsConfiguration config) throws Exception {
        this.restTemplate = restTemplate;
        this.restTemplate.setErrorHandler(new CmsResponseErrorHandler());
        this.contentMicroserviceUrl = config.getCmsClientUrl();
        CmsAccessToken accessToken = new CmsAccessToken(); //CmsRestTokenService.getRemoteCmsAccessToken(configuration);
        accessToken.setTokenType("temp");
        accessToken.setAccessToken("temp");
        this.invocCtx.getHttpHeaders().add("Authorization",
                new StringBuilder(
                        accessToken.getTokenType())
                        .append(" ")
                        .append(accessToken.getAccessToken()).toString());
    }    

    @SuppressWarnings("unchecked")
	private <OBJECT_TYPE> Resource queryForSingleObject(
            @SuppressWarnings("rawtypes") ParameterizedTypeReference typeRef,
            UriComponentsBuilder uriBuilder,
            Object... args) throws CmsRestServiceAccessException
    {
		String url = uriBuilder.build().expand(args).toUriString();
        ResponseData<Resource> response = null;
        HttpHeaders headers = invocCtx.getHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        try
        {
            response = (ResponseData<Resource>) restTemplate.exchange(url,
                    HttpMethod.GET,
                    entity,
                    typeRef).getBody();
        } catch (Exception ex)
        {
        	logger.debug(ex.getMessage());
            throw new CmsRestServiceAccessException(ex.getMessage());
        }
        ResponseHeader hdr = response.getHeader();
        if (hdr.getMessage() != "Success")
        {
        	logger.debug("Failed to query for resources. URL: " + url);
            throw new CmsRestServiceAccessException(response.getHeader().getMessage());
        }
        return response.getData();
    }
	
    @SuppressWarnings("unchecked")
	private <OBJECT_TYPE> List<Resource> queryForMultipleObjects(
            @SuppressWarnings("rawtypes") ParameterizedTypeReference typeRef,
            UriComponentsBuilder uriBuilder,
            Object... args) throws CmsRestServiceAccessException
    {
		String url = uriBuilder.build().expand(args).toUriString();
        ResponseData<List<Resource>> response = null;
        HttpHeaders headers = invocCtx.getHttpHeaders();
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
        try
        {
            response = (ResponseData<List<Resource>>) restTemplate.exchange(url,
                    HttpMethod.GET,
                    entity,
                    typeRef).getBody();
        } catch (Exception ex)
        {
        	logger.debug(ex.getMessage());
            throw new CmsRestServiceAccessException(ex.getMessage());
        }
        ResponseHeader hdr = response.getHeader();
        if (hdr.getMessage() != "Success")
        {
        	logger.debug("Failed to query for resources. URL: " + url);
            throw new CmsRestServiceAccessException(response.getHeader().getMessage());
        }
        return response.getData();
    }
	
    private UriComponentsBuilder createUriBuilder()
    {
        return UriComponentsBuilder.fromHttpUrl(contentMicroserviceUrl);
    }
 
    @Override
    public Resource getResourceByUrn(String index, String urn) throws Exception
    {
        UriComponentsBuilder uriBuilder = createUriBuilder()
                .path("get/{index}/getResourceByUrn/{urn}");

        return queryForSingleObject(
                new ParameterizedTypeReference<List<Resource>>()
        {
        },
        uriBuilder,
        index,
        urn);
    }
    
    @Override
    public Resource getResourceById(String index, String id) throws Exception
    {
        UriComponentsBuilder uriBuilder = createUriBuilder()
                .path("get/{index}/getResourceById/{id}");

        return queryForSingleObject(
                new ParameterizedTypeReference<List<Resource>>()
        {
        },
        uriBuilder,
        index,
        id);
    }
    
    @Override
    public Resource getResourcesByType(String index, String entityType) throws Exception
    {
        UriComponentsBuilder uriBuilder = createUriBuilder()
                .path("get/{index}/getResourceByType/{entityType}");

        return queryForSingleObject(
                new ParameterizedTypeReference<Resource>()
        {
        },
        uriBuilder,
        entityType);
    }
    
    @Override
    public List<Resource> searchResources(String index, Map<String, Object> requestParams) throws Exception
    {
        UriComponentsBuilder uriBuilder = createUriBuilder()
                .path("search/{index}");
        
        for (Map.Entry<String, Object> entry : requestParams.entrySet()) {
            uriBuilder.queryParam(entry.getKey(), entry.getValue());
        }

        return queryForMultipleObjects(
                new ParameterizedTypeReference<List<Resource>>()
        {
        },
        uriBuilder,
        index);
    }

    /**
     * Invocation context, sets default Content-type
     */
    private CmsRestServiceAccessInvocationContext invocCtx = new CmsRestServiceAccessInvocationContext();
    
    /**
     * Sets optional invocation context containing optional request header parameters
     * @param ctx invocation context, pass {@link Optional#empty()} to reset the previously set
     * @throws NullPointerException if <code>ctx</code> is <code>null</code>
     */
    public void setInvocationContext(CmsRestServiceAccessInvocationContext ctx) throws NullPointerException {
    	invocCtx = Optional.of(ctx).orElseThrow(() ->
			new NullPointerException("Invocation context cannot be null."));
    }
    
    public CmsRestServiceAccessInvocationContext getInvocationContext() {
    	return invocCtx;
    }
    
    /**
     * Stores and manages optional request header parameters for CMS REST Service;
     * default Content-type is set to {@link MediaType#APPLICATION_JSON}
     */
    /**
     * @author sc233q
     *
     */
    public static class CmsRestServiceAccessInvocationContext {

    	private HttpHeaders hdrs;

    	/**
    	 * Initialize HTTP headers; set Content-type to {@link MediaType#APPLICATION_JSON}
    	 */
    	public CmsRestServiceAccessInvocationContext() {
			super();
			hdrs = new HttpHeaders();
			hdrs.setContentType(MediaType.APPLICATION_JSON);
		}
    	
    	/**
    	 * Reset the default Content-type header value
    	 * @param mType new Content-type
    	 * @see #CmsRestServiceAccessInvocationContext
    	 * @see CmsRestServiceAccessInvocationContext
    	 */
    	public void setContentType(MediaType mType) {
    		hdrs.setContentType(mType);
    	}
    	
    	public MediaType getContentType() {
    		return hdrs.getContentType();
    	}
    	
    	public HttpHeaders getHttpHeaders() {
    		return hdrs;
    	}
    	
    	public void setRequestHeaderValue(String parmName, String parmVal) {
    		hdrs.add(parmName, parmVal);
    	}
    	
    	public void resetRequestHeaderValue(String parmName) {
    		hdrs.remove(parmName);
    	}
    	
    	public Optional<List<String>> getRequestHeaderValue(String parmName) {
    		return Optional.ofNullable(hdrs.get(parmName));
    	}
    }
}
