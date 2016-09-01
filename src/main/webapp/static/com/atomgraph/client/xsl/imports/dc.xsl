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
    <!ENTITY gc     "http://atomgraph.com/client/ns#">
    <!ENTITY gp     "http://graphity.org/gp#">    
    <!ENTITY rdf    "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY dc     "http://purl.org/dc/elements/1.1/">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:gc="&gc;"
xmlns:gp="&gp;"
xmlns:rdf="&rdf;"
xmlns:dc="&dc;"
exclude-result-prefixes="#all">

    <xsl:template match="dc:title | @dc:title" mode="gc:label">
	<xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="dc:description" mode="gc:description">
        <xsl:value-of select="."/>
    </xsl:template>

</xsl:stylesheet>