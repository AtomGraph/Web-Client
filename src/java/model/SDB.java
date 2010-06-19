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
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
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
        store = SDBFactory.connectStore(context.getRealPath("sdb.ttl"));
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
	
	String fileName = context.getRealPath("/owl/visualizations.owl");
	InputStream in = FileManager.get().open(fileName);
	schemaModel.read(in, Namespaces.VIS_NS);
	fileName = context.getRealPath("/owl/reports.owl");
	in = FileManager.get().open(fileName);
	schemaModel.read(in, Namespaces.REPORT_NS);
	fileName = context.getRealPath("/owl/spin.owl");
	in = FileManager.get().open(fileName);
	schemaModel.read(in, Namespaces.SPIN_NS);
	fileName = context.getRealPath("/owl/sioc.owl");
	in = FileManager.get().open(fileName);
	schemaModel.read(in, Namespaces.SIOC_NS);

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