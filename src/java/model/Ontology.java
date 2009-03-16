/*
 * Ontology.java
 *
 * Created on Ketvirtadienis, 2007, Kovo 29, 18.09
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package model;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import javax.servlet.ServletContext;

/**
 *
 * @author Pumba
 */
public final class Ontology
{
    private static ServletContext context = null;
    
    public static OntModel getJointOntology()
    {
	return (OntModel)context.getAttribute("jointOntology");
    }

    public static OntModel getDomainOntology()
    {
	return (OntModel)context.getAttribute("domainOntology");
    }
	
    public static OntModel getResourceOntology()
    {
	return (OntModel)context.getAttribute("resourceOntology");
    }

    public static OntModel getSystemOntology()
    {
	return (OntModel)context.getAttribute("systemOntology");
    }
	
    public static void init(ServletContext context)
    {
	
	Ontology.context = context;
	
	/*
	String M_DB_URL         = "jdbc:mysql://localhost/jena";
	String M_DB_USER        = "root";
	String M_DB_PASSWD      = "";
	String M_DB             = "MySQL";
	String M_DBDRIVER_CLASS = "com.mysql.jdbc.Driver";
	 */
	
	OntModel domainOntology = null;
	OntModel resourceOntology = null;
	OntModel jointOntology = null;
	OntModel systemOntology = null;
	
	try
	{
	    /*
	    Class.forName(M_DBDRIVER_CLASS);
	    IDBConnection conn = new DBConnection(M_DB_URL, M_DB_USER, M_DB_PASSWD, M_DB);
	    //conn.cleanDB(); // cleans database
	    ModelMaker maker = ModelFactory.createModelRDBMaker(conn);

	    Model base = maker.createModel("http://www.itu.dk/people/martynas/Thesis/whatsup.owl", false);
	    domainOntology = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM, base); //RDFS_MEM_RDFS_INF

	    base = maker.createModel("http://www.xml.lt/web-ont.owl#", false);
	    webPageOntology = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM, base); //RDFS_MEM_RDFS_INF

	    base = maker.createModel("http://www.xml.lt/system-ont.owl#", false);
	    systemOntology = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM, base); //RDFS_MEM_RDFS_INF

	     */
	    
	    domainOntology = ModelFactory.createOntologyModel();
	    InputStream in = new FileInputStream(new File(context.getRealPath("/data/whatsup.owl")));
	    domainOntology.read(in, Namespaces.DOM_ONT_NS);

	    in = new FileInputStream(new File(context.getRealPath("/data/whatsup-data.rdf")));
	    domainOntology.read(in, Namespaces.DOM_DATA_NS);
	    
	    resourceOntology = ModelFactory.createOntologyModel();
	    in = new FileInputStream(new File(context.getRealPath("/data/resources.owl")));
	    resourceOntology.read(in, Namespaces.RES_ONT_NS);

	    in = new FileInputStream(new File(context.getRealPath("/data/resource-data.rdf")));
	    resourceOntology.read(in, Namespaces.HOST_NS);

	    systemOntology = ModelFactory.createOntologyModel();
	    in = new FileInputStream(new File(context.getRealPath("/data/system.owl")));
	    systemOntology.read(in, Namespaces.SYS_ONT_NS);
	    
    	    /*
	    in = new FileInputStream(new File(context.getRealPath("/data/user-data.rdf")));
	    domainOntology.read(in, Namespaces.DOM_DATA_NS);

	    in = new FileInputStream(new File(context.getRealPath("/data/webpage-ont.rdf")));
	    webPageOntology.read(in, Namespaces.WEB_ONT_NS);

	    in = new FileInputStream(new File(context.getRealPath("/data/webpage-data.rdf")));
	    webPageOntology.read(in, Namespaces.HOST_NS);

	    in = new FileInputStream(new File(context.getRealPath("/data/system-ont.rdf")));
	    systemOntology.read(in, Namespaces.SYS_ONT_NS);

	    in = new FileInputStream(new File(context.getRealPath("/data/system-data.rdf")));
	    systemOntology.read(in, Namespaces.SYS_DATA_NS);
	    
	     */
	    
    	    in.close();
	    //conn.close();
	    
	    //domainOntology = (OntModel)maker.openModel("http://www.xml.lt/user-ont.owl#");
	    //webPageOntology = (OntModel)maker.openModel("http://www.xml.lt/web-ont.owl#");

	    //InfModel infOntology = ModelFactory.createInfModel(ReasonerRegistry.getOWLReasoner(), domainOntology);

	    jointOntology = ModelFactory.createOntologyModel(); //TODO Bauda kazkokia
	    jointOntology.add(domainOntology);
	    jointOntology.add(resourceOntology);
	    jointOntology.add(systemOntology);
	    
	    //jointOntology = ModelFactory.createUnion(domainOntology, resourceOntology);
	}
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

	context.setAttribute("domainOntology", domainOntology);
	context.setAttribute("resourceOntology", resourceOntology);
        context.setAttribute("systemOntology", systemOntology);
        context.setAttribute("jointOntology", jointOntology);

	System.out.println("COUNT :" + String.valueOf(jointOntology.size()));	
	
    }
    
}
