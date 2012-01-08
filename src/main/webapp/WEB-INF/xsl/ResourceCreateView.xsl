<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE uridef[
	<!ENTITY owl "http://www.w3.org/2002/07/owl#">
	<!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
	<!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
	<!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
	<!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
	<!ENTITY dc "http://purl.org/dc/elements/1.1/">
	<!ENTITY dct "http://purl.org/dc/terms/">
	<!ENTITY rev "http://purl.org/stuff/rev#">
	<!ENTITY foaf "http://xmlns.com/foaf/0.1/">
	<!ENTITY geo "http://www.w3.org/2003/01/geo/wgs84_pos#">
	<!ENTITY wm "http://semantic-web.dk/ontologies/wulffmorgenthaler#">
	<!ENTITY hn "http://semantic-web.dk/ontologies/heltnormalt#">
	<!ENTITY vis "http://code.google.com/apis/visualization/">
]>
<xsl:stylesheet version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:skos="http://www.w3.org/2004/02/skos/core#"
xmlns:geo="&geo;"
xmlns:foaf="&foaf;"
xmlns:rev="&rev;"
xmlns:wm="&wm;"
xmlns:hn="&hn;"
xmlns:vis="http://code.google.com/apis/visualization/"
xmlns:html="http://www.w3.org/1999/xhtml#"
xmlns:sparql="&sparql;"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:php="http://php.net/xsl"
xmlns="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="xsl rdf rdf dc dct skos geo foaf rev wm hn vis html sparql xs php"
>

