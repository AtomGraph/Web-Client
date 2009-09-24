/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model;

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
	store = SDBFactory.connectStore(context.getRealPath("sdb.ttl"));
	dataset = DatasetStore.create(store);
	//SDBFactory.c
	//SDBFactory.connectNamedModel(store, arg1);
	Model schemaModel = dataset.getNamedModel("http://temp.com/schema");

	String fileName = context.getRealPath("/owl/visualizations.owl");
	InputStream in = FileManager.get().open(fileName);
	if (in == null) throw new IllegalArgumentException("File: " + fileName + " not found");
	schemaModel.read(in, null);
	//schemaModel.write(System.out);
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
	return getDataset().getDefaultModel();
    }
}
