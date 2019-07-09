package com.att.ingestion.model.rest;

import java.io.Serializable;

public class ResponseError implements Serializable {

	private static final long serialVersionUID = 1L;
	private String error, description;

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
