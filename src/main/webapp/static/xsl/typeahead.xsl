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
    <!ENTITY gc "http://client.graphity.org/ontology#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl "http://www.w3.org/2002/07/owl#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sd "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
]>
<xsl:stylesheet
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:ixsl="http://saxonica.com/ns/interactiveXSLT"
xmlns:prop="http://saxonica.com/ns/html-property"
xmlns:style="http://saxonica.com/ns/html-style-property"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:sd="&sd;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="xs prop"
extension-element-prefixes="ixsl"
version="2.0"
xpath-default-namespace="http://www.w3.org/1999/xhtml"
>
    <xsl:param name="base-uri-string" as="xs:string"/>
    <xsl:param name="base-uri" select="xs:anyURI($base-uri-string)" as="xs:anyURI"/>
    <xsl:param name="absolute-path-string" as="xs:string"/>
    <xsl:param name="absolute-path" select="xs:anyURI($absolute-path-string)" as="xs:anyURI"/>
    <xsl:param name="lang" select="'en'" as="xs:string"/>
    <xsl:param name="graph-store-uri" select="resolve-uri('graphs', $base-uri)" as="xs:anyURI"/>
    <!-- <xsl:param name="mode" select="xs:anyURI('&gc;InputMode')" as="xs:anyURI?"/> -->

    <xsl:template name="main">
	<xsl:variable name="graphs-rdf" select="ixsl:call(ixsl:window(), 'loadXML', concat(resolve-uri('graphs', $base-uri), '?limit=100&amp;offset=0'))"/>
	<xsl:variable name="graphs" select="$graphs-rdf/rdf:RDF/*[self::sd:NamedGraph or rdf:type/@rdf:resource = '&sd;NamedGraph']"/>
	<!-- <xsl:message> Graphs: <xsl:copy-of select="$graphs"/></xsl:message> -->
	<xsl:result-document href="#select-graph" method="ixsl:replace-content">
	    <option>Default</option>
	    <xsl:if test="$graphs">
		<optgroup label="Named graphs">
		    <xsl:apply-templates select="$graphs"/>
		</optgroup>
	    </xsl:if>
	</xsl:result-document>
	
	<!--
	<xsl:variable name="properties-rdf" select="ixsl:call(ixsl:window(), 'loadXML', concat(resolve-uri('properties', $base-uri), '?limit=100&amp;offset=0'))"/>
	<xsl:variable name="properties" select="$properties-rdf/rdf:RDF/*[self::rdf:Property or rdf:type/@rdf:resource = '&rdf;Property' or self::owl:ObjectProperty or rdf:type/@rdf:resource = '&owl;ObjectProperty' or self::owl:DatatypeProperty or rdf:type/@rdf:resource = '&owl;DatatypeProperty']"/>
	<xsl:for-each select="ixsl:page()//select[@name = 'pu']">
	    <xsl:variable name="id" select="@id"/>
	    <xsl:result-document href="?select=//*[@id = $id]" method="ixsl:replace-content">
		<option>HELLO!</option>
		<xsl:apply-templates select="$properties"/>
	    </xsl:result-document>
	</xsl:for-each>
	-->
    </xsl:template>

    <xsl:template match="input[contains(@class, 'resource-typeahead')]" mode="ixsl:onkeyup">
	<xsl:choose>
	    <xsl:when test="@prop:value">
		<xsl:call-template name="load-resource-xml">
		    <xsl:with-param name="query" select="@prop:value"/>
		</xsl:call-template>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:result-document href="?select=../../following-sibling::div[1]/select" method="ixsl:replace-content"/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template name="load-resource-xml">
	<xsl:param name="query" as="xs:string"/>
	<xsl:variable name="event" select="ixsl:event()"/>
	<xsl:value-of select="ixsl:call(ixsl:window(), 'loadResourcesXML', $event, $query)"/>
    </xsl:template>

    <xsl:template match="input[contains(@class, 'property-typeahead')]" mode="ixsl:onkeyup">
	<xsl:choose>
	    <xsl:when test="@prop:value">
		<xsl:call-template name="load-property-xml">
		    <xsl:with-param name="query" select="@prop:value"/>
		</xsl:call-template>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:result-document href="?select=following-sibling::*" method="ixsl:replace-content"/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>
  
    <xsl:template name="load-property-xml">
	<xsl:param name="query" as="xs:string"/>
	<xsl:variable name="event" select="ixsl:event()"/>
	<xsl:value-of select="ixsl:call(ixsl:window(), 'loadPropertiesXML', $event, $query)"/>
    </xsl:template>

    <xsl:template match="ixsl:window()" mode="ixsl:onresourceTypeaheadCallback">
	<xsl:variable name="event" select="ixsl:event()"/>
	<xsl:variable name="target" select="ixsl:get($event, 'target')"/>
	<xsl:variable name="select" select="$target/following-sibling::select"/>	
	<xsl:message>CALLBACK! Typeahead &lt;select&gt; @id: <xsl:value-of select="$select/@id"/>
	</xsl:message>
	<xsl:result-document href="#{$select/@id}" method="ixsl:replace-content">
	<!-- <xsl:result-document href="?select=//select[@id = $typeahead-select/@id]" method="ixsl:replace-content"> -->
	    <xsl:variable name="typeahead-xml" select="ixsl:get(ixsl:window(), 'resourcesXML')"/>
	    <xsl:variable name="selected-resources" select="$typeahead-xml/rdf:RDF/*[not(@rdf:nodeID)][not(@rdf:about = resolve-uri('search', $base-uri))][not(starts-with(@rdf:about, concat(resolve-uri('search', $base-uri), '?query=')))]"/>
	    <xsl:apply-templates select="$selected-resources">
		<xsl:sort select="rdfs:label[1]"/>
		<xsl:sort select="dct:title[1]"/>
		<xsl:sort select="foaf:name[1]"/>
		<xsl:sort select="foaf:nick[1]"/>
		<xsl:sort select="sioc:name[1]"/>
		<xsl:sort select="@rdf:about"/>
	    </xsl:apply-templates>
	</xsl:result-document>
    </xsl:template>

    <xsl:template match="ixsl:window()" mode="ixsl:onpropertyTypeaheadCallback">
	<xsl:variable name="event" select="ixsl:event()"/>
	<xsl:variable name="target" select="ixsl:get($event, 'target')"/>
	<xsl:variable name="select" select="$target/following-sibling::select"/>	
	<xsl:message>CALLBACK! Typeahead &lt;select&gt; @id: <xsl:value-of select="$select/@id"/>
	</xsl:message>
	<xsl:result-document href="#{$select/@id}" method="ixsl:replace-content">
	    <xsl:variable name="typeahead-xml" select="ixsl:get(ixsl:window(), 'propertiesXML')"/>
	    <xsl:variable name="selected-resources" select="$typeahead-xml/rdf:RDF/*[self::rdf:Property or rdf:type/@rdf:resource = '&rdf;Property' or self::owl:ObjectProperty or rdf:type/@rdf:resource = '&owl;ObjectProperty' or self::owl:DatatypeProperty or rdf:type/@rdf:resource = '&owl;DatatypeProperty']"/>
	    <xsl:apply-templates select="$selected-resources">
		<xsl:sort select="rdfs:label[1]"/>
		<xsl:sort select="@rdf:about"/>
	    </xsl:apply-templates>
	</xsl:result-document>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]">
	<option value="{@rdf:about}">
	    <xsl:choose>
		<xsl:when test="rdfs:label">
		    <xsl:value-of select="rdfs:label"/>
		</xsl:when>
		<xsl:when test="dct:title">
		    <xsl:value-of select="dct:title"/>
		</xsl:when>
		<xsl:when test="foaf:name">
		    <xsl:value-of select="foaf:name"/>
		</xsl:when>
		<xsl:when test="foaf:givenName and foaf:familyName">
		    <xsl:value-of select="foaf:givenName"/>
		    <xsl:text> </xsl:text>
		    <xsl:value-of select="foaf:familyName"/>
		</xsl:when>
		<xsl:when test="foaf:nick">
		    <xsl:value-of select="foaf:nick"/>
		</xsl:when>
		<xsl:when test="sioc:name">
		    <xsl:value-of select="sioc:name"/>
		</xsl:when>
		<xsl:otherwise>
		    <xsl:value-of select="@rdf:about"/>
		</xsl:otherwise>
	    </xsl:choose>
	    <xsl:text> </xsl:text>
	    [<xsl:value-of select="@rdf:about | @rdf:nodeID"/>]
	</option>
    </xsl:template>

    <xsl:template match="button[contains(@class, 'add-statement')]" mode="ixsl:onclick">
	<xsl:message>Add statement</xsl:message>
	<xsl:result-document href="?select=.." method="ixsl:replace-content">
	    <xsl:call-template name="rdf:Property"/>
	</xsl:result-document>
	<xsl:result-document href="?select=../.." method="ixsl:append-content">
	    <xsl:copy-of select=".."/>
	</xsl:result-document>
    </xsl:template>

    <xsl:template name="rdf:Property"> <!-- match="div[contains(@class, 'control-group')]" -->
	<xsl:message>CONTEXT NODE NAME: <xsl:value-of select="local-name()"/> CLASS: <xsl:value-of select="@class"/></xsl:message>
	<xsl:message>CONTEXT NODE: <xsl:copy-of select="."/></xsl:message>

	<div class="xxx">
	    <input type="text" class="property-typeahead"/>
	    <select name="pu" id="select-{ixsl:call(ixsl:window(), 'generateUUID')}"/>
	</div>

	<button type="button" class="btn btn-small pull-right remove-statement" title="Remove this statement">&#x2715;</button>

	<div class="controls">
	    <ul class="nav nav-tabs">
		<li>
		    <xsl:attribute name="class">ou active</xsl:attribute>
		    <a>Resource</a>
		</li>
		<li>
		    <xsl:attribute name="class">ob</xsl:attribute>
		    <a>Blank node</a>
		</li>
		<li>
		    <xsl:attribute name="class">ol</xsl:attribute>
		    <a>Literal</a>
		</li>
	    </ul>
	</div>

	<div class="controls ou">
	    <input type="text" class="resource-typeahead"/>
	    <span class="help-inline">Filter</span>

	    <select name="ou" class="input-xxlarge" id="select-{ixsl:call(ixsl:window(), 'generateUUID')}"></select>
	    <span class="help-inline">Resource</span>
	</div>
	<div class="controls ob well well-small" style="display: none;"></div>
	<div class="controls ol" style="display: none;">
	    <input type="text" name="ol" class="input-block-level" />
	    <div class="controls-row">
		<input type="text" name="ll" class="span2" />
		<span class="help-inline span3">Language tag</span>
		<input type="text" name="lt" class="span4" />
		<span class="help-inline span3">Datatype URI</span>
	    </div>
	</div>
    </xsl:template>

    <xsl:template match="button[contains(@class, 'remove-statement')]" mode="ixsl:onclick">
	<xsl:message>Remove statement</xsl:message>
	<xsl:for-each select="..">
	    <ixsl:set-attribute name="style:display" select="'none'"/>
	</xsl:for-each>
	<!--
	<xsl:result-document href="?select=../.." method="ixsl:replace-content">
	    <xsl:for-each select="..">
		<xsl:copy-of select="preceding-sibling::*"/>

		<xsl:copy-of select="following-sibling::*"/>
	    </xsl:for-each>
	</xsl:result-document>
	-->
    </xsl:template>

    <xsl:template match="div[contains(@class, 'controls')]/ul[contains(@class, 'nav-tabs')]/li" mode="ixsl:onclick">
	<xsl:variable name="tab-pane-div" select="../../following-sibling::div[tokenize(@class, ' ') = tokenize(current()/@class, ' ')]"/>

	<xsl:for-each select="../li">
	    <!--
	    <xsl:message>
		!<xsl:value-of select="tokenize(@class, ' ')[not(. = 'active')]"/>!
	    </xsl:message>
	    -->
	    <ixsl:set-attribute name="class" select="string-join(tokenize(@class, ' ')[not(. = 'active')], ' ')"/>
	</xsl:for-each>
	<ixsl:set-attribute name="class" select="concat(@class, ' ', 'active')"/>

	<xsl:for-each select="../../following-sibling::div">
	    <ixsl:set-attribute name="style:display" select="'none'"/>
	</xsl:for-each>
	<xsl:for-each select="$tab-pane-div">
	    <ixsl:set-attribute name="style:display" select="'block'"/>
	</xsl:for-each>
	
	<xsl:if test="tokenize(@class, ' ') = 'ob'">
	    <xsl:message>BLANK NODE!</xsl:message>
	    <xsl:if test="not(../../following-sibling::div[tokenize(@class, ' ') = 'ob']/*)">
		<xsl:result-document href="?select=../../following-sibling::*[tokenize(@class, ' ') = 'ob']" method="ixsl:replace-content">
		    <div class="control-group">			
			<xsl:for-each select="../..">
			    <xsl:call-template name="rdf:Property"/>
			</xsl:for-each>
		    </div>

		    <div class="control-group">
			<button type="button" class="btn add-statement" title="Add new statement">&#x271A;</button>
		    </div>
		</xsl:result-document>
	    </xsl:if>
	</xsl:if>
    </xsl:template>

    <!--
    <xsl:template match="form" mode="ixsl:onsubmit">
	<xsl:value-of select="ixsl:call(ixsl:window(), 'alert', 'HELLO!')"/>
	<ixsl:set-attribute name="action" select="concat('?graph=', encode-for-uri('http://test'))"/>
    </xsl:template>
    -->

    <xsl:template match="button[@type = 'submit'][contains(@class, 'create-mode')]" mode="ixsl:onclick">
	<xsl:variable name="graph" select="ancestor::form//select[@name = 'graph']/@prop:value"/>
	<xsl:message>graph: <xsl:value-of select="$graph"/>!</xsl:message>
	<!-- <xsl:value-of select="ixsl:call(ixsl:window(), 'alert', ancestor::form//select[@name = 'graph']/@value)"/> -->
	<xsl:for-each select="ancestor::form">
	    <xsl:choose>
		<xsl:when test="$graph = 'Default'">
		    <ixsl:set-attribute name="action" select="concat($absolute-path, '?mode=', encode-for-uri('&gc;CreateMode'), '&amp;default=true')"/>
		</xsl:when>
		<xsl:otherwise>
		    <ixsl:set-attribute name="action" select="concat($absolute-path, '?mode=', encode-for-uri('&gc;CreateMode'), '&amp;graph=', encode-for-uri($graph))"/>
		</xsl:otherwise>
	    </xsl:choose>
	</xsl:for-each>
    </xsl:template>

    <xsl:template match="select[@name = 'pu']" mode="ixsl:onchange">
	<xsl:message>Selected property: <xsl:value-of select="@prop:value"/></xsl:message>
    </xsl:template>

</xsl:stylesheet>