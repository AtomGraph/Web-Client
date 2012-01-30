<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY gfb-app "http://graph.facebook.com/schema/application#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:url="&java;java.net.URLEncoder"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:gfb-app="&gfb-app;"
exclude-result-prefixes="url g rdf rdfs gfb-app">
    
    <xsl:template match="gfb-app:icon_url/@rdf:resource | gfb-app:logo_url/@rdf:resource">
	<dd>
	    <a href="{$base-uri}?uri={url:encode(., 'UTF-8')}">
		<img src="{.}" alt=""/>
	    </a>
	</dd>
    </xsl:template>

</xsl:stylesheet>