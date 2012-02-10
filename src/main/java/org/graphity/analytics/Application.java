/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.graphity.analytics;

import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.vocabulary.FOAF;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.DC;
import com.hp.hpl.jena.vocabulary.DCTerms;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;
import org.graphity.provider.ModelProvider;
import org.graphity.provider.RDFResourceXSLTWriter;
import org.graphity.util.LocatorLinkedData;
import org.graphity.vocabulary.Graphity;
import org.graphity.vocabulary.SIOC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class Application extends javax.ws.rs.core.Application
{
    private static final Logger log = LoggerFactory.getLogger(LocatorLinkedData.class);
    private Set<Class<?>> classes = new HashSet<Class<?>>();
    private Set<Object> singletons = new HashSet<Object>();
    @Context private ServletContext context = null;
    //@Context private UriInfo uriInfo = null;
    
    @PostConstruct
    public void init() // initialize locally cached ontologies
    {
	log.debug("Application.init() ServletContext: {}", context);
	try
	{
	    // http://www4.wiwiss.fu-berlin.de/lodcloud/state/#terms
	    // http://incubator.apache.org/jena/documentation/ontology/#compound_ontology_documents_and_imports_processing
	    //OntDocumentManager.getInstance().setCacheModels(false);
	    log.debug("OntDocumentManager is caching Models: {}", OntDocumentManager.getInstance().getCacheModels());
	    
	    // move this to external configuration
	    OntDocumentManager.getInstance().addAltEntry(RDF.getURI(), "file:///" + context.getRealPath("/WEB-INF/owl/rdf.owl"));
	    OntDocumentManager.getInstance().addAltEntry(RDFS.getURI(), "file:///" + context.getRealPath("/WEB-INF/owl/rdfs.owl"));
	    OntDocumentManager.getInstance().addAltEntry(OWL2.getURI(), "file:///" + context.getRealPath("/WEB-INF/owl/owl2.owl"));
	    OntDocumentManager.getInstance().addAltEntry(DC.getURI(), "file:///" + context.getRealPath("/WEB-INF/owl/dcelements.rdf"));
	    OntDocumentManager.getInstance().addAltEntry(DCTerms.getURI(), "file:///" + context.getRealPath("/WEB-INF/owl/dcterms.rdf"));
	    OntDocumentManager.getInstance().addAltEntry(FOAF.getURI(), "file:///" + context.getRealPath("/WEB-INF/owl/foaf.owl"));
	    OntDocumentManager.getInstance().addAltEntry(SIOC.getURI(), "file:///" + context.getRealPath("/WEB-INF/owl/sioc.owl"));
	    OntDocumentManager.getInstance().addAltEntry(Graphity.getURI(), "file:///" + context.getRealPath("/WEB-INF/graphity.ttl"));
	    OntDocumentManager.getInstance().addAltEntry("http://dbpedia.org/ontology/", "file:///" + context.getRealPath("/WEB-INF/owl/dbpedia-owl.owl"));

	    OntDocumentManager.getInstance().addModel(RDF.getURI(), ModelFactory.createOntologyModel().read(RDF.getURI()));
	    OntDocumentManager.getInstance().addModel(RDFS.getURI(), ModelFactory.createOntologyModel().read(RDFS.getURI()));
	    OntDocumentManager.getInstance().addModel(OWL2.getURI(), ModelFactory.createOntologyModel().read(OWL2.getURI()));
	    OntDocumentManager.getInstance().addModel(DC.getURI(), ModelFactory.createOntologyModel().read(DC.getURI()));
	    OntDocumentManager.getInstance().addModel(DCTerms.getURI(), ModelFactory.createOntologyModel().read(DCTerms.getURI()));
	    OntDocumentManager.getInstance().addModel(FOAF.getURI(), ModelFactory.createOntologyModel().read(FOAF.getURI()));
	    OntDocumentManager.getInstance().addModel(SIOC.getURI(), ModelFactory.createOntologyModel().read(SIOC.getURI()));
	    OntDocumentManager.getInstance().addModel(Graphity.getURI(), ModelFactory.createOntologyModel().read(Graphity.getURI(), FileUtils.langTurtle));
	    OntDocumentManager.getInstance().addModel("http://dbpedia.org/ontology/", ModelFactory.createOntologyModel().read("http://dbpedia.org/ontology/"));
	} catch (Exception ex)
	{
	    log.warn("Could not load ontology", ex);
	}

    }
    
    @Override
    public Set<Class<?>> getClasses()
    {
        classes.add(Resource.class);
        classes.add(FrontPageResource.class);
        classes.add(OAuthResource.class);
	
	//classes.add(ModelProvider.class);
	//classes.add(ResourceXSLTWriter.class);

        return classes;
    }

    @Override
    public Set<Object> getSingletons()
    {
	//singletons.add(new OAuthResource());
	singletons.add(new RDFResourceXSLTWriter());
	singletons.add(new ModelProvider());

	return singletons;
    }
}
