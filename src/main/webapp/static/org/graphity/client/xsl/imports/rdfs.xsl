<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2012 Martynas Jusevičius <martynas@graphity.org>

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
    <!ENTITY gc "http://graphity.org/gc#">
    <!ENTITY gp "http://graphity.org/gp#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:gp="&gp;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
exclude-result-prefixes="#all">

    <xsl:template match="rdfs:label | rdfs:comment | rdfs:seeAlso" mode="gc:PropertyListMode"/>

    <xsl:template match="rdfs:label | @rdfs:label" mode="gc:LabelMode">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="rdfs:comment" mode="gc:DescriptionMode">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="rdfs:seeAlso" mode="gc:SidebarNavMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))"/>
	
	<div class="well sidebar-nav">
	    <h2 class="nav-header">
		<xsl:apply-templates select="." mode="gc:InlineMode"/>
	    </h2>

	    <ul class="nav nav-pills nav-stacked">
		<xsl:for-each-group select="key('predicates', $this)" group-by="@rdf:resource">
		    <xsl:sort select="gc:object-label(@rdf:resource)" data-type="text" order="ascending" lang="{$gp:lang}"/>
		    <xsl:apply-templates select="current-group()[1]/@rdf:resource" mode="#current"/>
		</xsl:for-each-group>
	    </ul>
	</div>
    </xsl:template>

    <xsl:template match="rdfs:seeAlso/@rdf:resource" mode="gc:SidebarNavMode">
	<li>
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</li>
    </xsl:template>

    <xsl:template match="rdfs:subClassOf[@rdf:resource = '&foaf;Document']" mode="gc:EditMode">
        <xsl:apply-templates select="." mode="gc:InputMode">
            <xsl:with-param name="type" select="'hidden'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="#current">
            <xsl:with-param name="type" select="'hidden'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="@xml:lang | @rdf:datatype" mode="#current">
            <xsl:with-param name="type" select="'hidden'"/>
        </xsl:apply-templates>        
    </xsl:template>
    
</xsl:stylesheet>