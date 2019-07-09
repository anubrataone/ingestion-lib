package com.att.ingestion.descriptor;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{}foreach" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}foreachkey" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}field" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}var" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}include" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *       &lt;attribute name="description" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="jsonpathmatch" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="jsonmatchvalue" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="update" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="create" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="loadExistingURN" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="skipCms" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="urn" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="preload" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "foreach",
    "foreachkey",
    "field",
    "var",
    "include"
})
@XmlRootElement(name = "blueprint")
public class Blueprint {

    protected List<Foreach> foreach;
    protected List<Foreachkey> foreachkey;
    protected List<Field> field;
    protected List<Var> var;
    protected List<Include> include;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "version", required = true)
    protected int version;
    @XmlAttribute(name = "type", required = true)
    protected String type;
    @XmlAttribute(name = "description", required = true)
    protected String description;
    @XmlAttribute(name = "jsonpathmatch")
    protected String jsonpathmatch;
    @XmlAttribute(name = "jsonmatchvalue")
    protected String jsonmatchvalue;
    @XmlAttribute(name = "update")
    protected Boolean update;
    @XmlAttribute(name = "create")
    protected Boolean create;
    @XmlAttribute(name = "loadExistingURN")
    protected Boolean loadExistingURN;
    @XmlAttribute(name = "skipCms")
    protected Boolean skipCms;
    @XmlAttribute(name = "urn")
    protected String urn;
    @XmlAttribute(name = "preload")
    protected Boolean preload;

    /**
     * Gets the value of the foreach property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the foreach property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getForeach().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Foreach }
     * 
     * 
     */
    public List<Foreach> getForeach() {
        if (foreach == null) {
            foreach = new ArrayList<Foreach>();
        }
        return this.foreach;
    }

    /**
     * Gets the value of the foreachkey property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the foreachkey property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getForeachkey().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Foreachkey }
     * 
     * 
     */
    public List<Foreachkey> getForeachkey() {
        if (foreachkey == null) {
            foreachkey = new ArrayList<Foreachkey>();
        }
        return this.foreachkey;
    }

    /**
     * Gets the value of the field property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the field property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Field }
     * 
     * 
     */
    public List<Field> getField() {
        if (field == null) {
            field = new ArrayList<Field>();
        }
        return this.field;
    }

    /**
     * Gets the value of the var property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the var property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getVar().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Var }
     * 
     * 
     */
    public List<Var> getVar() {
        if (var == null) {
            var = new ArrayList<Var>();
        }
        return this.var;
    }

    /**
     * Gets the value of the include property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the include property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInclude().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Include }
     * 
     * 
     */
    public List<Include> getInclude() {
        if (include == null) {
            include = new ArrayList<Include>();
        }
        return this.include;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }
    /**
     * Gets the value of the version property.
     * 
     */
    public int getVersion() {
        return version;
    }

    /**
     * Sets the value of the version property.
     * 
     */
    public void setVersion(int value) {
        this.version = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(String value) {
        this.description = value;
    }

    /**
     * Gets the value of the jsonpathmatch property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJsonpathmatch() {
        return jsonpathmatch;
    }

    /**
     * Sets the value of the jsonpathmatch property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJsonpathmatch(String value) {
        this.jsonpathmatch = value;
    }

    /**
     * Gets the value of the jsonmatchvalue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJsonmatchvalue() {
        return jsonmatchvalue;
    }

    /**
     * Sets the value of the jsonmatchvalue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJsonmatchvalue(String value) {
        this.jsonmatchvalue = value;
    }

    /**
     * Gets the value of the update property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isUpdate() {
        return update;
    }

    /**
     * Sets the value of the update property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setUpdate(Boolean value) {
        this.update = value;
    }

    /**
     * Gets the value of the create property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isCreate() {
        return create;
    }

    /**
     * Sets the value of the create property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCreate(Boolean value) {
        this.create = value;
    }

    /**
     * Gets the value of the loadExistingURN property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isLoadExistingURN() {
        return loadExistingURN;
    }

    /**
     * Sets the value of the loadExistingURN property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setLoadExistingURN(Boolean value) {
        this.loadExistingURN = value;
    }

    /**
     * Gets the value of the skipCms property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isSkipCms() {
        return skipCms;
    }

    /**
     * Sets the value of the skipCms property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setSkipCms(Boolean value) {
        this.skipCms = value;
    }

    /**
     * Gets the value of the urn property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUrn() {
        return urn;
    }

    /**
     * Sets the value of the urn property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUrn(String value) {
        this.urn = value;
    }

    /**
     * Gets the value of the preload property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isPreload() {
        return preload;
    }

    /**
     * Sets the value of the preload property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setPreload(Boolean value) {
        this.preload = value;
    }

}
