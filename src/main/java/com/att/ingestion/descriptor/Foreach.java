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
 *         &lt;element ref="{}blueprint" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{}include" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="jsonpath" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="value" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="forceloop" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="splitter" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="removeDuplicates" type="{http://www.w3.org/2001/XMLSchema}boolean" />
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
    "blueprint",
    "include"
})
@XmlRootElement(name = "foreach")
public class Foreach {

    protected List<Foreach> foreach;
    protected List<Foreachkey> foreachkey;
    protected List<Field> field;
    protected List<Var> var;
    protected List<Blueprint> blueprint;
    protected List<Include> include;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "jsonpath")
    protected String jsonpath;
    @XmlAttribute(name = "value")
    protected String value;
    @XmlAttribute(name = "forceloop")
    protected Boolean forceloop;
    @XmlAttribute(name = "splitter")
    protected String splitter;
    @XmlAttribute(name = "removeDuplicates")
    protected Boolean removeDuplicates;

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
     * Gets the value of the blueprint property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the blueprint property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBlueprint().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Blueprint }
     * 
     * 
     */
    public List<Blueprint> getBlueprint() {
        if (blueprint == null) {
            blueprint = new ArrayList<Blueprint>();
        }
        return this.blueprint;
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
     * Gets the value of the jsonpath property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getJsonpath() {
        return jsonpath;
    }

    /**
     * Sets the value of the jsonpath property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setJsonpath(String value) {
        this.jsonpath = value;
    }

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the forceloop property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isForceloop() {
        return forceloop;
    }

    /**
     * Sets the value of the forceloop property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setForceloop(Boolean value) {
        this.forceloop = value;
    }

    /**
     * Gets the value of the splitter property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSplitter() {
        return splitter;
    }

    /**
     * Sets the value of the splitter property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSplitter(String value) {
        this.splitter = value;
    }

    /**
     * Gets the value of the removeDuplicates property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isRemoveDuplicates() {
        return removeDuplicates;
    }

    /**
     * Sets the value of the removeDuplicates property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setRemoveDuplicates(Boolean value) {
        this.removeDuplicates = value;
    }

}
