package com.att.ingestion.utils;

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.io.Serializable;

public class RestConfigurationInfo implements Serializable {

    private static Gson gson = new Gson();
    
    private static final long serialVersionUID = 1L;
    private String endpoint;
    private HttpHeaders headers;
    private HttpMethod method;

    public RestConfigurationInfo() {

    }

    public RestConfigurationInfo(String json) throws Exception {
        if (StringUtils.isEmpty(json)) {
            throw new Exception("Invalid Destination JSON Format");
        }
        RestConfigurationInfo info = gson.fromJson(json, RestConfigurationInfo.class);
        this.setEndpoint(info.getEndpoint());
        this.setMethod(info.getMethod());
        this.setHeaders(info.getHeaders());
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }


    public void addHeader(String headerName, String value) {
        if(null == headers){
            this.headers = new HttpHeaders();
        }
        this.getHeaders().add(headerName, value);
    }

    public void setHeaders(HttpHeaders headers) {
        this.headers = headers;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    @Override
    public String toString() {
        final HttpHeaders newHeader = new HttpHeaders();
        if(null != headers && headers.containsKey(HttpHeaders.AUTHORIZATION)){
            //for security reason - we do not expose Authorization information
            headers.entrySet().stream().forEach(entry->{
                    if(!entry.getKey().equals(HttpHeaders.AUTHORIZATION)){
                        newHeader.put(entry.getKey(), entry.getValue());
                    }
            });
        }
        return "RestConfigurationInfo{" +
                "endpoint='" + endpoint + '\'' +
                ", headers=" + newHeader +
                ", method=" + method +
                '}';
    }

}
