package com.att.ingestion.access.exception;

public class CmsRestServiceAccessException extends Exception {
    
	private static final long serialVersionUID = 2037160502696494305L;

    public CmsRestServiceAccessException(String response) {
        super(response);
    }

    @Override
    public String toString() {
        return "CmsRestServiceAccessException{" +
                "response=" + getMessage();
    }
}
