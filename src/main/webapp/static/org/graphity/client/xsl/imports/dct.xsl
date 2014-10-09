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
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY dct "http://purl.org/dc/terms/">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:dct="&dct;"
exclude-result-prefixes="#all">

    <xsl:template match="dct:title | dct:description | dct:subject" mode="gc:PropertyListMode"/>
    
    <xsl:template match="dct:title | @dct:title" mode="gc:LabelMode">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="dct:description" mode="gc:DescriptionMode">
        <xsl:value-of select="."/>
    </xsl:template>
    
    <xsl:template match="dct:subject" mode="gc:SidebarNavMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))"/>
	
	<div class="well sidebar-nav">
	    <h2 class="nav-header">
		<xsl:apply-templates select="." mode="gc:InlineMode"/>
	    </h2>
		
	    <!-- TO-DO: fix for a single resource! -->
	    <ul class="nav nav-pills nav-stacked">
		<xsl:for-each-group select="key('predicates', $this)" group-by="@rdf:resource">
		    <xsl:sort select="gc:object-label(@rdf:resource)" data-type="text" order="ascending" lang="{$lang}"/>
		    <xsl:apply-templates select="current-group()[1]/@rdf:resource" mode="#current"/>
		</xsl:for-each-group>
	    </ul>
	</div>
    </xsl:template>

    <xsl:template match="dct:subject/@rdf:resource" mode="gc:SidebarNavMode">
	<li>
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</li>
    </xsl:template>

    <xsl:template match="dct:created | dct:modified | dct:issued" mode="gc:InlinePropertyListMode">
        <dl class="pull-left" style="margin: 0; margin-right: 1em">
            <dt>
                <xsl:apply-templates select="." mode="gc:InlineMode"/>
            </dt>
            <dd>
                <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="gc:InlineMode"/>
            </dd>
        </dl>
    </xsl:template>

</xsl:stylesheet>