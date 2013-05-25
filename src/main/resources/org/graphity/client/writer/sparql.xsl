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
    <!ENTITY gc "http://client.graphity.org/ontology#">
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
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:sd="&sd;"
exclude-result-prefixes="#all">

    <xsl:param name="default-query" as="xs:string">PREFIX rdf: &lt;&rdf;&gt;
PREFIX rdfs: &lt;&rdfs;&gt;
PREFIX owl: &lt;&owl;&gt;
PREFIX xsd: &lt;&xsd;&gt;

SELECT DISTINCT *
WHERE
{
    { ?s ?p ?o }
    UNION
    {
	GRAPH ?g
	{ ?s ?p ?o }
    }
}
LIMIT 100</xsl:param>

    <!-- *[@rdf:about = resolve-uri(concat('sparql?=', encode-for-uri($query)), $base-uri)] -->
    <xsl:template match="*[@rdf:about = resolve-uri('sparql', $base-uri)]" priority="1">
	<xsl:apply-templates select="." mode="gc:HeaderMode"/>

	<form action="" method="get" id="query-form">
	    <xsl:apply-templates select="." mode="gc:QueryFormMode"/>
	</form>

	<xsl:if test="$query">
	    <xsl:apply-templates select="." mode="gc:QueryResultMode"/>
	</xsl:if>
    </xsl:template>

    <xsl:template match="*[@rdf:about = resolve-uri('sparql', $base-uri)]" mode="gc:QueryFormMode">
	<fieldset>
	    <textarea id="query-string" name="query" class="span12" rows="15">
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
		<span class="help-inline">For all queries, the maximum number of results is set to 100.</span>
	    </div>
	</fieldset>
    </xsl:template>

    <xsl:template match="*[@rdf:about = resolve-uri('sparql', $base-uri)]" mode="gc:QueryResultMode">
	<xsl:variable name="result-doc" select="document(concat($absolute-path, '?query=', encode-for-uri($query)))"/>

	<!-- result of CONSTRUCT or DESCRIBE -->
	<xsl:if test="$result-doc/rdf:RDF">
	    <div class="nav row-fluid">
		<div class="btn-group pull-right">
		    <a href="{@rdf:about}?query={encode-for-uri($query)}&amp;accept={encode-for-uri('application/rdf+xml')}" class="btn">RDF/XML</a>
		    <a href="{@rdf:about}?query={encode-for-uri($query)}&amp;accept={encode-for-uri('text/turtle')}" class="btn">Turtle</a>
		</div>
	    </div>

	    <xsl:apply-templates select="$result-doc/rdf:RDF" mode="gc:ListMode"/>
	</xsl:if>
	<!-- result of SELECT or ASK -->
	<xsl:if test="$result-doc/sparql:sparql">
	    <div class="nav row-fluid">
		<div class="btn-group pull-right">
		    <a href="{@rdf:about}?query={encode-for-uri($query)}&amp;accept={encode-for-uri('application/sparql-results+xml')}" class="btn">XML</a>
		    <a href="{@rdf:about}?query={encode-for-uri($query)}&amp;accept={encode-for-uri('application/sparql-results+json')}" class="btn">JSON</a>
		</div>
	    </div>
	    <xsl:apply-templates select="$result-doc/sparql:sparql"/>
	</xsl:if>
    </xsl:template>
    
    <xsl:template match="sparql:sparql">
	<table class="table table-bordered table-striped">
	    <xsl:apply-templates/>
	</table>
    </xsl:template>
    
    <xsl:template match="sparql:head">
	<thead>
	    <tr>
		<xsl:apply-templates/>
	    </tr>
	</thead>
    </xsl:template>

    <xsl:template match="sparql:variable">
	<th>
	    <xsl:value-of select="@name"/>
	</th>
    </xsl:template>

    <xsl:template match="sparql:results">
	<tbody>
	    <xsl:apply-templates/>
	</tbody>
    </xsl:template>

    <xsl:template match="sparql:result">
	<tr>
	    <xsl:apply-templates/>
	</tr>
    </xsl:template>

    <xsl:template match="sparql:binding">
	<td>
	    <xsl:apply-templates/>
	</td>
    </xsl:template>

</xsl:stylesheet>