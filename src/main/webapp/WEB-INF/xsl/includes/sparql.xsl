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
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl "http://www.w3.org/2002/07/owl#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:uuid="java:java.util.UUID"
exclude-result-prefixes="xsl xhtml xs g rdf rdfs owl sparql uuid">
<!-- xmlns:url="&java;java.net.URLEncoder" -->

    <xsl:variable name="default-query">
	<xsl:text>PREFIX rdf: &lt;&rdf;&gt;
PREFIX rdfs: &lt;&rdfs;&gt;
PREFIX owl: &lt;&owl;&gt;
PREFIX xsd: &lt;&xsd;&gt;</xsl:text>
	<xsl:text>

SELECT DISTINCT *
WHERE
{
    GRAPH ?g
    { ?s ?p ?o }
}
LIMIT 100</xsl:text>
    </xsl:variable>

    <xsl:template match="*[@rdf:about = resolve-uri('/sparql', $base-uri)]" priority="1">
	<xsl:variable name="subject-uri" select="resolve-uri(concat('/queries/', uuid:randomUUID()), $base-uri)" as="xs:anyURI"/>

	<xsl:choose>
	    <xsl:when test="key('resources-by-endpoint', $service-uri, $ont-model)">
		<xsl:apply-templates select="key('resources-by-endpoint', $service-uri, $ont-model)" mode="g:ListMode"/>
	    </xsl:when>
	    <xsl:when test="$service-uri">
		<div class="box main-info clearfix">
		    <h1>
			<a href="{$absolute-path}?service-uri={encode-for-uri($service-uri)}">
			    <xsl:value-of select="g:label($service-uri, /, $lang)"/>
			</a>
		    </h1>
		</div>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:apply-templates select="." mode="g:ListMode"/>
	    </xsl:otherwise>
	</xsl:choose>

	<form action="" method="get" id="query-form">
	    <p>
		<textarea cols="80" rows="20" id="query-string" name="query">
		    <xsl:choose>
			<xsl:when test="$query">
			    <xsl:value-of select="$query"/>
			</xsl:when>
			<xsl:otherwise>
			    <xsl:value-of select="$default-query"/>
			</xsl:otherwise>
		    </xsl:choose>
		</textarea>
	    </p>
	    <p>
		<xsl:if test="$service-uri">
		    <input type="hidden" name="service-uri" value="{$service-uri}"/>
		</xsl:if>
		<input type="hidden" name="mode" value="&g;TableMode"/>
		<button type="submit">Query</button>
		<em style="padding-left: 5px;">For all queries, the maximum number of results (<span style="font-family: monospace;">LIMIT</span>) is set to 100.</em>
	    </p>
	</form>

	<xsl:if test="$query">
	    <iframe frameborder="0" class="query-results" src="{$base-uri}sparql?query={encode-for-uri($query)}{if ($service-uri) then (concat('&amp;service-uri=', encode-for-uri($service-uri))) else ()}&amp;mode=EmbedMode"/>
	</xsl:if>

	<xsl:if test="$query">
	    <!--
	    <form action="" method="post">
		<p>
		    <input type="hidden" name="rdf"/>

		    <input type="hidden" name="su" value="{$subject-uri}"/>
		    <input type="hidden" name="pu" value="&dct;created"/>
		    <input type="hidden" name="lt" value="&xsd;dateTime"/>
		    <input type="hidden" name="ol" value="{current-dateTime()}"/>
		    <input type="hidden" name="pu" value="&dc;title"/>
		    <input type="hidden" name="lt" value="&xsd;string"/>
		    
		    <label for="query-title">
			<xsl:value-of select="g:label(xs:anyURI('&dc;title'), /, $lang)"/>
		    </label>
		    <input id="query-title" type="text" name="ol"/>
		    <br/>

		    <input type="hidden" name="pu" value="&dc;description"/>
		    <input type="hidden" name="lt" value="&xsd;string"/>
		    <label for="query-desc">
			<xsl:value-of select="g:label(xs:anyURI('&dc;description'), /, $lang)"/>
		    </label>
		    <br/>
		    <textarea cols="80" rows="20" id="query-desc" name="ol"/>

		    <input type="hidden" name="pu" value="&rdf;type"/>
		    <input type="hidden" name="ou" value="&sp;Query"/>
		    <input type="hidden" name="pu" value="&sp;text"/>
		    <input type="hidden" name="lt" value="&xsd;string"/>
		    <input type="hidden" name="ol" value="{$query}"/>
		</p>
		<p>
    <input type="hidden" name="sb" value="this"/>
    <input type="hidden" name="pu" value="&g;action"/>
    <input type="hidden" name="ou" value="&g;QueryAction"/>

		    <button type="submit">Save</button>
		</p>
	    </form>
	    -->
	    <!--
	    <fieldset id="visualizations">
		<legend>Visualizations</legend>

		<ul id="vis-types">
		    <xsl:for-each select="key('resources-by-domain', '&sp;Select', document('http://graphity.org/ontologies/visualizations'))">
			<xsl:apply-templates select="key('resources-by-subclass', rdfs:range/@rdf:resource)" mode="ClassPropertyListMode">
			    <xsl:with-param name="subject-uri" select="$subject-uri"/>
			    <xsl:with-param name="predicate-uri" select="@rdf:about"/>
			</xsl:apply-templates>
		    </xsl:for-each>
		</ul>
	    </fieldset>
	    -->
	</xsl:if>
    </xsl:template>

</xsl:stylesheet>