package com.att.ingestion.descriptor;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.quickplay.cms.ingestion.descriptor package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.quickplay.cms.ingestion.descriptor
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Blueprint }
     * 
     */
    public Blueprint createBlueprint() {
        return new Blueprint();
    }

    /**
     * Create an instance of {@link Foreach }
     * 
     */
    public Foreach createForeach() {
        return new Foreach();
    }

    /**
     * Create an instance of {@link Foreachkey }
     * 
     */
    public Foreachkey createForeachkey() {
        return new Foreachkey();
    }

    /**
     * Create an instance of {@link Field }
     * 
     */
    public Field createField() {
        return new Field();
    }

    /**
     * Create an instance of {@link Var }
     * 
     */
    public Var createVar() {
        return new Var();
    }

    /**
     * Create an instance of {@link Include }
     * 
     */
    public Include createInclude() {
        return new Include();
    }

    /**
     * Create an instance of {@link Blueprints }
     * 
     */
    public Blueprints createBlueprints() {
        return new Blueprints();
    }

    /**
     * Create an instance of {@link Map }
     * 
     */
    public Map createMap() {
        return new Map();
    }

    /**
     * Create an instance of {@link Entry }
     * 
     */
    public Entry createEntry() {
        return new Entry();
    }

    /**
     * Create an instance of {@link Group }
     * 
     */
    public Group createGroup() {
        return new Group();
    }

}
