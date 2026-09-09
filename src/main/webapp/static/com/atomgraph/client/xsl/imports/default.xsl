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
    <!ENTITY translations "https://w3id.org/atomgraph/client/xsl/translations.rdf#">
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
    <xsl:key name="resources-by-type" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="rdf:type/@rdf:resource"/>

    <!-- the label of a class given as a bare URI: rdfs:Resource takes the catalog's localized label,
         everything else delegates to the ac:object-label machinery over a synthesized object node, so the
         load-guarded document lookup lives in one place - the ac:object-label mode. The atomic-URI
         signature exists because $forClass travels as xs:anyURI*, never as nodes the mode could dispatch on -->
    <xsl:function name="ac:class-label" as="xs:string?">
        <xsl:param name="class" as="xs:anyURI"/>

        <xsl:choose>
            <xsl:when test="$class = '&rdfs;Resource'">
                <xsl:value-of>
                    <xsl:apply-templates select="key('resources', '&translations;resource', ac:translations())" mode="ac:label"/>
                </xsl:value-of>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="object" as="document-node()">
                    <xsl:document>
                        <rdf:Description rdf:resource="{$class}"/>
                    </xsl:document>
                </xsl:variable>

                <xsl:sequence select="ac:object-label($object/rdf:Description/@rdf:resource)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

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
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:PropertyListLabel">
        <dt>
            <xsl:apply-templates select="."/>
        </dt>
    </xsl:template>
    
    <xsl:template match="node() | @rdf:resource | @rdf:nodeID" mode="ac:PropertyListValue">
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

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:ResultsTableHeaderCell">
        <th scope="col">
            <xsl:apply-templates select="."/>
        </th>
    </xsl:template>
    
    <!-- every value of the property beyond the first is folded into the cell the first one opens -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:ResultsTableDataCell"/>

    <!-- the header fixes the column count, so a property gets one cell however many values it has, and they share it.
         Ordering them by the reader's languages puts the one they read first and leaves the rest reachable: a reader whose
         language the data lacks used to be shown the first value in document order, and every other reader was shown one
         value with no sign that the others existed -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[not(preceding-sibling::*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))])]" mode="ac:ResultsTableDataCell" priority="1">
        <xsl:variable name="property-uri" select="concat(namespace-uri(), local-name())" as="xs:string"/>

        <td>
            <!-- the values stack rather than run together, and the stack is what carries the height: a cell cannot cap its
                 own, so a property with many values has to be bounded by a container the cell holds -->
            <div class="values">
                <xsl:apply-templates select="../*[concat(namespace-uri(), local-name()) = $property-uri]" mode="ac:ResultsTableDataCellValue">
                    <xsl:sort select="ac:lang-rank(.)"/>
                </xsl:apply-templates>
            </div>
        </td>
    </xsl:template>

    <!-- one block per statement, so the values stack apart instead of running into each other. The statement is the unit,
         not the node under it: an XHTML literal is one value however many elements it is written with -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:ResultsTableDataCellValue">
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
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[@xml:lang]" mode="ac:ResultsTableDataCellValue" priority="1">
        <div class="value" lang="{@xml:lang}">
            <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>

            <xsl:apply-templates select="@xml:lang" mode="ac:lang-tag"/>
        </div>
    </xsl:template>

    <xsl:template match="srx:sparql" mode="ac:ResultsTable">
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
                <xsl:apply-templates select="key('resources', '&translations;query-results', ac:translations())" mode="ac:label"/>
            </caption>

            <xsl:apply-templates mode="#current"/>
        </table>
    </xsl:template>
    
    <xsl:template match="srx:head" mode="ac:ResultsTable">
        <thead>
            <tr>
                <xsl:apply-templates mode="#current"/>
            </tr>
        </thead>
    </xsl:template>

    <xsl:template match="srx:variable" mode="ac:ResultsTable">
        <th scope="col">
            <xsl:value-of select="@name"/>
        </th>
    </xsl:template>

    <xsl:template match="srx:results" mode="ac:ResultsTable">
        <tbody>
            <xsl:apply-templates mode="#current"/>
        </tbody>
    </xsl:template>

    <xsl:template match="srx:result" mode="ac:ResultsTable">
        <tr>
            <xsl:apply-templates mode="#current"/>
        </tr>
    </xsl:template>

    <xsl:template match="srx:binding" mode="ac:ResultsTable">
        <td>
            <xsl:apply-templates mode="#current"/>
        </td>
    </xsl:template>
    
    <xsl:template match="srx:uri" mode="ac:ResultsTable">
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

    <xsl:template match="text()[../@xml:lang]" mode="ac:PropertyListValue" priority="1">
        <dd>
            <xsl:apply-templates select="../@xml:lang" mode="ac:lang-tag"/>

            <xsl:apply-templates select="."/>
        </dd>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="ac:PropertyEditor">
        <xsl:apply-templates select="." mode="ac:PropertyListLabel"/>

        <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="ac:PropertyListValue"/>
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
                            <xsl:apply-templates select="key('resources', '&translations;add-stmt', ac:translations())" mode="ac:label"/>
                        </xsl:attribute>

                        <span class="msi sm" aria-hidden="true">add</span>
                    </button>
                </xsl:if>
                <xsl:if test="not($required)">
                    <button type="button" class="ldhc-iconbtn sz-xs in-destructive ap-ghost btn-remove-property">
                        <xsl:attribute name="title">
                            <xsl:apply-templates select="key('resources', '&translations;remove-stmt', ac:translations())" mode="ac:label"/>
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

    <!-- FIELD SHELL -->

    <!-- the design system's field shell: the single wrapper every text control rides in. The $control slot
         holds the pre-built input/textarea, $adorn an optional leading adornment; hidden inputs bypass the chrome -->
    <xsl:template match="node() | @*" mode="ac:FieldShell">
        <xsl:param name="control" as="item()*"/>
        <xsl:param name="type" as="xs:string?"/>
        <xsl:param name="size" select="'sz-sm'" as="xs:string"/>
        <xsl:param name="adorn" as="item()*"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="style" as="xs:string?"/>

        <xsl:choose>
            <xsl:when test="$type = 'hidden'">
                <xsl:sequence select="$control"/>
            </xsl:when>
            <xsl:otherwise>
                <div class="ldhc-field{if ($class) then ' ' || $class else ''}">
                    <xsl:if test="$style">
                        <xsl:attribute name="style" select="$style"/>
                    </xsl:if>

                    <div class="ldhc-field-box {$size}">
                        <xsl:sequence select="$adorn"/>
                        <xsl:sequence select="$control"/>
                    </div>
                </div>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- ALERT -->

    <!-- the design system's inline alert: variant, icon and title/text slots, plus a $body tail for
         links, technical detail or actions. Block-header alerts put their h2 in $body to keep the
         document outline -->
    <xsl:template match="node() | @*" mode="ac:Alert">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="variant" select="'va-negative'" as="xs:string"/>
        <xsl:param name="class" select="'ldhc-alert ' || $variant" as="xs:string"/>
        <xsl:param name="icon" select="(map{ 'va-negative': 'error', 'va-warning': 'warning', 'va-success': 'check_circle' }($variant), 'info')[1]" as="xs:string"/>
        <xsl:param name="title" as="item()*"/>
        <xsl:param name="text" as="item()*"/>
        <xsl:param name="body" as="item()*"/>

        <div role="alert">
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:attribute name="class" select="$class"/>

            <span class="ldhc-alert-ic">
                <span class="msi outline" aria-hidden="true">
                    <xsl:value-of select="$icon"/>
                </span>
            </span>
            <div class="ldhc-alert-body">
                <xsl:if test="exists($title)">
                    <span class="ldhc-alert-title">
                        <xsl:sequence select="$title"/>
                    </span>
                </xsl:if>
                <xsl:if test="exists($text)">
                    <span class="ldhc-alert-text">
                        <xsl:sequence select="$text"/>
                    </span>
                </xsl:if>

                <xsl:sequence select="$body"/>
            </div>
        </div>
    </xsl:template>

    <!-- SELECT SHELL -->

    <!-- the design system's select shell: the caret-adorned wrapper every dropdown rides in;
         multiple selects mark the shell and show no caret -->
    <xsl:template match="node() | @*" mode="ac:SelectShell">
        <xsl:param name="select" as="item()*"/>
        <xsl:param name="size" select="'sz-sm'" as="xs:string"/>
        <xsl:param name="multiple" select="false()" as="xs:boolean"/>

        <span class="ldhc-select {$size}{if ($multiple) then ' is-multiple' else ''}">
            <xsl:sequence select="$select"/>

            <xsl:if test="not($multiple)">
                <span class="msi sm ldhc-select-caret" aria-hidden="true">unfold_more</span>
            </xsl:if>
        </span>
    </xsl:template>

    <!-- IMAGE -->

    <!-- no-image fallback: it lives in this module, below every vocabulary ladder rule, so any vocab match
         beats it; without it the built-in rules would walk children in this mode and leak text into the
         image-URI sequences the ac:Depiction variables expect -->
    <xsl:template match="*" mode="ac:image"/>

    <!-- ANNOTATION TAG -->

    <!-- the single annotation Tag: the value-kind chips beside edit-mode controls all render through here,
         so a downstream layer re-skins every one by overriding this rule -->
    <xsl:template match="node() | @*" mode="ac:AnnotationTag">
        <xsl:param name="class" select="'ldhc-tag sz-sm em-quiet'" as="xs:string"/>
        <xsl:param name="key" as="xs:string?"/>
        <xsl:param name="title" as="xs:string?"/>
        <xsl:param name="label" as="item()*">
            <xsl:apply-templates select="key('resources', $key, ac:translations())" mode="ac:label"/>
        </xsl:param>

        <span class="{$class}">
            <xsl:if test="$title">
                <xsl:attribute name="title" select="$title"/>
            </xsl:if>

            <xsl:sequence select="$label"/>
        </span>
    </xsl:template>

    <!-- literal inputs ride the field shell: the box carries the chrome, the input stays bare -->
    <xsl:template match="text()" mode="ac:FormControl">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="ac:FieldShell">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="control" as="item()*">
                <xsl:apply-templates select="." mode="xhtml:Input">
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                    <xsl:with-param name="disabled" select="$disabled"/>
                </xsl:apply-templates>
            </xsl:with-param>
        </xsl:apply-templates>

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
                    <xsl:apply-templates select="." mode="ac:AnnotationTag">
                        <xsl:with-param name="key" select="'&translations;literal'"/>
                    </xsl:apply-templates>
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

        <xsl:apply-templates select="." mode="ac:FieldShell">
            <xsl:with-param name="control" as="item()*">
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
            </xsl:with-param>
        </xsl:apply-templates>

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="ac:ValueAnnotations"/>
        </xsl:if>
    </xsl:template>

    <!-- blank nodes that only have rdf:type xsd:* and no other properties become literal inputs -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID[key('resources', .)[not(* except rdf:type[starts-with(@rdf:resource, '&xsd;')])]]" mode="ac:FormControl" priority="2">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="required" select="false()" as="xs:boolean"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <!-- the field shell keeps this input on the shared control width; a bare input here sat at its
             UA-intrinsic width beside the capped fields -->
        <xsl:apply-templates select="." mode="ac:FieldShell">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="control" as="item()*">
                <xsl:call-template name="xhtml:Input">
                    <xsl:with-param name="name" select="'ol'"/>
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                    <xsl:with-param name="disabled" select="$disabled"/>
                </xsl:call-template>
            </xsl:with-param>
        </xsl:apply-templates>

        <!-- datatype -->
        <xsl:call-template name="xhtml:Input">
            <xsl:with-param name="name" select="'lt'"/>
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="value" select="key('resources', .)/rdf:type/@rdf:resource"/>
        </xsl:call-template>

        <xsl:if test="$type-label">
            <xsl:variable name="datatype" as="document-node()">
                <xsl:document>
                    <rdf:Description>
                        <xsl:element name="{../name()}" namespace="{../namespace-uri()}">
                            <xsl:attribute name="rdf:datatype" select="key('resources', .)/rdf:type/@rdf:resource"/>
                        </xsl:element>
                    </rdf:Description>
                </xsl:document>
            </xsl:variable>

            <xsl:apply-templates select="$datatype//@rdf:datatype" mode="ac:ValueAnnotations">
                <xsl:with-param name="type" select="$type"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID[key('resources', .)[not(* except rdf:type[starts-with(@rdf:resource, '&xsd;')])]]" mode="ac:ValueAnnotations" priority="2">
        <xsl:param name="type" as="xs:string?"/>

        <xsl:if test="not($type = 'hidden')">
            <xsl:apply-templates select="." mode="ac:AnnotationTag">
                <xsl:with-param name="key" select="'&translations;literal'"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <!-- special case for owl:NamedIndividual bnode instances which become typeaheads -->
    <xsl:template match="*[@rdf:nodeID]/*/@rdf:nodeID[key('resources', .)/rdf:type/@rdf:resource = '&owl;NamedIndividual']" mode="ac:FormControl" priority="2">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" select="'resource-typeahead typeahead'" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="required" select="false()" as="xs:boolean"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>
        <xsl:variable name="forClass" select="key('resources', .)/rdf:type/@rdf:resource" as="xs:anyURI"/>

        <xsl:apply-templates select="key('resources', .)" mode="ac:Typeahead">
            <xsl:with-param name="forClass" select="$forClass"/>
        </xsl:apply-templates>

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="ac:ValueAnnotations">
                <xsl:with-param name="type" select="$type"/>
                <xsl:with-param name="forClass" select="$forClass"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <!-- blank nodes that only have non-XSD rdf:type and no other properties become resource lookups -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID[key('resources', .)[not(* except rdf:type[not(starts-with(@rdf:resource, '&xsd;'))])]]" mode="ac:FormControl" priority="1">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" select="'resource-typeahead typeahead'" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="required" select="false()" as="xs:boolean"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>
        <xsl:param name="forClass" select="key('resources', .)/rdf:type/@rdf:resource" as="xs:anyURI*"/>

        <xsl:call-template name="ac:Lookup">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="forClass" select="$forClass"/>
        </xsl:call-template>

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="ac:ValueAnnotations">
                <xsl:with-param name="type" select="$type"/>
                <xsl:with-param name="forClass" select="$forClass"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <!-- object resource: committed values render as typeahead chips, open values as the combobox lookup -->
    <xsl:template match="@rdf:resource" mode="ac:FormControl">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="traversed-ids" as="xs:string*" tunnel="yes"/>
        <xsl:param name="inline" select="false()" as="xs:boolean" tunnel="yes"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>
        <xsl:param name="constructor" as="document-node()?"/>
        <xsl:param name="object-metadata" as="document-node()?" tunnel="yes"/>
        <xsl:param name="forClass" select="if ($constructor) then distinct-values(key('resources', key('resources-by-type', ../../rdf:type/@rdf:resource, $constructor)/*[concat(namespace-uri(), local-name()) = current()/../concat(namespace-uri(), local-name())]/@rdf:nodeID, $constructor)/rdf:type/@rdf:resource[not(. = '&rdfs;Class')]) else ()" as="xs:anyURI*"/>

        <xsl:choose>
            <xsl:when test="$type = 'hidden'">
                <xsl:apply-templates select="." mode="xhtml:Input">
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                    <xsl:with-param name="disabled" select="$disabled"/>
                </xsl:apply-templates>
            </xsl:when>
            <!-- object resource exists in the current document -->
            <xsl:when test="key('resources', .)">
                <xsl:apply-templates select="key('resources', .)" mode="ac:Typeahead">
                    <xsl:with-param name="forClass" select="$forClass"/>
                </xsl:apply-templates>

                <xsl:if test="$type-label">
                    <xsl:apply-templates select="." mode="ac:ValueAnnotations">
                        <xsl:with-param name="type" select="$type"/>
                        <xsl:with-param name="forClass" select="$forClass"/>
                    </xsl:apply-templates>
                </xsl:if>
            </xsl:when>
            <xsl:when test="exists($object-metadata)">
                <xsl:choose>
                    <xsl:when test="key('resources', ., $object-metadata)">
                        <xsl:apply-templates select="key('resources', ., $object-metadata)" mode="ac:Typeahead">
                            <xsl:with-param name="forClass" select="$forClass"/>
                        </xsl:apply-templates>

                        <xsl:if test="$type-label">
                            <xsl:apply-templates select="." mode="ac:ValueAnnotations">
                                <xsl:with-param name="type" select="$type"/>
                                <xsl:with-param name="forClass" select="$forClass"/>
                            </xsl:apply-templates>
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:call-template name="ac:Lookup">
                            <xsl:with-param name="value" select="."/>
                            <xsl:with-param name="forClass" select="$forClass"/>
                        </xsl:call-template>

                        <xsl:if test="$type-label">
                            <xsl:apply-templates select="." mode="ac:ValueAnnotations">
                                <xsl:with-param name="type" select="$type"/>
                            </xsl:apply-templates>
                        </xsl:if>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="ac:Lookup">
                    <xsl:with-param name="value" select="."/>
                    <xsl:with-param name="forClass" select="$forClass"/>
                </xsl:call-template>

                <xsl:if test="$type-label">
                    <xsl:apply-templates select="." mode="ac:ValueAnnotations">
                        <xsl:with-param name="type" select="$type"/>
                    </xsl:apply-templates>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@rdf:resource" mode="ac:ValueAnnotations">
        <xsl:param name="type" as="xs:string?"/>
        <xsl:param name="forClass" as="xs:anyURI*"/>

        <xsl:if test="not($type = 'hidden')">
            <xsl:apply-templates select="." mode="ac:AnnotationTag">
                <xsl:with-param name="class" select="'ldhc-tag sz-sm em-quiet an-term is-resource'"/>
                <xsl:with-param name="label" as="item()*">
                    <xsl:choose>
                        <xsl:when test="exists($forClass)">
                            <xsl:value-of select="$forClass ! ac:class-label(xs:anyURI(.))" separator=""/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates select="key('resources', '&translations;resource', ac:translations())" mode="ac:label"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:with-param>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <!-- object blank node -->
    <xsl:template match="*[@rdf:about]/*/@rdf:nodeID | *[@rdf:nodeID]/*/@rdf:nodeID" mode="ac:FormControl">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="traversed-ids" as="xs:string*" tunnel="yes"/>
        <xsl:param name="inline" select="false()" as="xs:boolean" tunnel="yes"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>
        <xsl:param name="constructor" as="document-node()?"/>
        <xsl:variable name="resource" select="key('resources', .)"/>

        <xsl:choose>
            <xsl:when test="$type = 'hidden'">
                <xsl:apply-templates select="." mode="xhtml:Input">
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                    <xsl:with-param name="disabled" select="$disabled"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:when test="$inline and $resource and not(. = $traversed-ids)">
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
            <xsl:when test="$resource">
                <xsl:variable name="forClass" select="if ($constructor) then distinct-values(key('resources', key('resources-by-type', ../../rdf:type/@rdf:resource, $constructor)/*[concat(namespace-uri(), local-name()) = current()/../concat(namespace-uri(), local-name())]/@rdf:nodeID, $constructor)/rdf:type/@rdf:resource[not(. = '&rdfs;Class')]) else ()" as="xs:anyURI*"/>
                <xsl:apply-templates select="$resource" mode="ac:Typeahead">
                    <xsl:with-param name="forClass" select="$forClass"/>
                </xsl:apply-templates>

                <xsl:if test="$type-label">
                    <xsl:apply-templates select="." mode="ac:ValueAnnotations">
                        <xsl:with-param name="type" select="$type"/>
                        <xsl:with-param name="forClass" select="$forClass"/>
                    </xsl:apply-templates>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
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
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID" mode="ac:ValueAnnotations">
        <xsl:param name="type" as="xs:string?"/>
        <xsl:param name="forClass" as="xs:anyURI*"/>

        <xsl:if test="not($type = 'hidden')">
            <xsl:apply-templates select="." mode="ac:AnnotationTag">
                <xsl:with-param name="class" select="'ldhc-tag sz-sm em-quiet an-term is-blank'"/>
                <xsl:with-param name="label" as="item()*">
                    <xsl:choose>
                        <xsl:when test="exists($forClass)">
                            <xsl:value-of select="$forClass ! ac:class-label(xs:anyURI(.))" separator=""/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates select="key('resources', '&translations;resource', ac:translations())" mode="ac:label"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:with-param>
            </xsl:apply-templates>
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
                <xsl:apply-templates select="key('resources', '&translations;language-tag', ac:translations())" mode="ac:label"/>
            </xsl:attribute>

            <xsl:apply-templates select="." mode="ac:FieldShell">
                <xsl:with-param name="type" select="$type"/>
                <xsl:with-param name="adorn" as="item()*">
                    <span class="ldhc-adorn"><span class="msi outline sm" aria-hidden="true">language</span></span>
                </xsl:with-param>
                <xsl:with-param name="control" as="item()*">
                    <xsl:apply-templates select="." mode="xhtml:Input">
                        <xsl:with-param name="type" select="$type"/>
                        <xsl:with-param name="id" select="$id"/>
                        <xsl:with-param name="disabled" select="$disabled"/>
                    </xsl:apply-templates>
                </xsl:with-param>
            </xsl:apply-templates>
        </span>
    </xsl:template>

    <xsl:template match="@xml:*[local-name() = 'lang']" mode="ac:ValueAnnotations">
        <xsl:param name="type" as="xs:string?"/>

        <xsl:if test="not($type = 'hidden')">
            <xsl:apply-templates select="." mode="ac:AnnotationTag">
                <xsl:with-param name="key" select="'&translations;language-tag'"/>
            </xsl:apply-templates>
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
            <xsl:apply-templates select="." mode="ac:AnnotationTag">
                <xsl:with-param name="title" select="."/>
                <xsl:with-param name="label" select="if (starts-with(., '&xsd;')) then 'xsd:' || substring-after(., '&xsd;') else string(.)"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <!-- LOOKUP -->

    <xsl:template name="ac:Lookup">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" select="'resource-typeahead typeahead'" as="xs:string?"/>
        <xsl:param name="value" as="xs:string?"/>
        <xsl:param name="list-class" select="'resource-typeahead typeahead'" as="xs:string"/>
        <xsl:param name="list-id" select="concat('ul-', $id)" as="xs:string"/>
        <xsl:param name="forClass" as="xs:anyURI*"/>

        <div class="ldhc-combobox sz-sm is-iri">
            <!-- data-for-class sits on the box, the input's parent, where the lookup handlers read it -->
            <div class="ldhc-cb-box">
                <xsl:if test="exists($forClass)">
                    <xsl:attribute name="data-for-class" select="string-join($forClass, ' ')"/>
                </xsl:if>

                <span class="msi outline sm" aria-hidden="true">search</span>
                <xsl:call-template name="xhtml:Input">
                    <xsl:with-param name="name" select="'ou'"/>
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                    <xsl:with-param name="value" select="$value"/>
                    <xsl:with-param name="autocomplete" select="false()"/>
                </xsl:call-template>
            </div>

            <div class="ldhc-cb-panel {$list-class}" id="{$list-id}" role="listbox" style="display: none;"></div>
        </div>
    </xsl:template>

    <!-- TYPEAHEAD -->

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="ac:Typeahead">
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" select="'cb-chip-btn add-typeahead'" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="title" select="(@rdf:about, @rdf:nodeID)[1]" as="xs:string?"/>
        <xsl:param name="forClass" as="xs:anyURI*"/>

        <span class="ldhc-cb-committed">
            <xsl:if test="exists($forClass)">
                <xsl:attribute name="data-for-class" select="string-join($forClass, ' ')"/>
            </xsl:if>

            <span class="ldhc-cb-chip">
                <span class="msi outline sm" aria-hidden="true">link</span>
                <span class="cb-chip-lbl">
                    <xsl:if test="$title">
                        <xsl:attribute name="title" select="$title"/>
                    </xsl:if>

                    <xsl:value-of>
                        <xsl:apply-templates select="." mode="ac:label"/>
                    </xsl:value-of>
                </span>
                <!-- the edit button carries the committed term's RDF/POST input, so re-picking replaces both together -->
                <button type="button">
                    <xsl:if test="$id">
                        <xsl:attribute name="id" select="$id"/>
                    </xsl:if>
                    <xsl:if test="$class">
                        <xsl:attribute name="class" select="$class"/>
                    </xsl:if>
                    <xsl:if test="$disabled">
                        <xsl:attribute name="disabled" select="'disabled'"/>
                    </xsl:if>
                    <xsl:if test="$title">
                        <xsl:attribute name="title" select="$title"/>
                    </xsl:if>

                    <span class="msi" aria-hidden="true">edit</span>

                    <xsl:if test="@rdf:about">
                        <input type="hidden" name="ou" value="{@rdf:about}"/>
                    </xsl:if>
                    <xsl:if test="@rdf:nodeID">
                        <input type="hidden" name="ob" value="{@rdf:nodeID}"/>
                    </xsl:if>
                </button>
            </span>
        </span>
    </xsl:template>

</xsl:stylesheet>