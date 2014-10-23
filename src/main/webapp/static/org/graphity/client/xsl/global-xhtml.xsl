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
    <!ENTITY java           "http://xml.apache.org/xalan/java/">
    <!ENTITY gc             "http://graphity.org/gc#">
    <!ENTITY rdf            "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs           "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl            "http://www.w3.org/2002/07/owl#">
    <!ENTITY xsd            "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql         "http://www.w3.org/2005/sparql-results#">
    <!ENTITY geo            "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY dbpedia-owl    "http://dbpedia.org/ontology/">
    <!ENTITY dc             "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct            "http://purl.org/dc/terms/">
    <!ENTITY foaf           "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc           "http://rdfs.org/sioc/ns#">
    <!ENTITY sp             "http://spinrdf.org/sp#">
    <!ENTITY sd             "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY void           "http://rdfs.org/ns/void#">
    <!ENTITY list           "http://jena.hpl.hp.com/ARQ/list#">
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
xmlns:geo="&geo;"
xmlns:dbpedia-owl="&dbpedia-owl;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:sp="&sp;"
xmlns:sd="&sd;"
xmlns:void="&void;"
xmlns:list="&list;"
xmlns:javaee="http://java.sun.com/xml/ns/javaee"
exclude-result-prefixes="#all">

    <xsl:import href="imports/local.xsl"/>
    <xsl:import href="imports/external.xsl"/>
    <xsl:import href="imports/dbpedia-owl.xsl"/>
    <xsl:import href="imports/dc.xsl"/>
    <xsl:import href="imports/dct.xsl"/>
    <xsl:import href="imports/doap.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/gc.xsl"/>
    <xsl:import href="imports/gp.xsl"/>
    <xsl:import href="imports/gr.xsl"/>
    <xsl:import href="imports/owl.xsl"/>
    <xsl:import href="imports/rdf.xsl"/>
    <xsl:import href="imports/rdfs.xsl"/>
    <xsl:import href="imports/sd.xsl"/>
    <xsl:import href="imports/sioc.xsl"/>
    <xsl:import href="imports/skos.xsl"/>
    <xsl:import href="imports/sp.xsl"/>
    <xsl:import href="imports/spin.xsl"/>
    <xsl:import href="imports/void.xsl"/>
    <xsl:import href="layout.xsl"/>

    <xsl:param name="uri" as="xs:anyURI?"/>
    <xsl:param name="label" as="xs:string?"/>

    <xsl:variable name="default-mode" select="if ($uri) then (xs:anyURI('&gc;ListReadMode')) else (if ($matched-ont-class/gc:defaultMode/@rdf:resource) then xs:anyURI($matched-ont-class/gc:defaultMode/@rdf:resource) else (if (key('resources', $absolute-path)/rdf:type/@rdf:resource = ('&sioc;Container', '&sioc;Space')) then xs:anyURI('&gc;ListMode') else xs:anyURI('&gc;ReadMode')))" as="xs:anyURI"/>

    <xsl:key name="resources-by-endpoint" match="*" use="void:sparqlEndpoint/@rdf:resource"/>

    <xsl:template match="rdf:RDF" mode="gc:TitleMode">
	<xsl:choose>
	    <xsl:when test="$uri">
		<xsl:apply-templates select="key('resources', $base-uri, document($base-uri))" mode="gc:LabelMode"/>
		<xsl:text> - </xsl:text>
		<xsl:apply-templates select="key('resources', $uri)" mode="gc:LabelMode"/>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:apply-imports/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="/" mode="gc:NavBarMode">
	<div class="navbar navbar-fixed-top">
	    <div class="navbar-inner">
		<div class="container-fluid">
                    <button class="btn btn-navbar" onclick="if ($('#collapsing-navbar').hasClass('in')) $('#collapsing-navbar').removeClass('collapse in').height(0); else $('#collapsing-navbar').addClass('collapse in').height('auto');">
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                        <span class="icon-bar"></span>
                    </button>

                    <a class="brand" href="{$base-uri}">
                        <xsl:for-each select="key('resources', $base-uri, document($base-uri))">
                            <img src="{foaf:logo/@rdf:resource}">
                                <xsl:attribute name="alt"><xsl:apply-templates select="." mode="gc:LabelMode"/></xsl:attribute>
                            </img>
                        </xsl:for-each>
                    </a>

                    <div id="collapsing-navbar" class="nav-collapse collapse">
                        <form action="{$base-uri}" method="get" class="navbar-form pull-left" accept-charset="UTF-8"
                              onsubmit="if ($(this).find('input[name=uri]').val().indexOf('http://') == -1) {{ $(this).attr('action', 'resources/labelled'); $(this).find('input[name=uri]').attr('name', 'label'); return true; }}">
                            <div class="input-append">
                                <xsl:if test="$uri">
                                    <xsl:variable name="uri-dataset-query"><![CDATA[
    PREFIX  void: <http://rdfs.org/ns/void#>

    DESCRIBE ?dataset
    WHERE
      { { SELECT  ?dataset
          WHERE
            { GRAPH ?g
                {   { ?dataset void:uriSpace ?uriSpace }
                  UNION
                    { ?dataset void:uriRegexPattern ?uriRegexPattern }
                  FILTER ( strstarts("]]><xsl:value-of select="$uri"/><![CDATA[", ?uriSpace) || regex("]]><xsl:value-of select="$uri"/><![CDATA[", ?uriRegexPattern) )
                }
            }
        }
      }
    ]]>
                                    </xsl:variable>
                                    <xsl:variable name="query-uri" select="concat(resolve-uri('sparql', $base-uri), '?query=', encode-for-uri($uri-dataset-query))" as="xs:string"/>
                                    <xsl:if test="doc-available($query-uri)">
                                        <xsl:if test="key('resources-by-type', '&void;Dataset', document($query-uri))">
                                            <xsl:attribute name="class">input-prepend input-append</xsl:attribute>
                                            <span class="add-on">
                                                <xsl:apply-templates select="key('resources-by-type', '&void;Dataset', document($query-uri))[1]/@rdf:about" mode="gc:InlineMode"/>
                                            </span>
                                        </xsl:if>
                                    </xsl:if>
                                </xsl:if>

                                <input type="text" name="uri" class="input-xxlarge">
                                    <xsl:if test="not(starts-with($uri, $base-uri))">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="$uri"/>
                                        </xsl:attribute>
                                    </xsl:if>
                                    <xsl:if test="$label">
                                        <xsl:attribute name="value">
                                            <xsl:value-of select="$label"/>
                                        </xsl:attribute>
                                    </xsl:if>
                                </input>
                                <button type="submit" class="btn btn-primary">Go</button>
                            </div>
                        </form>

                        <xsl:if test="key('resources-by-space', $base-uri, document($base-uri))[@rdf:about = resolve-uri('sparql', $base-uri) or @rdf:about = resolve-uri('ontology', $base-uri)]">
                            <xsl:variable name="space" select="($absolute-path, key('resources', $absolute-path)/sioc:has_container/@rdf:resource)" as="xs:anyURI*"/>
                            <ul class="nav pull-right">
                                <xsl:apply-templates select="key('resources-by-space', $base-uri, document($base-uri))[@rdf:about = resolve-uri('sparql', $base-uri) or @rdf:about = resolve-uri('ontology', $base-uri)]" mode="gc:NavBarMode">
                                    <xsl:sort select="gc:label(.)" order="ascending" lang="{$lang}"/>
                                    <xsl:with-param name="space" select="$space"/>
                                </xsl:apply-templates>
                            </ul>
                        </xsl:if>
                    </div>
		</div>
	    </div>
	</div>
    </xsl:template>

    <xsl:template match="rdf:RDF[$uri]" mode="gc:ReadMode">
        <xsl:apply-templates select="." mode="gc:ModeSelectMode"/>

        <xsl:apply-templates select="key('resources', $uri)" mode="gc:ReadMode"/>

        <xsl:apply-templates select="*[not(@rdf:about = $uri)]" mode="#current">
            <xsl:sort select="gc:label(.)" lang="{$lang}"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="rdf:RDF[$uri]" mode="gc:HeaderMode">
        <xsl:apply-templates select="key('resources', $uri)" mode="#current"/>
    </xsl:template>
    
    <xsl:template match="@rdf:about[. = $uri]" mode="gc:HeaderMode">
	<h1 class="page-header">
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</h1>
    </xsl:template>
    
    <xsl:template match="rdf:RDF[$uri]" mode="gc:ModeSelectMode">
        <ul class="nav nav-tabs">
            <xsl:apply-templates select="key('resources-by-type', '&gc;Mode', document('&gc;'))[rdf:type/@rdf:resource = '&gc;ItemMode']" mode="#current">
                <xsl:sort select="gc:label(.)"/>                    
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="@rdf:about" mode="gc:ModeSelectMode">
	<xsl:choose>
	    <xsl:when test="$uri">
		<xsl:choose>
		    <xsl:when test=". = $default-mode">
			<a href="{$absolute-path}{gc:query-string($uri, ())}">
			    <xsl:apply-templates select=".." mode="gc:LabelMode"/>
			</a>		
		    </xsl:when>
		    <xsl:otherwise>
			<a href="{$absolute-path}{gc:query-string($uri, .)}">
			    <xsl:apply-templates select=".." mode="gc:LabelMode"/>
			</a>		
		    </xsl:otherwise>
		</xsl:choose>
	    </xsl:when>
	    <xsl:otherwise>
		<xsl:apply-imports/>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>

    <xsl:template match="rdf:RDF[$absolute-path = resolve-uri('sparql', $base-uri)]" mode="gc:QueryFormMode">
	<p class="form-inline">
	    <label for="endpoint-select">SPARQL endpoint</label>
	    <xsl:text> </xsl:text>
	    <select id="endpoint-select" name="endpoint-uri" class="span6">
                <xsl:apply-templates select="key('resources', resolve-uri('sparql', $base-uri), document($base-uri))" mode="gc:OptionMode"/>
		<xsl:apply-templates select="key('resources-by-type', '&void;Dataset', document(resolve-uri('datasets?limit=100', $base-uri)))[void:sparqlEndpoint/@rdf:resource]" mode="gc:OptionMode">
                    <xsl:sort select="gc:label(.)" order="ascending"/>
		</xsl:apply-templates>
	    </select>
	</p>
	
	<xsl:apply-imports/>
    </xsl:template>

    <xsl:template match="*[@rdf:about | @rdf:nodeID][void:sparqlEndpoint/@rdf:resource]" mode="gc:OptionMode">
	<option value="{void:sparqlEndpoint/@rdf:resource}">
	    <xsl:if test="$endpoint-uri = void:sparqlEndpoint/@rdf:resource">
		<xsl:attribute name="selected">selected</xsl:attribute>
	    </xsl:if>

	    <xsl:apply-templates select="." mode="gc:LabelMode"/>
	    [<xsl:value-of select="void:sparqlEndpoint/@rdf:resource"/>]
	</option>	
    </xsl:template>
    
    <xsl:template match="*[@rdf:about = resolve-uri('sparql', $base-uri)]" mode="gc:OptionMode" priority="1">
	<option value="{@rdf:about}">
	    <xsl:if test="not($endpoint-uri) or $endpoint-uri = @rdf:about">
		<xsl:attribute name="selected">selected</xsl:attribute>
	    </xsl:if>

	    <xsl:apply-templates select="." mode="gc:LabelMode"/>
	    [<xsl:value-of select="@rdf:about"/>]
	</option>
    </xsl:template>

    <!-- only show edit mode for the main resource -->
    <xsl:template match="*[@rdf:about][$uri][not(@rdf:about = $uri)]" mode="gc:EditMode"/>
    
</xsl:stylesheet>