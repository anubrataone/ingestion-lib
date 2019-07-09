package com.att.ingestion.publish;

import java.io.InputStream;
import java.util.Map;

import com.att.ingestion.access.CmsRestAuthTokenService;
import com.att.ingestion.access.exception.AuthenticateException;
import com.att.ingestion.config.CmsConfiguration;
import com.att.ingestion.exceptions.IngestionServiceException;
import com.att.ingestion.model.Resource;
import com.att.ingestion.service.DescriptorService;
import com.att.ingestion.service.IngestionService;
import com.att.ingestion.service.ResourceLoader;

public class IngestionHandlerService {
	
    public Map<String, Resource> publishToCMS(InputStream descriptorStream, String offerJsonContent, String accessToken, CmsConfiguration cmsConfig) throws AuthenticateException, IngestionServiceException {

        try {
        	CmsRestAuthTokenService.validateAccessToken(accessToken);
            IngestionService server = getContentMsServer(cmsConfig);
            ResourceLoader resourceLoader = new ResourceLoader(server, 1, 20);
            DescriptorService executor = new DescriptorService(resourceLoader, descriptorStream);
            Map<String, Resource> resources = executor.exec(offerJsonContent);
            return resources;
        } catch(Exception e) {
        	throw new IngestionServiceException(e.getMessage());
        }
    }
    
    protected IngestionService getContentMsServer(CmsConfiguration config) throws Exception {
        return new IngestionService(config);
    }
}
