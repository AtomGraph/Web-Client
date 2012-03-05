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
    <!ENTITY dbpedia-owl "http://dbpedia.org/ontology/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
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
xmlns:dbpedia-owl="&dbpedia-owl;"
xmlns:foaf="&foaf;"
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
    <xsl:param name="php-os" as="xs:string?"/>
    <xsl:param name="lang" select="'en'" as="xs:string"/>
    <xsl:param name="gfb-app:id" select="'121081534640971'" as="xs:string"/>
    <xsl:param name="g-maps:key" select="'AIzaSyATfQRHyNn8HBo7Obi3ytqybeSHoqAbRYA'" as="xs:string"/>
    <xsl:param name="oauth:redirect_uri" select="resolve-uri('oauth', $base-uri)" as="xs:anyURI"/>
    <xsl:param name="rdf:type" as="xs:string?"/>
    
    <xsl:variable name="resource" select="/"/>
    <xsl:variable name="img-properties" select="('&foaf;depiction')" as="xs:string*"/>
    
    <xsl:key name="resources" match="*[*][@rdf:about] | *[*][@rdf:nodeID]" use="@rdf:about | @rdf:nodeID"/>
    	
    <xsl:template match="rdf:RDF">
	<html>
	    <head>
		<title>Graphity</title>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
		<link href="static/css/core.css" rel="stylesheet" type="text/css" media="screen" />
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
<!--
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
		    <div id="map_canvas" style="width:100%; height:100%; background-color: red;">&#160;</div>
		</xsl:if>
		
		<xsl:choose>
		    <xsl:when test="$mode = '&g;EditMode'">
			<xsl:apply-templates select="key('resources', $uri)" mode="g:EditMode"/>
			<xsl:apply-templates select="*[@rdf:about != $uri]">
			</xsl:apply-templates>			
		    </xsl:when>
		    <xsl:otherwise>			
			<xsl:apply-templates select="key('resources', $uri)"/>
			<xsl:apply-templates select="*[@rdf:about != $uri]">
			</xsl:apply-templates>
		    </xsl:otherwise>
		</xsl:choose>
-->

<div id="header">
	<div id="header-in">
		<a href="/" id="logo">Graphity</a>
		<div id="search">
			<xsl:if test="$service-uri = 'http://de.dydra.com/graphity/browser/sparql'">
			    <span class="lbl">Graphity</span>
			</xsl:if>
			<form action="" method="get">
				<input type="text" name="uri" value="{$uri}"/>
				<input type="submit" value="" class="button" />
			</form>
		</div>
		<!--
		<div id="languages">
			<div class="current">Lietuviškai</div>
			<ul>
				<li><a href="#">English</a></li>
				<li><a href="#">По-русски</a></li>
			</ul>
		</div>
		<div id="user">
			<div class="person"><div class="pic"><img src="static/img/temp/user.jpg" width="28" height="28" /></div></div>
			<ul>
				<li><a href="#">Profile</a></li>
				<li><a href="#">Lorem ipsum</a></li>
				<li><a href="#">Logout</a></li>
			</ul>
		</div>
		-->
	</div>
</div>
<div id="nav1">
	<ul>
		<li id="npeople"><a href="#">People</a></li>
		<li id="nphotos"><a href="#">Photos</a></li>
		<li id="nlocations" class="on"><a href="#">Locations</a></li>
		<li id="nproducts"><a href="#">Products</a></li>
		<li id="nads"><a href="#">Ads</a></li>
		<li id="nreports"><a href="#">Reports</a></li>
	</ul>
