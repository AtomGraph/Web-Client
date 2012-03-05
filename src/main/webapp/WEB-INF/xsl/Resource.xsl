<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY gfb-app "http://graph.facebook.com/schema/application#">
    <!ENTITY oauth "http://tools.ietf.org/html/draft-ietf-oauth-v2-23#">
    <!ENTITY g-maps "http://maps.googleapis.com/maps/api/js">
    <!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
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
xmlns:g-maps="&g-maps;"
xmlns:oauth="&oauth;"
xmlns:geo="&geo;"
xmlns:php="http://php.net/xsl"
xmlns:url="java.net.URLEncoder"
exclude-result-prefixes="xsl xhtml xs g rdf rdfs php url gfb-app g-maps oauth geo">
<!-- xmlns:url="&java;java.net.URLEncoder" -->

    <xsl:import href="imports/default.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/void.xsl"/>
    <xsl:import href="imports/dbpedia-owl.xsl"/>
    <xsl:import href="imports/facebook.xsl"/>

    <xsl:output method="xml" encoding="UTF-8" indent="yes" omit-xml-declaration="yes" media-type="application/xhtml+xml" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" />

    <xsl:preserve-space elements="pre"/>

    <xsl:param name="uri" as="xs:anyURI?"/>
    <xsl:param name="base-uri" as="xs:anyURI"/>
    <xsl:param name="service-uri" as="xs:anyURI?"/>
    <xsl:param name="mode" as="xs:string?"/>
    <xsl:param name="action" select="false()"/>
    <xsl:param name="php-os"/>
    <xsl:param name="lang" select="'en'" as="xs:string"/>
    <xsl:param name="gfb-app:id" select="'121081534640971'" as="xs:string"/>
    <xsl:param name="g-maps:key" select="'AIzaSyATfQRHyNn8HBo7Obi3ytqybeSHoqAbRYA'" as="xs:string"/>
    <xsl:param name="oauth:redirect_uri" select="resolve-uri('oauth', $base-uri)" as="xs:anyURI"/>
    <xsl:param name="rdf:type" as="xs:string?"/>
    
    <xsl:variable name="resource" select="/"/>

    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    	
    <xsl:template match="rdf:RDF">
	<html>
	    <head>
		<title>Graphity 
		    <xsl:if test="$uri">
			- <xsl:value-of select="g:label($uri, /, $lang)"/>
		    </xsl:if>
		</title>
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
		<script type="text/javascript" src="http://maps.googleapis.com/maps/api/js?key={$g-maps:key}&amp;sensor=false">&#160;</script>
		<xsl:if test="//*[geo:lat and geo:long]">
		    <script type="text/javascript">
		      function initialize() {
			var myOptions = {
			<xsl:if test="key('resources', $uri)[geo:lat and geo:long]">
			  center: new google.maps.LatLng(<xsl:value-of select="key('resources', $uri)/geo:lat"/>, <xsl:value-of select="key('resources', $uri)/geo:long"/>),
			</xsl:if>
			  zoom: 8,
			  mapTypeId: google.maps.MapTypeId.ROADMAP
			};
			var map = new google.maps.Map(document.getElementById("map_canvas"),
			    myOptions);
		      }
		    </script>
		</xsl:if>
	    </head>
	    <body>
		<xsl:if test="//*[geo:lat and geo:long]">
		     <xsl:attribute name="onload">initialize();</xsl:attribute>
		</xsl:if>
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
			    <a href="/">Home</a>
			</li>
			<li>
			    <a href="https://www.facebook.com/dialog/oauth?client_id={encode-for-uri($gfb-app:id)}&amp;redirect_uri={encode-for-uri($oauth:redirect_uri)}">Facebook</a>
			</li>
			<li>
			    <a href="classes">Classes</a>
			</li>
			<li>
			    <a href="?uri={encode-for-uri($uri)}&amp;accept={encode-for-uri('application/rdf+xml')}">RDF/XML</a>
			</li>
			<li>
			    <a href="?uri={encode-for-uri($uri)}&amp;accept={encode-for-uri('text/turtle')}">Turtle</a>
			</li>
		    </ul>
		</form>

		<xsl:if test="//*[geo:lat and geo:long]">
		    <div id="map_canvas" style="width:100%; height:100%;">&#160;</div>
		</xsl:if>
		
		<xsl:choose>
		    <xsl:when test="$uri">
			<xsl:apply-templates select="key('resources', $uri)"/>
		    </xsl:when>
		    <xsl:when test="$uri and $mode = '&g;EditMode'">
			<xsl:apply-templates select="key('resources', $uri)" mode="g:EditMode"/>
		    </xsl:when>
		    <xsl:otherwise>
			<xsl:apply-templates>
			    <!-- <xsl:sort select="dc:title" data-type="text" order="ascending"/> -->
			    <!-- <xsl:sort select="@rdf:about | @rdf:nodeID" data-type="text" order="ascending"/> -->
			</xsl:apply-templates>
		    </xsl:otherwise>
		</xsl:choose>
	    </body>
	</html>
    </xsl:template>

    <!-- subject -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]">
	<h1>
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID"/>
	    <a href="?uri={encode-for-uri(@rdf:about)}&amp;mode={encode-for-uri('&g;EditMode')}">Edit</a>
	    <!-- &amp;{encode-for-uri('rdf:type')}={} -->
	</h1>
	<dl>
	    <xsl:apply-templates select="rdf:type"/>
	    <xsl:apply-templates select="*[not(self::rdf:type)][not(rdfs:domain(xs:anyURI(concat(namespace-uri(.), local-name(.)))) = current()/rdf:type/@rdf:resource)][not(@xml:lang) or lang($lang)]">
		<xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
	    </xsl:apply-templates>	    
	</dl>
