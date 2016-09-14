<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2012 Martynas Jusevičius <martynas@atomgraph.com>

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
    <!ENTITY ac     "http://atomgraph.com/ns/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">    
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY sp     "http://spinrdf.org/sp#">
    <!ENTITY spin   "http://spinrdf.org/spin#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:uuid="java:java.util.UUID"
xmlns:url="&java;java.net.URLDecoder"
exclude-result-prefixes="#all">

    <xsl:output method="xhtml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" doctype-system="http://www.w3.org/MarkUp/DTD/xhtml-rdfa-2.dtd" doctype-public="-//W3C//DTD XHTML+RDFa 1.1//EN" media-type="application/xhtml+xml"/>

    <!-- BLOCK LEVEL -->
    
    <!-- subject -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:Block">
        <div about="{@rdf:about}">
            <xsl:next-match/>
        </div>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:BlockList">
	<div class="well" about="{@rdf:about}">
            <xsl:apply-templates select="." mode="ac:image"/>
            
            <xsl:apply-templates select="." mode="bs2:ModeList"/>

	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>
	    
	    <xsl:apply-templates select="." mode="ac:description"/>

	    <xsl:apply-templates select="." mode="bs2:TypeList"/>            

	    <xsl:if test="@rdf:nodeID">
		<xsl:apply-templates select="." mode="bs2:PropertyList"/>
	    </xsl:if>
	</div>

        <!--
        <div about="{@rdf:about}">
            <xsl:next-match/>
        </div>
        -->
    </xsl:template>

    <xsl:template match="*[rdf:type/@rdf:resource]" mode="bs2:TypeList" priority="1">
        <ul class="inline" rel="&rdf;type">
            <xsl:apply-templates select="rdf:type" mode="#current">
                <xsl:sort select="ac:object-label(@rdf:resource)" data-type="text" order="ascending" lang="{$lang}"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>
    
    <!-- INLINE LEVEL -->
    
    <xsl:template match="@rdf:about" mode="xhtml:Anchor">
	<a href="{.}" title="{.}" resource="{.}">
	    <xsl:if test="substring-after(., concat($request-uri, '#'))">
		<xsl:attribute name="id"><xsl:value-of select="substring-after(., concat($request-uri, '#'))"/></xsl:attribute>
	    </xsl:if>
            <span property="http://purl.org/dc/terms/title">
                <xsl:apply-templates select=".." mode="ac:label"/>
            </span>
	</a>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="xhtml:Anchor">
	<xsl:variable name="this" select="concat(namespace-uri(), local-name())"/>
        
	<span title="{$this}" property="{$this}">
	    <xsl:apply-templates select="." mode="ac:property-label"/>
	</span>
    </xsl:template>

    <!-- object resource -->    
    <xsl:template match="@rdf:resource | sparql:uri" mode="xhtml:Anchor">
	<a href="{.}" title="{.}" resource="{.}">
            <xsl:apply-templates select="." mode="ac:object-label"/>
	</a>
    </xsl:template>
	
    <!-- object blank node (avoid infinite loop) -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID" mode="xhtml:Anchor">
	<xsl:variable name="bnode" select="key('resources', .)[not(@rdf:nodeID = current()/../../@rdf:nodeID)][not(*/@rdf:nodeID = current()/../../@rdf:nodeID)]"/>

	<xsl:choose>
	    <xsl:when test="$bnode">
		<xsl:apply-templates select="$bnode" mode="bs2:Block">
                    <xsl:with-param name="nested" select="true()"/>
                </xsl:apply-templates>
	    </xsl:when>
	    <xsl:otherwise>
		<span id="{.}" title="{.}">
		    <xsl:apply-templates select="." mode="ac:label"/>
		</span>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype] | sparql:literal[@datatype]" mode="xhtml:Anchor">
	<span title="{../@rdf:datatype | @datatype}" datatype="{../@rdf:datatype}">
	    <xsl:value-of select="."/>
	</span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;float'] | text()[../@rdf:datatype = '&xsd;double'] | sparql:literal[@datatype = '&xsd;float'] | sparql:literal[@datatype = '&xsd;double']" priority="1" mode="xhtml:Anchor">
	<span title="{../@rdf:datatype}" datatype="{../@rdf:datatype}">
	    <xsl:value-of select="format-number(., '#####.00')"/>
	</span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;date'] | sparql:literal[@datatype = '&xsd;date']" priority="1" mode="xhtml:Anchor">
	<span title="{../@rdf:datatype}" datatype="{../@rdf:datatype}">
	    <xsl:value-of select="format-date(., '[D] [MNn] [Y]', $lang, (), ())"/>
	</span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;dateTime'] | sparql:literal[@datatype = '&xsd;dateTime']" priority="1" mode="xhtml:Anchor">
	<!-- http://www.w3.org/TR/xslt20/#date-time-examples -->
	<!-- http://en.wikipedia.org/wiki/Date_format_by_country -->
	<span title="{../@rdf:datatype}" datatype="{../@rdf:datatype}">
	    <xsl:value-of select="format-dateTime(., '[D] [MNn] [Y] [H01]:[m01]', $lang, (), ())"/>
	</span>
    </xsl:template>

</xsl:stylesheet>