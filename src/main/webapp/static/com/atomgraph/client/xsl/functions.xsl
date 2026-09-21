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
    <!ENTITY ac     "https://w3id.org/atomgraph/client#">
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs   "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY list   "http://jena.hpl.hp.com/ARQ/list#">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:list="&list;"
xmlns:map="http://www.w3.org/2005/xpath-functions/map"
exclude-result-prefixes="#all"
>

    <!-- http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext -->

    <xsl:key name="resources-by-subclass" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:subClassOf/@rdf:resource | rdfs:subClassOf/@rdf:nodeID"/>
    
    <!-- the value to show from a set of same-property literals: the first with text in the reader's
         most preferred language, else the first untagged one, else the first of any. Language preference
         is one rule, so it lives here rather than once per vocabulary module -->
    <xsl:function name="ac:preferred-lang" as="text()?">
        <xsl:param name="values" as="element()*"/>

        <xsl:variable name="texted" select="$values[text()]" as="element()*"/>
        <xsl:sequence select="((for $lang in ac:langs() return $texted[lang($lang)])[1], $texted[not(@xml:lang)], $texted)[1]/text()"/>
    </xsl:function>

    <xsl:function name="ac:absolute-path" as="xs:anyURI">
        <xsl:param name="href" as="xs:anyURI"/>
        
        <xsl:sequence select="xs:anyURI(if (contains($href, '?')) then substring-before($href, '?') else if (contains($href, '#')) then substring-before($href, '#') else $href)"/>
    </xsl:function>
    
    <!-- function stub so that Saxon-EE doesn't complain when compiling SEF; client-side stylesheets override it -->
    <xsl:function name="ac:uuid" as="xs:string" override-extension-function="no">
        <xsl:message terminate="yes">
            Not implemented -- com.atomgraph.client.writer.function.UUID needs to be registered as an extension function
        </xsl:message>
    </xsl:function>
    
    <!-- the languages the reader accepts, most preferred first, reduced to primary subtags and deduped.

         Server-side that is the Accept-Language list the writer supplies; client-side LinkedDataHub overrides this whole
         function with one that reads the browser's own list, because Web-Client carries no browser dependencies. The two
         bodies are guarded by use-when so exactly one exists in any compilation - neither engine ever sees the other's.

         Normalising here rather than at each declaration is what keeps the two engines agreeing: primary subtags because
         fn:lang() prefix-matches, so 'es' reaches a label tagged es-ES while 'es-ES' would not reach one tagged es; deduped
         because a browser sending es-ES,es yields the same subtag twice; and 'en' when the reader expressed no preference,
         which is the same floor the negotiation applies server-side. -->
    <xsl:function name="ac:langs" as="xs:string*" use-when="system-property('xsl:product-name') = 'SAXON'">
        <xsl:variable name="langs" select="distinct-values(for $lang in $ac:langs return tokenize($lang, '-')[1])[not(. = ('', '*'))]" as="xs:string*"/>

        <xsl:sequence select="if (exists($langs)) then $langs else 'en'"/>
    </xsl:function>

    <!-- position of a value's language in the accepted list, used as a sort key so each property leads with the reader's language.
         Values in a language the reader does not accept, and untagged values, rank last and so sort after the accepted ones
         - they are ordered, never withheld. Takes the node explicitly: the one-argument fn:lang tests the context item, and
         a rank computed over a range of integers has no node to test. -->
    <xsl:function name="ac:lang-rank" as="xs:integer">
        <xsl:param name="value" as="element()"/>

        <xsl:variable name="langs" select="ac:langs()" as="xs:string*"/>

        <xsl:sequence select="((for $i in 1 to count($langs) return if (lang($langs[$i], $value)) then $i else ())[1], count($langs) + 1)[1]"/>
    </xsl:function>

    <!-- the UI label catalog: the static file beside the stylesheets. Importing layers override this function
         to resolve the catalog their own way (e.g. a same-origin URL in the browser) -->
    <xsl:function name="ac:translations" as="document-node()">
        <xsl:sequence select="document('translations.rdf')"/>
    </xsl:function>

    <xsl:function name="ac:label" as="xs:string?">
        <xsl:param name="resource" as="element()"/>

        <xsl:variable name="labels" as="xs:string*">
            <xsl:apply-templates select="$resource" mode="ac:label"/>
        </xsl:variable>
        <xsl:sequence select="upper-case(substring($labels[1], 1, 1)) || substring($labels[1], 2)"/>
    </xsl:function>

    <xsl:function name="ac:description" as="xs:string?">
        <xsl:param name="resource" as="element()"/>

        <xsl:variable name="descriptions" as="xs:string*">
            <xsl:apply-templates select="$resource" mode="ac:description"/>
        </xsl:variable>
        <xsl:sequence select="$descriptions[1]"/>
    </xsl:function>

    <xsl:function name="ac:property-label" as="xs:string?">
        <xsl:param name="property" as="element()"/>

        <xsl:variable name="labels" as="xs:string*">
            <xsl:apply-templates select="$property" mode="ac:property-label"/>
        </xsl:variable>
        <xsl:sequence select="upper-case(substring($labels[1], 1, 1)) || substring($labels[1], 2)"/>
    </xsl:function>

    <xsl:function name="ac:object-label" as="xs:string?">
        <xsl:param name="object" as="node()"/>
        
        <xsl:variable name="labels" as="xs:string*">
            <xsl:apply-templates select="$object" mode="ac:object-label"/>
        </xsl:variable>
        <xsl:sequence select="$labels[1]"/>
    </xsl:function>

    <xsl:function name="ac:svg-label" as="xs:string?">
        <xsl:param name="resource" as="element()"/>

        <xsl:sequence select="ac:label($resource)"/>
    </xsl:function>
    
    <xsl:function name="ac:svg-object-label" as="xs:string?">
        <xsl:param name="object" as="attribute()"/>

        <xsl:sequence select="ac:object-label($object)"/>
    </xsl:function>
    
    <xsl:function name="ac:document-uri" as="xs:anyURI">
        <xsl:param name="uri" as="item()"/>
        <xsl:choose>
            <!-- strip trailing fragment identifier (#) -->
            <xsl:when test="contains($uri, '#')">
                <xsl:sequence select="xs:anyURI(substring-before($uri, '#'))"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="xs:anyURI($uri)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="ac:fragment-id" as="xs:string?">
        <xsl:param name="uri" as="xs:anyURI"/>
        
        <xsl:sequence select="substring-after($uri, '#')"/>
    </xsl:function>

    <xsl:function name="list:member" as="node()*">
        <xsl:param name="list" as="node()?"/>
        <xsl:param name="document" as="document-node()"/>

        <xsl:if test="$list">
            <xsl:sequence select="key('resources', $list/rdf:first/@rdf:resource, $document) | key('resources', $list/rdf:first/@rdf:nodeID, $document)"/>

            <xsl:sequence select="list:member(key('resources', $list/rdf:rest/@rdf:resource, $document), $document) | list:member(key('resources', $list/rdf:rest/@rdf:nodeID, $document), $document)"/>
        </xsl:if>
    </xsl:function>

    <!-- builds URL query string out of a parameter map and appends it to the given URI, if any -->
    <!-- TO-DO: add a fragment parameter? -->
    <xsl:function name="ac:build-uri" as="xs:anyURI?">
        <xsl:param name="absolute-path" as="xs:anyURI?"/>
        <xsl:param name="query-params" as="map(xs:string, xs:string*)"/>
        
        <xsl:sequence select="xs:anyURI(let $query-string :=
            if (map:size($query-params) ne 0) then
                '?' || string-join(
                   map:for-each(
                      $query-params,
                      function ($key, $values) {
                         for $value in $values return 
                            encode-for-uri($key) || '=' || encode-for-uri($value)
                      }
                   ),
                   codepoints-to-string(38)
                   )
            else
               ''
        return if ($absolute-path) then $absolute-path || $query-string else $query-string)"/>
    </xsl:function>
    
</xsl:stylesheet>