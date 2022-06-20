<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2020 Martynas Jusevičius <martynas@atomgraph.com>

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java   "http://xml.apache.org/xalan/java/">
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="#all">

    <xsl:import href="../../converters/RDFXML2SVG.xsl"/>
    <xsl:import href="../../group-sort-triples.xsl"/>
    <xsl:import href="../../xml-to-string.xsl"/>
    <xsl:import href="../../functions.xsl"/>
    <xsl:import href="../../imports/default.xsl"/>
    <xsl:import href="../../imports/dbpedia-owl.xsl"/>
    <xsl:import href="../../imports/dc.xsl"/>
    <xsl:import href="../../imports/dct.xsl"/>
    <xsl:import href="../../imports/doap.xsl"/>
    <xsl:import href="../../imports/foaf.xsl"/>
    <xsl:import href="../../imports/ldt.xsl"/>
    <xsl:import href="../../imports/rdf.xsl"/>
    <xsl:import href="../../imports/rdfs.xsl"/>
    <xsl:import href="../../imports/sd.xsl"/>
    <xsl:import href="../../imports/schema.xsl"/>
    <xsl:import href="../../imports/sioc.xsl"/>
    <xsl:import href="../../imports/skos.xsl"/>
    <xsl:import href="../../imports/sp.xsl"/>
    <xsl:import href="imports/default.xsl"/>
    <xsl:import href="imports/dh.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/sp.xsl"/>
    <xsl:import href="resource.xsl"/>
    <xsl:import href="document.xsl"/>
    <xsl:import href="container.xsl"/>
    
    <xsl:include href="layout.xsl"/>
    
</xsl:stylesheet>
