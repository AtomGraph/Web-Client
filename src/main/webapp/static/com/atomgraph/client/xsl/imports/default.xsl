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
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:url="&java;java.net.URLDecoder"
exclude-result-prefixes="#all">

    <xsl:param name="ldt:lang" select="'en'" as="xs:string"/>

    <xsl:key name="resources" match="*[*][@rdf:*[local-name() = 'about']] | *[*][@rdf:*[local-name() = 'nodeID']]" use="@rdf:*[local-name() = 'about'] | @rdf:*[local-name() = 'nodeID']"/>

    <!-- DEFINITIONS -->
    
    <xsl:template match="*[@rdf:*[local-name() = 'about'] or @rdf:*[local-name() = 'nodeID']]/*" mode="xhtml:DefinitionTerm">
        <dt>
            <xsl:apply-templates select="."/>
        </dt>
    </xsl:template>
    
    <xsl:template match="node() | @rdf:*[local-name() = 'resource'] | @rdf:*[local-name() = 'nodeID']" mode="xhtml:DefinitionDescription">
        <dd>
            <xsl:apply-templates select="."/>
        </dd>
    </xsl:template>
    
    <!-- OPTION MODE -->
    
    <xsl:template match="*[*][@rdf:*[local-name() = 'about']] | *[*][@rdf:*[local-name() = 'nodeID']]" mode="xhtml:Option">
        <xsl:param name="value" select="@rdf:*[local-name() = 'about'] | @rdf:*[local-name() = 'nodeID']" as="xs:string?"/>
        <xsl:param name="selected" as="xs:boolean?"/>
        <xsl:param name="disabled" as="xs:boolean?"/>

        <option>
            <xsl:if test="$value">
                <xsl:attribute name="value"><xsl:value-of select="$value"/></xsl:attribute>
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

    <!--
    <xsl:template match="*[key('resources-by-uri', @rdf:*[local-name() = 'about'])/@rdf:*[local-name() = 'about']]" mode="xhtml:Anchor" priority="1">
        <a href="{key('resources-by-uri', @rdf:*[local-name() = 'about'])/@rdf:*[local-name() = 'about']}" title="{@rdf:*[local-name() = 'about']}">
            <xsl:apply-templates select="." mode="ac:label"/>
        </a>
    </xsl:template>
    -->
    
    <!-- subject resource -->
    <xsl:template match="*[@rdf:*[local-name() = 'about']]" mode="xhtml:Anchor">
        <xsl:param name="href" select="@rdf:*[local-name() = 'about']" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="@rdf:*[local-name() = 'about']" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>

        <a href="{$href}">
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:value-of select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:apply-templates select="." mode="ac:label"/>
        </a>
    </xsl:template>
    
    <xsl:template match="*[@rdf:*[local-name() = 'nodeID']]" mode="xhtml:Anchor">
        <xsl:param name="id" select="@rdf:*[local-name() = 'nodeID']" as="xs:string"/>
        <xsl:param name="title" select="@rdf:*[local-name() = 'nodeID']" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>

        <span id="{$id}">
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:value-of select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:apply-templates select="." mode="ac:label"/>
        </span>
    </xsl:template>

    <!-- DEFAULT MODE -->
    
    <!-- property -->
    <xsl:template match="*[@rdf:*[local-name() = 'about'] or @rdf:*[local-name() = 'nodeID']]/*">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="concat(namespace-uri(), local-name())" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:value-of select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:apply-templates select="." mode="ac:property-label"/>
        </span>
    </xsl:template>

    <!--
    <xsl:template match="@rdf:*[local-name() = 'resource'][key('resources-by-uri', .)/@rdf:*[local-name() = 'about']]" priority="1">
        <a href="{key('resources-by-uri', .)/@rdf:*[local-name() = 'about']}" title="{.}">
           <xsl:apply-templates select="." mode="ac:object-label"/>
        </a>
    </xsl:template>
    -->

    <!-- object URI resource -->
    <xsl:template match="@rdf:*[local-name() = 'resource'] | srx:uri">
        <xsl:param name="href" select="." as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <a href="{$href}">
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:value-of select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:apply-templates select="." mode="ac:object-label"/>
        </a>
    </xsl:template>

    <!-- object blank node -->
    <xsl:template match="@rdf:*[local-name() = 'nodeID']">
        <xsl:param name="href" select="xs:anyURI(concat('#', .))" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <a href="{$href}">
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:value-of select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:apply-templates select="." mode="ac:object-label"/>
        </a>
    </xsl:template>
    
    <!-- object literal -->
    <xsl:template match="text()">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype] | srx:literal[@datatype]">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="../@rdf:datatype | @datatype" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:value-of select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:value-of select="."/>
        </span>
    </xsl:template>

    <xsl:template match="text()[../@rdf:datatype = '&xsd;float'] | text()[../@rdf:datatype = '&xsd;double'] | srx:literal[@datatype = '&xsd;float'] | srx:literal[@datatype = '&xsd;double']" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="../@rdf:datatype" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:value-of select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:value-of select="format-number(., '#####.00')"/>
        </span>
    </xsl:template>

    <xsl:template match="text()[. castable as xs:date][../@rdf:datatype = '&xsd;date'] | srx:literal[@datatype = '&xsd;date']" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="../@rdf:datatype" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:value-of select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:value-of select="format-date(., '[D] [MNn] [Y]', $ldt:lang, (), ())"/>
        </span>
    </xsl:template>

    <xsl:template match="text()[. castable as xs:dateTime][../@rdf:datatype = '&xsd;dateTime'] | srx:literal[@datatype = '&xsd;dateTime']" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="../@rdf:datatype" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:value-of select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <!-- http://www.w3.org/TR/xslt20/#date-time-examples -->
            <!-- http://en.wikipedia.org/wiki/Date_format_by_country -->
            <xsl:value-of select="format-dateTime(., '[D] [MNn] [Y] [H01]:[m01]', $ldt:lang, (), ())"/>
        </span>
    </xsl:template>

    <!-- @rdf:datatype -->
    <xsl:template match="@rdf:*[local-name() = 'datatype'][starts-with(., '&xsd;')]" priority="1">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" select="'help-inline'" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:value-of select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
            xsd:<xsl:value-of select="substring-after(., '&xsd;')"/>
        </span>
    </xsl:template>

    <!-- @rdf:datatype -->
    <xsl:template match="@rdf:*[local-name() = 'datatype']">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="title" select="." as="xs:string?"/>
        <xsl:param name="class" select="'help-inline'" as="xs:string?"/>
        
        <span>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:value-of select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            
            <xsl:value-of select="."/>
        </span>
    </xsl:template>
    
    <!-- TABLE PREDICATE MODE -->
    
    <xsl:template match="*[@rdf:*[local-name() = 'about'] or @rdf:*[local-name() = 'nodeID']]/*" mode="ac:TablePredicate">
        <xsl:sequence select="."/>
    </xsl:template>

    <!-- TABLE -->

    <xsl:template match="*[@rdf:*[local-name() = 'about'] or @rdf:*[local-name() = 'nodeID']]/*" mode="xhtml:TableHeaderCell">
        <th>
            <xsl:apply-templates select="."/>
        </th>
    </xsl:template>
    
    <xsl:template match="*[@rdf:*[local-name() = 'about'] or @rdf:*[local-name() = 'nodeID']]/*" mode="xhtml:TableDataCell"/>

    <!-- apply properties that match lang() -->
    <xsl:template match="*[@rdf:*[local-name() = 'about'] or @rdf:*[local-name() = 'nodeID']]/*[lang($ldt:lang)]" mode="xhtml:TableDataCell" priority="1">
        <td>
            <xsl:apply-templates select="node() | @rdf:*[local-name() = 'resource'] | @rdf:*[local-name() = 'nodeID']"/>
        </td>
    </xsl:template>
    
    <!-- apply the first one in the group if there's no lang() match -->
    <xsl:template match="*[@rdf:*[local-name() = 'about'] or @rdf:*[local-name() = 'nodeID']]/*[not(../*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))][lang($ldt:lang)])][not(preceding-sibling::*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))])]" mode="xhtml:TableDataCell" priority="1">
        <td>
            <xsl:apply-templates select="node() | @rdf:*[local-name() = 'resource'] | @rdf:*[local-name() = 'nodeID']"/>
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
        <a href="{.}" title="{.}">
            <xsl:value-of select="."/>
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
            <xsl:if test="$title">
                <xsl:attribute name="title"><xsl:value-of select="$title"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$value">
                <xsl:attribute name="value"><xsl:value-of select="$value"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$checked">
                <xsl:attribute name="checked">checked</xsl:attribute>
            </xsl:if>
        </input>
    </xsl:template>

    <!-- subject resource -->
    <!-- @rdf:*[local-name() = 'about'] -->
    <xsl:template match="@rdf:*[local-name() = 'about']" mode="xhtml:Input">
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
    <!-- @rdf:*[local-name() = 'nodeID'] -->
    <xsl:template match="@rdf:*[local-name() = 'nodeID']" mode="xhtml:Input">
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
    <!-- *[@rdf:*[local-name() = 'about'] or @rdf:*[local-name() = 'nodeID']]/* -->
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
    <!-- *[@rdf:*[local-name() = 'about'] or @rdf:*[local-name() = 'nodeID']]/*/@rdf:*[local-name() = 'resource'] -->
    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:*[local-name() = 'resource']" mode="xhtml:Input">
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
    <!-- *[@rdf:*[local-name() = 'about'] or @rdf:*[local-name() = 'nodeID']]/*/@rdf:*[local-name() = 'nodeID'] -->
    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:*[local-name() = 'nodeID']" mode="xhtml:Input" priority="1">
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
    <!-- *[@rdf:*[local-name() = 'about'] or @rdf:*[local-name() = 'nodeID']]/*/text() -->
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