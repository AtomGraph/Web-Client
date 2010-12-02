/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package view;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

/**
 *
 * @author Pumba
 */
public class JSONSerializer
{

    public static String serialize(ResultSet resultSet)
    {
        OutputStream jsonout = new ByteArrayOutputStream();
	ResultSetFormatter.outputAsJSON(jsonout, resultSet);
        return jsonout.toString();
    }

    public static String serialize(ResultSetRewindable resultSet)
    {
        if (resultSet != null) resultSet.reset();
        OutputStream jsonout = new ByteArrayOutputStream();
	ResultSetFormatter.outputAsJSON(jsonout, resultSet);
        return jsonout.toString();
    }
}
