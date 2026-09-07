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
<xsl:stylesheet version="3.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:map="http://www.w3.org/2005/xpath-functions/map"
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

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>

    <!-- LABEL -->
    
    <xsl:template match="node()" mode="ac:label"/>

    <!-- attempt use label of the primary topic before parsing label from URL -->
<!--    <xsl:template match="*[key('resources', foaf:primaryTopic/@rdf:*)]" mode="ac:label" priority="3">
        <xsl:apply-templates select="key('resources', foaf:primaryTopic/@rdf:*)" mode="#current"/>
    </xsl:template>-->

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
            <xsl:when test="doc-available(ac:document-uri(namespace-uri())) and key('resources', $this, document(ac:document-uri(namespace-uri())))" use-when="system-property('xsl:product-name') = 'SAXON'">
                <xsl:apply-templates select="key('resources', $this, document(ac:document-uri(namespace-uri())))" mode="ac:label"/>
            </xsl:when>
            <xsl:when test="contains($this, '#') and not(ends-with($this, '#'))">
                <xsl:sequence select="substring-after($this, '#')"/>
            </xsl:when>
            <xsl:when test="string-length(tokenize($this, '/')[last()]) &gt; 0">
                <xsl:sequence use-when="function-available('url:decode')" select="translate(url:decode(tokenize($this, '/')[last()], 'UTF-8'), '_', ' ')"/>
                <xsl:sequence use-when="not(function-available('url:decode'))" select="translate(tokenize($this, '/')[last()], '_', ' ')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="local-name()"/>
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

    <!-- LANGUAGE TAG -->

    <!-- the tag a value carries, shown wherever several languages of one value render side by side -->
    <xsl:template match="@xml:lang" mode="ac:lang-tag">
        <span class="ldhc-tag sz-sm em-quiet">
            <xsl:value-of select="."/>
        </span>
    </xsl:template>

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
                <xsl:attribute name="value" select="$value"/>
            </xsl:if>
            <xsl:if test="$selected">
                <xsl:attribute name="selected" select="'selected'"/>
            </xsl:if>
            <xsl:if test="$disabled">
                <xsl:attribute name="disabled" select="'disabled'"/>
            </xsl:if>
            <xsl:apply-templates select="." mode="ac:label"/>
        </option>
    </xsl:template>

    <!-- INLINE MODE -->
    
    <!-- subject resource -->
    <xsl:template match="@rdf:about" mode="xhtml:Anchor">
        <xsl:param name="href" select="." as="xs:anyURI"/>
        <xsl:param name="id" select="if (contains(., '#')) then substring-after(., '#') else ()" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="target" as="xs:string?"/>
        <xsl:param name="render-id" select="true()" as="xs:boolean" tunnel="yes"/>
        
        <a href="{$href}">
            <xsl:if test="$id and $render-id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:if test="$target">
                <xsl:attribute name="target" select="$target"/>
            </xsl:if>

            <!-- the label was chosen from whichever language the reader accepts, so the link says which one it ended up
                 in. A label built as a computed string has no literal behind it and inherits instead -->
            <xsl:variable name="label" as="item()*">
                <xsl:apply-templates select=".." mode="ac:label"/>
            </xsl:variable>
            <xsl:variable name="label-lang" select="$label[1][. instance of node()]/../@xml:lang" as="attribute()?"/>

            <xsl:if test="$label-lang">
                <xsl:attribute name="lang" select="$label-lang"/>
            </xsl:if>

            <xsl:sequence select="$label"/>
        </a>
    </xsl:template>
    
    <xsl:template match="@rdf:nodeID" mode="xhtml:Anchor">
        <xsl:param name="id" select="." as="xs:string"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>

        <span id="{$id}">
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <!-- the label was chosen from whichever language the reader accepts, so the link says which one it ended up
                 in. A label built as a computed string has no literal behind it and inherits instead -->
            <xsl:variable name="label" as="item()*">
                <xsl:apply-templates select=".." mode="ac:label"/>
            </xsl:variable>
            <xsl:variable name="label-lang" select="$label[1][. instance of node()]/../@xml:lang" as="attribute()?"/>

            <xsl:if test="$label-lang">
                <xsl:attribute name="lang" select="$label-lang"/>
            </xsl:if>

            <xsl:sequence select="$label"/>
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
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            
            <xsl:sequence select="ac:property-label(.)"/>
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
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:if test="$target">
                <xsl:attribute name="target" select="$target"/>
            </xsl:if>

            <!-- the label was picked from whichever language the reader accepts, so the link says which one it ended up in.
                 Bound as nodes rather than wrapped in xsl:value-of, which would atomize the literal and discard the tag
                 before it could be read. A label built as a computed string - a fragment identifier, a decoded path segment
                 - has no literal behind it and inherits instead -->
            <xsl:variable name="label" as="item()*">
                <xsl:apply-templates select="." mode="ac:object-label"/>
            </xsl:variable>
            <xsl:variable name="label-lang" select="$label[1][. instance of node()]/../@xml:lang" as="attribute()?"/>

            <xsl:if test="$label-lang">
                <xsl:attribute name="lang" select="$label-lang"/>
            </xsl:if>

            <xsl:value-of select="$label"/>
        </a>
    </xsl:template>

    <!-- object blank node -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID">
        <xsl:param name="href" select="xs:anyURI('#' || .)" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="target" as="xs:string?"/>

        <a href="{$href}">
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:if test="$target">
                <xsl:attribute name="target" select="$target"/>
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
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
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
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
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
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            
            <xsl:sequence select="format-date(., '[D] [MNn] [Y]', ac:langs()[1], (), ())"/>
        </span>
    </xsl:template>

    <xsl:template match="text()[. castable as xs:dateTime][../@rdf:datatype = '&xsd;dateTime'] | srx:literal[@datatype = '&xsd;dateTime']" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="../@rdf:datatype" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="timezone" select="implicit-timezone()" as="xs:dayTimeDuration?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>

            <!-- http://www.w3.org/TR/xslt20/#date-time-examples -->
            <!-- http://en.wikipedia.org/wiki/Date_format_by_country -->
            <xsl:sequence select="format-dateTime(adjust-dateTime-to-timezone(., $timezone), '[D] [MNn] [Y] [H01]:[m01]', ac:langs()[1], (), ())"/>
        </span>
    </xsl:template>

    <!-- @rdf:datatype -->
    <xsl:template match="@rdf:datatype[starts-with(., '&xsd;')]" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" select="'ldhc-tag sz-sm em-quiet'" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            
            xsd:<xsl:sequence select="substring-after(., '&xsd;')"/>
        </span>
    </xsl:template>

    <!-- @rdf:datatype -->
    <xsl:template match="@rdf:datatype">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" select="'ldhc-tag sz-sm em-quiet'" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            
            <xsl:sequence select="."/>
        </span>
    </xsl:template>

    <!-- TABLE -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="xhtml:TableHeaderCell">
        <th scope="col">
            <xsl:apply-templates select="."/>
        </th>
    </xsl:template>
    
    <!-- every value of the property beyond the first is folded into the cell the first one opens -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="xhtml:TableDataCell"/>

    <!-- the header fixes the column count, so a property gets one cell however many values it has, and they share it.
         Ordering them by the reader's languages puts the one they read first and leaves the rest reachable: a reader whose
         language the data lacks used to be shown the first value in document order, and every other reader was shown one
         value with no sign that the others existed -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[not(preceding-sibling::*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))])]" mode="xhtml:TableDataCell" priority="1">
        <xsl:variable name="property-uri" select="concat(namespace-uri(), local-name())" as="xs:string"/>

        <td>
            <!-- the values stack rather than run together, and the stack is what carries the height: a cell cannot cap its
                 own, so a property with many values has to be bounded by a container the cell holds -->
            <div class="values">
                <xsl:apply-templates select="../*[concat(namespace-uri(), local-name()) = $property-uri]" mode="xhtml:TableDataCellValue">
                    <xsl:sort select="ac:lang-rank(.)"/>
                </xsl:apply-templates>
            </div>
        </td>
    </xsl:template>

    <!-- one block per statement, so the values stack apart instead of running into each other. The statement is the unit,
         not the node under it: an XHTML literal is one value however many elements it is written with -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="xhtml:TableDataCellValue">
        <div class="value">
            <!-- an untagged literal makes no language claim, which HTML spells lang="". A typed value is not prose and
                 inherits, so a number or a date is read out in the reader's own language -->
            <xsl:if test="text() and (not(@rdf:datatype) or @rdf:datatype = '&xsd;string')">
                <xsl:attribute name="lang" select="''"/>
            </xsl:if>

            <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>
        </div>
    </xsl:template>

    <!-- each value declares its own language rather than inheriting the document's, since the cell holds several at once
         and the document default is wrong for all but one of them -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[@xml:lang]" mode="xhtml:TableDataCellValue" priority="1">
        <div class="value" lang="{@xml:lang}">
            <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>

            <xsl:apply-templates select="@xml:lang" mode="ac:lang-tag"/>
        </div>
    </xsl:template>

    <xsl:template match="srx:sparql" mode="xhtml:Table">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" as="xs:string?"/>
        <xsl:param name="class" select="'results-table'" as="xs:string?"/>
        
        <table>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            
            <caption class="ldhc-vh">
                <xsl:apply-templates select="key('resources', 'query-results', document(resolve-uri('static/com/atomgraph/client/xsl/translations.rdf', $ac:contextUri)))" mode="ac:label"/>
            </caption>

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
        <th scope="col">
            <xsl:value-of select="@name"/>
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
        <xsl:apply-templates select="."/>
    </xsl:template>

    <!-- INPUT MODE -->
    
    <xsl:template name="xhtml:Input">
        <xsl:param name="type" as="xs:string"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="name" as="xs:string?"/>
        <xsl:param name="style" as="xs:string?"/>
        <xsl:param name="disabled" as="xs:boolean?"/>
        <xsl:param name="title" as="xs:string?"/>
        <xsl:param name="value" as="xs:string?"/>
        <xsl:param name="checked" as="xs:boolean?"/>
        <xsl:param name="autocomplete" select="true()" as="xs:boolean?"/>
        
        <input type="{$type}">
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:if test="$name">
                <xsl:attribute name="name" select="$name"/>
            </xsl:if>
            <xsl:if test="$style">
                <xsl:attribute name="style" select="$style"/>
            </xsl:if>
            <xsl:if test="$disabled">
                <xsl:attribute name="disabled" select="'disabled'"/>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>
            <xsl:if test="$value">
                <xsl:attribute name="value" select="$value"/>
            </xsl:if>
            <xsl:if test="$checked">
                <xsl:attribute name="checked" select="'checked'"/>
            </xsl:if>
            <xsl:if test="not($autocomplete)">
                <xsl:attribute name="autocomplete" select="'off'"/>
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
    <xsl:template match="@rdf:datatype" mode="xhtml:Input">
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
    <xsl:template match="@xml:lang" mode="xhtml:Input">
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

    <!-- PROPERTY EDITOR -->

    <xsl:template match="text()[../@xml:lang]" mode="xhtml:DefinitionDescription" priority="1">
        <dd>
            <xsl:apply-templates select="../@xml:lang" mode="ac:lang-tag"/>

            <xsl:apply-templates select="."/>
        </dd>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:PropertyEditor">
        <xsl:apply-templates select="." mode="xhtml:DefinitionTerm"/>

        <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="xhtml:DefinitionDescription"/>
    </xsl:template>

    <!-- FORM CONTROLS -->

    <!-- @rdf:about | @rdf:nodeID -->
    <xsl:template match="*[*]/@rdf:*[local-name() = ('about', 'nodeID')]" mode="ac:FormControl">
        <xsl:param name="type" select="'hidden'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>

        <xsl:apply-templates select="." mode="xhtml:Input">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- one statement: predicate label, value controls, term annotations -->
    <xsl:template match="*[@rdf:*[local-name() = ('about',  'nodeID')]]/*" mode="ac:FormControl">
        <xsl:param name="this" select="concat(namespace-uri(), local-name())"/>
        <xsl:param name="violations" as="element()*"/>
        <xsl:param name="error" select="$violations/spin:violationPath/@rdf:resource = $this" as="xs:boolean"/>
        <xsl:param name="class" select="concat('statement', if ($error) then ' is-invalid' else ())" as="xs:string?"/>
        <xsl:param name="label" as="xs:string?">
            <xsl:apply-templates select="." mode="ac:property-label"/>
        </xsl:param>
        <xsl:param name="show-label" select="true()" as="xs:boolean"/>
        <xsl:param name="cloneable" select="false()" as="xs:boolean"/>
        <xsl:param name="required" select="false()" as="xs:boolean"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="for" select="generate-id((node() | @rdf:resource | @rdf:nodeID)[1])" as="xs:string"/>

        <div>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:apply-templates select="." mode="xhtml:Input">
                <xsl:with-param name="type" select="'hidden'"/>
            </xsl:apply-templates>
            <xsl:if test="$show-label">
                <label class="ldhc-label" for="{$for}" title="{$this}">
                    <xsl:sequence select="$label"/>
                </label>
            </xsl:if>

            <div class="values">
                <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="#current"/>

                <xsl:if test="$cloneable">
                    <button type="button" class="ldhc-iconbtn sz-xs in-accent ap-ghost btn-add">
                        <xsl:attribute name="title">
                            <xsl:apply-templates select="key('resources', 'add-stmt', document(resolve-uri('static/com/atomgraph/client/xsl/translations.rdf', $ac:contextUri)))" mode="ac:label"/>
                        </xsl:attribute>

                        <span class="msi sm" aria-hidden="true">add</span>
                    </button>
                </xsl:if>
                <xsl:if test="not($required)">
                    <button type="button" class="ldhc-iconbtn sz-xs in-destructive ap-ghost btn-remove-property">
                        <xsl:attribute name="title">
                            <xsl:apply-templates select="key('resources', 'remove-stmt', document(resolve-uri('static/com/atomgraph/client/xsl/translations.rdf', $ac:contextUri)))" mode="ac:label"/>
                        </xsl:attribute>

                        <span class="msi sm" aria-hidden="true">remove</span>
                    </button>
                </xsl:if>
            </div>
            <xsl:if test="@xml:lang | @rdf:datatype">
                <div class="annotations">
                    <xsl:apply-templates select="@xml:lang | @rdf:datatype" mode="#current"/>
                </div>
            </xsl:if>
        </div>
    </xsl:template>

    <!-- literal inputs ride the field shell: the box carries the chrome, the input stays bare -->
    <xsl:template match="text()" mode="ac:FormControl">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:choose>
            <xsl:when test="$type = 'hidden'">
                <xsl:apply-templates select="." mode="xhtml:Input">
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                    <xsl:with-param name="disabled" select="$disabled"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <div class="ldhc-field">
                    <div class="ldhc-field-box sz-sm">
                        <xsl:apply-templates select="." mode="xhtml:Input">
                            <xsl:with-param name="type" select="$type"/>
                            <xsl:with-param name="id" select="$id"/>
                            <xsl:with-param name="class" select="$class"/>
                            <xsl:with-param name="disabled" select="$disabled"/>
                        </xsl:apply-templates>
                    </div>
                </div>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="ac:ValueAnnotations">
                <xsl:with-param name="type" select="$type"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <xsl:template match="text()" mode="ac:ValueAnnotations">
        <xsl:param name="type" as="xs:string?"/>

        <xsl:if test="not($type = 'hidden')">
            <xsl:choose>
                <xsl:when test="../@rdf:datatype">
                    <xsl:apply-templates select="../@rdf:datatype" mode="#current"/>
                </xsl:when>
                <xsl:otherwise>
                    <span class="ldhc-tag sz-sm em-quiet">
                        <xsl:apply-templates select="key('resources', 'literal', document(resolve-uri('static/com/atomgraph/client/xsl/translations.rdf', $ac:contextUri)))" mode="ac:label"/>
                    </span>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template match="text()[string-length(.) &gt; 50]" mode="ac:FormControl">
        <xsl:param name="name" select="'ol'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="style" as="xs:string?"/>
        <xsl:param name="value" select="." as="xs:string?"/>
        <xsl:param name="rows" as="xs:integer?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <div class="ldhc-field">
            <div class="ldhc-field-box sz-sm">
                <textarea name="{$name}">
                    <xsl:if test="$id">
                        <xsl:attribute name="id" select="$id"/>
                    </xsl:if>
                    <xsl:if test="$class">
                        <xsl:attribute name="class" select="$class"/>
                    </xsl:if>
                    <xsl:if test="$style">
                        <xsl:attribute name="style" select="$style"/>
                    </xsl:if>
                    <xsl:if test="$rows">
                        <xsl:attribute name="rows" select="$rows"/>
                    </xsl:if>
                    <xsl:if test="$disabled">
                        <xsl:attribute name="disabled" select="'disabled'"/>
                    </xsl:if>

                    <xsl:sequence select="$value"/>
                </textarea>
            </div>
        </div>

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="ac:ValueAnnotations"/>
        </xsl:if>
    </xsl:template>

    <!-- blank nodes that only have rdf:type xsd:string and no other properties become literal inputs -->
    <xsl:template match="*[@rdf:nodeID]/*/@rdf:nodeID[key('resources', .)[not(* except rdf:type[starts-with(@rdf:resource, '&xsd;')])]]" mode="ac:FormControl" priority="2">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="required" select="false()" as="xs:boolean"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <div class="ldhc-field">
            <div class="ldhc-field-box sz-sm">
                <xsl:call-template name="xhtml:Input">
                    <xsl:with-param name="name" select="'ol'"/>
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                    <xsl:with-param name="disabled" select="$disabled"/>
                </xsl:call-template>
            </div>
        </div>

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="ac:ValueAnnotations">
                <xsl:with-param name="type" select="$type"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[@rdf:nodeID]/*/@rdf:nodeID[key('resources', .)[not(* except rdf:type[starts-with(@rdf:resource, '&xsd;')])]]" mode="ac:ValueAnnotations" priority="2">
        <xsl:param name="type" as="xs:string?"/>

        <xsl:if test="not($type = 'hidden')">
            <span class="ldhc-tag sz-sm em-quiet">
                <xsl:apply-templates select="key('resources', 'literal', document(resolve-uri('static/com/atomgraph/client/xsl/translations.rdf', $ac:contextUri)))" mode="ac:label"/>
            </span>
        </xsl:if>
    </xsl:template>

    <!-- @rdf:resource, @rdf:nodeID -->
    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:*[local-name() = ('resource', 'nodeID')]" mode="ac:FormControl">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:choose>
            <xsl:when test="$type = 'hidden'">
                <xsl:apply-templates select="." mode="xhtml:Input">
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                    <xsl:with-param name="disabled" select="$disabled"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <div class="ldhc-field">
                    <div class="ldhc-field-box sz-sm">
                        <xsl:apply-templates select="." mode="xhtml:Input">
                            <xsl:with-param name="type" select="$type"/>
                            <xsl:with-param name="id" select="$id"/>
                            <xsl:with-param name="class" select="$class"/>
                            <xsl:with-param name="disabled" select="$disabled"/>
                        </xsl:apply-templates>
                    </div>
                </div>
            </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="ac:ValueAnnotations">
                <xsl:with-param name="type" select="$type"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:*[local-name() = ('resource', 'nodeID')]" mode="ac:ValueAnnotations">
        <xsl:param name="type" as="xs:string?"/>

        <xsl:if test="not($type = 'hidden')">
            <span class="ldhc-tag sz-sm em-quiet">
                <xsl:apply-templates select="key('resources', 'resource', document(resolve-uri('static/com/atomgraph/client/xsl/translations.rdf', $ac:contextUri)))" mode="ac:label"/>
            </span>
        </xsl:if>
    </xsl:template>

    <!-- @xml:lang: a narrow field beside the value it tags -->
    <xsl:template match="@xml:*[local-name() = 'lang']" mode="ac:FormControl">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" select="'lang-field'" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <span>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:attribute name="title">
                <xsl:apply-templates select="key('resources', 'language-tag', document(resolve-uri('static/com/atomgraph/client/xsl/translations.rdf', $ac:contextUri)))" mode="ac:label"/>
            </xsl:attribute>

            <div class="ldhc-field">
                <div class="ldhc-field-box sz-sm">
                    <span class="ldhc-adorn"><span class="msi outline sm" aria-hidden="true">language</span></span>
                    <xsl:apply-templates select="." mode="xhtml:Input">
                        <xsl:with-param name="type" select="$type"/>
                        <xsl:with-param name="id" select="$id"/>
                        <xsl:with-param name="disabled" select="$disabled"/>
                    </xsl:apply-templates>
                </div>
            </div>
        </span>
    </xsl:template>

    <xsl:template match="@xml:*[local-name() = 'lang']" mode="ac:ValueAnnotations">
        <xsl:param name="type" as="xs:string?"/>

        <xsl:if test="not($type = 'hidden')">
            <span class="ldhc-tag sz-sm em-quiet">
                <xsl:apply-templates select="key('resources', 'language-tag', document(resolve-uri('static/com/atomgraph/client/xsl/translations.rdf', $ac:contextUri)))" mode="ac:label"/>
            </span>
        </xsl:if>
    </xsl:template>

    <!-- @rdf:datatype -->
    <xsl:template match="@rdf:*[local-name() = 'datatype']" mode="ac:FormControl">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="xhtml:Input">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="disabled" select="$disabled"/>
        </xsl:apply-templates>

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="ac:ValueAnnotations">
                <xsl:with-param name="type" select="$type"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@rdf:*[local-name() = 'datatype']" mode="ac:ValueAnnotations">
        <xsl:param name="type" as="xs:string?"/>

        <xsl:if test="not($type = 'hidden')">
            <span class="ldhc-tag sz-sm em-quiet" title="{.}">
                <xsl:value-of select="if (starts-with(., '&xsd;')) then 'xsd:' || substring-after(., '&xsd;') else ."/>
            </span>
        </xsl:if>
    </xsl:template>

    <!-- *[@rdf:about or @rdf:nodeID]/*/@rdf:* -->
    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:*[local-name() = ('resource', 'nodeID')]" mode="ac:FormControl" priority="1">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="traversed-ids" as="xs:string*" tunnel="yes"/>
        <xsl:param name="template"  as="element()?"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>
        <xsl:variable name="resource" select="key('resources', .)"/>

        <xsl:choose>
            <xsl:when test="$resource and not(. = $traversed-ids)">
                <xsl:apply-templates select="." mode="xhtml:Input">
                    <xsl:with-param name="type" select="'hidden'"/>
                </xsl:apply-templates>

                <xsl:apply-templates select="$resource" mode="#current">
                    <xsl:with-param name="traversed-ids" select="(., $traversed-ids)" tunnel="yes"/>
                </xsl:apply-templates>

                <!-- restore subject context -->
                <xsl:apply-templates select="../../@rdf:about | ../../@rdf:nodeID" mode="#current">
                    <xsl:with-param name="type" select="'hidden'"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match>
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                    <xsl:with-param name="disabled" select="$disabled"/>
                    <xsl:with-param name="type-label" select="$type-label"/>
                </xsl:next-match>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:*[local-name() = ('resource', 'nodeID')]" mode="ac:ValueAnnotations" priority="1">
        <xsl:param name="type" as="xs:string?"/>

        <xsl:if test="not($type = 'hidden')">
            <span class="ldhc-tag sz-sm em-quiet">
                <xsl:apply-templates select="key('resources', 'resource', document(resolve-uri('static/com/atomgraph/client/xsl/translations.rdf', $ac:contextUri)))" mode="ac:label"/>
            </span>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>