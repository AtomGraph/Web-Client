<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright 2020 Martynas Jusevičius <martynas@atomgraph.com>

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
<xsl:stylesheet version="3.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
exclude-result-prefixes="#all">

    <!-- the shared Web-Client layer: functions, the value/property dispatch, the vocabulary modules and
         the resource/document/container renderers - everything below the page layout, aggregated once so
         every consumer (internal-layout, external-layout, downstream masters such as LinkedDataHub's)
         imports one module and inherits one precedence order. Import order IS the precedence ladder
         (XSLT 3.0 §3.10.3): no module may enter any closure twice, and additions belong on the right
         rung here, not in the consumers. Converters are standalone by design and never aggregated. -->

    <xsl:import href="group-sort-triples.xsl"/>
    <xsl:import href="functions.xsl"/>
    <xsl:import href="imports/default.xsl"/>
    <xsl:import href="imports/dbpedia-owl.xsl"/>
    <!-- imported first among the vocabulary modules: sh:name/sh:description are modeling-layer fallbacks
         that must lose to every data-layer label, including the language-negotiated ladders that follow -->
    <xsl:import href="imports/sh.xsl"/>
    <xsl:import href="imports/dc.xsl"/>
    <xsl:import href="imports/dct.xsl"/>
    <xsl:import href="imports/dh.xsl"/>
    <xsl:import href="imports/doap.xsl"/>
    <xsl:import href="imports/foaf.xsl"/>
    <xsl:import href="imports/ldt.xsl"/>
    <xsl:import href="imports/rdf.xsl"/>
    <xsl:import href="imports/rdfs.xsl"/>
    <xsl:import href="imports/sd.xsl"/>
    <xsl:import href="imports/schema.xsl"/>
    <xsl:import href="imports/sioc.xsl"/>
    <xsl:import href="imports/skos.xsl"/>
    <xsl:import href="imports/sp.xsl"/>
    <xsl:import href="resource.xsl"/>
    <xsl:import href="document.xsl"/>
    <xsl:import href="container.xsl"/>

</xsl:stylesheet>
