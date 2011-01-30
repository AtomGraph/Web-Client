<?xml version="1.0"?> 
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:results="http://www.w3.org/2005/sparql-results#"
		xmlns:xsltsparql="http://berrueta.net/research/xsltsparql"
                version="2.0">

  <xsl:function name="xsltsparql:sparqlEndpoint">
    <xsl:param name="query"/>
    <xsl:param name="endpointUrl"/>
    <xsl:variable name="encodedQuery"
		  select="encode-for-uri($query)"/>
    <xsl:variable name="requestedUrl"
		  select="concat($endpointUrl,'?query=',$encodedQuery)"/>
    <xsl:variable name="endpointResponse"
		  select="document($requestedUrl)"/>
    <xsl:if test="not($endpointResponse/results:sparql)">
      <xsl:message>
	The response does not contain &lt;sparql:sparql&gt; in the
	root!
      </xsl:message>
      <xsl:message>
	Results: <xsl:copy-of select="$endpointResponse"/>
      </xsl:message>
    </xsl:if>
    <xsl:copy-of select="$endpointResponse/results:sparql"/>
  </xsl:function>

  <xsl:function name="xsltsparql:commonPrefixes">
    <xsl:value-of select="concat(
      'PREFIX owl: &lt;http://www.w3.org/2002/07/owl#&gt; ',
      'PREFIX xsd: &lt;http://www.w3.org/2001/XMLSchema#&gt; ',
      'PREFIX iswc: &lt;http://annotation.semanticweb.org/iswc/iswc.daml#&gt; ',
      'PREFIX rdfs: &lt;http://www.w3.org/2000/01/rdf-schema#&gt; ',
      'PREFIX dctype: &lt;http://purl.org/dc/dcmitype/&gt; ',
      'PREFIX rdf: &lt;http://www.w3.org/1999/02/22-rdf-syntax-ns#&gt; ',
      'PREFIX foaf: &lt;http://xmlns.com/foaf/0.1/&gt; ',
      'PREFIX dc: &lt;http://purl.org/dc/elements/1.1/&gt; ', 
      'PREFIX doap: &lt;http://usefulinc.com/ns/doap#&gt; ', 
      'PREFIX vcard: &lt;http://www.w3.org/2001/vcard-rdf/3.0#&gt; ',
      'PREFIX skos: &lt;http://www.w3.org/2004/02/skos/core#&gt; ',
      'PREFIX swrc: &lt;http://swrc.ontoware.org/ontology#&gt; ')" />
  </xsl:function>

</xsl:stylesheet>