</div>
<div id="wrapper">
	<div id="wrapper-in" class="clearfix">
	    <xsl:choose>
		<xsl:when test="$uri">
		    
		    <div class="grid2">
			<xsl:apply-templates select="key('resources', $uri)"/>
			<!-- <xsl:apply-templates select="*[not(@rdf:about = $uri)]"/> -->
		    </div>

		    <div class="grid1">
			    <!-- NAVIGATION -->
			    <h3 class="hd2">Navigate</h3>
			    <div id="nav2">
				    <a href="#" class="goback">Back</a>
				    <form action="">
					    <select><option>- City -</option></select>
					    <select><option>- Title -</option></select>
				    </form>
				    <a href="#" class="gofwd">Forward</a>
			    </div>
			    <!-- end of NAVIGATION -->

			    <!-- SIMILAR -->
			    <h3 class="hd2">Similar</h3>
			    <ul class="list-other">
				    <li class="item">
					    <span class="pic"><a href="#"><img src="static/img/temp/pic.jpg" width="37" height="37" /></a></span>
					    <span class="title"><a href="#">Kaunas</a></span>
					    <ul class="whatis clearfix">
						    <li><a href="#">City</a></li>
						    <li><a href="#">Location</a></li>
					    </ul>
				    </li>
				    <li class="item">
					    <span class="pic"><a href="#"><img src="static/img/temp/pic2.jpg" width="37" height="37" /></a></span>
					    <span class="title"><a href="#">Klaipėda</a></span>
					    <ul class="whatis clearfix">
						    <li><a href="#">City</a></li>
						    <li><a href="#">Location</a></li>
						    <li><a href="#">Port</a></li>
					    </ul>
				    </li>
				    <li class="item">
					    <span class="pic"><a href="#"><img src="static/img/temp/pic3.jpg" width="37" height="37" /></a></span>
					    <span class="title"><a href="#">Riga</a></span>
					    <ul class="whatis clearfix">
						    <li><a href="#">City</a></li>
						    <li><a href="#">Location</a></li>
						    <li><a href="#">Port</a></li>
					    </ul>
				    </li>
			    </ul>
			    <!-- end of SIMILAR -->

			    <!-- ELSEWHERE -->
			    <h3 class="hd2">Elsewhere</h3>
			    <ul class="list-other">
				    <li class="item">
					    <span class="pic"><a href="#"><img src="static/img/temp/pic.jpg" width="37" height="37" /></a></span>
					    <span class="title"><a href="#">Vilnius</a></span>
					    <span class="subtitle">Facebook</span>
				    </li>
				    <li class="item">
					    <span class="pic"><a href="#"><img src="static/img/temp/pic.jpg" width="37" height="37" /></a></span>
					    <span class="title"><a href="#">@Vilnius</a></span>
					    <span class="subtitle">Twitter</span>
				    </li>
				    <li class="item">
					    <span class="pic"><a href="#"><img src="static/img/temp/pic.jpg" width="37" height="37" /></a></span>
					    <span class="title"><a href="#">Vilnius</a></span>
					    <span class="subtitle">YouTube</span>
				    </li>
			    </ul>
			    <!-- end of ELSEWHERE -->

			    <a href="#"><img src="static/img/temp/ad.jpg" width="250" height="250" /></a>
		    </div>
		    
		</xsl:when>
		<xsl:otherwise>
		    <div class="grid1">
			    <!-- NAVIGATION -->
			    <h3 class="hd2">Navigate</h3>
			    <div id="nav2">
				    <a href="#" class="goback">Back</a>
				    <form action="">
					    <select><option>- City -</option></select>
					    <select><option>- Title -</option></select>
				    </form>
				    <a href="#" class="gofwd">Forward</a>
			    </div>
			    <!-- end of NAVIGATION -->

			    <!-- SIMILAR -->
			    <h3 class="hd2">Similar</h3>
			    <ul class="list-other">
				    <li class="item">
					    <span class="pic"><a href="#"><img src="static/img/temp/pic.jpg" width="37" height="37" /></a></span>
					    <span class="title"><a href="#">Kaunas</a></span>
					    <ul class="whatis clearfix">
						    <li><a href="#">City</a></li>
						    <li><a href="#">Location</a></li>
					    </ul>
				    </li>
				    <li class="item">
					    <span class="pic"><a href="#"><img src="static/img/temp/pic2.jpg" width="37" height="37" /></a></span>
					    <span class="title"><a href="#">Klaipėda</a></span>
					    <ul class="whatis clearfix">
						    <li><a href="#">City</a></li>
						    <li><a href="#">Location</a></li>
						    <li><a href="#">Port</a></li>
					    </ul>
				    </li>
				    <li class="item">
					    <span class="pic"><a href="#"><img src="static/img/temp/pic3.jpg" width="37" height="37" /></a></span>
					    <span class="title"><a href="#">Riga</a></span>
					    <ul class="whatis clearfix">
						    <li><a href="#">City</a></li>
						    <li><a href="#">Location</a></li>
						    <li><a href="#">Port</a></li>
					    </ul>
				    </li>
			    </ul>
			    <!-- end of SIMILAR -->

			    <!-- ELSEWHERE -->
			    <h3 class="hd2">Elsewhere</h3>
			    <ul class="list-other">
				    <li class="item">
					    <span class="pic"><a href="#"><img src="static/img/temp/pic.jpg" width="37" height="37" /></a></span>
					    <span class="title"><a href="#">Vilnius</a></span>
					    <span class="subtitle">Facebook</span>
				    </li>
				    <li class="item">
					    <span class="pic"><a href="#"><img src="static/img/temp/pic.jpg" width="37" height="37" /></a></span>
					    <span class="title"><a href="#">@Vilnius</a></span>
					    <span class="subtitle">Twitter</span>
				    </li>
				    <li class="item">
					    <span class="pic"><a href="#"><img src="static/img/temp/pic.jpg" width="37" height="37" /></a></span>
					    <span class="title"><a href="#">Vilnius</a></span>
					    <span class="subtitle">YouTube</span>
				    </li>
			    </ul>
			    <!-- end of ELSEWHERE -->

			    <a href="#"><img src="static/img/temp/ad.jpg" width="250" height="250" /></a>
		    </div>

		    <div class="grid2">
			<xsl:apply-templates/>
		    </div>
		    
		</xsl:otherwise>
	    </xsl:choose>
	</div>
