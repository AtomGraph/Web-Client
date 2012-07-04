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
    <!ENTITY foaf "http://xmlns.com/foaf/0.1/">
    <!ENTITY dbpedia "http://dbpedia.org/resource/">
    <!ENTITY dct "http://purl.org/dc/terms/">
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
xmlns:foaf="&foaf;"
xmlns:dbpedia="&dbpedia;"
xmlns:dct="&dct;"
xmlns:uuid="java:java.util.UUID"
exclude-result-prefixes="xsl xhtml xs g rdf rdfs owl foaf dbpedia uuid">

    <!-- RDF/POST Encoding for RDF http://www.lsrn.org/semweb/rdfpost.html -->
    
    <xsl:template match="*[@rdf:about = resolve-uri('rdf-post', $base-uri)]" priority="1">
	<xsl:next-match/>
	
	<form action="" method="post" class="form-horizontal" onsubmit="if (this.elements['input-mbox'].value != '') this.elements['input-mbox'].value = 'mailto:' + this.elements['input-mbox'].value;">
	    <fieldset>
		<input type="hidden" name="rdf"/>
		<input type="hidden" name="sb" value="{concat('agent', uuid:randomUUID())}"/>

		<div class="control-group">
		    <label for="select-type" class="control-label">I represent</label>
		    <div class="controls">
			<input type="hidden" name="pu" value="&rdf;type" id="select-type"/>
			<select name="ou" id="select-type">
			    <option value="&foaf;Person">Myself, an individual user</option>
			    <option value="&dbpedia;Company">Commercial company</option>
			    <option value="&dbpedia;Government_agency">Government department or agency</option>
			    <option value="&dbpedia;Research_institute">Research/educational institution</option>
			    <option value="&dbpedia;Non-governmental_organization">Non-governmental organization</option>
			</select>
		    </div>
		</div>
		<div class="control-group">
		    <label for="select-interest" class="control-label">I'm interested in</label>
		    <div class="controls">
			<input type="hidden" name="pu" value="&foaf;topic_interest"/>
			<select name="ou" id="select-interest" multiple="multiple" size="5">
			    <option value="&dbpedia;Data_management">Content/data management</option>
			    <option value="&dbpedia;Open_data">(Linked) Data publishing</option>
			    <option value="&dbpedia;Analytics">Linked Data visualizations and/or analytics</option>
			    <option value="&dbpedia;Semantic_Integration">Integration of Web 2.0 services and/or social media</option>
			    <option value="&dbpedia;Web_application_framework">Semantic platform for rapid webapp development</option>
			</select>
			<p class="help-block">Feel free to select multiple</p>
		    </div>
		</div>
		<div class="control-group">
		    <label for="input-license" class="control-label">License of this data</label>
		    <div class="controls">
			<input type="hidden" name="pu" value="&dct;license"/>
			<select name="ou">
			    <option value="http://opendatacommons.org/licenses/odbl/1.0/" selected="selected"/>
			</select>
		    </div>
		</div>
	    </fieldset>
	    <fieldset>
		<div class="control-group">
		    <label for="input-mbox" class="control-label">E-mail address</label>
		    <div class="controls">
			<input type="hidden" name="pu" value="&foaf;mbox"/>
			<input id="input-mbox" type="text" name="ou" class="input-large"/>
			<p>Fill out if you want to be informed about Graphity releases</p>
		    </div>
		</div>
		<div class="form-actions">
		    <button type="submit" class="btn btn-primary">Submit to Graphity</button>
		    <button type="submit" class="btn">Test locally</button>
		</div>
	    </fieldset>
	</form>
    </xsl:template>

    <xsl:template match="*[@rdf:about = resolve-uri('rdf-post', $base-uri)]" mode="SidebarNav" priority="1">
	<div class="well sidebar-nav">
	    <h2 class="nav-header">See also</h2>
		
	    <ul class="nav nav-pills nav-stacked">
		<li>
		    <a href="http://www.lsrn.org/semweb/rdfpost.html">RDF/POST Encoding for RDF</a>
		</li>
		<li>
		    <a href="http://opendatacommons.org/licenses/odbl/1.0/">Open Database License (ODbL) v1.0</a>
		</li>
	    </ul>
	</div>
    </xsl:template>
	
</xsl:stylesheet>