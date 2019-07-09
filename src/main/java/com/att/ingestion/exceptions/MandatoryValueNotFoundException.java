package com.att.ingestion.exceptions;

public class MandatoryValueNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;
	
    public MandatoryValueNotFoundException() {
        super();
    }

    public MandatoryValueNotFoundException(String message) {
        super(message);
    }

}
