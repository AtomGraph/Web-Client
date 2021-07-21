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
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY srx    "http://www.w3.org/2005/sparql-results#">
    <!ENTITY ldt    "https://www.w3.org/ns/ldt#">
    <!ENTITY sp     "http://spinrdf.org/sp#">
    <!ENTITY spin   "http://spinrdf.org/spin#">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:xsd="&xsd;"
xmlns:srx="&srx;"
xmlns:ldt="&ldt;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:foaf="&foaf;"
xmlns:url="&java;java.net.URLDecoder"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="#all">

    <xsl:param name="ldt:lang" select="'en'" as="xs:string"/>

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>

    <!-- LABEL -->
    
    <xsl:template match="node()" mode="ac:label"/>

    <!-- attempt use label of the primary topic before parsing label from URL -->
    <xsl:template match="*[key('resources', foaf:primaryTopic/@rdf:*)]" mode="ac:label" priority="3">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/@rdf:*)" mode="#current"/>
    </xsl:template>

    <xsl:template match="*[contains(@rdf:about, '#') and not(ends-with(@rdf:about, '#'))]" mode="ac:label" priority="2">
        <xsl:sequence select="substring-after(@rdf:about, '#')"/>
    </xsl:template>

    <xsl:template match="*[string-length(tokenize(@rdf:about, '/')[last()]) &gt; 0]" mode="ac:label" priority="1">
        <xsl:variable name="label" use-when="function-available('url:decode')" select="translate(url:decode(tokenize(@rdf:about, '/')[last()], 'UTF-8'), '_', ' ')"/>
        <xsl:variable name="label" use-when="not(function-available('url:decode'))" select="translate(tokenize(@rdf:about, '/')[last()], '_', ' ')"/>
        <xsl:sequence select="$label"/>
    </xsl:template>
    
    <xsl:template match="*[@rdf:about] | *[@rdf:nodeID]" mode="ac:label">
        <xsl:value-of select="@rdf:about | @rdf:nodeID"/>
    </xsl:template>
    
    <!-- PROPERTY LABEL -->
    
    <xsl:template match="node()" mode="ac:property-label"/>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:property-label" priority="1">
        <xsl:variable name="this" select="concat(namespace-uri(), local-name())"/>
        
        <xsl:choose>
            <xsl:when test="key('resources', $this)">
                <xsl:apply-templates select="key('resources', $this)" mode="ac:label"/>
            </xsl:when>
            <xsl:when test="doc-available(namespace-uri()) and key('resources', $this, document(namespace-uri()))" use-when="system-property('xsl:product-name') = 'SAXON'" >
                <xsl:apply-templates select="key('resources', $this, document(namespace-uri()))" mode="ac:label"/>
            </xsl:when>
            <xsl:when test="contains($this, '#') and not(ends-with($this, '#'))">
                <xsl:sequence select="substring-after($this, '#')"/>
            </xsl:when>
            <xsl:when test="string-length(tokenize($this, '/')[last()]) &gt; 0">
                <xsl:sequence use-when="function-available('url:decode')" select="translate(url:decode(tokenize($this, '/')[last()], 'UTF-8'), '_', ' ')"/>
                <xsl:sequence use-when="not(function-available('url:decode'))" select="translate(tokenize($this, '/')[last()], '_', ' ')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="$this"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!-- OBJECT LABEL -->
    
    <xsl:template match="node()" mode="ac:object-label"/>
        
    <xsl:template match="@rdf:resource | @rdf:nodeID | srx:uri" mode="ac:object-label" priority="1">
        <xsl:choose>
            <xsl:when test="key('resources', .)">
                <xsl:apply-templates select="key('resources', .)" mode="ac:label"/>
            </xsl:when>
            <xsl:when test="doc-available(ac:document-uri(.)) and key('resources', ., document(ac:document-uri(.)))" use-when="system-property('xsl:product-name') = 'SAXON'">
                <xsl:apply-templates select="key('resources', ., document(ac:document-uri(.)))" mode="ac:label"/>
            </xsl:when>
            <xsl:when test="contains(., '#') and not(ends-with(., '#'))">
                <xsl:sequence select="substring-after(., '#')"/>
            </xsl:when>
            <xsl:when test="string-length(tokenize(., '/')[last()]) &gt; 0">
                <xsl:sequence use-when="function-available('url:decode')" select="translate(url:decode(tokenize(., '/')[last()], 'UTF-8'), '_', ' ')"/>
                <xsl:sequence use-when="not(function-available('url:decode'))" select="translate(tokenize(., '/')[last()], '_', ' ')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    

    <!-- DESCRIPTION -->

    <xsl:template match="node()" mode="ac:description"/>
    
    <!-- IMAGE -->

    <xsl:template match="node()" mode="ac:image"/>

    <!-- DEFINITIONS -->
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="xhtml:DefinitionTerm">
        <dt>
            <xsl:apply-templates select="."/>
        </dt>
    </xsl:template>
    
    <xsl:template match="node() | @rdf:resource | @rdf:nodeID" mode="xhtml:DefinitionDescription">
        <dd>
            <xsl:apply-templates select="."/>
        </dd>
    </xsl:template>
    
    <!-- OPTION MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="xhtml:Option">
        <xsl:param name="value" select="@rdf:about | @rdf:nodeID" as="xs:string?"/>
        <xsl:param name="selected" as="xs:boolean?"/>
        <xsl:param name="disabled" as="xs:boolean?"/>

        <option>
            <xsl:if test="$value">
                <xsl:attribute name="value"><xsl:sequence select="$value"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$selected">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:if test="$disabled">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="." mode="ac:label"/>
        </option>
    </xsl:template>

    <!-- INLINE MODE -->
    
    <!-- subject resource -->
    <xsl:template match="*[@rdf:about]" mode="xhtml:Anchor">
        <xsl:param name="href" select="@rdf:about" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="@rdf:about" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="target" as="xs:string?"/>

        <a href="{$href}">
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:sequence select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:sequence select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:sequence select="$class"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$target">
                <xsl:attribute name="target"><xsl:sequence select="$target"/></xsl:attribute>
            </xsl:if>
            
            <xsl:apply-templates select="." mode="ac:label"/>
        </a>
    </xsl:template>
    
    <xsl:template match="*[@rdf:nodeID]" mode="xhtml:Anchor">
        <xsl:param name="id" select="@rdf:nodeID" as="xs:string"/>
        <xsl:param name="title" select="@rdf:nodeID" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>

        <span id="{$id}">
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:sequence select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:sequence select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="." mode="ac:label"/>
        </span>
    </xsl:template>

    <!-- DEFAULT MODE -->
    
    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="concat(namespace-uri(), local-name())" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:sequence select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:sequence select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:sequence select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:variable name="label" as="xs:string?">
                <xsl:apply-templates select="." mode="ac:property-label"/>
            </xsl:variable>
            <xsl:sequence select="upper-case(substring($label, 1, 1)) || substring($label, 2)"/>
        </span>
    </xsl:template>

    <!-- object URI resource -->
    <xsl:template match="@rdf:resource | srx:uri">
        <xsl:param name="href" select="." as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="target" as="xs:string?"/>

        <a href="{$href}">
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:sequence select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:sequence select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:sequence select="$class"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$target">
                <xsl:attribute name="target"><xsl:sequence select="$target"/></xsl:attribute>
            </xsl:if>
            
            <xsl:value-of>
                <xsl:apply-templates select="." mode="ac:object-label"/>
            </xsl:value-of>
        </a>
    </xsl:template>

    <!-- object blank node -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID">
        <xsl:param name="href" select="xs:anyURI(concat('#', .))" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="target" as="xs:string?"/>

        <a href="{$href}">
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:sequence select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:sequence select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:sequence select="$class"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$target">
                <xsl:attribute name="target"><xsl:sequence select="$target"/></xsl:attribute>
            </xsl:if>
            
            <xsl:value-of>
                <xsl:apply-templates select="." mode="ac:object-label"/>
            </xsl:value-of>
        </a>
    </xsl:template>
    
    <!-- object literal -->
    <xsl:template match="text()">
        <xsl:sequence select="."/>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype] | srx:literal[@datatype]">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="../@rdf:datatype | @datatype" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:sequence select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:sequence select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:sequence select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:sequence select="."/>
        </span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;float'] | text()[../@rdf:datatype = '&xsd;double'] | srx:literal[@datatype = '&xsd;float'] | srx:literal[@datatype = '&xsd;double']" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="../@rdf:datatype" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:sequence select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:sequence select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:sequence select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:sequence select="format-number(., '#####.00')"/>
        </span>
    </xsl:template>

    <xsl:template match="text()[. castable as xs:date][../@rdf:datatype = '&xsd;date'] | srx:literal[@datatype = '&xsd;date']" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="../@rdf:datatype" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:sequence select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:sequence select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:sequence select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:sequence select="format-date(., '[D] [MNn] [Y]', $ldt:lang, (), ())"/>
        </span>
    </xsl:template>

    <xsl:template match="text()[. castable as xs:dateTime][../@rdf:datatype = '&xsd;dateTime'] | srx:literal[@datatype = '&xsd;dateTime']" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="../@rdf:datatype" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="timezone" select="implicit-timezone()" as="xs:dayTimeDuration?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:sequence select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:sequence select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:sequence select="$class"/></xsl:attribute>
            </xsl:if>

            <!-- http://www.w3.org/TR/xslt20/#date-time-examples -->
            <!-- http://en.wikipedia.org/wiki/Date_format_by_country -->
            <xsl:sequence select="format-dateTime(adjust-dateTime-to-timezone(., $timezone), '[D] [MNn] [Y] [H01]:[m01]', $ldt:lang, (), ())"/>
        </span>
    </xsl:template>

    <!-- @rdf:datatype -->
    <xsl:template match="@rdf:*[local-name() = 'datatype'][starts-with(., '&xsd;')]" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" select="'help-inline'" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:sequence select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:sequence select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:sequence select="$class"/></xsl:attribute>
            </xsl:if>
            
            xsd:<xsl:sequence select="substring-after(., '&xsd;')"/>
        </span>
    </xsl:template>

    <!-- @rdf:datatype -->
    <xsl:template match="@rdf:*[local-name() = 'datatype']">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" select="'help-inline'" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:sequence select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:sequence select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:sequence select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:sequence select="."/>
        </span>
    </xsl:template>
    
    <!-- TABLE PREDICATE MODE -->
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:TablePredicate">
        <xsl:sequence select="."/>
    </xsl:template>

    <!-- TABLE -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="xhtml:TableHeaderCell">
        <th>
            <xsl:apply-templates select="."/>
        </th>
    </xsl:template>
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="xhtml:TableDataCell"/>

    <!-- apply properties that match lang() -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[lang($ldt:lang)]" mode="xhtml:TableDataCell" priority="1">
        <td>
            <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>
        </td>
    </xsl:template>
    
    <!-- apply the first one in the group if there's no lang() match -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[not(../*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))][lang($ldt:lang)])][not(preceding-sibling::*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))])]" mode="xhtml:TableDataCell" priority="1">
        <td>
            <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>
        </td>
    </xsl:template>

    <xsl:template match="srx:sparql" mode="xhtml:Table">
        <table class="table table-bordered table-striped">
            <xsl:apply-templates mode="#current"/>
        </table>
    </xsl:template>
    
    <xsl:template match="srx:head" mode="xhtml:Table">
        <thead>
            <tr>
                <xsl:apply-templates mode="#current"/>
            </tr>
        </thead>
    </xsl:template>

    <xsl:template match="srx:variable" mode="xhtml:Table">
        <th>
            <xsl:sequence select="@name"/>
        </th>
    </xsl:template>

    <xsl:template match="srx:results" mode="xhtml:Table">
        <tbody>
            <xsl:apply-templates mode="#current"/>
        </tbody>
    </xsl:template>

    <xsl:template match="srx:result" mode="xhtml:Table">
        <tr>
            <xsl:apply-templates mode="#current"/>
        </tr>
    </xsl:template>

    <xsl:template match="srx:binding" mode="xhtml:Table">
        <td>
            <xsl:apply-templates mode="#current"/>
        </td>
    </xsl:template>
    
    <xsl:template match="srx:uri" mode="xhtml:Table">
        <a href="{.}" title="{.}">
            <xsl:sequence select="."/>
        </a>
    </xsl:template>

    <!-- INPUT MODE -->
    
    <xsl:template name="xhtml:Input">
        <xsl:param name="name" as="xs:string"/>
        <xsl:param name="type" as="xs:string"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="style" as="xs:string?"/>
        <xsl:param name="disabled" as="xs:boolean?"/>
        <xsl:param name="title" as="xs:string?"/>
        <xsl:param name="value" as="xs:string?"/>
        <xsl:param name="checked" as="xs:boolean?"/>

        <input type="{$type}" name="{$name}">
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:sequence select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:sequence select="$class"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$style">
                <xsl:attribute name="style"><xsl:sequence select="$style"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$disabled">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:sequence select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$value">
                <xsl:attribute name="value"><xsl:sequence select="$value"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$checked">
                <xsl:attribute name="checked">checked</xsl:attribute>
            </xsl:if>
        </input>
    </xsl:template>

    <!-- subject resource -->
    <!-- @rdf:about -->
    <xsl:template match="@rdf:about" mode="xhtml:Input">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="title" as="xs:string?"/>

        <xsl:call-template name="xhtml:Input">
            <xsl:with-param name="name" select="'su'"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="disabled" select="$disabled"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="value" select="."/>
        </xsl:call-template>
    </xsl:template>

    <!-- subject blank node -->
    <!-- @rdf:nodeID -->
    <xsl:template match="@rdf:nodeID" mode="xhtml:Input">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="title" as="xs:string?"/>

        <xsl:call-template name="xhtml:Input">
            <xsl:with-param name="name" select="'sb'"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="disabled" select="$disabled"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="value" select="."/>
        </xsl:call-template>
    </xsl:template>

    <!-- property -->
    <!-- *[@rdf:about or @rdf:nodeID]/* -->
    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*" mode="xhtml:Input">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="title" as="xs:string?"/>

        <xsl:call-template name="xhtml:Input">
            <xsl:with-param name="name" select="'pu'"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="disabled" select="$disabled"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="value" select="concat(namespace-uri(), local-name())"/>
        </xsl:call-template>
    </xsl:template>
    
    <!-- object resource -->
    <!-- *[@rdf:about or @rdf:nodeID]/*/@rdf:resource -->
    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:resource" mode="xhtml:Input">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="title" as="xs:string?"/>

        <xsl:call-template name="xhtml:Input">
            <xsl:with-param name="name" select="'ou'"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="disabled" select="$disabled"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="value" select="."/>
        </xsl:call-template>
    </xsl:template>

    <!-- object blank node -->
    <!-- *[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID -->
    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:nodeID" mode="xhtml:Input" priority="1">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="title" as="xs:string?"/>

        <xsl:call-template name="xhtml:Input">
            <xsl:with-param name="name" select="'ob'"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="disabled" select="$disabled"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="value" select="."/>
        </xsl:call-template>
    </xsl:template>

    <!-- object literal -->
    <!-- *[@rdf:about or @rdf:nodeID]/*/text() -->
    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/text()" mode="xhtml:Input">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="title" as="xs:string?"/>

        <xsl:call-template name="xhtml:Input">
            <xsl:with-param name="name" select="'ol'"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="disabled" select="$disabled"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="value" select="."/>
        </xsl:call-template>
    </xsl:template>

    <!-- datatype -->
    <!-- @rdf:datatype -->
    <xsl:template match="@rdf:*[local-name() = 'datatype']" mode="xhtml:Input">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="title" as="xs:string?"/>

        <xsl:call-template name="xhtml:Input">
            <xsl:with-param name="name" select="'lt'"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="disabled" select="$disabled"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="value" select="."/>
        </xsl:call-template>
    </xsl:template>

    <!-- language tag -->
    <!-- @xml:lang -->
    <xsl:template match="@xml:*[local-name() = 'lang']" mode="xhtml:Input">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="title" as="xs:string?"/>

        <xsl:call-template name="xhtml:Input">
            <xsl:with-param name="name" select="'ll'"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="disabled" select="$disabled"/>
            <xsl:with-param name="title" select="$title"/>
            <xsl:with-param name="value" select="."/>
        </xsl:call-template>
    </xsl:template>

</xsl:stylesheet>