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
    <!ENTITY gc     "http://graphity.org/gc#">
    <!ENTITY gp     "http://graphity.org/gp#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY sd     "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY void   "http://rdfs.org/ns/void#">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:gp="&gp;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:sd="&sd;"
xmlns:void="&void;"
xmlns:javaee="http://java.sun.com/xml/ns/javaee"
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

    <xsl:template match="rdf:RDF[key('resources', $gc:uri)/rdf:type/@rdf:resource = '&gp;SPARQLEndpoint']" priority="2">
        <xsl:param name="selected-resources" select="*[rdf:type/@rdf:resource = '&foaf;Document'][not(@rdf:about = $gc:uri)]" as="element()*"/>

	<div class="container-fluid">
	    <div class="row-fluid">
		<div class="span8">
                    <xsl:apply-templates select="." mode="gc:BreadCrumbMode"/>

                    <xsl:apply-templates select="." mode="gc:HeaderMode"/> 

                    <xsl:apply-templates select="." mode="gc:QueryFormMode"/>

                    <xsl:if test="$query">
                        <xsl:apply-templates select="." mode="gc:QueryResultMode"/>
                    </xsl:if>
                </div>

		<div class="span4">
		    <xsl:apply-templates select="." mode="gc:SidebarNavMode"/>
		</div>
	    </div>
	</div>
    </xsl:template>

    <xsl:template match="rdf:RDF[key('resources', $gc:uri)/rdf:type/@rdf:resource = '&gp;SPARQLEndpoint']" mode="gc:StyleMode" priority="1">
        <xsl:next-match/>
        
        <link href="{resolve-uri('static/css/yasqe.css', $gc:contextUri)}" rel="stylesheet" type="text/css"/>
    </xsl:template>
    
    <xsl:template match="rdf:RDF[key('resources', $gc:uri)/rdf:type/@rdf:resource = '&gp;SPARQLEndpoint']" mode="gc:QueryFormMode">
        <xsl:param name="method" select="'get'" as="xs:string"/>
        <xsl:param name="action" select="''" as="xs:string"/>
        <xsl:param name="id" select="'query-form'" as="xs:string?"/>
        <xsl:param name="class" select="'form-horizontal'" as="xs:string?"/>
        <xsl:param name="accept-charset" select="'UTF-8'" as="xs:string?"/>
        <xsl:param name="enctype" as="xs:string?"/>
        <!-- <xsl:param name="query-string" as="xs:string?"/> -->
        
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
        
            <fieldset>
                <textarea id="query-string" name="query" class="span12" rows="15">
                    <xsl:choose>
                        <xsl:when test="$query">
                            <xsl:value-of select="$query"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$default-query"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </textarea>

                <script src="{resolve-uri('static/js/yasqe.js', $gc:contextUri)}" type="text/javascript"></script>
                <script type="text/javascript">
                    <![CDATA[
                    var yasqe = YASQE.fromTextArea(document.getElementById("query-string"), { persistent: null });
                    ]]>
                </script>

                <div class="form-actions">
                    <xsl:if test="$gp:mode">
                        <input type="hidden" name="mode" value="{$gp:mode}"/>
                    </xsl:if>
                    <button type="submit" class="btn btn-primary">Query</button>
                </div>
            </fieldset>
	</form>            
    </xsl:template>

    <xsl:template match="rdf:RDF[key('resources', $gc:uri)/rdf:type/@rdf:resource = '&gp;SPARQLEndpoint']" mode="gc:QueryResultMode">
	<xsl:param name="result-doc" select="document(concat($gc:uri, gc:query-string((), $query, $gp:mode, ())))"/>

	<!-- result of CONSTRUCT or DESCRIBE -->
	<xsl:if test="$result-doc/rdf:RDF">
            <xsl:apply-templates select="." mode="gc:ModeSelectMode"/>

            <xsl:for-each select="$result-doc/rdf:RDF">
                <xsl:choose>
                    <xsl:when test="$gp:mode = '&gc;ListMode'">
                        <xsl:apply-templates select="*" mode="gc:ListMode">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="$gp:mode = '&gc;TableMode'">
                        <xsl:apply-templates select="." mode="gc:TableMode">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="$gp:mode = '&gc;ThumbnailMode'">
                        <xsl:apply-templates select="." mode="gc:ThumbnailMode">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="$gp:mode = '&gc;MapMode'">
                        <xsl:apply-templates select="." mode="gc:MapMode">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="$gp:mode = '&gc;EditMode'">
                        <xsl:apply-templates select="." mode="gc:EditMode">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>                            
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="." mode="gc:ReadMode">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
	</xsl:if>
        
	<!-- result of SELECT or ASK -->
	<xsl:if test="$result-doc/sparql:sparql">
	    <xsl:apply-templates select="$result-doc/sparql:sparql" mode="gc:TableMode"/>
	</xsl:if>
    </xsl:template>
    
</xsl:stylesheet>