<!-- *[@rdf:about] resource -->
<!-- *[@rdf:resource] property pointing to object resource without further properties -->
<!-- *[concat(namespace-uri(.), local-name(.))] - property -->

	<xsl:template match="*[@rdf:about]">
        <div id="content">            
            <div class="box">
                <div class="cntnt" style="color: black;">
                    <h1>
                        <xsl:apply-templates select="." mode="title"/>
                    </h1>

                    <form action="" method="post" class="preprocess-form">
                        <dl>
                            <!-- for all unique properties of the current resource that are not rdf:type and do not have one of the types of this resource as rdfs:domain -->
                            <xsl:apply-templates select="*[not(key('resources', concat(namespace-uri(.), local-name(.)))/rdfs:domain/@rdf:resource = ../rdf:type/@rdf:resource)][generate-id() = generate-id(key('properties', concat(namespace-uri(.), local-name(.)))[parent::*/@rdf:about = current()/@rdf:about][1])]" mode="html:dt">
                                <xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
                            </xsl:apply-templates>
                        </dl>
                <!--
                <xsl:if test="not(self::rdf:Description)">
                    <h3>
                        <a href="{concat(namespace-uri(.), local-name(.))}">
                            <xsl:value-of select="concat(namespace-uri(.), local-name(.))"/>
                        </a>
                    </h3>
                </xsl:if>
                -->

                        <!-- for all types of this resource that are domain of one of the properties of this resource -->
                        <!-- rdf:type[../*[concat(namespace-uri(.), local-name(.)) = key('resources-by-domain', current()/@rdf:resource)/@rdf:about]] -->
                        <xsl:for-each select="rdf:type">
                            <xsl:if test="../*[concat(namespace-uri(.), local-name(.)) = key('resources-by-domain', current()/@rdf:resource)/@rdf:about]">
                                <div class="type-container">
                                    <h3>
                                        <xsl:apply-templates select="." mode="object"/>
                                    </h3>

                                    <dl>
                                        <xsl:apply-templates select="../*[concat(namespace-uri(.), local-name(.)) = key('resources-by-domain', current()/@rdf:resource)/@rdf:about]" mode="html:dt">
                                            <xsl:sort select="concat(namespace-uri(.), local-name(.))" data-type="text" order="ascending"/>
                                        </xsl:apply-templates>
                                    </dl>
                                </div>
                            </xsl:if>
                        </xsl:for-each>
                        <p>
                            <button type="submit">Save</button>
                        </p>
                    </form>		
                </div>
            </div>
        </div>
	</xsl:template>

	<xsl:template match="*/*[@rdf:resource or text()]" mode="html:dt">
		<dt>
			<xsl:apply-templates select="." mode="property"/>
		</dt>
		<!-- <xsl:apply-templates select="key('properties', concat(namespace-uri(.), local-name(.)))[generate-id(parent::*) = generate-id(current()/parent::*)]" mode="html:dd"> -->
		<xsl:apply-templates select="../*[concat(namespace-uri(.), local-name(.)) = concat(namespace-uri(current()), local-name(current()))]" mode="html:dd">
			<xsl:sort select="@rdf:resource" data-type="text" order="ascending"/>
		</xsl:apply-templates>
	</xsl:template>

	<!-- <xsl:template match="*/rdf:type[@rdf:resource or text()]" mode="html:dt" priority="1"/> -->

	<xsl:template match="*/*[@rdf:resource or * or text()][concat(namespace-uri(preceding-sibling::*[1]), local-name(preceding-sibling::*[1])) = concat(namespace-uri(.), local-name(.))]" mode="html:dt"/>

	<xsl:template match="*/*[@rdf:resource or * or text()]" mode="html:dd">
		<dd>
			<xsl:apply-templates select="." mode="object"/>
		</dd>
	</xsl:template>

	<xsl:template match="*[@rdf:about | @rdf:resource]" mode="title">
		<xsl:choose>
			<xsl:when test="rdfs:label">
				<xsl:value-of select="rdfs:label[1]"/>
			</xsl:when>
			<xsl:when test="dc:title">
				<xsl:value-of select="dc:title[1]"/>
			</xsl:when>
			<xsl:when test="skos:prefLabel">
				<xsl:value-of select="skos:prefLabel[1]"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="@rdf:about | @rdf:resource"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="*[@rdf:about]" mode="html:a">
		<a>
            <xsl:attribute name="href">
                <xsl:if test="not(starts-with(@rdf:about, $base-uri))">
                    <xsl:text>?browse=</xsl:text>
                </xsl:if>
                <xsl:value-of select="php:function('rawurlencode', string(@rdf:about))"/>
            </xsl:attribute>
            <xsl:apply-templates select="." mode="title"/>
		</a>
	</xsl:template>

	<xsl:template match="*[@rdf:resource]" mode="html:a">
        <input type="text" name="su" value="{@rdf:resource}"/>
		<a>
            <xsl:attribute name="href">
                <xsl:if test="not(starts-with(@rdf:resource, $base-uri))">
                    <xsl:text>?browse=</xsl:text>
                </xsl:if>
                <xsl:value-of select="php:function('rawurlencode', string(@rdf:resource))"/>
            </xsl:attribute>
            <xsl:apply-templates select="." mode="title"/>
		</a>
	</xsl:template>

	<xsl:template match="*/*[@rdf:resource or text()]" mode="html:a">
        <input type="text" name="su" value="{@rdf:resource}"/>
		<a>
            <xsl:attribute name="href">
                <xsl:if test="not(starts-with(concat(namespace-uri(.), local-name(.)), $base-uri))">
                    <xsl:text>?browse=</xsl:text>
                </xsl:if>
                <xsl:value-of select="php:function('rawurlencode', string(concat(namespace-uri(.), local-name(.))))"/>
            </xsl:attribute>
            <xsl:apply-templates select="." mode="title"/>
		</a>
	</xsl:template>

	<xsl:template match="*/*[@rdf:resource or text()]" mode="property">
        <input type="text" name="pu" value="{concat(namespace-uri(.), local-name(.))}"/>
		<xsl:choose>
			<xsl:when test="key('resources', concat(namespace-uri(.), local-name(.)))">
				<xsl:apply-templates select="key('resources', concat(namespace-uri(.), local-name(.)))"/>
			</xsl:when>
			<xsl:otherwise>
				<a href="?browse={concat(namespace-uri(.), local-name(.))}">
					<xsl:value-of select="concat(namespace-uri(.), local-name(.))"/>
				</a>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="*/*[@rdf:resource]" mode="object">
        <input type="text" name="ou" value="{@rdf:resource}"/>
		<xsl:choose>
			<xsl:when test="key('resources', @rdf:resource)">
				<xsl:apply-templates select="key('resources', @rdf:resource)"/>
			</xsl:when>
			<xsl:when test="@rdf:resource">
                <xsl:apply-templates select="." mode="html:a"/>
			</xsl:when>
			<xsl:when test="*">
				<xsl:apply-templates/>
			</xsl:when>
			<xsl:otherwise>
				<!-- <xsl:value-of select="."/> -->
                <input type="text" name="ol" value="{.}"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="*/*[*]" mode="object">
        <input type="text" name="ou" value="{@rdf:resource}"/>
		<xsl:choose>
			<xsl:when test="key('resources', @rdf:resource)">
				<xsl:apply-templates select="key('resources', @rdf:resource)"/>
			</xsl:when>
			<xsl:when test="@rdf:resource">
                <xsl:apply-templates select="." mode="html:a"/>
			</xsl:when>
			<xsl:when test="*">
				<xsl:apply-templates/>
			</xsl:when>
			<xsl:otherwise>
				<!-- <xsl:value-of select="."/> -->
                <input type="text" name="ol" value="{.}"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="*/*[@text()]" mode="object">
        <input type="text" name="ol" value="{text()}"/>
		<xsl:choose>
			<xsl:when test="key('resources', @rdf:resource)">
				<xsl:apply-templates select="key('resources', @rdf:resource)"/>
			</xsl:when>
			<xsl:when test="@rdf:resource">
                <xsl:apply-templates select="." mode="html:a"/>
			</xsl:when>
			<xsl:when test="*">
				<xsl:apply-templates/>
			</xsl:when>
			<xsl:otherwise>
				<!-- <xsl:value-of select="."/> -->
                <input type="text" name="ol" value="{.}"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

    <xsl:template match="*/foaf:img[@rdf:resource] | */foaf:depiction[@rdf:resource]" mode="object">
        <img src="{@rdf:resource}">
            <xsl:attribute name="alt">
                <xsl:apply-templates select=".." mode="title"/>
            </xsl:attribute>
        </img>
    </xsl:template>

</xsl:stylesheet>
