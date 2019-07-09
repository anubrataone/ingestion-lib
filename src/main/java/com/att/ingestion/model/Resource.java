package com.att.ingestion.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Resource {

	@JsonProperty("id")
	private String id;
	
	@JsonProperty("url")
	private String urn;

	@JsonProperty("entity_type")
    private String entityType;

	@JsonProperty("bp_name")
    private String bpName;
    
    @JsonProperty("bp_version")
    private Integer bpVersionNum;

    @JsonProperty("others")
    private Map<String,Object> properties = new HashMap<>();

    //######################################
    // timestamps
    //######################################
    
    private Instant createdTime;

    private Instant updatedTime;

    private Instant lastCheckedTime;

    //###################################
    // INTERNALLY USED PROPERTIES
    //###################################

    //////////////////////////////////////////////////////////////////////////
    // METHODS
    //////////////////////////////////////////////////////////////////////////
    
    public String getEntityType() {
		return entityType;
	}
	
    public void setEntityType(String entityType) {
		this.entityType = entityType;
	}
	
	public String getBpName() {
		return bpName;
	}
	
	public void setBpName(String bpName) {
		this.bpName = bpName;
	}

	public void setId(String id) {
		this.id = id;
	}

    public String getUrn()
    {
        return urn;
    }
	public void setUrn(String urn)
    {
        this.urn = urn;
    }

    public Integer getBpVersionNum()
    {
        return bpVersionNum;
    }

    public void setBpVersionNum(Integer bpVersionNum)
    {
        this.bpVersionNum = bpVersionNum;
    }

    public String toString()
    {
        return "Resource [id=" + id + ", urn=" + urn + ", entityType="
                + entityType + ", bpName=" + bpName + ", bpVersionNum="
                + bpVersionNum +", properties=" + properties + "]";
    }

    public String getId()
    {
        return id;
    }

    public void setCreatedTime(Long epochMilli)
    {
        if (epochMilli != null)
        {
            this.createdTime = Instant.ofEpochMilli(epochMilli);
        }
    }

    public Long getUpdatedTime()
    {
        return updatedTime != null ? updatedTime.toEpochMilli() : null;
    }

    public void setUpdatedTime(Long epochMilli)
    {
        if (epochMilli != null)
        {
            this.updatedTime = Instant.ofEpochMilli(epochMilli);
        }
    }

    public Long getLastCheckedTime()
    {
        return lastCheckedTime != null ? lastCheckedTime.toEpochMilli() : null;
    }

    public void setLastCheckedTime(Long epochMilli)
    {
        if (epochMilli != null)
        {
            this.lastCheckedTime = Instant.ofEpochMilli(epochMilli);
        }
    }

    ///////////////////////////////////
    // INHERITANCE MEHTODS
    //////////////////////////////////

	public Long getCreatedTime()
    {
        return createdTime != null ? createdTime.toEpochMilli() : null;
    }

	public Map<String,Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String,Object> properties) {
		this.properties = properties;
	}
}