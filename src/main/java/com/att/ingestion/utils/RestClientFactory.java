package com.att.ingestion.utils;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * This class is thread-safe - can be used as static & accessed by multiple concurrent threads
 * Help me to keep it as-is
 **/
public class RestClientFactory {

    private static RestClientFactory instance;
    private HttpComponentsClientHttpRequestFactory requestFactory = null;

    static {
        getInstance();
    }

    public RestClientFactory(RestClientFactory instance) {
        if (null != instance) {
            RestClientFactory.instance = instance;
        }
    }

    private RestClientFactory() {
        buildRequestFactory();
    }

    public static RestClientFactory getInstance() {
        if (null == instance) {
            instance = new RestClientFactory();
        }
        return instance;
    }

    private HttpComponentsClientHttpRequestFactory buildRequestFactory() {
        if (null == requestFactory) {
            requestFactory = new HttpComponentsClientHttpRequestFactory();
            useApacheHttpClientWithSelfSignedSupport(requestFactory);
        }
        return requestFactory;
    }

    public RestTemplate initRestTemplateSelfSignedHttps() {
        RestTemplate restTemplate = new RestTemplate(this.requestFactory);
        return restTemplate;
    }

    private static void useApacheHttpClientWithSelfSignedSupport(
            HttpComponentsClientHttpRequestFactory requestFactory) {
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();

        requestFactory.setHttpClient(httpClient);
    }
}
