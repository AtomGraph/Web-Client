<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
]>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:url="&java;java.net.URLEncoder"
xmlns:php="http://php.net/xsl"
exclude-result-prefixes="xsl xhtml g rdf php java">

    <xsl:import href="imports/rdf.xsl"/>
    <xsl:import href="imports/dbpedia-owl.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>

    <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" media-type="application/xhtml+xml" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" />

    <xsl:preserve-space elements="pre"/>

    <xsl:param name="uri"/>
    <xsl:param name="base-uri"/>
    <xsl:param name="view"/>
    <xsl:param name="action" select="false()"/>
    <xsl:param name="php-os"/>
    <xsl:param name="fb-app-id" select="'264143360289485'"/>
    <xsl:param name="lang" select="'en'"/>

    <xsl:variable name="resource" select="/"/>
    <xsl:variable name="lower-case" select="'abcdefghijklmnopqrstuvwxyz'" />
    <xsl:variable name="upper-case" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

    <xsl:key name="resources" match="*[@rdf:about] | *[@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <!--
    <xsl:key name="resources-by-domain" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:domain/@rdf:resource"/>
    <xsl:key name="resources-by-type" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdf:type/@rdf:resource"/>
    <xsl:key name="resources-by-is-part-of" match="*[@rdf:about] | *[@rdf:nodeID]" use="dct:isPartOf/@rdf:resource"/>
    <xsl:key name="resources-by-issued-date" match="*[@rdf:about] | *[@rdf:nodeID]" use="dct:issued"/>
    <xsl:key name="resources-by-container" match="*[@rdf:about] | *[@rdf:nodeID]" use="sioc:has_container/@rdf:resource"/>
    <xsl:key name="resources-by-subject" match="*[@rdf:about] | *[@rdf:nodeID]" use="dc:subject/@rdf:resource"/>
    <xsl:key name="uri-resources" match="*[@rdf:about]" use="@rdf:about"/>
    <xsl:key name="bnodes" match="*[@rdf:nodeID]" use="@rdf:nodeID"/>
    <xsl:key name="properties" match="*" use="concat(namespace-uri(.), local-name(.))"/>
    <xsl:key name="object-properties" match="*[@rdf:resource or */@rdf:about]" use="concat(namespace-uri(.), local-name(.))"/>
    <xsl:key name="datatype-properties" match="*[*]" use="concat(namespace-uri(.), local-name(.))"/>
    -->
    
    <xsl:template match="rdf:RDF">
	<html>
	    <head>
		<title>Graphity</title>
	    </head>
	    <body>
		<form action="" method="get">
		    <input type="text" name="uri" value="{$uri}" size="60"/>
		    <button type="submit">Browse</button>
		</form>
		<xsl:apply-templates select="key('resources', $uri)"/>
		<!-- <xsl:apply-templates select="*[@rdf:about != $uri]"> -->
		    <!-- <xsl:sort select="dc:title" data-type="text" order="ascending"/> -->
		    <!-- <xsl:sort select="@rdf:about | @rdf:nodeID" data-type="text" order="ascending"/> -->
		<!-- </xsl:apply-templates> -->
	    </body>
	</html>
    </xsl:template>

    <!-- subject -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]">
	<h1>
	    <xsl:apply-templates select="@rdf:about" mode="g:label"/> <!-- what about nodeID? -->
	</h1>
	<dl>
	    <xsl:apply-templates select="rdf:type"/>
	    <xsl:apply-templates select="*[not(concat(namespace-uri(.), local-name(.)) = '&rdf;type')][not(@xml:lang) or lang($lang)]">
		<xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
	    </xsl:apply-templates>
	</dl>
	<hr/>
    </xsl:template>    

    <!-- property -->
    <xsl:template match="*[@rdf:about]/* | *[@rdf:nodeID]/*">
	<xsl:variable name="uri" select="concat(namespace-uri(.), local-name(.))"/>
	<!-- do not repeat property name if it's the same as the previous one -->
	<xsl:if test="not(concat(namespace-uri(preceding-sibling::*[1]), local-name(preceding-sibling::*[1])) = $uri)">
	    <dt>
		<a href="{$base-uri}?uri={url:encode($uri, 'UTF-8')}">
		    <xsl:apply-templates select="." mode="g:label"/>
		</a>
	    </dt>
	</xsl:if>
	<xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>
    </xsl:template>

    <!-- object -->
    <xsl:template match="*[@rdf:about]/*/@rdf:resource | *[@rdf:nodeID]/*/@rdf:resource | *[@rdf:about]/*/@rdf:nodeID | *[@rdf:nodeID]/*/@rdf:nodeID | *[@rdf:about]/*/text() | *[@rdf:nodeID]/*/text()">
	<dd>
	    <xsl:apply-imports/>
	</dd>
    </xsl:template>
	
</xsl:stylesheet>