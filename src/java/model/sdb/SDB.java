/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package model.sdb;

import model.*;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sdb.SDBFactory;
import com.hp.hpl.jena.sdb.Store;
import com.hp.hpl.jena.sdb.StoreDesc;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.store.DatasetStore;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author Pumba
 */
public class SDB
{
    private static Store store = null;
    private static Dataset dataset = null;
    private static StoreDesc storeDesc = new StoreDesc("layout2", "MySQL");

    public static final String ONTOLOGY_MODEL_NAME = "http://temp.com/schema"; // QUIRK
    public static final String INSTANCE_MODEL_NAME = "http://temp.com/instances"; // QUIRK
    public static final String UNION_MODEL_NAME = "urn:x-arq:UnionGraph";

    public static void init(ServletContext context)
    {
	com.hp.hpl.jena.sdb.SDB.getContext().setTrue(com.hp.hpl.jena.sdb.SDB.unionDefaultGraph);
	//String storeFilename = "file:///" + context.getRealPath("/WEB-INF/sdb.ttl"); //context.getResource("/WEB-INF/sdb.ttl").toExternalForm(); //context.getRealPath("/WEB-INF/sdb.ttl");
	//storeDesc = StoreDesc.read(storeFilename);
	//Model schemaModel = SDBFactory.connectNamedModel(store, "http://temp.com/schema");
	        
        getSchemaModel().removeAll(); // clean model
	getSchemaModel().read(context.getResourceAsStream("/WEB-INF/owl/visualizations.owl"), Namespaces.VIS_NS);
	getSchemaModel().read(context.getResourceAsStream("/WEB-INF/owl/reports.owl"), Namespaces.REPORT_NS);
	getSchemaModel().read(context.getResourceAsStream("/WEB-INF/owl/spin.owl"), Namespaces.SPIN_NS);
	getSchemaModel().read(context.getResourceAsStream("/WEB-INF/owl/sioc.owl"), Namespaces.SIOC_NS);

        System.out.println("Schema model size: " + getSchemaModel().size());
        System.out.println("Instance model size: " + getInstanceModel().size());
        System.out.println("Union model size: " + getDefaultModel().size());
	//if (dataset.containsNamedModel("http://temp.com/schema")) System.out.print("CONTAINS");
	//while (dataset.listNames().hasNext())
	    //System.out.print(dataset.listNames());

	try
	{
	    closeStore();
	}
	catch (SQLException ex)
	{
	    Logger.getLogger(SDB.class.getName()).log(Level.SEVERE, null, ex);
	}
    }
    
    public static Store getStore() throws SQLException
    {
	if (store == null)
	{
	    //BasicDataSource dataSource = getDataSource("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/odt2epub", "odt2epub", "Marchius", 8);
	    //SDBConnection conn = SDBFactory.createConnection(dataSource.getConnection());
	    SDBConnection conn = SDBFactory.createConnection(ConnectionPool.getInstance().getConnection());
	    store = SDBFactory.connectStore(conn, storeDesc);
	}
	return store;
    }

    public static void closeStore() throws SQLException
    {
	if (store != null)
	{
	    store.getConnection().close();
	    if (!store.isClosed()) store.close();
	}
	store = null;
	dataset = null;
    }

    public static Dataset getDataset()
    {
	if (dataset == null)
	try
	{
	    dataset = DatasetStore.create(getStore());
	} catch (SQLException ex)
	{
	    Logger.getLogger(SDB.class.getName()).log(Level.SEVERE, null, ex);
	}
	return dataset;
    }
    
    public static Model getDefaultModel()
    {
	//return getDataset().getDefaultModel();
        return getDataset().getNamedModel(UNION_MODEL_NAME);
    }
    
    public static Model getInstanceModel()
    {
	return getDataset().getNamedModel(INSTANCE_MODEL_NAME);
    }

    public static Model getSchemaModel()
    {
	return getDataset().getNamedModel(ONTOLOGY_MODEL_NAME);
    }

    public static OntClass getReportClass()
    {

        return (OntClass)getDefaultModel().createResource(Namespaces.REPORT_NS + "Report").as(OntClass.class);
    }
    
    private static BasicDataSource getDataSource(String driver, String url, String username, String password, Integer maxConnections)
    {
	BasicDataSource dataSource = new BasicDataSource();
	dataSource.setDriverClassName(driver);
	dataSource.setUrl(url);
	dataSource.setUsername(username);
	dataSource.setValidationQuery("SELECT 1 AS test");
	dataSource.setTestOnBorrow(true);
	dataSource.setTestOnReturn(true);
	dataSource.setMaxActive(maxConnections);
	dataSource.setMaxIdle(maxConnections);
	if (password != null) dataSource.setPassword(password);
	return dataSource;
    }

}