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
    <!ENTITY gc "http://graphity.org/gc#">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:gc="&gc;"
xmlns:rdf="&rdf;"
exclude-result-prefixes="#all">

    <xsl:template match="rdf:type/@rdf:resource" priority="1">
	<span title="{.}" class="btn btn-type">
            <xsl:next-match/>
        </span>
    </xsl:template>

    <!--
    <xsl:template match="rdf:type/@rdf:resource[. = $rdf:type]" priority="2" mode="xhtml:Anchor">
	<span title="{.}" class="btn btn-type btn-primary">
            <xsl:next-match/>
        </span>
    </xsl:template>
    -->
    
</xsl:stylesheet>