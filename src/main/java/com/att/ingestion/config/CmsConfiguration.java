package com.att.ingestion.config;

import org.apache.commons.codec.binary.Base64;

public class CmsConfiguration {
    
    private String user;
    private String password;
    private String oauthHost;
    private int oauthPort;
    private int timeout;
    private String cmsAuthUrl;
    private String cmsAdminUrl;
    private String cmsAdminHost;
    private int cmsAdminPort;
	private String cmsClientUrl;
    private String cmsClientHost;
    private int cmsClientPort;

    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) throws SecurityException {
        this.password = Base64.encodeBase64String(password.getBytes());
    }
    
    public String getOauthHost() {
        return oauthHost;
    }
    
    public void setOauthHost(String oauthHost) {
        this.oauthHost = oauthHost;
    }
    
    public int getOauthPort() {
        return oauthPort;
    }
    
    public void setOauthPort(int oauthPort) {
        this.oauthPort = oauthPort;
    }
	
    public int getTimeout() {
		return timeout;
	}
	
    public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	public String getCmsAdminUrl() {
		return cmsAdminHost+":"+cmsAdminPort;
	}
	
	public String getCmsClientUrl() {
		return cmsClientHost+":"+cmsClientPort;
	}
	
	public String getCmsAdminHost() {
		return cmsAdminHost;
	}
	
    public void setCmsAdminHost(String cmsAdminHost) {
		this.cmsAdminHost = cmsAdminHost;
	}
	
	public int getCmsAdminPort() {
		return cmsAdminPort;
	}
	
	public void setCmsAdminPort(int cmsAdminPort) {
		this.cmsAdminPort = cmsAdminPort;
	}
	
	public String getCmsClientHost() {
		return cmsClientHost;
	}
	
	public void setCmsClientHost(String cmsClientHost) {
		this.cmsClientHost = cmsClientHost;
	}
	
	public int getCmsClientPort() {
		return cmsClientPort;
	}
	
	public void setCmsClientPort(int cmsClientPort) {
		this.cmsClientPort = cmsClientPort;
	}
	
	public String getCmsAuthUrl() {
		return oauthHost+":"+oauthPort;
	}

	@Override
    public String toString() {
        return "CmsConfiguration [user=" + user + ", password=" + password + ", oauthHost=" + oauthHost + ", oauthPort="
                + oauthPort + ", cmsAuthUrl=" + cmsAuthUrl + ", cmsAdminUrl=" + cmsAdminUrl + ", cmsClientUrl=" + cmsClientUrl + "]";
    }
}

