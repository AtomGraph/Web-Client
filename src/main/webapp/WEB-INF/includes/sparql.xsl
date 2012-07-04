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
    <!ENTITY sd "http://www.w3.org/ns/sparql-service-description#">
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
xmlns:sd="&sd;"
exclude-result-prefixes="xsl xhtml xs g rdf rdfs owl sparql sd">

    <xsl:param name="default-query" as="xs:string">PREFIX rdf: &lt;&rdf;&gt;
PREFIX rdfs: &lt;&rdfs;&gt;
PREFIX owl: &lt;&owl;&gt;
PREFIX xsd: &lt;&xsd;&gt;

SELECT DISTINCT *
WHERE
{
    GRAPH ?g
    { ?s ?p ?o }
}
LIMIT 100</xsl:param>

    <xsl:template match="*[@rdf:about = resolve-uri('sparql', $base-uri)]" priority="1">
	<xsl:choose>
	    <xsl:when test="$service-uri">
		<div class="well">
		    <h1>
			<a href="{$absolute-path}?service-uri={encode-for-uri($service-uri)}">
			    <xsl:value-of select="g:label($service-uri, /, $lang)"/>
			</a>
		    </h1>
		</div>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:next-match/>
	    </xsl:otherwise>
	</xsl:choose>
	
	<form action="" method="get" id="query-form">
	    <fieldset>
		<div class="control-group">
		    <label for="endpoint-uri" class="control-label">
			<xsl:value-of select="g:label(xs:anyURI('&sd;endpoint'), /, $lang)"/>
		    </label>
		    <input type="text" name="service-uri" id="endpoint-uri" value="{$service-uri}" class="input-xlarge"/>
		</div>
		<textarea id="query-string" name="query" class="span12" rows="10">
		    <xsl:choose>
			<xsl:when test="$query">
			    <xsl:value-of select="$query"/>
			</xsl:when>
			<xsl:otherwise>
			    <xsl:value-of select="$default-query"/>
			</xsl:otherwise>
		    </xsl:choose>
		</textarea>
		<div class="form-actions">
		    <button type="submit" class="btn btn-primary">Query</button>
		    <em>For all queries, the maximum number of results (<span style="font-family: monospace;">LIMIT</span>) is set to 100.</em>
		</div>
	    </fieldset>
	</form>

	<!--
	<xsl:if test="$query">
	    <iframe frameborder="0" src="{$base-uri}sparql?query={encode-for-uri($query)}{if ($service-uri) then (concat('&amp;service-uri=', encode-for-uri($service-uri))) else ()}&amp;mode=EmbedMode"/>
	</xsl:if>
	-->
    </xsl:template>

</xsl:stylesheet>