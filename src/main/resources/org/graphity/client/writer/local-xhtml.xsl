<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY gc "http://client.graphity.org/ontology#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl "http://www.w3.org/2002/07/owl#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY ldp "http://www.w3.org/ns/ldp#">
    <!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
    <!ENTITY skos "http://www.w3.org/2004/02/skos/core#">
    <!ENTITY sp "http://spinrdf.org/sp#">
    <!ENTITY spin "http://spinrdf.org/spin#">
    <!ENTITY sd "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY list "http://jena.hpl.hp.com/ARQ/list#">
    <!ENTITY xhv "http://www.w3.org/1999/xhtml/vocab#">
    <!ENTITY void "http://rdfs.org/ns/void#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:ldp="&ldp;"
xmlns:geo="&geo;"
xmlns:dbpedia-owl="&dbpedia-owl;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:skos="&skos;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:sd="&sd;"
xmlns:list="&list;"
xmlns:xhv="&xhv;"
xmlns:void="&void;"
xmlns:url="&java;java.net.URLDecoder"
exclude-result-prefixes="#all">

    <xsl:import href="imports/local.xsl"/>
    <xsl:import href="imports/dbpedia-owl.xsl"/>
    <xsl:import href="imports/dc.xsl"/>
    <xsl:import href="imports/dct.xsl"/>
    <xsl:import href="imports/doap.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/owl.xsl"/>
    <xsl:import href="imports/rdf.xsl"/>
    <xsl:import href="imports/rdfs.xsl"/>
    <xsl:import href="imports/sd.xsl"/>
    <xsl:import href="imports/sioc.xsl"/>
    <xsl:import href="imports/skos.xsl"/>
    <xsl:import href="imports/void.xsl"/>
    <xsl:import href="layout.xsl"/>

</xsl:stylesheet>