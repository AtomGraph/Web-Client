<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2012 Martynas Jusevičius <martynas@atomgraph.com>

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
    <!ENTITY gc     "http://atomgraph.com/client/ns#">
    <!ENTITY g      "http://atomgraph.com/core/ns#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">    
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY http   "http://www.w3.org/2011/http#">
    <!ENTITY ldt    "http://www.w3.org/ns/ldt#">
    <!ENTITY sp     "http://spinrdf.org/sp#">
    <!ENTITY spin   "http://spinrdf.org/spin#">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:foaf="&foaf;"
xmlns:bs2="http://graphity.org/xsl/bootstrap/2.3.2"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:url="&java;java.net.URLDecoder"
exclude-result-prefixes="#all">

    <xsl:param name="gc:sitemap" as="document-node()?"/>

    <!-- PROPERTY LIST MODE -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="bs2:PropertyList">
	<xsl:variable name="this" select="xs:anyURI(concat(namespace-uri(), local-name()))" as="xs:anyURI"/>

	<xsl:if test="not(preceding-sibling::*[concat(namespace-uri(), local-name()) = $this])">
	    <dt>
		<xsl:apply-templates select="."/>
	    </dt>
	</xsl:if>
	<dd>
	    <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID"/>
	</dd>
    </xsl:template>

    <xsl:template match="rdf:type[@rdf:resource] | foaf:primaryTopic[key('resources', (@rdf:resource, @rdf:nodeID))] | foaf:isPrimaryTopicOf[key('resources', (@rdf:resource, @rdf:nodeID))]" mode="bs2:FormControl" priority="1">
        <xsl:apply-templates select="." mode="xhtml:Input">
            <xsl:with-param name="type" select="'hidden'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="#current">
            <xsl:with-param name="type" select="'hidden'"/>
        </xsl:apply-templates>
        <xsl:apply-templates select="@xml:lang | @rdf:datatype" mode="#current">
            <xsl:with-param name="type" select="'hidden'"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- FORM MODE -->
    
    <!-- @rdf:about | @rdf:nodeID -->
    <xsl:template match="*[*]/@rdf:*[local-name() = ('about', 'nodeID')]" mode="bs2:FormControl">
	<xsl:param name="type" select="'hidden'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>

        <xsl:apply-templates select="." mode="xhtml:Input">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
        </xsl:apply-templates>
    </xsl:template>

    <!-- *[@rdf:about or @rdf:nodeID]/* -->
    <xsl:template match="*[@rdf:*[local-name() = ('about',  'nodeID')]]/*" mode="bs2:FormControl">
        <xsl:param name="this" select="concat(namespace-uri(), local-name())"/>
        <xsl:param name="violations" as="element()*"/>
        <xsl:param name="error" select="$violations/spin:violationPath/@rdf:resource = $this" as="xs:boolean"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="label" select="true()" as="xs:boolean"/>
        <xsl:param name="cloneable" select="false()" as="xs:boolean"/>
        <xsl:param name="required" select="not(preceding-sibling::*[concat(namespace-uri(), local-name()) = $this]) and (if ($gc:sitemap) then (key('resources', key('resources', ../rdf:type/@rdf:resource, $gc:sitemap)/spin:constraint/(@rdf:resource|@rdf:nodeID), $gc:sitemap)[rdf:type/@rdf:resource = '&ldt;MissingPropertyValue'][sp:arg1/@rdf:resource = $this]) else true())" as="xs:boolean"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="for" select="generate-id((node() | @rdf:resource | @rdf:nodeID)[1])" as="xs:string"/>

        <div class="control-group">
	    <xsl:if test="$error">
		<xsl:attribute name="class">control-group error</xsl:attribute>
	    </xsl:if>
            <xsl:apply-templates select="." mode="xhtml:Input">
                <xsl:with-param name="type" select="'hidden'"/>
            </xsl:apply-templates>
            <xsl:if test="$label">
                <label class="control-label" for="{$for}" title="{$this}">
                    <xsl:apply-templates select="." mode="gc:property-label"/>
                </label>
            </xsl:if>
            <xsl:if test="$cloneable">
                <div class="btn-group pull-right">
                    <button type="button" class="btn btn-small pull-right btn-add" title="Add another statement">&#x271a;</button>
                </div>
            </xsl:if>

            <div class="controls">
                <xsl:if test="not($required)">
                    <div class="btn-group pull-right">
                        <button type="button" class="btn btn-small pull-right btn-remove" title="Remove this statement">&#x2715;</button>
                    </div>
                </xsl:if>

                <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="#current"/>
            </div>
            <xsl:if test="@xml:lang | @rdf:datatype">
                <div class="controls">
                    <xsl:apply-templates select="@xml:lang | @rdf:datatype" mode="#current"/>
                </div>
            </xsl:if>
        </div>
    </xsl:template>

    <xsl:template match="text()" mode="bs2:FormControl">
	<xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="xhtml:Input">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>
        </xsl:apply-templates>
        
        <xsl:if test="not($type = 'hidden') and $type-label">
            <xsl:choose>
                <xsl:when test="../@rdf:datatype">
                    <xsl:apply-templates select="../@rdf:datatype"/>
                </xsl:when>
                <xsl:otherwise>
                    <span class="help-inline">Literal</span>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <xsl:template match="text()[string-length(.) &gt; 50]" mode="bs2:FormControl">
	<xsl:param name="name" select="'ol'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="style" as="xs:string?"/>
	<xsl:param name="value" select="." as="xs:string?"/>
        <xsl:param name="rows" as="xs:integer?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>        
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
            <xsl:if test="$disabled">
                <xsl:attribute name="disabled">disabled</xsl:attribute>
            </xsl:if>

            <xsl:value-of select="$value"/>
        </textarea>
        
        <xsl:if test="$type-label">
            <xsl:choose>
                <xsl:when test="../@rdf:datatype">
                    <xsl:apply-templates select="../@rdf:datatype"/>
                </xsl:when>
                <xsl:otherwise>
                    <span class="help-inline">Literal</span>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <!-- @rdf:resource, @rdf:nodeID -->
    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:*[local-name() = ('resource', 'nodeID')]" mode="bs2:FormControl">
	<xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>        
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="xhtml:Input">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>            
        </xsl:apply-templates>

        <xsl:if test="not($type = 'hidden') and $type-label">
            <span class="help-inline">Resource</span>
        </xsl:if>
    </xsl:template>

    <!-- @xml:lang -->
    <xsl:template match="@xml:*[local-name() = 'lang']" mode="bs2:FormControl">
	<xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" select="'input-mini'" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>        
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="xhtml:Input">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>            
        </xsl:apply-templates>
        
        <xsl:if test="not($type = 'hidden') and $type-label">
            <span class="help-inline">Language</span>
        </xsl:if>
    </xsl:template>

    <!-- @rdf:datatype -->
    <xsl:template match="@rdf:*[local-name() = 'datatype']" mode="bs2:FormControl">
	<xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>        
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="xhtml:Input">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>            
        </xsl:apply-templates>
        
        <xsl:if test="not($type = 'hidden') and $type-label">
            <span class="help-inline">Datatype</span>
        </xsl:if>
    </xsl:template>
    
    <!-- *[@rdf:about or @rdf:nodeID]/*/@rdf:* -->
    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:*[local-name() = ('resource', 'nodeID')]" mode="bs2:FormControl" priority="1">
	<xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>        
        <xsl:param name="traversed-ids" as="xs:string*" tunnel="yes"/>
        <xsl:param name="template"  as="element()?"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>
        <xsl:variable name="resource" select="key('resources', .)"/>

	<xsl:choose>
	    <xsl:when test="$resource and not(. = $traversed-ids)">
                <xsl:apply-templates select="." mode="xhtml:Input">
                    <xsl:with-param name="type" select="'hidden'"/>
                </xsl:apply-templates>

                <xsl:apply-templates select="$resource" mode="#current">
                    <xsl:with-param name="traversed-ids" select="(., $traversed-ids)" tunnel="yes"/>
                </xsl:apply-templates>

                <xsl:apply-templates select="../../@rdf:about | ../../@rdf:nodeID" mode="#current"/>
            </xsl:when>
	    <xsl:otherwise>
                <xsl:apply-templates select="." mode="xhtml:Input">
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                    <xsl:with-param name="disabled" select="$disabled"/>
                </xsl:apply-templates>
                
                <xsl:if test="not($type = 'hidden') and $type-label">
                    <span class="help-inline">Resource</span>
                </xsl:if>
	    </xsl:otherwise>
	</xsl:choose>
    </xsl:template>    

</xsl:stylesheet>