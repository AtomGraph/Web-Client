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
    <!ENTITY java   "http://xml.apache.org/xalan/java/">
    <!ENTITY g      "http://graphity.org/g#">
    <!ENTITY gp     "http://graphity.org/gp#">
    <!ENTITY gc     "http://graphity.org/gc#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY xhv    "http://www.w3.org/1999/xhtml/vocab#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY geo    "http://www.w3.org/2003/01/geo/wgs84_pos#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY http   "http://www.w3.org/2011/http#">
    <!ENTITY dct    "http://purl.org/dc/terms/">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
    <!ENTITY sp     "http://spinrdf.org/sp#">
    <!ENTITY spin   "http://spinrdf.org/spin#">
    <!ENTITY dqc    "http://semwebquality.org/ontologies/dq-constraints#">
    <!ENTITY void   "http://rdfs.org/ns/void#">
    <!ENTITY sioc   "http://rdfs.org/sioc/ns#">
    <!ENTITY list   "http://jena.hpl.hp.com/ARQ/list#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:g="&g;"
xmlns:gp="&gp;"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:http="&http;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:void="&void;"
xmlns:list="&list;"
xmlns:xhv="&xhv;"
xmlns:geo="&geo;"
xmlns:url="&java;java.net.URLDecoder"
xmlns:bs2="http://graphity.org/xsl/bootstrap/2.3.2"
xmlns:javaee="http://java.sun.com/xml/ns/javaee"
xmlns:saxon="http://saxon.sf.net/"
exclude-result-prefixes="#all">
    
    <xsl:template match="rdf:RDF" mode="bs2:EditMode">
        <xsl:param name="method" select="'post'" as="xs:string"/>   
        <xsl:param name="action" select="xs:anyURI(concat($g:absolutePath, '?_method=PUT&amp;mode=', encode-for-uri('&gc;EditMode')))" as="xs:anyURI"/>
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'form-horizontal'" as="xs:string?"/>
        <xsl:param name="button-class" select="'btn btn-primary'" as="xs:string?"/>
        <xsl:param name="accept-charset" select="'UTF-8'" as="xs:string?"/>
        <xsl:param name="enctype" as="xs:string?"/>
        <xsl:param name="resources" select="*[not(key('predicates-by-object', @rdf:nodeID))]" as="element()*" tunnel="yes"/>

        <form method="{$method}" action="{$action}">
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$accept-charset">
                <xsl:attribute name="accept-charset"><xsl:value-of select="$accept-charset"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$enctype">
                <xsl:attribute name="enctype"><xsl:value-of select="$enctype"/></xsl:attribute>
            </xsl:if>

            <xsl:comment>This form uses RDF/POST encoding: http://www.lsrn.org/semweb/rdfpost.html</xsl:comment>
	    <xsl:call-template name="gc:InputTemplate">
		<xsl:with-param name="name" select="'rdf'"/>
		<xsl:with-param name="type" select="'hidden'"/>
	    </xsl:call-template>

            <xsl:apply-templates select="$resources" mode="#current">
                <xsl:sort select="gc:label(.)"/>
            </xsl:apply-templates>
                
            <xsl:apply-templates select="." mode="bs2:FormActionsMode">
                <xsl:with-param name="button-class" select="$button-class"/>
            </xsl:apply-templates>
        </form>
    </xsl:template>

    <!-- hide metadata -->
    <xsl:template match="*[gc:layoutOf/@rdf:resource = $g:requestUri]" mode="bs2:EditMode" priority="1"/>

    <xsl:template match="*[rdf:type/@rdf:resource = '&http;Response'] | *[rdf:type/@rdf:resource = '&spin;ConstraintViolation']" mode="bs2:EditMode" priority="1"/>

    <xsl:template match="*[*][@rdf:about] | *[*][@rdf:nodeID]" mode="bs2:EditMode">
        <xsl:param name="id" select="generate-id()" as="xs:string?"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="legend" select="if (@rdf:about) then true() else not(key('predicates-by-object', @rdf:nodeID))" as="xs:boolean"/>
        <xsl:param name="constraint-violations" select="key('violations-by-root', (@rdf:about, @rdf:nodeID))" as="element()*"/>
        <xsl:param name="parent-uri" select="key('resources', $g:requestUri)/(sioc:has_parent, sioc:has_container)/@rdf:resource" as="xs:anyURI?"/>
        <xsl:param name="parent-doc" select="document($parent-uri)" as="document-node()?"/>
        <xsl:param name="construct-uri" select="if ($parent-doc) then key('resources-by-constructor-of', $parent-uri, $parent-doc)[gp:forClass/@rdf:resource = key('resources', $g:requestUri)/rdf:type/@rdf:resource]/@rdf:about else ()" as="xs:anyURI*"/>
        <xsl:param name="template-doc" select="document($construct-uri)" as="document-node()?" tunnel="yes"/>
        <xsl:param name="template" select="$template-doc/rdf:RDF/*[@rdf:nodeID][every $type in rdf:type/@rdf:resource satisfies current()/rdf:type/@rdf:resource = $type]" as="element()*"/>
        <xsl:param name="traversed-ids" select="@rdf:nodeID" as="xs:string*" tunnel="yes"/>

        <fieldset>
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>            
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>

            <xsl:if test="$legend">
                <legend>
                    <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="gc:InlineMode"/>
                </legend>
            </xsl:if>

            <xsl:apply-templates select="$constraint-violations" mode="bs2:ConstraintViolationMode"/>

            <xsl:apply-templates select="@rdf:about | @rdf:nodeID" mode="#current"/>

            <xsl:if test="not($template)">
                <xsl:message>bs2:EditMode is active but spin:constructor is not defined for resource '<xsl:value-of select="@rdf:about | @rdf:nodeID"/>'</xsl:message>
            </xsl:if>
            <xsl:apply-templates select="* | $template/*[not(concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype) = current()/*/concat(namespace-uri(), local-name(), @xml:lang, @rdf:datatype))]" mode="#current">
                <xsl:sort select="gc:property-label(.)"/>
                <xsl:with-param name="constraint-violations" select="$constraint-violations"/>
                <xsl:with-param name="traversed-ids" select="$traversed-ids" tunnel="yes"/>
            </xsl:apply-templates>
        </fieldset>
    </xsl:template>

    <xsl:template match="@rdf:about | @rdf:nodeID" mode="bs2:EditMode">
        <xsl:apply-templates select="." mode="gc:InputMode">
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="id" select="generate-id()"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="bs2:EditMode">
        <xsl:param name="this" select="concat(namespace-uri(), local-name())"/>
        <xsl:param name="constraint-violations" as="element()*"/>
	<xsl:param name="class" as="xs:string?"/>
        <xsl:param name="label" select="true()" as="xs:boolean"/>
        <xsl:param name="cloneable" select="false()" as="xs:boolean"/>
        <xsl:param name="required" select="not(preceding-sibling::*[concat(namespace-uri(), local-name()) = $this]) and (if ($gc:sitemap) then (key('resources', key('resources', ../rdf:type/@rdf:resource, $gc:sitemap)/spin:constraint/(@rdf:resource|@rdf:nodeID), $gc:sitemap)[rdf:type/@rdf:resource = '&gp;MissingPropertyValue'][sp:arg1/@rdf:resource = $this]) else true())" as="xs:boolean"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="for" select="generate-id((node() | @rdf:resource | @rdf:nodeID)[1])" as="xs:string"/>

        <div class="control-group">
	    <xsl:if test="$constraint-violations/spin:violationPath/@rdf:resource = $this">
		<xsl:attribute name="class">control-group error</xsl:attribute>
	    </xsl:if>
            <xsl:apply-templates select="." mode="gc:InputMode">
                <xsl:with-param name="type" select="'hidden'"/>
            </xsl:apply-templates>
            <xsl:if test="$label">
                <label class="control-label" for="{$for}" title="{$this}">
                    <xsl:apply-templates select="." mode="gc:PropertyLabelMode"/>
                </label>
            </xsl:if>
            <xsl:if test="$cloneable">
                <div class="btn-group pull-right">
                    <button type="button" class="btn btn-small pull-right btn-add" title="Add another statement">&#x271a;</button>
                </div>
            </xsl:if>
            <xsl:if test="not($required)">
                <div class="btn-group pull-right">
                    <button type="button" class="btn btn-small pull-right btn-remove" title="Remove this statement">&#x2715;</button>
                </div>
            </xsl:if>

            <div class="controls">
                <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="#current"/>
            </div>
            <xsl:if test="@xml:lang | @rdf:datatype">
                <div class="controls">
                    <xsl:apply-templates select="@xml:lang | @rdf:datatype" mode="#current"/>
                </div>
            </xsl:if>
        </div>
    </xsl:template>

    <xsl:template match="text()" mode="bs2:EditMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="gc:InputMode">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
        </xsl:apply-templates>
        
        <xsl:if test="not($type = 'hidden') and $type-label">
            <xsl:choose>
                <xsl:when test="../@rdf:datatype">
                    <xsl:apply-templates select="../@rdf:datatype" mode="gc:InlineMode"/>
                </xsl:when>
                <xsl:otherwise>
                    <span class="help-inline">Literal</span>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template match="text()[string-length(.) &gt; 50]" mode="bs2:EditMode">
	<xsl:param name="name" select="'ol'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="style" as="xs:string?"/>
	<xsl:param name="value" select="." as="xs:string?"/>
        <xsl:param name="rows" as="xs:integer?"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>
        
        <textarea name="{$name}">
            <xsl:if test="$id">
                <xsl:attribute name="id"><xsl:value-of select="$id"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class"><xsl:value-of select="$class"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$style">
                <xsl:attribute name="style"><xsl:value-of select="$style"/></xsl:attribute>
            </xsl:if>
            <xsl:if test="$rows">
                <xsl:attribute name="rows"><xsl:value-of select="$rows"/></xsl:attribute>
            </xsl:if>

            <xsl:value-of select="$value"/>
        </textarea>
        
        <xsl:if test="$type-label">
            <xsl:choose>
                <xsl:when test="../@rdf:datatype">
                    <xsl:apply-templates select="../@rdf:datatype" mode="gc:InlineMode"/>
                </xsl:when>
                <xsl:otherwise>
                    <span class="help-inline">Literal</span>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@rdf:resource" mode="bs2:EditMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="gc:InputMode">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
        </xsl:apply-templates>

        <xsl:if test="not($type = 'hidden') and $type-label">
            <span class="help-inline">Resource</span>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@xml:lang" mode="bs2:EditMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" select="'input-mini'" as="xs:string?"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="gc:InputMode">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
        </xsl:apply-templates>
        
        <xsl:if test="not($type = 'hidden') and $type-label">
            <span class="help-inline">Language</span>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@rdf:datatype" mode="bs2:EditMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="gc:InputMode">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
        </xsl:apply-templates>
        
        <xsl:if test="not($type = 'hidden') and $type-label">
            <span class="help-inline">Datatype</span>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:nodeID" mode="bs2:EditMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
        <xsl:param name="traversed-ids" as="xs:string*" tunnel="yes"/>
        <xsl:param name="template"  as="element()?"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

	<xsl:choose>
            <!-- loop if node not visited already -->
	    <xsl:when test="not(. = $traversed-ids)">
                <xsl:apply-templates select="." mode="gc:InputMode">
                    <xsl:with-param name="type" select="'hidden'"/>
                </xsl:apply-templates>

                <xsl:variable name="bnode" select="key('resources', .)"/>
                <xsl:if test="$bnode">
                    <xsl:apply-templates select="$bnode" mode="#current">
                        <xsl:with-param name="traversed-ids" select="(., $traversed-ids)" tunnel="yes"/>
                    </xsl:apply-templates>
                    <!-- restore subject context -->
                    <xsl:apply-templates select="../../@rdf:about | ../../@rdf:nodeID" mode="#current"/>
                </xsl:if>
            </xsl:when>
	    <xsl:otherwise>
                <xsl:apply-templates select="." mode="gc:InputMode">
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                </xsl:apply-templates>
                
                <xsl:if test="not($type = 'hidden') and $type-label">
                    <span class="help-inline">Blank node</span>
                </xsl:if>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>
                
</xsl:stylesheet>