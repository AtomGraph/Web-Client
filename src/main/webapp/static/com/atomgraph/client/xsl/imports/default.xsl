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
    <!ENTITY gp     "http://graphity.org/gp#">
    <!ENTITY gc     "http://atomgraph.com/client/ns#">
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
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:url="&java;java.net.URLDecoder"
exclude-result-prefixes="#all">

    <xsl:param name="gp:lang" select="'en'" as="xs:string"/>

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <xsl:key name="resources-by-uri" match="*[@rdf:about]" use="gc:uri/@rdf:resource"/>

    <!-- LIST ITEM -->
    
    <!-- more like gc:ListItemAnchor?? -->
    <xsl:template match="*[*][@rdf:about]" mode="xhtml:ListItem">
        <xsl:param name="active" as="xs:boolean?"/>
        
        <li>
            <xsl:if test="$active">
                <xsl:attribute name="class">active</xsl:attribute>
            </xsl:if>

            <a href="{@rdf:about}">
                <xsl:apply-templates select="." mode="gc:label"/>
            </a>
        </li>
    </xsl:template>
    
    <!-- OPTION MODE -->
    
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="xhtml:Option">
        <xsl:param name="selected" as="xs:boolean?"/>
        <xsl:param name="disabled" as="xs:boolean?"/>

        <option value="{@rdf:about | @rdf:nodeID}">
            <xsl:if test="$selected">
                <xsl:attribute name="selected">selected</xsl:attribute>
            </xsl:if>
            <xsl:if test="$disabled">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>
            <xsl:apply-templates select="." mode="gc:label"/>
        </option>
    </xsl:template>
    
    <!-- TABLE MODE -->
    
    <!--
    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:Table">
	<td>
	    <xsl:apply-templates select="." mode="xhtml:Anchor"/>
	</td>
    </xsl:template>
    -->

    <!-- INLINE MODE -->

    <xsl:template match="*[key('resources-by-uri', @rdf:about)/@rdf:about]" mode="xhtml:Anchor" priority="1">
	<a href="{key('resources-by-uri', @rdf:about)/@rdf:about}" title="{@rdf:about}">
	    <xsl:apply-templates select="." mode="gc:label"/>
	</a>
    </xsl:template>
    
    <!-- subject resource -->
    <xsl:template match="*[@rdf:about]" mode="xhtml:Anchor">
	<a href="{@rdf:about}" title="{@rdf:about}">
            <!--
	    <xsl:if test="substring-after(., concat($g:requestUri, '#'))">
		<xsl:attribute name="id"><xsl:value-of select="substring-after(., concat($g:requestUri, '#'))"/></xsl:attribute>
	    </xsl:if>	
            -->
	    <xsl:apply-templates select="." mode="gc:label"/>
	</a>
    </xsl:template>
    
    <xsl:template match="*[@rdf:nodeID]" mode="xhtml:Anchor">
	<span id="{@rdf:nodeID}" title="{@rdf:nodeID}">
	    <xsl:apply-templates select="." mode="gc:label"/>
	</span>
    </xsl:template>

    <!-- DEFAULT MODE -->
    
    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*">
	<xsl:variable name="this" select="concat(namespace-uri(), local-name())"/>
        
	<span title="{$this}">
	    <xsl:apply-templates select="." mode="gc:property-label"/>
	</span>
    </xsl:template>

    <xsl:template match="@rdf:resource[key('resources-by-uri', .)/@rdf:about]" priority="1">
	<a href="{key('resources-by-uri', .)/@rdf:about}" title="{.}">
           <xsl:apply-templates select="." mode="gc:object-label"/>
	</a>
    </xsl:template>

    <!-- object resource -->
    <xsl:template match="@rdf:resource | sparql:uri">
	<a href="{.}" title="{.}">
            <xsl:apply-templates select="." mode="gc:object-label"/>
	</a>
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

    <xsl:template match="text()[. castable as xs:date][../@rdf:datatype = '&xsd;date'] | sparql:literal[@datatype = '&xsd;date']" priority="1">
	<span title="{../@rdf:datatype}">
	    <xsl:value-of select="format-date(., '[D] [MNn] [Y]', $gp:lang, (), ())"/>
	</span>
    </xsl:template>

    <xsl:template match="text()[. castable as xs:dateTime][../@rdf:datatype = '&xsd;dateTime'] | sparql:literal[@datatype = '&xsd;dateTime']" priority="1">
	<!-- http://www.w3.org/TR/xslt20/#date-time-examples -->
	<!-- http://en.wikipedia.org/wiki/Date_format_by_country -->
	<span title="{../@rdf:datatype}">
	    <xsl:value-of select="format-dateTime(., '[D] [MNn] [Y] [H01]:[m01]', $gp:lang, (), ())"/>
	</span>
    </xsl:template>

    <!-- @rdf:datatype -->
    <xsl:template match="@rdf:*[local-name() = 'datatype'][starts-with(., '&xsd;')]" priority="1">
        <span class="help-inline" title="{.}">
            xsd:<xsl:value-of select="substring-after(., '&xsd;')"/>
        </span>
    </xsl:template>

    <!-- @rdf:datatype -->
    <xsl:template match="@rdf:*[local-name() = 'datatype']">
        <span class="help-inline" title="{.}">
            <xsl:value-of select="."/>
        </span>
    </xsl:template>
    
    <!-- TABLE PREDICATE MODE -->
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:TablePredicate">
        <xsl:sequence select="."/>
    </xsl:template>

    <!-- TABLE -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="xhtml:TableHeaderCell">
	<th>
            <xsl:apply-templates select="."/>
	</th>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:TableCell" priority="1">
        <xsl:param name="resource" as="element()"/>
        <xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>
        <xsl:variable name="predicate" select="$resource/*[concat(namespace-uri(), local-name()) = $this]"/>
        <xsl:choose>
            <xsl:when test="$predicate">
                <xsl:apply-templates select="$predicate" mode="xhtml:TableDataCell"/>
            </xsl:when>
            <xsl:otherwise>
                <td></td>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="xhtml:TableDataCell"/>

    <!-- apply properties that match lang() -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[lang($gp:lang)]" mode="xhtml:TableDataCell" priority="1">
	<td>
	    <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>
	</td>
    </xsl:template>
    
    <!-- apply the first one in the group if there's no lang() match -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[not(../*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))][lang($gp:lang)])][not(preceding-sibling::*[concat(namespace-uri(), local-name()) = concat(namespace-uri(current()), local-name(current()))])]" mode="xhtml:TableDataCell" priority="1">
	<td>
	    <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>
	</td>
    </xsl:template>

    <xsl:template match="sparql:sparql" mode="xhtml:Table">
	<table class="table table-bordered table-striped">
	    <xsl:apply-templates mode="#current"/>
	</table>
    </xsl:template>
    
    <xsl:template match="sparql:head" mode="xhtml:Table">
	<thead>
	    <tr>
		<xsl:apply-templates mode="#current"/>
	    </tr>
	</thead>
    </xsl:template>

    <xsl:template match="sparql:variable" mode="xhtml:Table">
	<th>
	    <xsl:value-of select="@name"/>
	</th>
    </xsl:template>

    <xsl:template match="sparql:results" mode="xhtml:Table">
	<tbody>
	    <xsl:apply-templates mode="#current"/>
	</tbody>
    </xsl:template>

    <xsl:template match="sparql:result" mode="xhtml:Table">
	<tr>
	    <xsl:apply-templates mode="#current"/>
	</tr>
    </xsl:template>

    <xsl:template match="sparql:binding" mode="xhtml:Table">
	<td>
	    <xsl:apply-templates mode="#current"/>
	</td>
    </xsl:template>
    
    <xsl:template match="sparql:uri" mode="xhtml:Table">
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
    <!-- @rdf:about -->
    <xsl:template match="@rdf:*[local-name() = 'about']" mode="xhtml:Input">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

	<xsl:call-template name="xhtml:Input">
	    <xsl:with-param name="name" select="'su'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- subject blank node -->
    <!-- @rdf:nodeID -->
    <xsl:template match="@rdf:*[local-name() = 'nodeID']" mode="xhtml:Input">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

	<xsl:call-template name="xhtml:Input">
	    <xsl:with-param name="name" select="'sb'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
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

	<xsl:call-template name="xhtml:Input">
	    <xsl:with-param name="name" select="'pu'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="concat(namespace-uri(), local-name())"/>
	</xsl:call-template>
    </xsl:template>
    
    <!-- object resource -->
    <!-- *[@rdf:about or @rdf:nodeID]/*/@rdf:resource -->
    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:*[local-name() = 'resource']" mode="xhtml:Input">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

	<xsl:call-template name="xhtml:Input">
	    <xsl:with-param name="name" select="'ou'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

    <!-- object blank node -->
    <!-- *[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID -->
    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:*[local-name() = 'nodeID']" mode="xhtml:Input" priority="1">
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>

	<xsl:call-template name="xhtml:Input">
	    <xsl:with-param name="name" select="'ob'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
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

	<xsl:call-template name="xhtml:Input">
	    <xsl:with-param name="name" select="'ol'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
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

	<xsl:call-template name="xhtml:Input">
	    <xsl:with-param name="name" select="'lt'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
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

	<xsl:call-template name="xhtml:Input">
	    <xsl:with-param name="name" select="'ll'"/>
	    <xsl:with-param name="type" select="$type"/>
	    <xsl:with-param name="id" select="$id"/>
	    <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
	    <xsl:with-param name="value" select="."/>
	</xsl:call-template>
    </xsl:template>

</xsl:stylesheet>