<!-- <xsl:value-of select="rdfs:domain(.)"/> -->
	<xsl:apply-templates select="rdf:type/@rdf:resource[../../*/xs:anyURI(concat(namespace-uri(.), local-name(.))) = g:inDomainOf(.)]" mode="g:TypeMode">
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

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="g:EditMode">
	<h1>
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID"/>
	</h1>
	<form action="" method="post" enctype="multipart/form-data">
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="g:EditMode"/>
	    <dl>
		<xsl:apply-templates select="rdf:type" mode="g:EditMode"/>
		<xsl:apply-templates select="*[not(self::rdf:type)][not(@xml:lang) or lang($lang)]" mode="g:EditMode">
		    <xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
		</xsl:apply-templates>	    
	    </dl>
	</form>
	<hr/>
    </xsl:template>    

    <xsl:template match="rdf:type/@rdf:resource" mode="g:TypeMode">
	<xsl:variable name="in-domain-properties" select="../../*[xs:anyURI(concat(namespace-uri(.), local-name(.))) = g:inDomainOf(current()) or rdfs:domain(xs:anyURI(concat(namespace-uri(.), local-name(.)))) = xs:anyURI(current())][not(@xml:lang) or lang($lang)]"/>
	<h2>
	    <xsl:apply-imports/>
	</h2>
	<dl>
	    <xsl:choose>
		<xsl:when test="$mode = '&g;EditMode'">
		    <xsl:apply-templates select="$in-domain-properties" mode="g:EditMode">
			<xsl:with-param name="type" select="."/>
		    </xsl:apply-templates>
		</xsl:when>
		<xsl:otherwise>
		    <!-- <xsl:apply-templates select="../../*[xs:anyURI(concat(namespace-uri(.), local-name(.))) = g:inDomainOf(current())][not(@xml:lang) or lang($lang)]"> -->
		    <!-- <xsl:apply-templates select="../../*[rdfs:domain(xs:anyURI(concat(namespace-uri(.), local-name(.)))) = xs:anyURI(current())][not(@xml:lang) or lang($lang)]"> --> <!-- not(self::rdf:type) --> 
		    <xsl:apply-templates select="$in-domain-properties">
			<xsl:with-param name="type" select="."/>
		    </xsl:apply-templates>
		</xsl:otherwise>
	    </xsl:choose>
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
	</xsl:if>
	<xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="g:EditMode"/>
	<xsl:if test="position() = last()">
	    <dd>
		<button>Add</button>
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