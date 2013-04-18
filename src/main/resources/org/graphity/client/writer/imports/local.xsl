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
    <!ENTITY ldp "http://www.w3.org/ns/ldp#">
    <!ENTITY dc "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
    <!ENTITY skos "http://www.w3.org/2004/02/skos/core#">
    <!ENTITY sp "http://spinrdf.org/sp#">
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:ldp="&ldp;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:skos="&skos;"
xmlns:sp="&sp;"
xmlns:dbpedia-owl="&dbpedia-owl;"
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
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<span title="{$this}">
	    <xsl:apply-templates select="." mode="gc:LabelMode"/>
	</span>
    </xsl:template>
	
    <!-- object blank node (avoid infinite loop) -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID">
	<xsl:apply-templates select="key('resources', .)[not(@rdf:nodeID = current()/../../@rdf:nodeID)]"/>
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
    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:LabelMode">
	<xsl:for-each select="..">
	    <xsl:choose>
		<xsl:when test="rdfs:label[lang($lang)]">
		    <xsl:value-of select="rdfs:label[lang($lang)][1]"/>
		</xsl:when>
		<xsl:when test="foaf:nick[lang($lang)]">
		    <xsl:value-of select="foaf:nick[lang($lang)][1]"/>
		</xsl:when>
		<xsl:when test="foaf:name[lang($lang)]">
		    <xsl:value-of select="foaf:name[lang($lang)][1]"/>
		</xsl:when>
		<xsl:when test="dc:title[lang($lang)]">
		    <xsl:value-of select="dc:title[lang($lang)][1]"/>
		</xsl:when>
		<xsl:when test="dct:title[lang($lang)]">
		    <xsl:value-of select="dct:title[lang($lang)][1]"/>
		</xsl:when>
		<xsl:when test="skos:prefLabel[lang($lang)]">
		    <xsl:value-of select="skos:prefLabel[lang($lang)][1]"/>
		</xsl:when>

		<xsl:when test="rdfs:label[not(@xml:lang)]">
		    <xsl:value-of select="rdfs:label[not(@xml:lang)][1]"/>
		</xsl:when>
		<xsl:when test="foaf:nick[not(@xml:lang)]">
		    <xsl:value-of select="foaf:nick[not(@xml:lang)][1]"/>
		</xsl:when>
		<xsl:when test="foaf:name[not(@xml:lang)]">
		    <xsl:value-of select="foaf:name[not(@xml:lang)][1]"/>
		</xsl:when>
		<xsl:when test="dc:title[not(@xml:lang)]">
		    <xsl:value-of select="dc:title[not(@xml:lang)][1]"/>
		</xsl:when>
		<xsl:when test="dct:title[not(@xml:lang)]">
		    <xsl:value-of select="dct:title[not(@xml:lang)][1]"/>
		</xsl:when>
		<xsl:when test="skos:prefLabel[not(@xml:lang)]">
		    <xsl:value-of select="skos:prefLabel[not(@xml:lang)][1]"/>
		</xsl:when>

		<xsl:when test="rdfs:label[lang('en')]">
		    <xsl:value-of select="rdfs:label[lang('en')][1]"/>
		</xsl:when>
		<xsl:when test="foaf:nick[lang('en')]">
		    <xsl:value-of select="foaf:nick[lang('en')][1]"/>
		</xsl:when>
		<xsl:when test="foaf:name[lang('en')]">
		    <xsl:value-of select="foaf:name[lang('en')][1]"/>
		</xsl:when>
		<xsl:when test="dc:title[lang('en')]">
		    <xsl:value-of select="dc:title[lang('en')][1]"/>
		</xsl:when>
		<xsl:when test="dct:title[lang('en')]">
		    <xsl:value-of select="dct:title[lang('en')][1]"/>
		</xsl:when>
		<xsl:when test="skos:prefLabel[lang('en')]">
		    <xsl:value-of select="skos:prefLabel[lang('en')][1]"/>
		</xsl:when>

		<xsl:when test="rdfs:label | @rdfs:label">
		    <xsl:value-of select="(rdfs:label | @rdfs:label)[1]"/>
		</xsl:when>
		<xsl:when test="foaf:nick | @foaf:nick">
		    <xsl:value-of select="(foaf:nick | @foaf:nick)[1]"/>
		</xsl:when>
		<xsl:when test="foaf:firstName and foaf:lastName">
		    <xsl:value-of select="concat(foaf:firstName[1], ' ', foaf:lastName[1])"/>
		</xsl:when>
		<xsl:when test="foaf:givenName and foaf:familyName">
		    <xsl:value-of select="concat(foaf:givenName[1], ' ', foaf:familyName[1])"/>
		</xsl:when>
		<xsl:when test="foaf:name | @foaf:name">
		    <xsl:value-of select="(foaf:name | @foaf:name)[1]"/>
		</xsl:when>
		<xsl:when test="dc:title | @dc:title">
		    <xsl:value-of select="(dc:title | @dc:title)[1]"/>
		</xsl:when>
		<xsl:when test="dct:title | @dct:title">
		    <xsl:value-of select="(dct:title | @dct:title)[1]"/>
		</xsl:when>
		<xsl:when test="skos:prefLabel | @skos:prefLabel">
		    <xsl:value-of select="(skos:prefLabel | @skos:prefLabel)[1]"/>
		</xsl:when>
		<xsl:when test="contains(@rdf:about, '#') and not(ends-with(@rdf:about, '#'))">
		    <xsl:value-of select="substring-after(@rdf:about, '#')"/>
		</xsl:when>
		<xsl:when test="string-length(tokenize(@rdf:about, '/')[last()]) &gt; 0">
		    <xsl:value-of select="translate(url:decode(tokenize(@rdf:about, '/')[last()], 'UTF-8'), '_', ' ')"/>
		</xsl:when>
		<xsl:otherwise>
		    <xsl:value-of select="@rdf:about | @rdf:nodeID"/>
		</xsl:otherwise>
	    </xsl:choose>
	</xsl:for-each>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:LabelMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<xsl:variable name="doc" select="document(namespace-uri())"/>
	<xsl:choose>
	    <xsl:when test="key('resources', $this, $doc)/@rdf:about">
		<xsl:apply-templates select="key('resources', $this, $doc)/@rdf:about" mode="gc:LabelMode"/>
	    </xsl:when>
	    <xsl:when test="key('resources', $this)">
		<xsl:apply-templates select="key('resources', $this)/@rdf:about" mode="gc:LabelMode"/>
	    </xsl:when>
	    <xsl:when test="contains($this, '#') and not(ends-with($this, '#'))">
		<xsl:value-of select="substring-after($this, '#')"/>
	    </xsl:when>
	    <xsl:when test="string-length(tokenize($this, '/')[last()]) &gt; 0">
		<xsl:value-of select="translate(url:decode(tokenize($this, '/')[last()], 'UTF-8'), '_', ' ')"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:value-of select="."/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <!-- object -->
    <xsl:template match="@rdf:resource | sparql:uri" mode="gc:LabelMode">
	<xsl:variable name="doc" select="document(gc:document-uri(.))"/>
	<xsl:choose>
	    <xsl:when test="key('resources', ., $doc)/@rdf:about">
		<xsl:apply-templates select="key('resources', ., $doc)/@rdf:about" mode="gc:LabelMode"/>
	    </xsl:when>
	    <xsl:when test="key('resources', ., $doc)/@rdf:nodeID">
		<xsl:apply-templates select="key('resources', ., $doc)/@rdf:nodeID" mode="gc:LabelMode"/>
	    </xsl:when>
	    <xsl:when test="key('resources', .)/@rdf:about">
		<xsl:apply-templates select="key('resources', .)/@rdf:about" mode="gc:LabelMode"/>
	    </xsl:when>
	    <xsl:when test="key('resources', .)/@rdf:nodeID">
		<xsl:apply-templates select="key('resources', .)/@rdf:nodeID" mode="gc:LabelMode"/>
	    </xsl:when>
	    <xsl:when test="contains(., '#') and not(ends-with(., '#'))">
		<xsl:value-of select="substring-after(., '#')"/>
	    </xsl:when>
	    <xsl:when test="string-length(tokenize(., '/')[last()]) &gt; 0">
		<xsl:value-of select="translate(url:decode(tokenize(., '/')[last()], 'UTF-8'), '_', ' ')"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:value-of select="."/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <!-- DESCRIPTION MODE -->
    
    <xsl:template match="@rdf:about | @rdf:nodeID" mode="gc:DescriptionMode">
	<xsl:for-each select="..">
	    <xsl:if test="rdfs:comment[lang($lang) or not(@xml:lang)] or dc:description[lang($lang) or not(@xml:lang)] or dct:description[lang($lang) or not(@xml:lang)] or dbpedia-owl:abstract[lang($lang) or not(@xml:lang)] or sioc:content[lang($lang) or not(@xml:lang)]">
		<p>
		    <xsl:choose>
			<xsl:when test="rdfs:comment[lang($lang)]">
			    <xsl:value-of select="substring(rdfs:comment[lang($lang)][1], 1, 300)"/>
			</xsl:when>
			<xsl:when test="dc:description[lang($lang)]">
			    <xsl:value-of select="substring(dc:description[lang($lang)][1], 1, 300)"/>
			</xsl:when>
			<xsl:when test="dct:description[lang($lang)]">
			    <xsl:value-of select="substring(dct:description[lang($lang)][1], 1, 300)"/>
			</xsl:when>
			<xsl:when test="dbpedia-owl:abstract[lang($lang)]">
			    <xsl:value-of select="substring(dbpedia-owl:abstract[lang($lang)][1], 1, 300)"/>
			</xsl:when>
			<xsl:when test="sioc:content[lang($lang)]">
			    <xsl:value-of select="substring(sioc:content[lang($lang)][1], 1, 300)"/>
			</xsl:when>

			<xsl:when test="rdfs:comment[not(@xml:lang)]">
			    <xsl:value-of select="substring(rdfs:comment[not(@xml:lang)][1], 1, 300)"/>
			</xsl:when>
			<xsl:when test="dc:description[not(@xml:lang)]">
			    <xsl:value-of select="substring(dc:description[not(@xml:lang)][1], 1, 300)"/>
			</xsl:when>
			<xsl:when test="dct:description[not(@xml:lang)]">
			    <xsl:value-of select="substring(dct:description[not(@xml:lang)][1], 1, 300)"/>
			</xsl:when>
			<xsl:when test="dbpedia-owl:abstract[not(@xml:lang)]">
			    <xsl:value-of select="substring(dbpedia-owl:abstract[not(@xml:lang)][1], 1, 300)"/>
			</xsl:when>
			<xsl:when test="sioc:content[not(@xml:lang)]">
			    <xsl:value-of select="substring(sioc:content[not(@xml:lang)][1], 1, 300)"/>
			</xsl:when>
		    </xsl:choose>
		</p>
	    </xsl:if>
	</xsl:for-each>
    </xsl:template>

    <!-- HEADER MODE -->
    
    <xsl:template match="@rdf:about[. = $absolute-path]" mode="gc:HeaderMode">
	<xsl:apply-templates select="." mode="gc:MediaTypeSelectMode"/>

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
	<div class="btn-group pull-right">
	    <!--
	    <xsl:if test="@rdf:about != $absolute-path">
		<a href="{@rdf:about}" class="btn">Source</a>
	    </xsl:if>
	    -->
	    <xsl:if test="$query-res/sp:text">
		<a href="{resolve-uri('sparql', $base-uri)}?query={encode-for-uri($query-res/sp:text)}" class="btn">SPARQL</a>
	    </xsl:if>
	    <a href="?uri={encode-for-uri(.)}&amp;accept={encode-for-uri('application/rdf+xml')}" class="btn">RDF/XML</a>
	    <a href="?uri={encode-for-uri(.)}&amp;accept={encode-for-uri('text/turtle')}" class="btn">Turtle</a>
	</div>
    </xsl:template>

    <!-- IMAGE MODE -->

    <xsl:template match="*" mode="gc:HeaderImageMode"/>
    
    <xsl:template match="*" mode="gc:ListImageMode"/>

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
	<xsl:apply-templates select="key('resources', .)[not(@rdf:nodeID = current()/../../@rdf:nodeID)]"/>
    </xsl:template>

    <!-- TABLE HEADER MODE -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:TableHeaderMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>

	<th>
	    <a href="{$absolute-path}{gc:query-string($offset, $limit, $this, $desc, $mode)}" title="{$this}">
		<xsl:apply-templates select="." mode="gc:LabelMode"/>
	    </a>
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
    
    <xsl:template match="rdfs:seeAlso | owl:sameAs | dc:subject | dct:subject" mode="gc:SidebarNavMode" priority="1">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>
	
	<div class="well sidebar-nav">
	    <h2 class="nav-header">
		<xsl:apply-templates select="."/>
	    </h2>
		
	    <!-- TO-DO: fix for a single resource! -->
	    <ul class="nav nav-pills nav-stacked">
		<xsl:for-each-group select="key('predicates', $this)" group-by="@rdf:resource">
		    <xsl:sort select="gc:label(@rdf:resource, /, $lang)" data-type="text" order="ascending" lang="{$lang}"/>
		    <xsl:apply-templates select="current-group()[1]/@rdf:resource" mode="gc:SidebarNavMode"/>
		</xsl:for-each-group>
	    </ul>
	</div>
    </xsl:template>

    <!-- ignore all other properties -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="gc:SidebarNavMode"/>

    <xsl:template match="rdfs:seeAlso/@rdf:resource | owl:sameAs/@rdf:resource | dc:subject/@rdf:resource | dct:subject/@rdf:resource" mode="gc:SidebarNavMode">
	<li>
	    <xsl:apply-templates select="."/>
	</li>
    </xsl:template>

    <!-- INPUT MODE -->
    
    <xsl:template name="gc:InputTemplate">
	<xsl:param name="name" as="xs:string"/>
	<xsl:param name="type" select="'text'" as="xs:string"/>
	<xsl:param name="id" as="xs:string?"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="style" as="xs:string?"/>
	<xsl:param name="value" as="xs:string?"/>

	<xsl:choose>
	    <!-- special case to give more input space for object literals -->
	    <xsl:when test="$name = 'ol'">
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

</xsl:stylesheet>