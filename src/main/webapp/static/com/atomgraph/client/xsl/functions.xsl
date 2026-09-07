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
    <!ENTITY xsd    "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY sparql "http://www.w3.org/2005/sparql-results#">
    <!ENTITY ldt    "https://www.w3.org/ns/ldt#">
    <!ENTITY dc     "http://purl.org/dc/elements/1.1/">
    <!ENTITY dct    "http://purl.org/dc/terms/">
    <!ENTITY foaf   "http://xmlns.com/foaf/0.1/">
    <!ENTITY skos   "http://www.w3.org/2004/02/skos/core#">
    <!ENTITY sp     "http://spinrdf.org/sp#">
    <!ENTITY list   "http://jena.hpl.hp.com/ARQ/list#">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:xsd="&xsd;"
xmlns:sparql="&sparql;"
xmlns:ldt="&ldt;"
xmlns:dc="&dc;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:skos="&skos;"
xmlns:sp="&sp;"
xmlns:list="&list;"
xmlns:map="http://www.w3.org/2005/xpath-functions/map"
exclude-result-prefixes="#all"
>

    <!-- http://xml.apache.org/xalan-j/extensions_xsltc.html#java_ext -->

    <xsl:key name="resources-by-subclass" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:subClassOf/@rdf:resource | rdfs:subClassOf/@rdf:nodeID"/>
    <xsl:key name="resources-by-domain" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:domain/@rdf:resource"/>
    <xsl:key name="resources-by-range" match="*[@rdf:about] | *[@rdf:nodeID]" use="rdfs:range/@rdf:resource"/>
    <xsl:key name="resources-by-broader" match="*[@rdf:about] | *[@rdf:nodeID]" use="skos:broader/@rdf:resource"/>
    <xsl:key name="resources-by-narrower" match="*[@rdf:about] | *[@rdf:nodeID]" use="skos:narrower/@rdf:resource"/>
    
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
    
    <!-- function stub so that Saxon-EE doesn't complain when compiling SEF -->
    <xsl:function name="ac:construct" as="document-node()*" override-extension-function="no">
        <xsl:param name="ontology" as="xs:anyURI"/>
        <xsl:param name="classes" as="xs:anyURI*"/>
        <xsl:param name="base" as="xs:anyURI"/>
            
        <xsl:message use-when="system-property('xsl:product-name') = 'SAXON'" terminate="yes">
            Not implemented -- com.atomgraph.client.writer.function.ConstructForClass needs to be registered as an extension function
        </xsl:message>
    </xsl:function>
    
    <!-- function stub so that Saxon-EE doesn't complain when compiling SEF -->
    <xsl:function name="ac:construct" as="document-node()*" override-extension-function="no">
        <xsl:param name="query" as="xs:string"/>
            
        <xsl:message use-when="system-property('xsl:product-name') = 'SAXON'" terminate="yes">
            Not implemented -- com.atomgraph.client.writer.function.Construct needs to be registered as an extension function
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

    <!-- the UI label catalog. The canonical URI is location-mapped to the bundled copy (prefix-mapping.n3), so
         resolution stays local like the system ontologies'; importing layers override this function to resolve
         the catalog their own way (e.g. a same-origin URL in the browser) -->
    <xsl:function name="ac:translations" as="document-node()">
        <xsl:sequence select="document('https://w3id.org/atomgraph/client/xsl/translations.rdf')"/>
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

    <xsl:function name="rdfs:domain" as="attribute()*">
        <xsl:param name="property-uri" as="xs:anyURI*"/>
        <xsl:for-each select="$property-uri">
            <xsl:for-each select="document(ac:document-uri($property-uri))">
                <xsl:sequence select="key('resources', $property-uri)/rdfs:domain/@rdf:resource"/>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="ac:inDomainOf" as="attribute()*">
        <xsl:param name="type-uri" as="xs:anyURI*"/>
        <xsl:for-each select="$type-uri">
            <xsl:for-each select="document(ac:document-uri(.))">
                <xsl:sequence select="key('resources-by-domain', $type-uri)/@rdf:about"/>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="rdfs:range" as="attribute()*">
        <xsl:param name="property-uri" as="xs:anyURI*"/>
        <xsl:for-each select="$property-uri">
            <xsl:for-each select="document(ac:document-uri($property-uri))">
                <xsl:sequence select="key('resources', $property-uri)/rdfs:range/@rdf:resource"/>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="rdfs:subClassOf" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:sequence select="rdfs:subClassOf($uri, document(ac:document-uri($uri)))"/>
    </xsl:function>

    <xsl:function name="rdfs:subClassOf" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:param name="document" as="document-node()"/>
        <xsl:for-each select="$document">
            <xsl:sequence select="key('resources', $uri)/rdfs:subClassOf/@rdf:resource"/>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="ac:superClassOf" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:sequence select="ac:superClassOf($uri, document(ac:document-uri($uri)))"/>
    </xsl:function>

    <xsl:function name="ac:superClassOf" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:param name="document" as="document-node()"/>
        <xsl:for-each select="$document">
            <xsl:sequence select="key('resources-by-subclass', $uri)/@rdf:about"/>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="skos:broader" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:sequence select="skos:broader($uri, document(ac:document-uri($uri)))"/>
    </xsl:function>

    <xsl:function name="skos:broader" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:param name="document" as="document-node()"/>
        <xsl:for-each select="$document">
            <xsl:sequence select="key('resources', $uri)/skos:broader/@rdf:resource | key('resources-by-narrower', $uri)/@rdf:about"/>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="skos:narrower" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:sequence select="skos:narrower($uri, document(ac:document-uri($uri)))"/>
    </xsl:function>

    <xsl:function name="skos:narrower" as="attribute()*">
        <xsl:param name="uri" as="xs:anyURI*"/>
        <xsl:param name="document" as="document-node()"/>
        <xsl:for-each select="$document">
            <xsl:sequence select="key('resources', $uri)/skos:narrower/@rdf:resource | key('resources-by-broader', $uri)/@rdf:about"/>
        </xsl:for-each>
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
    
    <xsl:function name="ac:visit-elements" as="element()*">
        <xsl:param name="element" as="element()"/>
        <xsl:param name="type" as="xs:string?"/>
        
        <xsl:choose>
            <xsl:when test="$element/rdf:type/@rdf:resource = $type">
                <xsl:sequence select="key('resources', $element/sp:query/(@rdf:resource, @rdf:nodeID), root($element))"/>
            </xsl:when>
            <xsl:when test="list:member($element, root($element))">
                <xsl:sequence select="list:member($element, root($element))/ac:visit-elements(., $type)"/>
            </xsl:when>
            <xsl:when test="$element/sp:elements/@rdf:nodeID">
                <xsl:sequence select="key('resources', $element/sp:elements/@rdf:nodeID, root($element))/ac:visit-elements(., $type)"/>
            </xsl:when>
        </xsl:choose>
    </xsl:function>
    
</xsl:stylesheet>