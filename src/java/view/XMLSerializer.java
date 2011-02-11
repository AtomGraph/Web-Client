/*
 * FormXMLSerializer.java
 *
 * Created on Še�?tadienis, 2007, Kovo 10, 15.41
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package view;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.query.ResultSetRewindable;
import java.io.StringWriter;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import model.vocabulary.Namespaces;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import dk.semantic_web.diy.controller.Error;

/**
 *
 * @author Pumba
 */
public class XMLSerializer
{

    private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder builder = null;
    private static Transformer transformer = null;

    private static void init() throws ParserConfigurationException, TransformerConfigurationException
    {
	if (builder == null) builder = factory.newDocumentBuilder();
	if (transformer == null) transformer = TransformerFactory.newInstance().newTransformer();
	
	transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes"); // easier to save XMLLiterals?
    }

    public static String serialize(Document document) throws TransformerConfigurationException, TransformerException, ParserConfigurationException
    {
	XMLSerializer.init();

	//initialize StreamResult with File object to save to file
	StreamResult result = new StreamResult(new StringWriter());
	DOMSource source = new DOMSource(document);

	transformer.transform(source, result);

	//System.out.println(result.getWriter().toString());

	return result.getWriter().toString();
    }

    public static String serialize(ResultSet resultSet)
    {
        return ResultSetFormatter.asXMLString(resultSet);
    }

    public static String serialize(ResultSetRewindable resultSet)
    {
        if (resultSet != null) resultSet.reset();
        return ResultSetFormatter.asXMLString(resultSet);
    }

    // refactor into serialize(Error error) ???
    public static String serialize(List<Error> errors) throws ParserConfigurationException, TransformerConfigurationException, TransformerException
    {
	XMLSerializer.init();

        Document document = builder.newDocument();

	Element sparql = document.createElementNS(Namespaces.SPARQL_NS, "sparql:sparql");
	document.appendChild(sparql);

	Element results = document.createElementNS(Namespaces.SPARQL_NS, "sparql:results");
	sparql.appendChild(results);

	if (errors != null)
	    for (Error error : errors)
	    {
		Element result = document.createElementNS(Namespaces.SPARQL_NS, "sparql:result");
		results.appendChild(result);

		Element nameBinding = document.createElementNS(Namespaces.SPARQL_NS, "sparql:binding");
		result.appendChild(nameBinding);
		nameBinding.setAttribute("name", "error");

		Element uri = document.createElementNS(Namespaces.SPARQL_NS, "sparql:uri");
		nameBinding.appendChild(uri);
		uri.appendChild(document.createTextNode(error.getURI()));

		if (error.getMessage() != null)
		{
		    Element msgBinding = document.createElementNS(Namespaces.SPARQL_NS, "sparql:binding");
		    result.appendChild(msgBinding);
		    msgBinding.setAttribute("name", "message");

		    Element literal = document.createElementNS(Namespaces.SPARQL_NS, "sparql:literal");
		    msgBinding.appendChild(literal);
		    literal.appendChild(document.createTextNode(error.getMessage()));
		}
	    }

	return serialize(document);
    }

}
