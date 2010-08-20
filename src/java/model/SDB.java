/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.store.DatasetStore;
import com.hp.hpl.jena.util.FileManager;
import java.io.InputStream;
import javax.servlet.ServletContext;

/**
 *
 * @author Pumba
 */
public class SDB
{
    private static Store store = null;
    private static Dataset dataset = null;

    public static void init(ServletContext context)
    {
	com.hp.hpl.jena.sdb.SDB.getContext().setTrue(com.hp.hpl.jena.sdb.SDB.unionDefaultGraph);
        store = SDBFactory.connectStore(context.getRealPath("/WEB-INF/sdb.ttl"));
	dataset = DatasetStore.create(store);
	Model schemaModel = SDBFactory.connectNamedModel(store, "http://temp.com/schema");
/*
Model instanceModel = SDBFactory.connectNamedModel(store, "http://temp.com/instances");
schemaModel.removeAll(); // clean model
instanceModel.removeAll();
schemaModel.add(schemaModel.createResource("http://temp.com/class"), RDF.type, OWL.Class);
instanceModel.add(instanceModel.createResource("http://temp.com/instance"), RDF.type, schemaModel.createResource("http://temp.com/class"));
System.out.println("Schema model size: " + dataset.getNamedModel("http://temp.com/schema").size());
System.out.println("Instance model size: " + dataset.getNamedModel("http://temp.com/instances").size());
System.out.println("Union model size: " + dataset.getNamedModel("urn:x-arq:UnionGraph").size());
*/
        
        schemaModel.removeAll(); // clean model
	
	schemaModel.read(context.getResourceAsStream("/WEB-INF/owl/visualizations.owl"), Namespaces.VIS_NS);
	schemaModel.read(context.getResourceAsStream("/WEB-INF/owl/reports.owl"), Namespaces.REPORT_NS);
	schemaModel.read(context.getResourceAsStream("/WEB-INF/owl/spin.owl"), Namespaces.SPIN_NS);
	schemaModel.read(context.getResourceAsStream("/WEB-INF/owl/sioc.owl"), Namespaces.SIOC_NS);

        System.out.println("Schema model size: " + dataset.getNamedModel("http://temp.com/schema").size());
        System.out.println("Instance model size: " + dataset.getNamedModel("http://temp.com/instances").size());
        System.out.println("Union model size: " + dataset.getNamedModel("urn:x-arq:UnionGraph").size());
	//if (dataset.containsNamedModel("http://temp.com/schema")) System.out.print("CONTAINS");
	//while (dataset.listNames().hasNext())
	    //System.out.print(dataset.listNames());
    }
    
    public static Store getStore()
    {
	return store;
    }

    public static Dataset getDataset()
    {
	return dataset;
    }
    
    public static Model getDefaultModel()
    {
	//return getDataset().getDefaultModel();
        return getDataset().getNamedModel("urn:x-arq:UnionGraph");
    }
    
    public static Model getInstanceModel()
    {
	return getDataset().getNamedModel("http://temp.com/instances");
    }

    public static Model getSchemaModel()
    {
	return getDataset().getNamedModel("http://temp.com/schema");
    }

    public static OntClass getReportClass()
    {

        return (OntClass)getDefaultModel().createResource(Namespaces.REPORT_NS + "Report").as(OntClass.class);
    }
}