/*
 * Namespaces.java
 *
 * Created on Tre�iadienis, 2007, Vasario 7, 16.49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package model;

/**
 * Keeps commonly used RDF namespaces.
 * @author Pumba
 */
public final class Namespaces
{
    
    //public static String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    //public static String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
    //public static String XSD_NS = "http://www.w3.org/2001/XMLSchema#";
    public static final String HOST_NS = "http://localhost:8080/";
    //public static final String WEB_DATA_NS = "http://www.xml.lt/webpage-data.owl#";
    public static final String SYS_DATA_NS = "http://www.xml.lt/system-data.owl#";
    public static final String DOM_DATA_NS = HOST_NS + "Resource/"; //"http://www.xml.lt/domain-data.owl#";
    public static final String RES_ONT_NS = HOST_NS + "Ontology/Resources/";
    public static final String SYS_ONT_NS = HOST_NS + "Ontology/System/";
    public static final String DOM_ONT_NS = HOST_NS + "Ontology/Domain/";
    public static final String GEO_NS = "http://www.w3.org/2003/01/geo/wgs84_pos#";
    public static final String SPARQL_NS = "http://www.w3.org/2005/sparql-results#";
    public static final String FOAF_NS = "http://xmlns.com/foaf/0.1/";
    public static final String SIOC_NS = "http://rdfs.org/sioc/ns#";
    public static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
    
}
