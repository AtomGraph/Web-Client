/*
 * QueryStringBuilder.java
 *
 * Created on Tre�iadienis, 2007, Vasario 14, 23.26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package view;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Static helper class, used to format SPARQL query strings from template files with placeholders and arguments.
 * @author Pumba
 */
public class QueryStringBuilder
{
    
    /** Formats a SPARQL query string
     @param queryFileName File path of the query template
     @param args Arguments to fill placeholders
     @return Formatted query string
     */
    public static String build(String queryFileName, Object... args) throws FileNotFoundException, IOException
    {
        return build(new FileInputStream(queryFileName), args);
    }

    public static String build(InputStream query, Object... args) throws FileNotFoundException, IOException
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(query));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = br.readLine()) != null) sb.append(line + "\n");
        br.close();
        String queryString = sb.toString();
        return String.format(queryString, args);
    }

}
