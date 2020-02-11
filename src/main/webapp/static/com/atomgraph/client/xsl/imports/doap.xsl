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
    <!ENTITY doap   "http://usefulinc.com/ns/doap#">
]>
<xsl:stylesheet version="2.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:ac="&ac;"
xmlns:rdf="&rdf;"
xmlns:doap="&doap;"
exclude-result-prefixes="#all">
    
    <xsl:template match="doap:name | @doap:name" mode="ac:label">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="doap:description" mode="ac:description">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="doap:homepage/@rdf:resource | doap:browse/@rdf:resource | doap:location/@rdf:resource | doap:file-release/@rdf:resource">
        <a href="{.}">
            <xsl:value-of select="."/>
        </a>
    </xsl:template>

</xsl:stylesheet>