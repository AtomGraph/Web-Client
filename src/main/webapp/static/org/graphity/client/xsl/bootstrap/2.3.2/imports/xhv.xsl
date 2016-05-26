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
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY xhv    "http://www.w3.org/1999/xhtml/vocab#">    
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
xmlns:xhv="&xhv;"
xmlns:bs2="http://graphity.org/xsl/bootstrap/2.3.2"
exclude-result-prefixes="#all">

    <!--
    <xsl:template match="xhv:prev[@rdf:resource]" mode="bs2:PaginationMode">
        <a href="{@rdf:resource}" class="active">
            &#8592; <xsl:apply-templates select="key('resources', concat(namespace-uri(), local-name()), document(''))" mode="gc:LabelMode"/>
        </a>
    </xsl:template>
        
    <xsl:template match="xhv:next[@rdf:resource]" mode="bs2:PaginationMode">
        <a href="{@rdf:resource}">
            <xsl:apply-templates select="key('resources', concat(namespace-uri(), local-name()), document(''))" mode="gc:LabelMode"/> &#8594;
        </a>
    </xsl:template>
    -->
    
</xsl:stylesheet>