</div>
	    </body>
	</html>
    </xsl:template>

    <!-- subject -->
    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]">
<div class="box main-info clearfix">
	<xsl:if test="foaf:depiction/@rdf:resource">
	    <div class="main-pic">
		<a href="{$base-uri}?uri={url:encode(foaf:depiction/@rdf:resource, 'UTF-8')}">
		    <img src="{foaf:depiction/@rdf:resource}" alt=""/>
		</a>
		<!-- <img src="static/img/temp/vilnius.jpg" width="690" height="322" /> -->
	    </div>
	</xsl:if>
	<h1>
	    <xsl:apply-templates select="@rdf:about | @rdf:nodeID"/>
	    <!-- <a href="?uri={encode-for-uri(@rdf:about)}&amp;mode={encode-for-uri('&g;EditMode')}">Edit</a> -->	
	</h1>
	<ul class="other">
		<li id="opeople"><a href="#">People</a></li>
		<li id="ophotos"><a href="#">Photos</a></li>
		<li id="otimeline"><a href="#">Timeline</a></li>
		<li id="omap"><a href="#">Map</a></li>
		<li id="oreports"><a href="#">Reports</a></li>
	</ul>
	<xsl:if test="rdf:type/@rdf:resource">
	    <ul class="whatis clearfix">
		<xsl:for-each select="rdf:type/@rdf:resource">
		    <li>
			<xsl:apply-templates select="."/>
		    </li>
		</xsl:for-each>
	    </ul>
	</xsl:if>
	<xsl:if test="dbpedia-owl:abstract[lang($lang)]">
	    <p class="intro">
		<xsl:value-of select="substring(dbpedia-owl:abstract[lang($lang)], 1, 300)"/>
	    </p>
	</xsl:if>
	<div class="actions clearfix">
		<img src="static/img/temp/fb.gif" width="100" style="float: left; margin-top: 2px;" />
		<div class="share">
			<strong>Share:</strong>
			<a href="#" class="facebook">on Facebook</a>
			<a href="#" class="twitter">on Twitter</a>
			<a href="#" class="mail">Send email</a>
		</div>
		<div class="rate">
			<strong>Rate:</strong>
			<ul class="rating">
				<li class="on"><a href="#">1</a></li>
				<li class="on"><a href="#">2</a></li>
				<li><a href="#">3</a></li>
			</ul>
		</div>
		<div class="export">
			<strong>Export:</strong>
			<ul>
				<li>
				    <a href="?uri={encode-for-uri($uri)}&amp;accept={encode-for-uri('application/rdf+xml')}">RDF/XML</a>
				</li>
				<li>
				    <a href="?uri={encode-for-uri($uri)}&amp;accept={encode-for-uri('text/turtle')}">Turtle</a>
				</li>
				<!--
				<li><a href="#">Turtle</a></li>
				<li><a href="#">ePub</a></li>
				-->
			</ul>
		</div>
	</div>
</div>
	
<xsl:if test="*[not(self::rdf:type)][not(self::foaf:depiction)][not(self::dbpedia-owl:abstract)][not(rdfs:domain(xs:anyURI(concat(namespace-uri(.), local-name(.)))) = current()/rdf:type/@rdf:resource)][not(@xml:lang) or lang($lang)]">
    <div class="grid4">
	    <div class="box no-hd">
		    <a href="#" class="btn-edit"><span>Edit</span></a>
		    <dl class="list-default clearfix">
			<xsl:apply-templates select="*[not(self::rdf:type)][not(self::foaf:depiction)][not(self::dbpedia-owl:abstract)][not(rdfs:domain(xs:anyURI(concat(namespace-uri(.), local-name(.)))) = current()/rdf:type/@rdf:resource)][not(@xml:lang) or lang($lang)]">
			    <xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
			</xsl:apply-templates>	    
		    </dl>
	    </div>
    </div>
