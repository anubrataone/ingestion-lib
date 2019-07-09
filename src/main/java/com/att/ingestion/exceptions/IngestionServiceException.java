package com.att.ingestion.exceptions;

public class IngestionServiceException extends Exception
{

    private static final long serialVersionUID = 1L;

    private static String message = "CMS Service Error: ";

    public IngestionServiceException()
    {
        super(message);
    }
    
    public IngestionServiceException(String msg)
    {
        super(msg);
    }

    public String getMessage()
    {
        return super.getMessage();
    }
}
