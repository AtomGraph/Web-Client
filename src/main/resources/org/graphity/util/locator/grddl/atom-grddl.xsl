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
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY atom "http://www.w3.org/2005/Atom">
    <!ENTITY atom-owl "http://bblfish.net/work/atom-owl/2006-06-06/#">
    <!ENTITY media "http://search.yahoo.com/mrss/">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
    <!ENTITY dbpedia "http://dbpedia.org/resource/">
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:grddl="http://www.w3.org/2003/g/data-view#"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:atom="&atom;"
xmlns:atom-owl="&atom-owl;"
xmlns:media="&media;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:dbpedia="&dbpedia;"
xmlns:dbpedia-owl="&dbpedia-owl;"
exclude-result-prefixes="xsl xs grddl rdf rdfs atom atom-owl media dct foaf sioc dbpedia dbpedia-owl"
>

    <xsl:output method="xml" indent="yes" encoding="UTF-8" media-type="application/rdf+xml"/>

    <!-- Transforms Atom feeds into RDF/XML descriptions using established vocabularies -->
    <!-- http://bblfish.net/work/atom-owl/2006-06-06/AtomOwl.html -->
    
    <xsl:template match="/">
	<rdf:RDF>
	    <xsl:apply-templates/>
	</rdf:RDF>
    </xsl:template>

    <xsl:template match="atom:feed">
	<atom-owl:Feed rdf:about="{atom:id}">
	    <xsl:apply-templates select="*[not(self::atom:entry)]"/>
	</atom-owl:Feed>
	
	<xsl:apply-templates select="atom:entry"/>
    </xsl:template>

    <xsl:template match="atom:published">
	<dct:issued rdf:datatype="&xsd;dateTime">
	    <xsl:value-of select="."/>
	</dct:issued>
    </xsl:template>

    <xsl:template match="atom:updated">
	<dct:modified rdf:datatype="&xsd;dateTime">
	    <xsl:value-of select="."/>
	</dct:modified>
    </xsl:template>

    <xsl:template match="atom:entry">
	<atom-owl:Entry rdf:about="{atom:id}">
	    <rdf:type rdf:resource="&sioc;Post"/>
	    
	    <xsl:apply-templates/>
	</atom-owl:Entry>
    </xsl:template>

    <xsl:template match="atom:title">
	<dct:title>
	    <xsl:value-of select="."/>
	</dct:title>
    </xsl:template>

    <xsl:template match="atom:content">
	<dct:description>
	    <xsl:value-of select="."/>
	</dct:description>
    </xsl:template>

    <xsl:template match="atom:author">
	<sioc:has_creator>
	    <sioc:UserAccount rdf:about="{atom:uri}">
		<xsl:apply-templates/>
	    </sioc:UserAccount>
	</sioc:has_creator>
    </xsl:template>

    <xsl:template match="atom:name">
	<sioc:name>
	    <xsl:value-of select="."/>
	</sioc:name>
    </xsl:template>

    <xsl:template match="atom:category">
	<dct:subject rdf:resource="&dbpedia;{encode-for-uri(concat(upper-case(substring(@term, 1, 1)), substring(@term, 2)))}"/>
    </xsl:template>
	
    <xsl:template match="atom:logo">
	<foaf:logo rdf:resource="{.}"/>
    </xsl:template>

    <xsl:template match="atom:link[@rel = 'alternate']">
	<dct:hasFormat rdf:resource="{@href}"/>
    </xsl:template>
	
    <!-- ignore other elements, otherwise they will produce unwanted text nodes -->
    <xsl:template match="*"/>

</xsl:stylesheet>