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
    <!ENTITY a      "https://w3id.org/atomgraph/core#">
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
    <!ENTITY translations "https://w3id.org/atomgraph/client/xsl/translations.rdf#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY srx    "http://www.w3.org/2005/sparql-results#">
    <!ENTITY sd     "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY ldt    "https://www.w3.org/ns/ldt#">
    <!ENTITY spl    "http://spinrdf.org/spl#">
    <!ENTITY void   "http://rdfs.org/ns/void#">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
]>
<xsl:stylesheet version="3.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:a="&a;"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:srx="&srx;"
xmlns:sd="&sd;"
xmlns:ldt="&ldt;"
xmlns:spl="&spl;"
xmlns:void="&void;"
exclude-result-prefixes="#all">

    <xsl:param name="default-query" as="xs:string">SELECT DISTINCT *
WHERE
{
    { ?s ?p ?o }
    UNION
    {
        GRAPH ?g
        { ?s ?p ?o }
    }
}
LIMIT 100</xsl:param>

    <xsl:template match="rdf:RDF[$ac:mode = '&ac;QueryEditorMode']" mode="ac:Main" priority="2">
        <xsl:param name="id" as="xs:string?"/>
        <xsl:param name="class" select="'main'" as="xs:string?"/>
        
        <div>
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            
            <xsl:call-template name="ac:SPARQLPane">
                <xsl:with-param name="uri" select="ac:absolute-path(base-uri())"/>
                <xsl:with-param name="mode" select="$ac:mode"/>
                <xsl:with-param name="endpoint" select="$ac:endpoint"/>
                <xsl:with-param name="query" select="$ac:query"/>
                <xsl:with-param name="default-query" select="$default-query"/>
            </xsl:call-template>

            <xsl:if test="$ac:query">
                <xsl:call-template name="ac:QueryResult"/>
            </xsl:if>
        </div>
    </xsl:template>

    <xsl:template match="rdf:RDF[$ac:mode = '&ac;QueryEditorMode']" mode="ac:Aside" priority="2"/>

    <xsl:template match="rdf:RDF[$ac:mode = '&ac;QueryEditorMode']" mode="xhtml:Style" priority="1">
        <xsl:next-match/>
        
        <link href="{resolve-uri('static/css/yasqe.css', $ac:contextUri)}" rel="stylesheet" type="text/css"/>
    </xsl:template>

    <xsl:template name="ac:SPARQLPane">
        <xsl:param name="method" select="'get'" as="xs:string"/>
        <xsl:param name="action" select="xs:anyURI('')" as="xs:anyURI"/>
        <xsl:param name="id" select="'query-form'" as="xs:string?"/>
        <xsl:param name="class" select="'query-form'" as="xs:string?"/>
        <xsl:param name="accept-charset" select="'UTF-8'" as="xs:string?"/>
        <xsl:param name="enctype" as="xs:string?"/>
        <xsl:param name="uri" as="xs:anyURI?"/>
        <xsl:param name="mode" as="xs:anyURI*"/>
        <xsl:param name="endpoint" as="xs:anyURI?"/>
        <xsl:param name="query" as="xs:string?"/>
        <xsl:param name="default-query" as="xs:string?"/>
        
        <form method="{$method}" action="{$action}">
            <xsl:if test="$id">
                <xsl:attribute name="id" select="$id"/>
            </xsl:if>
            <xsl:if test="$class">
                <xsl:attribute name="class" select="$class"/>
            </xsl:if>
            <xsl:if test="$accept-charset">
                <xsl:attribute name="accept-charset" select="$accept-charset"/>
            </xsl:if>
            <xsl:if test="$enctype">
                <xsl:attribute name="enctype" select="$enctype"/>
            </xsl:if>
        
            <fieldset>
                <label class="ldhc-label" for="endpoint-uri">
                    <xsl:apply-templates select="key('resources', '&translations;endpoint', ac:translations())" mode="ac:label"/>
                </label>
                <div class="ldhc-field">
                    <div class="ldhc-field-box sz-md">
                        <input type="text" id="endpoint-uri" name="endpoint">
                            <xsl:if test="$endpoint">
                                <xsl:attribute name="value" select="$endpoint"/>
                            </xsl:if>
                        </input>
                    </div>
                </div>

                <textarea id="query-string" name="query" rows="15">
                    <xsl:choose>
                        <xsl:when test="$query">
                            <xsl:sequence select="$query"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:sequence select="$default-query"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </textarea>

                <script src="{resolve-uri('static/js/yasqe.js', $ac:contextUri)}" type="text/javascript"></script>
                <script type="text/javascript">
                    <![CDATA[
                    var yasqe = YASQE.fromTextArea(document.getElementById("query-string"), { persistent: null });
                    ]]>
                </script>

                <div class="form-actions">
                    <xsl:if test="$uri">
                        <input type="hidden" name="uri" value="{$uri}"/>
                    </xsl:if>
                    <xsl:if test="$endpoint">
                        <input type="hidden" name="endpoint" value="{$endpoint}"/>
                    </xsl:if>
                    <xsl:for-each select="$mode">
                        <input type="hidden" name="mode" value="{.}"/>
                    </xsl:for-each>
                    
                    <button type="submit" class="ldhc-btn in-primary ap-solid sz-md">
                        <span class="msi sm" aria-hidden="true">play_arrow</span>
                        <xsl:apply-templates select="key('resources', '&translations;query', ac:translations())" mode="ac:label"/>
                    </button>
                </div>
            </fieldset>
        </form>
    </xsl:template>

    <xsl:template name="ac:QueryResult">
        <xsl:param name="result-doc" select="document(ac:build-uri($ac:endpoint, map{ 'query': string($ac:query) }))"/>

        <!-- result of CONSTRUCT or DESCRIBE -->
        <xsl:if test="$result-doc/rdf:RDF">
            <xsl:apply-templates select="." mode="ac:ModeList"/>

            <xsl:for-each select="$result-doc/rdf:RDF">
                <xsl:choose>
                    <xsl:when test="$ac:mode = '&ac;ListMode'">
                        <xsl:apply-templates select="*" mode="ac:List">
                            <!-- <xsl:with-param name="selected-resources" select="*" tunnel="yes"/> -->
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="$ac:mode = '&ac;TableMode'">
                        <xsl:apply-templates select="." mode="xhtml:Table">
                            <!-- <xsl:with-param name="selected-resources" select="*" tunnel="yes"/> -->
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="$ac:mode = '&ac;GridMode'">
                        <xsl:apply-templates select="." mode="ac:Grid">
                            <!-- <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>-->
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="$ac:mode = '&ac;MapMode'">
                        <xsl:apply-templates select="." mode="ac:Map">
                            <!-- <xsl:with-param name="selected-resources" select="*" tunnel="yes"/> -->
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="$ac:mode = '&ac;EditMode'">
                        <xsl:apply-templates select="." mode="ac:ResourceForm">
                            <!-- <xsl:with-param name="selected-resources" select="*" tunnel="yes"/> -->
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select=".">
                            <!-- <xsl:with-param name="selected-resources" select="*" tunnel="yes"/> -->
                        </xsl:apply-templates>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:if>
        
        <!-- result of SELECT or ASK -->
        <xsl:if test="$result-doc/srx:sparql">
            <xsl:apply-templates select="$result-doc/srx:sparql" mode="xhtml:Table"/>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>