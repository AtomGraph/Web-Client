<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
]>
<xsl:stylesheet version="1.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
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
    <xsl:param name="service-uri" select="false()"/>
    <xsl:param name="view"/>
    <xsl:param name="action" select="false()"/>
    <xsl:param name="php-os"/>
    <xsl:param name="fb-app-id" select="'264143360289485'"/>
    <xsl:param name="lang" select="'en'"/>

    <xsl:variable name="resource" select="/"/>
    <xsl:variable name="lower-case" select="'abcdefghijklmnopqrstuvwxyz'" />
    <xsl:variable name="upper-case" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <!--
    <xsl:key name="resources" match="*[@rdf:about] | *[@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
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
		    <label for="uri">URI</label>
		    <input type="text" id="uri" name="uri" value="{$uri}" size="60"/>
		    <label for="service-uri">SPARQL endpoint</label>
		    <input type="text" id="service-uri" name="service-uri" size="60">
			<xsl:if test="$service-uri">
			    <xsl:attribute name="value">
				<xsl:value-of select="$service-uri"/>
			    </xsl:attribute>
			</xsl:if>
		    </input>
		    <button type="submit">Browse</button>
		</form>
		<xsl:apply-templates select="key('resources', $uri)"/>
		<xsl:apply-templates select="*[@rdf:about != $uri]">
		    <!-- <xsl:sort select="dc:title" data-type="text" order="ascending"/> -->
		    <!-- <xsl:sort select="@rdf:about | @rdf:nodeID" data-type="text" order="ascending"/> -->
		</xsl:apply-templates>
	    </body>
	</html>
    </xsl:template>

    <!-- subject -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]">
	<h1>
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="g:label"/> <!-- what about nodeID? -->
	</h1>
	<dl>
	    <xsl:apply-templates select="rdf:type"/>
	    <xsl:apply-templates select="*[not(self::rdf:type)][not(@xml:lang) or lang($lang)]">
		<xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
	    </xsl:apply-templates>	    
	</dl>
	<xsl:apply-templates select="rdf:type/@rdf:resource" mode="g:type">
	    <!-- <xsl:sort select="@rdf:resource | @rdf:nodeID" data-type="text" order="ascending"/> -->
	</xsl:apply-templates>
	<!--
	<dl>
	    <xsl:apply-templates select="rdf:type"/>
	    <xsl:apply-templates select="*[not(self::rdf:type)][not(@xml:lang) or lang($lang)]">
		<xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
	    </xsl:apply-templates>
	</dl>
	-->
	<hr/>
    </xsl:template>    

    <xsl:template match="rdf:type/@rdf:resource" mode="g:type">
	<xsl:variable name="this" select="."/>
	<h2>
	    <xsl:apply-imports/>
	</h2>
	<dl>
	    <xsl:apply-templates select="../../*[not(self::rdf:type)][not(@xml:lang) or lang($lang)]">
		<xsl:with-param name="type" select="$this"/>
	    </xsl:apply-templates>
	</dl>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*">
	<xsl:param name="type" select="false()"/>
	<xsl:variable name="this" select="concat(namespace-uri(.), local-name(.))"/>
	<xsl:variable name="domain">
	    <xsl:apply-templates select="." mode="rdfs:domain"/>
	</xsl:variable>
	<xsl:if test="(not($type) and not(boolean($domain)))or $type = $domain">
	    <!-- do not repeat property name if it's the same as the previous one -->
	    <xsl:if test="not(concat(namespace-uri(preceding-sibling::*[1]), local-name(preceding-sibling::*[1])) = $this)">
		<!-- @xml:lang = preceding-sibling::*[1]/@xml:lang -->
		<dt>
		    <a href="{$base-uri}?uri={url:encode($this, 'UTF-8')}">
			<xsl:apply-templates select="." mode="g:label"/>
		    </a>
		</dt>
	    </xsl:if>
	    <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>
	</xsl:if>
    </xsl:template>

    <!-- object -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:resource | *[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID | *[@rdf:about or @rdf:nodeID]/*/text()">
	<dd>
	    <xsl:apply-imports/>
	</dd>
    </xsl:template>
	
</xsl:stylesheet>