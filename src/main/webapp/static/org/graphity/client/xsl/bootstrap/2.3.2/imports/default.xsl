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
    <!ENTITY gp     "http://graphity.org/gp#">
    <!ENTITY gc     "http://graphity.org/gc#">
    <!ENTITY g      "http://graphity.org/g#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">    
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY sp     "http://spinrdf.org/sp#">
    <!ENTITY spin   "http://spinrdf.org/spin#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:gp="&gp;"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:bs2="http://graphity.org/xsl/bootstrap/2.3.2"
xmlns:url="&java;java.net.URLDecoder"
exclude-result-prefixes="#all">

    <!-- HEADER MODE -->
    
    <xsl:template match="@rdf:about | @rdf:nodeID" mode="bs2:HeaderMode">
	<h2>
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</h2>
    </xsl:template>

    <!-- LIST MODE -->
    
    <xsl:template match="@rdf:about | @rdf:nodeID" mode="bs2:ListMode">
        <h2>
            <xsl:apply-templates select="." mode="gc:InlineMode"/>
        </h2>
    </xsl:template>

    <!-- GRID MODE -->
    
    <xsl:template match="@rdf:about | @rdf:nodeID" mode="bs2:GridMode">
        <h2>
            <xsl:apply-templates select="." mode="gc:InlineMode"/>
        </h2>
    </xsl:template>

    <!-- EDIT MODE -->
    
    <xsl:template match="@rdf:about | @rdf:nodeID" mode="bs2:EditMode">
	<xsl:param name="type" select="'hidden'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>

        <xsl:apply-templates select="." mode="gc:InputMode">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
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
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="gc:InputMode">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>            
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
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>        
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="gc:InputMode">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>            
        </xsl:apply-templates>

        <xsl:if test="not($type = 'hidden') and $type-label">
            <span class="help-inline">Resource</span>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@xml:lang" mode="bs2:EditMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" select="'input-mini'" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>        
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="gc:InputMode">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>            
        </xsl:apply-templates>
        
        <xsl:if test="not($type = 'hidden') and $type-label">
            <span class="help-inline">Language</span>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@rdf:datatype" mode="bs2:EditMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>        
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:apply-templates select="." mode="gc:InputMode">
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
	    <xsl:with-param name="disabled" select="$disabled"/>            
        </xsl:apply-templates>
        
        <xsl:if test="not($type = 'hidden') and $type-label">
            <span class="help-inline">Datatype</span>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*/@rdf:*" mode="bs2:EditMode">
	<xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
	<xsl:param name="class" as="xs:string?"/>
	<xsl:param name="disabled" select="false()" as="xs:boolean"/>        
        <xsl:param name="traversed-ids" as="xs:string*" tunnel="yes"/>
        <xsl:param name="template"  as="element()?"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>
        <xsl:variable name="resource" select="key('resources', .)"/>

	<xsl:choose>
            <!-- loop if node not visited already -->
	    <xsl:when test="$resource and not(. = $traversed-ids)">
                <xsl:apply-templates select="." mode="gc:InputMode">
                    <xsl:with-param name="type" select="'hidden'"/>
                </xsl:apply-templates>

                <xsl:apply-templates select="$resource" mode="#current">
                    <xsl:with-param name="traversed-ids" select="(., $traversed-ids)" tunnel="yes"/>
                </xsl:apply-templates>
                <!-- restore subject context -->
                <xsl:apply-templates select="../../@rdf:about | ../../@rdf:nodeID" mode="#current"/>
            </xsl:when>
	    <xsl:otherwise>
                <xsl:apply-templates select="." mode="gc:InputMode">
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

    <!-- MEDIA TYPE SELECT MODE -->
    
    <!-- ideally should provide all serialization formats supported by Jena -->
    <xsl:template match="@rdf:about" mode="bs2:MediaTypeSelectMode">
	<a href="{.}?accept={encode-for-uri('application/rdf+xml')}" class="btn">RDF/XML</a>
	<a href="{.}?accept={encode-for-uri('text/turtle')}" class="btn">Turtle</a>
    </xsl:template>
    
    <!-- SIDEBAR NAV MODE -->
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="bs2:SidebarNavMode"/>
    
    <xsl:template match="@rdf:nodeID | @rdf:resource" mode="bs2:SidebarNavMode">
	<li>
	    <xsl:apply-templates select="." mode="gc:InlineMode"/>
	</li>
    </xsl:template>
    
    <!-- INLINE PROPERTY LIST MODE -->
    
    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="bs2:InlinePropertyListMode"/>

</xsl:stylesheet>