<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:dbpedia-owl="&dbpedia-owl;"
exclude-result-prefixes="g rdf rdfs dbpedia-owl">

    <xsl:template match="dbpedia-owl:thumbnail/@rdf:resource">
	<a href="{$base-uri}?uri={encode-for-uri(.)}">
	    <img src="{.}" alt=""/>
	</a>
    </xsl:template>

</xsl:stylesheet>