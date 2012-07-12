<?xml version="1.0" encoding="UTF-8"?>
<!--
Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
-->
<!DOCTYPE xsl:stylesheet [
    <!ENTITY java "http://xml.apache.org/xalan/java/">
    <!ENTITY g "http://graphity.org/ontology/">
    <!ENTITY rdf "http://www.w3.org/1999/02/22-rdf-syntax-ns#">
    <!ENTITY rdfs "http://www.w3.org/2000/01/rdf-schema#">
    <!ENTITY owl "http://www.w3.org/2002/07/owl#">
    <!ENTITY xsd "http://www.w3.org/2001/XMLSchema#">
    <!ENTITY dct "http://purl.org/dc/terms/">
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY sioc "http://rdfs.org/sioc/ns#">
    <!ENTITY dbpedia "http://dbpedia.org/resource/">
]>
<xsl:stylesheet version="2.0"
xmlns="http://www.w3.org/1999/xhtml"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xhtml="http://www.w3.org/1999/xhtml"
xmlns:xs="http://www.w3.org/2001/XMLSchema"
xmlns:g="&g;"
xmlns:rdf="&rdf;"
xmlns:rdfs="&rdfs;"
xmlns:owl="&owl;"
xmlns:dct="&dct;"
xmlns:foaf="&foaf;"
xmlns:sioc="&sioc;"
xmlns:dbpedia="&dbpedia;"
xmlns:uuid="java:java.util.UUID"
exclude-result-prefixes="xsl xhtml xs g rdf rdfs owl dct foaf sioc dbpedia uuid">

    <!-- RDF/POST Encoding for RDF http://www.lsrn.org/semweb/rdfpost.html -->
    
    <xsl:template match="*[@rdf:about = resolve-uri('post', $base-uri)]" priority="1">
	<xsl:apply-templates select="." mode="Header"/>
	
	<form action="" method="post" class="form-horizontal">
	    <fieldset>
		<input type="hidden" name="rdf"/>
		<input type="hidden" name="sb" value="{concat('post', uuid:randomUUID())}"/>

		<div class="control-group">
		    <label for="select-type" class="control-label">
			<xsl:value-of select="g:label(xs:anyURI('&rdf;type'), /, $lang)"/>
		    </label>
		    <div class="controls">
			<input type="hidden" name="pu" value="&rdf;type"/>
			<input type="hidden" name="ou" value="&sioc;Post"/>
			<xsl:apply-templates select="key('resources', '&sioc;Post', document('&sioc;'))/@rdf:about"/>
		    </div>
		</div>
		<div class="control-group">
		    <label class="control-label" for="input-title" >
			<xsl:value-of select="g:label(xs:anyURI('&dct;title'), /, $lang)"/>
		    </label>
		    <div class="controls">
			<input type="hidden" name="pu" value="&dct;title"/>
			<input type="text" name="ol" id="input-title"/>
		    </div>
		</div>
		<div class="control-group">
		    <label class="control-label" for="input-content" >
			<xsl:value-of select="g:label(xs:anyURI('&sioc;content'), /, $lang)"/>
		    </label>
		    <div class="controls">
			<input type="hidden" name="pu" value="&sioc;content"/>
			<textarea name="ol" class="span12" id="input-content" rows="10"/>
		    </div>
		</div>
		<div class="control-group">
		    <label class="control-label" for="select-interest">
			<xsl:value-of select="g:label(xs:anyURI('&dct;subject'), /, $lang)"/>
		    </label>
		    <div class="controls">
			<input type="hidden" name="pu" value="&dct;subject"/>
			<select name="ou" id="select-interest" multiple="multiple" size="5">
			    <option value="&dbpedia;Data_management">
				<xsl:value-of select="g:label(xs:anyURI('&dbpedia;Data_management'), /, $lang)"/>
			    </option>
			    <option value="&dbpedia;Open_data">
				<xsl:value-of select="g:label(xs:anyURI('&dbpedia;Open_data'), /, $lang)"/>
			    </option>
			    <option value="&dbpedia;Analytics">
				<xsl:value-of select="g:label(xs:anyURI('&dbpedia;Analytics'), /, $lang)"/>
			    </option>
			    <option value="&dbpedia;Semantic_Integration">
				<xsl:value-of select="g:label(xs:anyURI('&dbpedia;Semantic_Integration'), /, $lang)"/>
			    </option>
			    <option value="&dbpedia;Web_application_framework">
				<xsl:value-of select="g:label(xs:anyURI('&dbpedia;Web_application_framework'), /, $lang)"/>
			    </option>
			</select>
			<p class="help-block">Feel free to select multiple</p>
		    </div>
		</div>
		<div class="control-group">
		    <label class="control-label" for="input-created">
			<xsl:value-of select="g:label(xs:anyURI('&dct;created'), /, $lang)"/>
		    </label>
		    <div class="controls">
			<input type="hidden" name="pu" value="&dct;created"/>
			<input type="text" name="ol" value="{current-dateTime()}" id="input-created"/>
			<input type="hidden" name="lt" value="&xsd;dateTime"/>
		    </div>
		</div>
		<div class="form-actions">
		    <button type="submit" class="btn btn-primary">Preview</button>
		</div>
	    </fieldset>
	</form>
    </xsl:template>

    <xsl:template match="*[@rdf:about = resolve-uri('post', $base-uri)]" mode="SidebarNav" priority="1">
	<div class="well sidebar-nav">
	    <h2 class="nav-header">See also</h2>
		
	    <ul class="nav nav-pills nav-stacked">
		<li>
		    <a href="http://www.lsrn.org/semweb/rdfpost.html">RDF/POST Encoding for RDF</a>
		</li>
	    </ul>
	</div>
    </xsl:template>
	
</xsl:stylesheet>