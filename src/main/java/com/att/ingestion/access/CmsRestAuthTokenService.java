package com.att.ingestion.access;

import java.text.MessageFormat;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.att.ingestion.access.exception.AuthenticateException;
import com.att.ingestion.access.model.CmsAccessToken;
import com.att.ingestion.config.CmsConfiguration;
import com.att.ingestion.utils.RestClientFactory;
import com.att.ingestion.utils.RestConfigurationInfo;

public class CmsRestAuthTokenService {
	
    public static final MessageFormat AUTHENTICATE_URL_FORMATTER = new MessageFormat("{0}/oauth/token?grant_type=password");
    public static final MessageFormat AUTHENTICATE_BODY = new MessageFormat("grant_type=password&username={0}&password={1}");

    private CmsRestAuthTokenService(){

    }

    public static CmsAccessToken getRemoteCmsAccessToken(CmsConfiguration configuration) throws AuthenticateException {
        ResponseEntity<CmsAccessToken> response;

        RestConfigurationInfo config = new RestConfigurationInfo();

        config.addHeader("Authorization", "Basic Y2xpZW50Og==");
        config.addHeader("Content-Type", "application/x-www-form-urlencoded");

        Object[] endPointArgs = {configuration.getCmsAuthUrl()};
        config.setEndpoint(AUTHENTICATE_URL_FORMATTER.format(endPointArgs));

        config.setMethod(HttpMethod.POST);

        Object[] bodyArgs = {configuration.getUser(), configuration.getPassword()};
        HttpEntity<String> entity = new HttpEntity<>(AUTHENTICATE_BODY.format(bodyArgs), config.getHeaders());

        response = RestClientFactory.getInstance().initRestTemplateSelfSignedHttps().exchange(config.getEndpoint(),
                config.getMethod(), entity, CmsAccessToken.class);

        if (response.getStatusCode().equals(HttpStatus.OK)) {
            return response.getBody();
        }

        throw new AuthenticateException(response.getStatusCode().toString());
    }
    
    public static boolean validateAccessToken(String accessToken) throws AuthenticateException {
    	//TODO ... validate the accessToken ...and throw error if token no valid or return true
    	return true;
    }
}
