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
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">    
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY http   "http://www.w3.org/2011/http#">
    <!ENTITY ldt    "https://www.w3.org/ns/ldt#">
    <!ENTITY sp     "http://spinrdf.org/sp#">
    <!ENTITY spin   "http://spinrdf.org/spin#">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:xsd="&xsd;"
xmlns:sp="&sp;"
xmlns:spin="&spin;"
xmlns:foaf="&foaf;"
xmlns:bs2="http://graphity.org/xsl/bootstrap/2.3.2"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
exclude-result-prefixes="#all">

    <!-- PROPERTY LIST -->

    <xsl:template match="*[@rdf:about or @rdf:nodeID]/*" mode="bs2:PropertyList">
        <xsl:apply-templates select="." mode="xhtml:DefinitionTerm"/>
        
        <xsl:apply-templates select="node() | @rdf:resource | @rdf:nodeID" mode="xhtml:DefinitionDescription"/>
    </xsl:template>

    <!-- FORM -->
    
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
        <xsl:param name="required" select="false()" as="xs:boolean"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="for" select="generate-id((node() | @rdf:resource | @rdf:nodeID)[1])" as="xs:string"/>

        <div class="control-group">
            <xsl:if test="$error">
                <xsl:attribute name="class" select="'control-group error'"/>
            </xsl:if>
            <xsl:apply-templates select="." mode="xhtml:Input">
                <xsl:with-param name="type" select="'hidden'"/>
            </xsl:apply-templates>
            <xsl:if test="$label">
                <label class="control-label" for="{$for}" title="{$this}">
                    <xsl:value-of>
                        <xsl:apply-templates select="." mode="ac:property-label"/>
                    </xsl:value-of>
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
                        <button type="button" class="btn btn-small pull-right btn-remove-property" title="Remove this statement">&#x2715;</button>
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

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="bs2:FormControlTypeLabel">
                <xsl:with-param name="type" select="$type"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <xsl:template match="text()" mode="bs2:FormControlTypeLabel">
        <xsl:param name="type" as="xs:string?"/>
        
        <xsl:if test="not($type = 'hidden')">
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
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:if test="$style">
                <xsl:attribute name="style" select="$style"/>
            </xsl:if>
            <xsl:if test="$rows">
                <xsl:attribute name="rows" select="$rows"/>
            </xsl:if>
            <xsl:if test="$disabled">
                <xsl:attribute name="disabled" select="'disabled'"/>
            </xsl:if>

            <xsl:sequence select="$value"/>
        </textarea>

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="bs2:FormControlTypeLabel"/>
        </xsl:if>
    </xsl:template>

    <!-- blank nodes that only have rdf:type xsd:string and no other properties become literal inputs -->
    <xsl:template match="*[@rdf:nodeID]/*/@rdf:nodeID[key('resources', .)[not(* except rdf:type[starts-with(@rdf:resource, '&xsd;')])]]" mode="bs2:FormControl" priority="2">
        <xsl:param name="type" select="'text'" as="xs:string"/>
        <xsl:param name="id" select="generate-id()" as="xs:string"/>
        <xsl:param name="class" as="xs:string?"/>
        <xsl:param name="disabled" select="false()" as="xs:boolean"/>
        <xsl:param name="required" select="false()" as="xs:boolean"/>
        <xsl:param name="type-label" select="true()" as="xs:boolean"/>

        <xsl:call-template name="xhtml:Input">
            <xsl:with-param name="name" select="'ol'"/>
            <xsl:with-param name="type" select="$type"/>
            <xsl:with-param name="id" select="$id"/>
            <xsl:with-param name="class" select="$class"/>
            <xsl:with-param name="disabled" select="$disabled"/>
        </xsl:call-template>

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="bs2:FormControlTypeLabel">
                <xsl:with-param name="type" select="$type"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="*[@rdf:nodeID]/*/@rdf:nodeID[key('resources', .)[not(* except rdf:type[starts-with(@rdf:resource, '&xsd;')])]]" mode="bs2:FormControlTypeLabel" priority="2">
        <xsl:param name="type" as="xs:string?"/>

        <xsl:if test="not($type = 'hidden')">
            <span class="help-inline">Literal</span>
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

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="bs2:FormControlTypeLabel">
                <xsl:with-param name="type" select="$type"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:*[local-name() = ('resource', 'nodeID')]" mode="bs2:FormControlTypeLabel">
        <xsl:param name="type" as="xs:string?"/>

        <xsl:if test="not($type = 'hidden')">
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

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="bs2:FormControlTypeLabel">
                <xsl:with-param name="type" select="$type"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <xsl:template match="@xml:*[local-name() = 'lang']" mode="bs2:FormControlTypeLabel">
        <xsl:param name="type" as="xs:string?"/>

        <xsl:if test="not($type = 'hidden')">
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

        <xsl:if test="$type-label">
            <xsl:apply-templates select="." mode="bs2:FormControlTypeLabel">
                <xsl:with-param name="type" select="$type"/>
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="@rdf:*[local-name() = 'datatype']" mode="bs2:FormControlTypeLabel">
        <xsl:param name="type" as="xs:string?"/>

        <xsl:if test="not($type = 'hidden')">
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

                <!-- restore subject context -->
                <xsl:apply-templates select="../../@rdf:about | ../../@rdf:nodeID" mode="#current">
                    <xsl:with-param name="type" select="'hidden'"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="." mode="xhtml:Input">
                    <xsl:with-param name="type" select="$type"/>
                    <xsl:with-param name="id" select="$id"/>
                    <xsl:with-param name="class" select="$class"/>
                    <xsl:with-param name="disabled" select="$disabled"/>
                </xsl:apply-templates>
                
                <xsl:if test="$type-label">
                    <xsl:apply-templates select="." mode="bs2:FormControlTypeLabel">
                        <xsl:with-param name="type" select="$type"/>
                    </xsl:apply-templates>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="*[@rdf:*[local-name() = ('about', 'nodeID')]]/*/@rdf:*[local-name() = ('resource', 'nodeID')]" mode="bs2:FormControlTypeLabel" priority="1">
        <xsl:param name="type" as="xs:string?"/>

        <xsl:if test="not($type = 'hidden')">
            <span class="help-inline">Resource</span>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="*[@rdf:*[local-name() = 'parseType'] = 'Literal']/xhtml:*" mode="bs2:FormControl">
        <xsl:param name="id" select="generate-id()" as="xs:string"/>

        <textarea name="ol" id="{$id}" class="wymeditor">
            <xsl:apply-templates select="xhtml:*" mode="xml-to-string"/>
        </textarea>
        <xsl:call-template name="xhtml:Input">
            <xsl:with-param name="type" select="'hidden'"/>
            <xsl:with-param name="name" select="'lt'"/>
            <xsl:with-param name="value" select="'&rdf;XMLLiteral'"/>
        </xsl:call-template>
    </xsl:template>
    
</xsl:stylesheet>