<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY java "http://xml.apache.org/xalan/java/">
]>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:rdf="&rdf;"
xmlns:foaf="&foaf;"
xmlns:url="&java;java.net.URLEncoder"
exclude-result-prefixes="rdf foaf url">

    <xsl:template match="foaf:img/@rdf:resource | foaf:depiction/@rdf:resource | foaf:thumbnail/@rdf:resource | foaf:logo/@rdf:resource">
	<dd>
	    <a href="{$base-uri}?uri={url:encode(., 'UTF-8')}">
		<img src="{.}" alt=""/>
	    </a>
	</dd>
    </xsl:template>

</xsl:stylesheet>