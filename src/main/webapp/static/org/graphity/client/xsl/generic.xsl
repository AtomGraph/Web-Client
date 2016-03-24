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
    <!ENTITY java   "http://xml.apache.org/xalan/java/">
    <!ENTITY gp     "http://graphity.org/gp#">
    <!ENTITY gc     "http://graphity.org/gc#">
    <!ENTITY g      "http://graphity.org/g#">
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
xmlns:gc="&gc;"
xmlns:gp="&gp;"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:url="&java;java.net.URLDecoder"
exclude-result-prefixes="#all">

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>

    <!-- LABEL MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:LabelMode">
        <xsl:variable name="labels" as="xs:string*">
            <xsl:variable name="lang-labels" as="xs:string*">
                <xsl:apply-templates select="*[lang($gp:lang)]" mode="#current"/>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="not(empty($lang-labels))">
                    <xsl:sequence select="$lang-labels"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="#current"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="not(empty($labels))">
                <xsl:value-of select="concat(upper-case(substring($labels[1], 1, 1)), substring($labels[1], 2))"/>
            </xsl:when>
            <xsl:when test="contains(@rdf:about, '#') and not(ends-with(@rdf:about, '#'))">
                <xsl:variable name="label" select="substring-after(@rdf:about, '#')"/>
                <xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
            </xsl:when>
            <xsl:when test="string-length(tokenize(@rdf:about, '/')[last()]) &gt; 0">
                <xsl:variable name="label" use-when="function-available('url:decode')" select="translate(url:decode(tokenize(@rdf:about, '/')[last()], 'UTF-8'), '_', ' ')"/>
                <xsl:variable name="label" use-when="not(function-available('url:decode'))" select="translate(tokenize(@rdf:about, '/')[last()], '_', ' ')"/>
                <xsl:value-of select="concat(upper-case(substring($label, 1, 1)), substring($label, 2))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="@rdf:about | @rdf:nodeID"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- DESCRIPTION MODE -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:DescriptionMode">
        <xsl:variable name="descriptions" as="xs:string*">
            <xsl:variable name="lang-descriptions" as="xs:string*">
                <xsl:apply-templates select="*[lang($gp:lang)]" mode="#current"/>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="not(empty($lang-descriptions))">
                    <xsl:sequence select="$lang-descriptions"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates mode="#current"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="not(empty($descriptions))">
            <p>
                <xsl:copy-of select="substring($descriptions[1], 1, 300)"/>
            </p>
        </xsl:if>
    </xsl:template>

    <!-- OPTION MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="gc:OptionMode">
        <xsl:param name="selected" as="xs:boolean?"/>
        <xsl:param name="disabled" as="xs:boolean?"/>

        <option value="{@rdf:about | @rdf:nodeID}">
            <xsl:if test="$selected">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:if test="$disabled">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="." mode="gc:LabelMode"/>
        </option>
    </xsl:template>

    <!-- IMAGE MODE -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:ImageMode"/>
        
    <!-- TABLE MODE -->

    <xsl:template match="rdf:RDF" mode="gc:TableMode">
        <xsl:param name="selected-resources" as="element()*" tunnel="yes"/>
	<xsl:param name="predicates" as="element()*">
            <!-- <xsl:for-each-group select="$selected-resources/* | key('resources', $selected-resources/foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))/*" group-by="concat(namespace-uri(), local-name())"> -->
	    <xsl:for-each-group select="$selected-resources/*" group-by="concat(namespace-uri(), local-name())">
		<xsl:sort select="gc:property-label(.)" order="ascending" lang="{$gp:lang}"/>
		<xsl:apply-templates select="current-group()[1]" mode="gc:TablePredicateMode"/>
            </xsl:for-each-group>
	</xsl:param>

	<table class="table table-bordered table-striped">
	    <thead>
		<tr>
		    <th>
			<xsl:apply-templates select="key('resources', '&rdfs;Resource', document('&rdfs;'))" mode="gc:LabelMode"/>
		    </th>
		    <xsl:apply-templates select="$predicates" mode="gc:TableHeaderMode"/>
		</tr>
	    </thead>
	    <tbody>
		<xsl:apply-templates select="$selected-resources" mode="#current">
		    <xsl:with-param name="predicates" select="$predicates" tunnel="yes"/>
                    <xsl:sort select="gc:label(.)" lang="{$gp:lang}"/>
                </xsl:apply-templates>
	    </tbody>
	</table>
    </xsl:template>

    <xsl:template match="*[*][@rdf:about or @rdf:nodeID]" mode="gc:TableMode">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="predicates" as="element()*" tunnel="yes"/>

	<tr>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

	    <xsl:apply-templates select="$predicates" mode="gc:TableCellMode">
                <xsl:with-param name="resource" select="."/>
            </xsl:apply-templates>
	</tr>
    </xsl:template>

    <!--
    <xsl:template match="*[key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))]/*" mode="gc:TablePredicateMode" priority="1"/>

    <xsl:template match="*[key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))]" mode="gc:TableMode" priority="1">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/(@rdf:resource, @rdf:nodeID))" mode="#current"/>
    </xsl:template>
    -->
    
    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:TableMode">
	<td>
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</td>
    </xsl:template>

    <!-- INLINE MODE -->
    
    <!-- subject resource -->
    <xsl:template match="@rdf:about" mode="gc:InlineMode">
	<a href="{.}" title="{.}">
            <!--
	    <xsl:if test="substring-after(., concat($g:requestUri, '#'))">
		<xsl:attribute name="id"><xsl:value-of select="substring-after(., concat($g:requestUri, '#'))"/></xsl:attribute>
	    </xsl:if>	
            -->
	    <xsl:apply-templates select=".." mode="gc:LabelMode"/>
	</a>
    </xsl:template>
    
    <xsl:template match="@rdf:nodeID" mode="gc:InlineMode">
	<span id="{.}" title="{.}">
	    <xsl:apply-templates select=".." mode="gc:LabelMode"/>
	</span>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:InlineMode">
	<xsl:variable name="this" select="concat(namespace-uri(), local-name())"/>
        
	<span title="{$this}">
	    <xsl:apply-templates select="." mode="gc:PropertyLabelMode"/>
	</span>
    </xsl:template>

    <!-- object resource -->    
    <xsl:template match="@rdf:resource | sparql:uri" mode="gc:InlineMode">
	<a href="{.}" title="{.}">
            <xsl:apply-templates select="." mode="gc:ObjectLabelMode"/>
	</a>
    </xsl:template>

    <!-- object literal -->
    <xsl:template match="text()" mode="gc:InlineMode">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype] | sparql:literal[@datatype]" mode="gc:InlineMode">
	<span title="{../@rdf:datatype | @datatype}">
	    <xsl:value-of select="."/>
	</span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;float'] | text()[../@rdf:datatype = '&xsd;double'] | sparql:literal[@datatype = '&xsd;float'] | sparql:literal[@datatype = '&xsd;double']" priority="1" mode="gc:InlineMode">
	<span title="{../@rdf:datatype}">
	    <xsl:value-of select="format-number(., '#####.00')"/>
	</span>
    </xsl:template>

    <xsl:template match="text()[. castable as xs:date][../@rdf:datatype = '&xsd;date'] | sparql:literal[@datatype = '&xsd;date']" priority="1" mode="gc:InlineMode">
	<span title="{../@rdf:datatype}">
	    <xsl:value-of select="format-date(., '[D] [MNn] [Y]', $gp:lang, (), ())"/>
	</span>
    </xsl:template>

    <xsl:template match="text()[. castable as xs:dateTime][../@rdf:datatype = '&xsd;dateTime'] | sparql:literal[@datatype = '&xsd;dateTime']" priority="1" mode="gc:InlineMode">
	<!-- http://www.w3.org/TR/xslt20/#date-time-examples -->
	<!-- http://en.wikipedia.org/wiki/Date_format_by_country -->
	<span title="{../@rdf:datatype}">
	    <xsl:value-of select="format-dateTime(., '[D] [MNn] [Y] [H01]:[m01]', $gp:lang, (), ())"/>
	</span>
    </xsl:template>

    <xsl:template match="@rdf:datatype[starts-with(., '&xsd;')]" mode="gc:InlineMode" priority="1">
        <span class="help-inline" title="{.}">
            xsd:<xsl:value-of select="substring-after(., '&xsd;')"/>
        </span>
    </xsl:template>

    <xsl:template match="@rdf:datatype" mode="gc:InlineMode">
        <span class="help-inline" title="{.}">
            <xsl:value-of select="."/>
        </span>
    </xsl:template>

    <!-- LABEL MODES -->
    
    <xsl:template match="node()" mode="gc:LabelMode"/>
    
    <xsl:template match="node()" mode="gc:PropertyLabelMode"/>
        
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:PropertyLabelMode">
	<xsl:variable name="this" select="concat(namespace-uri(), local-name())"/>
        
        <xsl:choose>
            <xsl:when test="key('resources', $this)">
                <xsl:apply-templates select="key('resources', $this)" mode="gc:LabelMode"/>
            </xsl:when>
            <xsl:when test="doc-available(namespace-uri()) and key('resources', $this, document(namespace-uri()))" use-when="system-property('xsl:product-name') = 'SAXON'" >
                <xsl:apply-templates select="key('resources', $this, document(namespace-uri()))" mode="gc:LabelMode"/>
            </xsl:when>
            <xsl:when test="contains(concat(namespace-uri(), local-name()), '#') and not(ends-with(concat(namespace-uri(), local-name()), '#'))">
                <xsl:value-of select="substring-after(concat(namespace-uri(), local-name()), '#')"/>
            </xsl:when>
            <xsl:when test="string-length(tokenize($this, '/')[last()]) &gt; 0">
                <xsl:value-of use-when="function-available('url:decode')" select="translate(url:decode(tokenize($this, '/')[last()], 'UTF-8'), '_', ' ')"/>
                <xsl:value-of use-when="not(function-available('url:decode'))" select="translate(tokenize($this, '/')[last()], '_', ' ')"/>                    
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$this"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="node()" mode="gc:ObjectLabelMode"/>
        
    <xsl:template match="@rdf:resource | @rdf:nodeID | sparql:uri" mode="gc:ObjectLabelMode">
        <xsl:choose>
            <xsl:when test="key('resources', .)">
                <xsl:apply-templates select="key('resources', .)" mode="gc:LabelMode"/>
            </xsl:when>
            <xsl:when test="doc-available(gc:document-uri(.)) and key('resources', ., document(gc:document-uri(.)))" use-when="system-property('xsl:product-name') = 'SAXON'" >
                <xsl:apply-templates select="key('resources', ., document(gc:document-uri(.)))" mode="gc:LabelMode"/>
            </xsl:when>
            <xsl:when test="contains(., '#') and not(ends-with(., '#'))">
                <xsl:value-of select="substring-after(., '#')"/>
            </xsl:when>
            <xsl:when test="string-length(tokenize(., '/')[last()]) &gt; 0">
                <xsl:value-of use-when="function-available('url:decode')" select="translate(url:decode(tokenize(., '/')[last()], 'UTF-8'), '_', ' ')"/>
                <xsl:value-of use-when="not(function-available('url:decode'))" select="translate(tokenize(., '/')[last()], '_', ' ')"/>                    
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="."/>
            </xsl:otherwise>
        </xsl:choose>        
    </xsl:template>
    
    <!-- DESCRIPTION MODE -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:DescriptionMode"/>
    
    <!-- PROPERTY LIST MODE -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:PropertyListMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>

	<xsl:if test="not(preceding-sibling::*[concat(namespace-uri(), local-name()) = $this])">
	    <dt>
		<xsl:apply-templates select="." mode="gc:InlineMode"/>
	    </dt>
	</xsl:if>
	<dd>
	    <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="gc:InlineMode"/>
	</dd>
    </xsl:template>

    <!-- TABLE PREDICATE MODE -->
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:TablePredicateMode">
        <xsl:sequence select="."/>
    </xsl:template>

    <!-- TABLE CELL MODE -->
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:TableCellMode" priority="1">
        <xsl:param name="resource" as="element()"/>
        <xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>
        <xsl:variable name="predicate" select="$resource/*[concat(namespace-uri(), local-name()) = $this]"/>
        <xsl:choose>
            <xsl:when test="$predicate">
                <xsl:apply-templates select="$predicate" mode="gc:TableMode"/>
            </xsl:when>
            <xsl:otherwise>
                <td></td>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- TABLE HEADER MODE -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:TableHeaderMode">
	<th>
            <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</th>
    </xsl:template>

    <!-- TABLE MODE -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:TableMode"/>

    <!-- apply properties that match lang() -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[lang($gp:lang)]" mode="gc:TableMode" priority="1">
	<td>
	    <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="gc:InlineMode"/>
	</td>
    </xsl:template>
    
    <!-- apply the first one in the group if there's no lang() match -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[not(../*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))][lang($gp:lang)])][not(preceding-sibling::*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))])]" mode="gc:TableMode" priority="1">
	<td>
	    <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="gc:InlineMode"/>
	</td>
    </xsl:template>

    <xsl:template match="sparql:sparql" mode="gc:TableMode">
	<table class="table table-bordered table-striped">
	    <xsl:apply-templates mode="#current"/>
	</table>
    </xsl:template>
    
    <xsl:template match="sparql:head" mode="gc:TableMode">
	<thead>
	    <tr>
		<xsl:apply-templates mode="#current"/>
	    </tr>
	</thead>
    </xsl:template>

    <xsl:template match="sparql:variable" mode="gc:TableMode">
	<th>
	    <xsl:value-of select="@name"/>
	</th>
    </xsl:template>

    <xsl:template match="sparql:results" mode="gc:TableMode">
	<tbody>
	    <xsl:apply-templates mode="#current"/>
	</tbody>
    </xsl:template>

    <xsl:template match="sparql:result" mode="gc:TableMode">
	<tr>
	    <xsl:apply-templates mode="#current"/>
	</tr>
    </xsl:template>

    <xsl:template match="sparql:binding" mode="gc:TableMode">
	<td>
	    <xsl:apply-templates mode="#current"/>
	</td>
    </xsl:template>
    
    <xsl:template match="sparql:uri" mode="gc:TableMode">
	<a href="{.}" title="{.}">
	    <xsl:value-of select="."/>
	</a>
    </xsl:template>

    <!-- INPUT MODE -->
    
    <xsl:template name="gc:InputTemplate">
	<xsl:param name="name" as="xs:string"/>
	<xsl:param name="type" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="style" as="xs:string?"/>
	<xsl:param name="disabled" as="xs:boolean?"/>
        <xsl:param name="value" as="xs:string?"/>

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
            <xsl:if test="$disabled">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <xsl:if test="$value">
                <xsl:attribute name="value"><xsl:value-of select="$value"/></xsl:attribute>
            </xsl:if>
        </input>
    </xsl:template>

    <!-- subject resource -->
    <xsl:template match="@rdf:about" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'su'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- subject blank node -->
    <xsl:template match="@rdf:nodeID" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'sb'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'pu'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="concat(namespace-uri(), local-name())"/>
	</xsl:call-template>
    </xsl:template>
    
    <!-- object resource -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:resource" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'ou'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- object blank node -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'ob'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- object literal -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/text()" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'ol'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- datatype -->
    <xsl:template match="@rdf:datatype" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'lt'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- language tag -->
    <xsl:template match="@xml:lang" mode="gc:InputMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

	<xsl:call-template name="gc:InputTemplate">
	    <xsl:with-param name="name" select="'ll'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

</xsl:stylesheet>