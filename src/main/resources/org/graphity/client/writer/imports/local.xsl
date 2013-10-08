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
    <!ENTITY sp "http://spinrdf.org/sp#">
    <!ENTITY spin "http://spinrdf.org/spin#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
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

    <xsl:param name="lang" select="'en'" as="xs:string"/>

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>

    <xsl:template match="rdf:type/@rdf:resource" priority="1">
	<span title="{.}" class="btn">
	    <xsl:next-match/>
	</span>
    </xsl:template>

    <!-- subject resource -->
    <xsl:template match="@rdf:about">
	<a href="{.}" title="{.}">
	    <xsl:if test="substring-after(., concat($request-uri, '#'))">
		<xsl:attribute name="id"><xsl:value-of select="substring-after(., concat($request-uri, '#'))"/></xsl:attribute>
	    </xsl:if>	
	    <xsl:apply-templates select="." mode="gc:LabelMode"/>
	</a>
    </xsl:template>

    <!-- object resource -->    
    <xsl:template match="@rdf:resource | sparql:uri">
	<a href="{.}" title="{.}">
	    <xsl:apply-templates select="." mode="gc:LabelMode"/>
	</a>
    </xsl:template>

    <xsl:template match="@rdf:nodeID">
	<span id="{.}" title="{.}">
	    <xsl:apply-templates select="." mode="gc:LabelMode"/>
	</span>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>
	<span title="{$this}">
	    <xsl:apply-templates select="." mode="gc:LabelMode"/>
	</span>
    </xsl:template>
	
    <!-- object blank node (avoid infinite loop) -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID">
	<xsl:variable name="bnode" select="key('resources', .)[not(@rdf:nodeID = current()/../../@rdf:nodeID)][not(*/@rdf:nodeID = current()/../../@rdf:nodeID)]"/>
	
	<xsl:choose>
	    <xsl:when test="$bnode">
		<xsl:apply-templates select="$bnode"/>
	    </xsl:when>
	    <xsl:otherwise>
		<span id="{.}" title="{.}">
		    <xsl:apply-templates select="." mode="gc:LabelMode"/>
		</span>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <!-- object literal -->
    <xsl:template match="text()">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype] | sparql:literal[@datatype]">
	<span title="{../@rdf:datatype | @datatype}">
	    <xsl:value-of select="."/>
	</span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;float'] | text()[../@rdf:datatype = '&xsd;double'] | sparql:literal[@datatype = '&xsd;float'] | sparql:literal[@datatype = '&xsd;double']" priority="1">
	<span title="{../@rdf:datatype}">
	    <xsl:value-of select="format-number(., '#####.00')"/>
	</span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;date'] | sparql:literal[@datatype = '&xsd;date']" priority="1">
	<span title="{../@rdf:datatype}">
	    <xsl:value-of select="format-date(., '[D] [MNn] [Y]', $lang, (), ())"/>
	</span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;dateTime'] | sparql:literal[@datatype = '&xsd;dateTime']" priority="1">
	<!-- http://www.w3.org/TR/xslt20/#date-time-examples -->
	<!-- http://en.wikipedia.org/wiki/Date_format_by_country -->
	<span title="{../@rdf:datatype}">
	    <xsl:value-of select="format-dateTime(., '[D] [MNn] [Y] [H01]:[m01]', $lang, (), ())"/>
	</span>
    </xsl:template>

    <!-- LABEL MODE -->
    
    <!-- subject -->
    <xsl:template match="@rdf:about[contains(., '#') and not(ends-with(., '#'))]" mode="gc:LabelMode" priority="3">
	<xsl:variable name="label" select="substring-after(., '#')"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about[string-length(tokenize(., '/')[last()]) &gt; 0]" mode="gc:LabelMode" priority="2">
	<xsl:variable name="label" select="translate(url:decode(tokenize(., '/')[last()], 'UTF-8'), '_', ' ')"/>
	<xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:LabelMode">
	<xsl:value-of select="."/>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[key('resources', concat(namespace-uri(), local-name()), document(namespace-uri()))/@rdf:about]" mode="gc:LabelMode" priority="4">
	<xsl:apply-templates select="key('resources', concat(namespace-uri(), local-name()), document(namespace-uri()))/@rdf:about" mode="gc:LabelMode"/>
    </xsl:template>
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[key('resources', concat(namespace-uri(), local-name()))/@rdf:about]" mode="gc:LabelMode" priority="3">
	<xsl:apply-templates select="key('resources', concat(namespace-uri(), local-name()))/@rdf:about" mode="gc:LabelMode"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[contains(concat(namespace-uri(), local-name()), '#') and not(ends-with(concat(namespace-uri(), local-name()), '#'))]" mode="gc:LabelMode" priority="2">
	<xsl:value-of select="substring-after(concat(namespace-uri(), local-name()), '#')"/>
    </xsl:template>
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[string-length(tokenize(concat(namespace-uri(), local-name()), '/')[last()]) &gt; 0]" mode="gc:LabelMode" priority="1">
	<xsl:variable name="this" select="concat(namespace-uri(), local-name())" as="xs:string"/>
	<xsl:value-of select="translate(url:decode(tokenize($this, '/')[last()], 'UTF-8'), '_', ' ')"/>	
    </xsl:template>
	
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:LabelMode">
	<xsl:value-of select="concat(namespace-uri(), local-name())"/>
    </xsl:template>

    <!-- object -->
    <xsl:template match="@rdf:resource[key('resources', ., document(gc:document-uri(.)))/@rdf:about] | @rdf:resource[key('resources', ., document(gc:document-uri(.)))/@rdf:nodeID] | sparql:uri[key('resources', ., document(gc:document-uri(.)))/@rdf:about] | sparql:uri[key('resources', ., document(gc:document-uri(.)))/@rdf:nodeID]" mode="gc:LabelMode" priority="4">
	<xsl:apply-templates select="key('resources', ., document(gc:document-uri(.)))/@rdf:about" mode="gc:LabelMode"/>
    </xsl:template>

    <xsl:template match="@rdf:resource[key('resources', ., document(gc:document-uri(.)))/@rdf:nodeID] | sparql:uri[key('resources', ., document(gc:document-uri(.)))/@rdf:nodeID]" mode="gc:LabelMode" priority="4">
	<xsl:apply-templates select="key('resources', ., document(gc:document-uri(.)))/@rdf:nodeID" mode="gc:LabelMode"/>
    </xsl:template>

    <xsl:template match="@rdf:resource[key('resources', .)/@rdf:about] | sparql:uri[key('resources', .)/@rdf:about]" mode="gc:LabelMode" priority="3">
	<xsl:apply-templates select="key('resources', .)/@rdf:about" mode="gc:LabelMode"/>
    </xsl:template>

    <xsl:template match="@rdf:resource[key('resources', .)/@rdf:nodeID] | sparql:uri[key('resources', .)/@rdf:nodeID]" mode="gc:LabelMode" priority="3">
	<xsl:apply-templates select="key('resources', .)/@rdf:nodeID" mode="gc:LabelMode"/>
    </xsl:template>

    <xsl:template match="@rdf:resource[contains(., '#') and not(ends-with(., '#'))] | sparql:uri[contains(., '#') and not(ends-with(., '#'))]" mode="gc:LabelMode" priority="2">
	<xsl:value-of select="substring-after(., '#')"/>
    </xsl:template>

    <xsl:template match="@rdf:resource[string-length(tokenize(., '/')[last()]) &gt; 0] | sparql:uri[string-length(tokenize(., '/')[last()]) &gt; 0]" mode="gc:LabelMode" priority="1">
	<xsl:value-of select="translate(url:decode(tokenize(., '/')[last()], 'UTF-8'), '_', ' ')"/>
    </xsl:template>

    <xsl:template match="@rdf:resource | sparql:uri" mode="gc:LabelMode">
	<xsl:value-of select="."/>
    </xsl:template>

    <!-- DESCRIPTION MODE -->
    
    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:DescriptionMode"/>

    <!-- HEADER MODE -->
    
    <xsl:template match="@rdf:about[. = $absolute-path]" mode="gc:HeaderMode">
	<div class="btn-group pull-right">
	    <xsl:if test="$query-res/sp:text">
		<a href="{resolve-uri('sparql', $base-uri)}?query={encode-for-uri($query-res/sp:text)}" class="btn">SPARQL</a>
	    </xsl:if>
	    <xsl:apply-templates select="." mode="gc:MediaTypeSelectMode"/>
	</div>

	<h1 class="page-header">
	    <xsl:apply-templates select="."/>
	</h1>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:HeaderMode">
	<h2>
	    <xsl:apply-templates select="."/>
	</h2>
    </xsl:template>

    <xsl:template match="rdf:type" mode="gc:HeaderMode">
	<li>
	    <xsl:apply-templates select="@rdf:resource | @rdf:nodeID"/>
	</li>
    </xsl:template>

    <xsl:template match="@rdf:about" mode="gc:MediaTypeSelectMode">
	<xsl:if test="$query-res/sp:text">
	    <a href="{resolve-uri('sparql', $base-uri)}?query={encode-for-uri($query-res/sp:text)}" class="btn">SPARQL</a>
	</xsl:if>
    </xsl:template>

    <!-- IMAGE MODE -->

    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:ImageMode"/>
    
    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:ParaImageMode"/>

    <!-- PROPERTY LIST MODE -->
    
    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:PropertyListMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>

	<xsl:if test="not(preceding-sibling::*[concat(namespace-uri(), local-name()) = $this])">
	    <dt>
		<xsl:apply-templates select="."/>
	    </dt>
	</xsl:if>
	<dd>
	    <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="gc:PropertyListMode"/>
	</dd>
    </xsl:template>
    
    <xsl:template match="node() | @rdf:resource" mode="gc:PropertyListMode">
	<xsl:apply-templates select="."/>
    </xsl:template>

    <!-- include blank nodes recursively but avoid infinite loop -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID" mode="gc:PropertyListMode">
	<xsl:variable name="bnode" select="key('resources', .)[not(@rdf:nodeID = current()/../../@rdf:nodeID)][not(*/@rdf:nodeID = current()/../../@rdf:nodeID)]"/>

	<xsl:choose>
	    <xsl:when test="$bnode">
		<xsl:apply-templates select="$bnode"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:apply-templates select="."/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <!-- TABLE HEADER MODE -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:TableHeaderMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>

	<th>
	    <span title="{$this}">
		<xsl:apply-templates select="." mode="gc:LabelMode"/>
	    </span>
	</th>
    </xsl:template>

    <!-- TABLE MODE -->
    
    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:TableMode">
	<td>
	    <xsl:apply-templates select="."/>
	</td>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:TableMode"/>

    <!-- apply properties that match lang() -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[lang($lang)]" mode="gc:TableMode" priority="1">
	<td>
	    <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>
	</td>
    </xsl:template>
    
    <!-- apply the first one in the group if there's no lang() match -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[not(../*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))][lang($lang)])][not(preceding-sibling::*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))])]" mode="gc:TableMode" priority="1">
	<td>
	    <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>
	</td>
    </xsl:template>

    <!-- SIDEBAR NAV MODE -->
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:SidebarNavMode"/>

    <!-- INPUT MODE -->
    
    <xsl:template name="gc:InputTemplate">
	<xsl:param name="name" as="xs:string"/>
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="style" as="xs:string?"/>
	<xsl:param name="value" as="xs:string?"/>
	<xsl:param name="rows" as="xs:integer?"/>

	<xsl:choose>
	    <!-- special case to give more input space for object literals -->
	    <xsl:when test="(not($type) and string-length($value) &gt; 100) or $rows">
		<textarea name="{$name}">
		    <xsl:if test="$id">
			<xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
		    </xsl:if>
		    <xsl:if test="$class">
			<xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
		    </xsl:if>
		    <xsl:if test="$style">
			<xsl:attribute name="style"><xsl:value-of select="$style"/></xsl:attribute>
		    </xsl:if>
		    <xsl:if test="$rows">
			<xsl:attribute name="rows"><xsl:value-of select="$rows"/></xsl:attribute>
		    </xsl:if>
		    
		    <xsl:value-of select="$value"/>
		</textarea>
	    </xsl:when>
	    <xsl:otherwise>
		<input type="{$type}" name="{$name}">
		    <xsl:if test="$id">
			<xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
		    </xsl:if>
		    <xsl:if test="$class">
			<xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
		    </xsl:if>
		    <xsl:if test="$style">
			<xsl:attribute name="style"><xsl:value-of select="$style"/></xsl:attribute>
		    </xsl:if>
		    <xsl:if test="$value">
			<xsl:attribute name="value"><xsl:value-of select="$value"/></xsl:attribute>
		    </xsl:if>
		</input>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <!-- subject resource -->
    <xsl:template match="@rdf:about" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" select="'input-xxlarge'" as="xs:string?"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'su'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- subject blank node -->
    <xsl:template match="@rdf:nodeID" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'sb'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" select="'input-xxlarge'" as="xs:string?"/>
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'pu'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="value" select="$this"/>
	</xsl:call-template>
    </xsl:template>
    
    <!-- object resource -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:resource" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" select="'input-xxlarge'" as="xs:string?"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'ou'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- object blank node -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'ob'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- object literal -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/text()" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'ol'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- datatype -->
    <xsl:template match="@rdf:datatype" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" select="'input-xlarge'" as="xs:string?"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'lt'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- language tag -->
    <xsl:template match="@xml:lang" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" select="'input-mini'" as="xs:string?"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'ll'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- EDIT MODE -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:EditMode">
	<xsl:param name="pu" select="concat(namespace-uri(), local-name())" as="xs:string"/>
	<xsl:param name="constraint-violations" as="element()*"/>
	<xsl:variable name="ranges" select="rdfs:range(xs:anyURI($pu))"/>

	<div class="control-group">
	    <div class="xxx">
		<input type="text" class="property-typeahead"/>
		<select name="pu" id="select-{uuid:randomUUID()}">
		    <option value="{$pu}" selected="selected">
			<xsl:apply-templates select="." mode="gc:LabelMode"/>
			<xsl:text> </xsl:text>
			[<xsl:value-of select="$pu"/>]
		    </option>
		</select>
	    </div>

	    <button type="button" class="btn btn-small pull-right remove-statement" title="Remove this statement">&#x2715;</button>

	    <xsl:variable name="types" as="xs:string*">
		<xsl:choose>
		    <xsl:when test="$ranges = '&rdfs;Literal'">
			<xsl:sequence select="('ol')"/>
		    </xsl:when>
		    <xsl:when test="$ranges = ('&rdfs;Resource', '&owl;Thing')">
			<xsl:sequence select="('ou', 'ob')"/>
		    </xsl:when>
		    <xsl:otherwise>
			<xsl:sequence select="('ou', 'ob', 'ol')"/>
		    </xsl:otherwise>
		</xsl:choose>
	    </xsl:variable>
	    
	    <div class="controls">
		<xsl:if test="count($types) &gt; 1">
		    <ul class="nav nav-tabs">
			<xsl:if test="$types = 'ou'">
			    <li>
				<xsl:attribute name="class">ou<xsl:if test="@rdf:resource or self::rdf:RDF"> active</xsl:if></xsl:attribute>
				<a>Resource</a>
			    </li>
			</xsl:if>
			<xsl:if test="$types = 'ob'">
			    <li>
				<xsl:attribute name="class">ob<xsl:if test="@rdf:nodeID"> active</xsl:if></xsl:attribute>
				<a>Blank node</a>
			    </li>
			</xsl:if>
			<xsl:if test="$types = 'ol'">
			    <li>
				<xsl:attribute name="class">ol<xsl:if test="text()"> active</xsl:if></xsl:attribute>
				<a>Literal</a>
			    </li>
			</xsl:if>
		    </ul>
		</xsl:if>
	    </div>

	    <div class="controls ou">
		<xsl:if test="not(@rdf:resource or self::rdf:RDF)">
		    <xsl:attribute name="style">display: none;</xsl:attribute>
		</xsl:if>

		<input type="text" class="resource-typeahead"/>
		<span class="help-inline">Filter</span>
		<br/>
		<select name="ou" class="input-xxlarge" id="select-{uuid:randomUUID()}">
		    <xsl:if test="@rdf:resource">
			<option value="{@rdf:resource}">
			    <xsl:apply-templates select="@rdf:resource" mode="gc:LabelMode"/>
			    <xsl:text> </xsl:text>
			    [<xsl:value-of select="@rdf:resource"/>]
			</option>
		    </xsl:if>
		</select>
		<!--
		<xsl:call-template name="gc:ObjectTemplate">
		    <xsl:with-param name="name" select="'ou'"/>
		    <xsl:with-param name="class" select="'input-xxlarge'"/>
		</xsl:call-template>
		-->
		<span class="help-inline">Resource</span>
		<!--
		<xsl:for-each select="$constraint-violations[spin:violationPath/@rdf:resource = '&foaf;knows'][1]">
		    <span class="help-inline">
			<xsl:value-of select="rdfs:label"/>
		    </span>
		</xsl:for-each>
		-->
		<!--
		<br/>
		<input type="file" name="file"/>
		-->
	    </div>
	    <div class="controls ob well well-small">
		<xsl:if test="not(@rdf:nodeID)">
		    <xsl:attribute name="style">display: none;</xsl:attribute>
		</xsl:if>

		<xsl:variable name="bnode" select="key('resources', @rdf:nodeID)[not(@rdf:nodeID = current()/../@rdf:nodeID)]"/>
		<xsl:if test="$bnode">
		    <xsl:apply-templates select="$bnode" mode="gc:EditMode"/>
		    
		    <!--
		    <div class="control-group">
			<button type="button" class="btn add-statement" title="Add new statement">&#x271A;</button>
		    </div>
		    -->
		</xsl:if>
	    </div>
	    <div class="controls ol">
		<xsl:if test="not(text())">
		    <xsl:attribute name="style">display: none;</xsl:attribute>
		</xsl:if>

		<div class="controls-row">
		    <xsl:call-template name="gc:ObjectTemplate">
			<xsl:with-param name="name" select="'ol'"/>
			<xsl:with-param name="class" select="'span10'"/>
		    </xsl:call-template>
		    <span class="help-inline span2">Literal</span>
		</div>
		<div class="controls-row">
		    <xsl:call-template name="gc:ObjectTypeTemplate">
			<xsl:with-param name="name" select="'ll'"/>
			<xsl:with-param name="class" select="'span2'"/>
		    </xsl:call-template>
		    <span class="help-inline span3">Language tag</span>
		    <xsl:call-template name="gc:ObjectTypeTemplate">
			<xsl:with-param name="name" select="'lt'"/>
			<xsl:with-param name="class" select="'span3'"/>
		    </xsl:call-template>
		    <span class="help-inline span3">Datatype URI</span>
		</div>
	    </div>
	</div>
    </xsl:template>

    <xsl:template name="gc:SubjectTemplate">
	<xsl:param name="name" select="if (@rdf:about) then 'su' else 'sb'" as="xs:string"/>
	<xsl:param name="value" select="if ($name = 'su') then @rdf:about else @rdf:nodeID"/>
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" select="concat('input-', uuid:randomUUID())" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="style" as="xs:string?"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="name" select="$name"/>
	    <xsl:with-param name="value" select="$value"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="style" select="$style"/>
	</xsl:call-template>
    </xsl:template>

    <xsl:template name="gc:PropertyTemplate">
	<xsl:param name="value" select="concat(namespace-uri(), local-name())"/>
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" select="concat('input-', uuid:randomUUID())" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="style" as="xs:string?"/>
	<xsl:variable name="name" select="'pu'" as="xs:string"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="name" select="$name"/>
	    <xsl:with-param name="value" select="$value"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="style" select="$style"/>
	</xsl:call-template>
    </xsl:template>

    <xsl:template name="gc:ObjectTemplate">
	<xsl:param name="name" select="if (@rdf:resource) then 'ou' else (if (@rdf:nodeID) then 'ob' else 'ol')" as="xs:string"/>
	<xsl:param name="value" select="if ($name = 'ou') then @rdf:resource else (if ($name = 'ob') then @rdf:nodeID else text())"/>
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" select="concat('input-', uuid:randomUUID())" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="style" as="xs:string?"/>
	<xsl:param name="rows" as="xs:integer?"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="name" select="$name"/>
	    <xsl:with-param name="value" select="$value"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="style" select="$style"/>
	    <xsl:with-param name="rows" select="$rows"/>
	</xsl:call-template>
    </xsl:template>

    <xsl:template name="gc:ObjectTypeTemplate">
	<xsl:param name="name" select="if (@xml:lang) then 'll' else 'lt'" as="xs:string"/>
	<xsl:param name="value" select="if ($name = 'll') then @xml:lang else @rdf:datatype"/>
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" select="concat('input-', uuid:randomUUID())" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="style" as="xs:string?"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="name" select="$name"/>
	    <xsl:with-param name="value" select="$value"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="style" select="$style"/>
	</xsl:call-template>
    </xsl:template>

    <xsl:template name="gc:ControlLabelTemplate">
	<xsl:param name="value" select="concat(namespace-uri(), local-name())"/>
	<xsl:param name="for" as="xs:string?"/>
	<xsl:param name="constraint-violations" as="element()*"/>

	<xsl:if test="$constraint-violations/spin:violationPath/@rdf:resource = $value">
	    <xsl:attribute name="class">control-group warning</xsl:attribute>
	</xsl:if>
	    
	<label class="control-label">
	    <xsl:if test="$for">
		<xsl:attribute name="for"><xsl:value-of select="$for"/></xsl:attribute>
	    </xsl:if>
	    <xsl:apply-templates select="key('resources', $value, document(gc:document-uri(xs:anyURI($value))))/@rdf:about" mode="gc:LabelMode"/>
	</label>
	<xsl:call-template name="gc:PropertyTemplate">
	    <xsl:with-param name="type" select="'hidden'"/>
	    <xsl:with-param name="value" select="$value"/>
	</xsl:call-template>
    </xsl:template>

    <xsl:template name="gc:ObjectControlsTemplate">
	<xsl:param name="pu" select="concat(namespace-uri(), local-name())"/>
	<xsl:param name="types" as="xs:string+"/>
	<xsl:param name="ou" select="@rdf:resource" as="xs:string?"/>
	<xsl:param name="ob" select="@rdf:nodeID" as="xs:string?"/>
	<xsl:param name="ol" select="text()" as="xs:string?"/>
	<xsl:param name="ll" select="@xml:lang" as="xs:string?"/>
	<xsl:param name="lt" select="@rdf:datatype" as="xs:string?"/>
	<xsl:param name="id" select="concat('input-', uuid:randomUUID())" as="xs:string"/>
	<xsl:param name="constraint-violations" as="element()*"/>

	<xsl:if test="$types = 'ou'">
	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'ou'"/>
		<xsl:with-param name="id" select="$id"/>
		<xsl:with-param name="value" select="$ou"/>
	    </xsl:call-template>
	</xsl:if>
	<xsl:if test="$types = 'ob'">
	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'ob'"/>
		<xsl:with-param name="id" select="$id"/>
		<xsl:with-param name="value" select="$ob"/>
	    </xsl:call-template>
	</xsl:if>
	<xsl:if test="$types = 'ol'">
	    <!-- <div class="controls-row"> -->
		<xsl:call-template name="gc:InputTemplate">
		    <xsl:with-param name="name" select="'ol'"/>
		    <xsl:with-param name="id" select="$id"/>
		    <xsl:with-param name="value" select="$ol"/>
		    <xsl:with-param name="class" select="'span4'"/>
		</xsl:call-template>
		<span class="help-inline span2">Literal</span>

		<xsl:if test="$types = 'll'">
		    <xsl:call-template name="gc:InputTemplate">
			<xsl:with-param name="name" select="'ll'"/>
			<xsl:with-param name="value" select="$ll"/>
			<xsl:with-param name="class" select="'span2'"/>
		    </xsl:call-template>
		    <span class="help-inline span3">Language tag</span>
		</xsl:if>
		<xsl:if test="$types = 'lt'">
		    <xsl:call-template name="gc:InputTemplate">
			<xsl:with-param name="name" select="'lt'"/>
			<xsl:with-param name="value" select="$lt"/>
			<xsl:with-param name="class" select="'span3'"/>
		    </xsl:call-template>
		    <span class="help-inline span3">Datatype URI</span>
		</xsl:if>
		
		<xsl:if test="$constraint-violations[spin:violationPath/@rdf:resource = $pu]">
		    <span class="help-inline">
			<xsl:for-each select="$constraint-violations[spin:violationPath/@rdf:resource = $pu][1]">
			    <xsl:value-of select="rdfs:label"/>
			</xsl:for-each>
		    </span>
		</xsl:if>
	    <!-- </div> -->
	</xsl:if>
    </xsl:template>

</xsl:stylesheet>