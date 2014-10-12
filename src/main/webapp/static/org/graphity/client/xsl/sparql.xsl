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
    <!ENTITY gc     "http://graphity.org/gc#">
    <!ENTITY gs     "http://graphity.org/gs#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY sd     "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY void   "http://rdfs.org/ns/void#">
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
xmlns:void="&void;"
xmlns:javaee="http://java.sun.com/xml/ns/javaee"
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

    <xsl:template match="rdf:RDF[$absolute-path = resolve-uri('sparql', $base-uri)]" mode="gc:ReadMode" priority="2">
	<form action="" method="get" id="query-form">
	    <xsl:apply-templates select="." mode="gc:QueryFormMode"/>
	</form>

	<xsl:if test="$query">
	    <xsl:apply-templates select="." mode="gc:QueryResultMode"/>
	</xsl:if>
    </xsl:template>

    <xsl:template match="rdf:RDF[$absolute-path = resolve-uri('sparql', $base-uri)][not($mode)]" mode="gc:StyleMode" priority="1">
        <xsl:next-match/>
        
        <link href="static/css/yasqe.css" rel="stylesheet" type="text/css"/>
    </xsl:template>
    
    <xsl:template match="rdf:RDF[$absolute-path = resolve-uri('sparql', $base-uri)]" mode="gc:QueryFormMode">
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

            <script src="static/js/yasqe.js" type="text/javascript"></script>
            <script type="text/javascript">
                <![CDATA[
                var yasqe = YASQE.fromTextArea(document.getElementById("query-string"), {persistent: null});
                ]]>
            </script>

            <div class="form-actions">
		<button type="submit" class="btn btn-primary">Query</button>
		<span class="help-inline">For all queries, the maximum number of results is set to <xsl:value-of select="key('init-param-by-name', '&gs;resultLimit', $config)/javaee:param-value"/>.</span>
	    </div>
	</fieldset>
    </xsl:template>

    <xsl:template match="rdf:RDF[$absolute-path = resolve-uri('sparql', $base-uri)]" mode="gc:QueryResultMode">
	<xsl:variable name="result-doc" select="document(concat($absolute-path, gc:query-string($endpoint-uri, $query, ())))"/>

	<!-- result of CONSTRUCT or DESCRIBE -->
	<xsl:if test="$result-doc/rdf:RDF">
	    <div class="nav row-fluid">
		<div class="btn-group pull-right">
		    <a href="{$endpoint-uri}?query={encode-for-uri($query)}" class="btn">Source</a>
		    <!--
		    <a href="{@rdf:about}{gc:query-string($endpoint-uri, $query, 'application/rdf+xml')}" class="btn">RDF/XML</a>
		    <a href="{@rdf:about}{gc:query-string($endpoint-uri, $query, 'text/turtle')}" class="btn">Turtle</a>
		    -->
		</div>
	    </div>

	    <xsl:apply-templates select="$result-doc/rdf:RDF/*" mode="gc:ListReadMode"/>
	</xsl:if>
	<!-- result of SELECT or ASK -->
	<xsl:if test="$result-doc/sparql:sparql">
	    <div class="nav row-fluid">
		<div class="btn-group pull-right">
		    <a href="{$endpoint-uri}?query={encode-for-uri($query)}" class="btn">Source</a>
		    <!--
		    <a href="{@rdf:about}{gc:query-string($endpoint-uri, $query, 'application/sparql-results+xml')}" class="btn">XML</a>
		    <a href="{@rdf:about}{gc:query-string($endpoint-uri, $query, 'application/sparql-results+json')}" class="btn">JSON</a>
		    -->
		</div>
	    </div>

	    <xsl:apply-templates select="$result-doc/sparql:sparql" mode="gc:TableMode"/>
	</xsl:if>
    </xsl:template>

    <xsl:template match="*[@rdf:about][$absolute-path = resolve-uri('sparql', $base-uri)]" mode="gc:ListReadMode" priority="2">
        <xsl:apply-templates select="." mode="gc:HeaderMode"/>

        <xsl:apply-templates select="." mode="gc:PropertyListMode"/>
    </xsl:template>

</xsl:stylesheet>