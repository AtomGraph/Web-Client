package dk.semantic_web.sem_rep.model.sdb;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.sql.DataSource;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.Logger;

/**
 *
 * @author Pumba
 */

public class ConnectionPool
{
    private static Logger logger = Logger.getLogger(ConnectionPool.class);
    private static ConnectionPool instance = null;
    private static DataSource ds = null;
    private static GenericObjectPool pool = null;

    private ConnectionPool()
    {
	try
	{
	    logger.info("Initializing connection pool");

	    //Class.forName("com.mysql.db.Driver");
	    Class.forName("com.mysql.jdbc.Driver");

	    pool = new GenericObjectPool();
	    pool.setMinIdle(5);
	    pool.setMaxActive(15);
	    pool.setTestOnBorrow(true);

	    ConnectionFactory connectionFactory =
	    new DriverManagerConnectionFactory("jdbc:mysql://localhost/reports", "reports", "semantic"); // QUIRK

	    @SuppressWarnings("all")
	    PoolableConnectionFactory poolableConnectionFactory =
	    new PoolableConnectionFactory(connectionFactory, pool, null, "SELECT 1;", false, true);

	    ds = new PoolingDataSource(pool);
	}
	catch (ClassNotFoundException e)
	{
	    logger.error("The database driver could not be found.");
	    System.exit(-1);
	}
    }

    public static ConnectionPool getInstance()
    {
	if ( instance == null )
	    synchronized (ConnectionPool.class)
	    {
		if ( instance == null ) instance = new ConnectionPool();
	    }
	return instance;
    }

    public Connection getConnection()
    {
	try
	{
	    return ds.getConnection();
	}
	catch (SQLException ex)
	{
	    java.util.logging.Logger.getLogger(ConnectionPool.class.getName()).log(Level.SEVERE, null, ex);
	}
	return null;
    }

}