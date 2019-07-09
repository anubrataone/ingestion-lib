package com.att.ingestion.access.exception;

public class AuthenticateException extends Exception {
    
	private static final long serialVersionUID = 2037160502696494305L;

    public AuthenticateException(String response) {
        super(response);
    }

    @Override
    public String toString() {
        return "AuthenticationException{" +
                "response=" + getMessage();
    }
}
