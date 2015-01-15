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
    <!ENTITY gs     "http://graphity.org/gs#">
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

    <xsl:param name="default-query" as="xs:string">PREFIX rdf: &lt;&rdf;&gt;
PREFIX rdfs: &lt;&rdfs;&gt;
PREFIX owl: &lt;&owl;&gt;
PREFIX xsd: &lt;&xsd;&gt;

SELECT DISTINCT *
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

    <xsl:template match="rdf:RDF[$gp:absolutePath = resolve-uri('sparql', $gp:baseUri)]" priority="2">
        <xsl:param name="selected-resources" select="*[rdf:type/@rdf:resource = '&foaf;Document'][not(@rdf:about = $gp:absolutePath)]" as="element()*"/>

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

    <xsl:template match="rdf:RDF[$gp:absolutePath = resolve-uri('sparql', $gp:baseUri)]" mode="gc:StyleMode" priority="1">
        <xsl:next-match/>
        
        <link href="{resolve-uri('static/css/yasqe.css', $gc:contextUri)}" rel="stylesheet" type="text/css"/>
    </xsl:template>
    
    <xsl:template match="rdf:RDF[$gp:absolutePath = resolve-uri('sparql', $gp:baseUri)]" mode="gc:QueryFormMode">
        <form action="" method="get" id="query-form">
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
                    var yasqe = YASQE.fromTextArea(document.getElementById("query-string"), {persistent: null});
                    ]]>
                </script>

                <div class="form-actions">
                    <xsl:if test="$gc:mode">
                        <input type="hidden" name="mode" value="{$gc:mode}"/>
                    </xsl:if>
                    <button type="submit" class="btn btn-primary">Query</button>
                    <!-- <span class="help-inline">For all queries, the maximum number of results is set to <xsl:value-of select="$gs:resultLimit"/>.</span> -->
                </div>
            </fieldset>
	</form>            
    </xsl:template>

    <xsl:template match="rdf:RDF[$gp:absolutePath = resolve-uri('sparql', $gp:baseUri)]" mode="gc:QueryResultMode">
	<xsl:param name="result-doc" select="document(concat($gp:absolutePath, gc:query-string((), $query, $gc:mode, ())))"/>

	<!-- result of CONSTRUCT or DESCRIBE -->
	<xsl:if test="$result-doc/rdf:RDF">
            <xsl:apply-templates select="." mode="gc:ModeSelectMode"/>

            <xsl:for-each select="$result-doc/rdf:RDF">
                <xsl:choose>
                    <xsl:when test="$gc:mode = '&gc;ListMode'">
                        <xsl:apply-templates select="*" mode="gc:ListMode">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="$gc:mode = '&gc;TableMode'">
                        <xsl:apply-templates select="." mode="gc:TableMode">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="$gc:mode = '&gc;ThumbnailMode'">
                        <xsl:apply-templates select="." mode="gc:ThumbnailMode">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="$gc:mode = '&gc;MapMode'">
                        <xsl:apply-templates select="." mode="gc:MapMode">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="$gc:mode = '&gc;EditMode'">
                        <xsl:apply-templates select="." mode="gc:EditMode">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>                            
                    </xsl:when>
                    <xsl:when test="$gc:mode = '&gc;CreateMode'">
                        <xsl:apply-templates select="." mode="gc:CreateMode">
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

    <xsl:template match="rdf:RDF[$gp:absolutePath = resolve-uri('sparql', $gp:baseUri)]" mode="gc:ModeSelectMode" priority="1">
        <ul class="nav nav-tabs">
            <xsl:apply-templates select="key('resources-by-type', '&gc;ContainerMode', document('&gc;'))[not(@rdf:about = '&gc;CreateMode')]" mode="#current">
                <xsl:sort select="gc:label(.)"/>
            </xsl:apply-templates>
        </ul>
    </xsl:template>

    <xsl:template match="@rdf:about[$gp:absolutePath = resolve-uri('sparql', $gp:baseUri)]" mode="gc:ModeSelectMode" priority="1">
        <a href="{$gp:absolutePath}{gc:query-string((), $query, ., ())}">
            <xsl:apply-templates select=".." mode="gc:LabelMode"/>
        </a>
    </xsl:template>
    
</xsl:stylesheet>