</xsl:if>
<xsl:if test="rdf:type/@rdf:resource[../../*/xs:anyURI(concat(namespace-uri(.), local-name(.))) = g:inDomainOf(.)]">
    <div class="grid5">

	    <xsl:apply-templates select="rdf:type/@rdf:resource[../../*/xs:anyURI(concat(namespace-uri(.), local-name(.))) = g:inDomainOf(.)]" mode="g:TypeMode">
		<!-- <xsl:sort select="@rdf:resource | @rdf:nodeID" data-type="text" order="ascending"/> -->
	    </xsl:apply-templates>

    </div>
</xsl:if>

	<!--
	<dl>
	    <xsl:apply-templates select="rdf:type"/>
	    <xsl:apply-templates select="*[not(self::rdf:type)][not(@xml:lang) or lang($lang)]">
		<xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
	    </xsl:apply-templates>
	</dl>
	-->
<div class="clearer">&#160;</div>
	<!-- <hr/> -->
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
<div class="box">
	<a href="#" class="btn-edit"><span>Edit</span></a>
	<h2 class="hd">
	    <xsl:apply-imports/>
	</h2>
	<dl class="list-default clearfix">
	    <xsl:choose>
		<xsl:when test="$mode = '&g;EditMode'">
		    <xsl:apply-templates select="$in-domain-properties" mode="g:EditMode">
			<xsl:with-param name="type" select="."/>
		    </xsl:apply-templates>
		</xsl:when>
		<xsl:otherwise>
		    <!-- <xsl:apply-templates select="../../*[xs:anyURI(concat(namespace-uri(.), local-name(.))) = g:inDomainOf(current())][not(@xml:lang) or lang($lang)]"> -->
		    <!-- <xsl:apply-templates select="../../*[rdfs:domain(xs:anyURI(concat(namespace-uri(.), local-name(.)))) = xs:anyURI(current())][not(@xml:lang) or lang($lang)]"> --> <!-- not(self::rdf:type) --> 
		    <xsl:apply-templates select="$in-domain-properties[not(concat(namespace-uri(.), local-name(.)) = '&foaf;depiction')]">
			<xsl:with-param name="type" select="."/>
		    </xsl:apply-templates>
		</xsl:otherwise>
	    </xsl:choose>

<!--
		<dt><span><a href="#">Part</a></span></dt>
		<dd><span>
			<a href="#">Antakalnis </a><br />
			<a href="#">Žirmūnai </a><br />
			<a href="#">Fabijoniškės</a><br />
			<a href="#">Lazdynai</a>
		</span></dd>

		<dt><span><a href="#">Is part of</a></span></dt>
		<dd><span><a href="#">Lithuania</a></span></dd>
-->
	</dl>
</div>
    </xsl:template>

    <!-- property -->
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(.), local-name(.)))" as="xs:anyURI"/>
<!--
<dt class="first"><span><a href="#">Page</a></span></dt>
<dd class="first"><span><a href="#">www.vilnius.lt</a></span></dd>
-->

	<xsl:if test="not(concat(namespace-uri(preceding-sibling::*[1]), local-name(preceding-sibling::*[1])) = $this)">
	    <!-- @xml:lang = preceding-sibling::*[1]/@xml:lang -->
	    <dt>
		<span>
		    <xsl:apply-imports/>
		</span>
	    </dt>
	</xsl:if>
	<!-- <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/> -->
	<xsl:apply-templates select="node() | @rdf:resource"/>
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
	    <xsl:if test="concat(namespace-uri(..), local-name(..)) = concat(namespace-uri(../preceding-sibling::*[1]), local-name(../preceding-sibling::*[1]))">
		<xsl:attribute name="class">no-dt</xsl:attribute>
	    </xsl:if>

	    <span>
		<xsl:apply-imports/>
	    </span>
	</dd>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:resource | *[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID | *[@rdf:about or @rdf:nodeID]/*/text()" mode="g:EditMode">
	<dd>
	    <xsl:apply-imports/>
	</dd>
    </xsl:template>

</xsl:stylesheet>