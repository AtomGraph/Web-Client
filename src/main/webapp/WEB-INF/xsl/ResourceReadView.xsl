<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY gfb-app "http://graph.facebook.com/schema/application#">
    <!ENTITY oauth "http://tools.ietf.org/html/draft-ietf-oauth-v2-23#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:gfb-app="&gfb-app;"
xmlns:oauth="&oauth;"
xmlns:php="http://php.net/xsl"
xmlns:url="java.net.URLEncoder"
exclude-result-prefixes="xsl xhtml g rdf php url">
<!-- xmlns:url="&java;java.net.URLEncoder" -->

    <xsl:import href="imports/default.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/void.xsl"/>
    <xsl:import href="imports/dbpedia-owl.xsl"/>
    <xsl:import href="imports/facebook.xsl"/>

    <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" media-type="application/xhtml+xml" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" />

    <xsl:preserve-space elements="pre"/>

    <xsl:param name="uri"/>
    <xsl:param name="base-uri"/>
    <xsl:param name="service-uri" select="false()"/>
    <xsl:param name="view" as="xs:anyURI"/>
    <xsl:param name="action" select="false()"/>
    <xsl:param name="php-os"/>
    <!-- <xsl:param name="fb-app-id" select="'264143360289485'"/> -->
    <xsl:param name="lang" select="'en'" as="xs:string"/>
    <xsl:param name="gfb-app:id" select="'121081534640971'" as="xs:string"/>
    <xsl:param name="oauth:redirect_uri" select="resolve-uri('oauth', $base-uri)" as="xs:anyURI"/>
    
    <xsl:variable name="resource" select="/"/>

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    <!--
    <xsl:key name="resources" match="*[@rdf:about] | *[@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
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
		<base href="{$base-uri}" />
		<script type="text/javascript">
		<![CDATA[
		  var _gaq = _gaq || [];
		  _gaq.push(['_setAccount', 'UA-1004105-8']);
		  _gaq.push(['_trackPageview']);

		  (function() {
		    var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
		    ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
		    var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
		  })();
		  ]]>
		</script>
	    </head>
	    <body>
		<form action="" method="get">
		    <fieldset>
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
		    </fieldset>
		    <ul>
			<li>
			    <a href="https://www.facebook.com/dialog/oauth?client_id={encode-for-uri($gfb-app:id)}&amp;redirect_uri={encode-for-uri($oauth:redirect_uri)}">Facebook</a>
			</li>
			<li>
			    <a href="classes">Classes</a>
			</li>
		    </ul>
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
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID"/>
	</h1>
	<dl>
	    <xsl:apply-templates select="rdf:type"/>
	    <xsl:apply-templates select="*[not(self::rdf:type)][not(@xml:lang) or lang($lang)]">
		<xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
	    </xsl:apply-templates>	    
	</dl>
<!-- <xsl:value-of select="rdfs:domain(.)"/> -->
	<xsl:apply-templates select="rdf:type/@rdf:resource[not(empty(g:inDomainOf(.)))]" mode="g:TypeMode">
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

    <xsl:template match="rdf:type/@rdf:resource" mode="g:TypeMode">
	<xsl:variable name="this" select="."/>
	<h2>
	    <xsl:apply-imports/>
	</h2>
	<dl>
	    <!-- <xsl:apply-templates select="../../*[xs:anyURI(concat(namespace-uri(.), local-name(.))) = g:inDomainOf(current())][not(@xml:lang) or lang($lang)]"> -->
	    <!-- <xsl:apply-templates select="../../*[rdfs:domain(xs:anyURI(concat(namespace-uri(.), local-name(.)))) = xs:anyURI(current())][not(@xml:lang) or lang($lang)]"> --> <!-- not(self::rdf:type) --> 
	    <xsl:apply-templates select="../../*[xs:anyURI(concat(namespace-uri(.), local-name(.))) = g:inDomainOf(current()) or rdfs:domain(xs:anyURI(concat(namespace-uri(.), local-name(.)))) = xs:anyURI(current())][not(@xml:lang) or lang($lang)]">
		<xsl:with-param name="type" select="$this"/>
	    </xsl:apply-templates>
	</dl>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<xsl:if test="not(concat(namespace-uri(preceding-sibling::*[1]), local-name(preceding-sibling::*[1])) = $this)">
	    <!-- @xml:lang = preceding-sibling::*[1]/@xml:lang -->
	    <dt>
		<xsl:apply-imports/>
	    </dt>
	</xsl:if>
	<xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="g:EditMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<xsl:if test="not(concat(namespace-uri(preceding-sibling::*[1]), local-name(preceding-sibling::*[1])) = $this)">
	    <!-- @xml:lang = preceding-sibling::*[1]/@xml:lang -->
	    <dt>
		<xsl:apply-imports/>
	    </dt>
	</xsl:if>
	<xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="g:EditMode"/>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*[@rdf:resource or @rdf:nodeID]" mode="g:EditMode">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
	<xsl:if test="not(concat(namespace-uri(preceding-sibling::*[1]), local-name(preceding-sibling::*[1])) = $this)">
	    <!-- @xml:lang = preceding-sibling::*[1]/@xml:lang -->
	    <dt>
		<xsl:apply-imports/>
	    </dt>
	    <dd>
		<select name="ou">
		    <xsl:apply-templates select="following-sibling::*[concat(namespace-uri(), local-name()) = $this]/@rdf:*" mode="g:EditMode"/>
		</select>
	    </dd>
	</xsl:if>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:resource | *[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID | *[@rdf:about or @rdf:nodeID]/*/text()">
	<dd>
	    <xsl:apply-imports/>
	</dd>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:resource | *[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID | *[@rdf:about or @rdf:nodeID]/*/text()" mode="g:EditMode">
	<dd>
	    <xsl:apply-imports/>
	</dd>
    </xsl:template>

</xsl:stylesheet>