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
    <!ENTITY a      "http://atomgraph.com/ns/core#">
    <!ENTITY ac     "http://atomgraph.com/ns/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY owl    "http://www.w3.org/2002/07/owl#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY sd     "http://www.w3.org/ns/sparql-service-description#">
    <!ENTITY ldt    "http://www.w3.org/ns/ldt#">    
    <!ENTITY core   "http://www.w3.org/ns/ldt/core/domain#">
    <!ENTITY spl    "http://spinrdf.org/spl#">    
    <!ENTITY void   "http://rdfs.org/ns/void#">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:a="&a;"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:sparql="&sparql;"
xmlns:sd="&sd;"
xmlns:ldt="&ldt;"
xmlns:core="&core;"
xmlns:spl="&spl;"
xmlns:void="&void;"
xmlns:bs2="http://graphity.org/xsl/bootstrap/2.3.2"
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

    <xsl:template match="rdf:RDF[key('resources', $a:absolutePath)/rdf:type/@rdf:resource = '&core;SPARQLEndpoint']" mode="bs2:Main" priority="2">
        <xsl:apply-templates select="." mode="bs2:BreadCrumbList"/>

        <xsl:apply-templates select="." mode="bs2:QueryForm"/>

        <xsl:if test="$query">
            <xsl:apply-templates select="." mode="ac:QueryResult"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="rdf:RDF[key('resources', $a:absolutePath)/rdf:type/@rdf:resource = '&core;SPARQLEndpoint']" mode="bs2:Right" priority="2"/>

    <xsl:template match="rdf:RDF[$a:absolutePath][key('resources', $a:absolutePath)/rdf:type/@rdf:resource = '&core;SPARQLEndpoint']" mode="xhtml:Style" priority="1">
        <xsl:next-match/>
        
        <link href="{resolve-uri('static/css/yasqe.css', $ac:contextUri)}" rel="stylesheet" type="text/css"/>
    </xsl:template>

    <xsl:template match="*[rdf:type/@rdf:resource = '&core;SPARQLEndpoint']" mode="bs2:ModeList" priority="1"/>
        
    <xsl:template match="rdf:RDF[$a:absolutePath][key('resources', $a:absolutePath)/rdf:type/@rdf:resource = '&core;SPARQLEndpoint']" mode="bs2:QueryForm">
        <xsl:param name="method" select="'get'" as="xs:string"/>
        <xsl:param name="action" select="xs:anyURI('')" as="xs:anyURI"/>
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

                <script src="{resolve-uri('static/js/yasqe.js', $ac:contextUri)}" type="text/javascript"></script>
                <script type="text/javascript">
                    <![CDATA[
                    var yasqe = YASQE.fromTextArea(document.getElementById("query-string"), { persistent: null });
                    ]]>
                </script>

                <div class="form-actions">
                    <xsl:if test="key('resources', key('resources', $a:requestUri)/ldt:arg/@rdf:nodeID)[spl:predicate/@rdf:resource = '&ac;mode']/rdf:value/@rdf:resource">
                        <input type="hidden" name="mode" value="{key('resources', key('resources', $a:requestUri)/ldt:arg/@rdf:nodeID)[spl:predicate/@rdf:resource = '&ac;mode']/rdf:value/@rdf:resource}"/>
                    </xsl:if>
                    <button type="submit" class="btn btn-primary">Query</button>
                </div>
            </fieldset>
	</form>            
    </xsl:template>

    <xsl:template match="rdf:RDF[$a:absolutePath][key('resources', $a:absolutePath)/rdf:type/@rdf:resource = '&core;SPARQLEndpoint']" mode="ac:QueryResult">
	<xsl:param name="result-doc" select="document(concat($a:absolutePath, ac:query-string((), $query, key('resources', key('resources', $a:requestUri)/ldt:arg/@rdf:nodeID)[spl:predicate/@rdf:resource = '&ac;mode']/rdf:value/@rdf:resource, ())))"/>

	<!-- result of CONSTRUCT or DESCRIBE -->
	<xsl:if test="$result-doc/rdf:RDF">
            <xsl:apply-templates select="." mode="bs2:ModeList"/>

            <xsl:for-each select="$result-doc/rdf:RDF">
                <xsl:choose>
                    <xsl:when test="key('resources', key('resources', $a:requestUri)/ldt:arg/@rdf:nodeID)[spl:predicate/@rdf:resource = '&ac;mode']/rdf:value/@rdf:resource = '&ac;ListMode'">
                        <xsl:apply-templates select="*" mode="bs2:BlockList">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="key('resources', key('resources', $a:requestUri)/ldt:arg/@rdf:nodeID)[spl:predicate/@rdf:resource = '&ac;mode']/rdf:value/@rdf:resource = '&ac;TableMode'">
                        <xsl:apply-templates select="." mode="xhtml:Table">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="key('resources', key('resources', $a:requestUri)/ldt:arg/@rdf:nodeID)[spl:predicate/@rdf:resource = '&ac;mode']/rdf:value/@rdf:resource = '&ac;GridMode'">
                        <xsl:apply-templates select="." mode="bs2:Grid">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="key('resources', key('resources', $a:requestUri)/ldt:arg/@rdf:nodeID)[spl:predicate/@rdf:resource = '&ac;mode']/rdf:value/@rdf:resource = '&ac;MapMode'">
                        <xsl:apply-templates select="." mode="bs2:Map">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:when>
                    <xsl:when test="key('resources', key('resources', $a:requestUri)/ldt:arg/@rdf:nodeID)[spl:predicate/@rdf:resource = '&ac;mode']/rdf:value/@rdf:resource = '&ac;EditMode'">
                        <xsl:apply-templates select="." mode="bs2:EditForm">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>                            
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="." mode="bs2:Block">
                            <xsl:with-param name="selected-resources" select="*" tunnel="yes"/>
                        </xsl:apply-templates>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
	</xsl:if>
        
	<!-- result of SELECT or ASK -->
	<xsl:if test="$result-doc/sparql:sparql">
	    <xsl:apply-templates select="$result-doc/sparql:sparql" mode="xhtml:Table"/>
	</xsl:if>
    </xsl:template>
    
</xsl:stylesheet>