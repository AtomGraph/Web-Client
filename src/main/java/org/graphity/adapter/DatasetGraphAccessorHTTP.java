package org.graphity.adapter;

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 * [See end of file]
 */

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.util.graph.GraphFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.*;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.openjena.atlas.io.IO;
import org.openjena.atlas.lib.Sink;
import org.openjena.atlas.logging.Log;
import org.openjena.fuseki.*;
import org.openjena.fuseki.conneg.TypedInputStream;
import org.openjena.fuseki.http.DatasetGraphAccessor;
import org.openjena.fuseki.http.HttpSC;
import org.openjena.fuseki.migrate.UnmodifiableGraph;
import org.openjena.riot.Lang;
import org.openjena.riot.RiotReader;
import org.openjena.riot.WebContent;
import org.openjena.riot.lang.LangRDFXML;
import org.openjena.riot.lang.LangRIOT;
import org.openjena.riot.lang.SinkTriplesToGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatasetGraphAccessorHTTP implements DatasetGraphAccessor
{
    private static final Logger log = LoggerFactory.getLogger(DatasetGraphAccessorHTTP.class);

    private final String remote ;
    // Library
    static final String paramGraph = "graph" ; 
    static final String paramDefault = "default" ; 

    private String user = null;
    private char[] password = null;
    private String apiKey = null;

    /** Create a DatasetUpdater for the remote URL */
    public DatasetGraphAccessorHTTP(String remote)
    {
        this.remote = remote ;
    }

    public DatasetGraphAccessorHTTP(String remote, String user, char[] password)
    {
        this(remote);
	this.user = user;
	this.password = password;
    }

    public DatasetGraphAccessorHTTP(String remote, String apiKey)
    {
        this(remote);
	this.apiKey = apiKey;
    }

    @Override
    public Graph httpGet()                            { return doGet(targetDefault()) ; }

    @Override
    public Graph httpGet(Node graphName)              { return doGet(target(graphName)) ; }
    
    private Graph doGet(String url)
    {
        HttpUriRequest httpGet = new HttpGet(url) ;
        try {
            return exec(url, null, httpGet, true) ;
        } catch (FusekiNotFoundException ex)
        {
            return null ;  
        }
    }
    
    @Override
    public boolean httpHead()
    {
        return doHead(targetDefault()) ;
    }
    
    @Override
    public boolean httpHead(Node graphName)
    {
        return doHead(target(graphName)) ;
    }

    private boolean doHead(String url)
    {
        HttpUriRequest httpHead = new HttpHead(url) ;
        try {
            exec(url, null, httpHead, false) ;
            return true ;
        } catch (FusekiRequestException ex)
        {
            if ( ex.getStatusCode() == HttpSC.NOT_FOUND_404 )
                return false ;
            throw ex ;
        }
    }

    @Override
    public void httpPut(Graph data)                   { doPut(targetDefault(), data) ; }

    @Override
    public void httpPut(Node graphName, Graph data)   { doPut(target(graphName), data) ; }

    private void doPut(String url, Graph data)
    {
        HttpUriRequest httpPut = new HttpPut(url) ;
        exec(url, data, httpPut, false) ;
    }
    
    @Override
    public void httpDelete()                          { doDelete(targetDefault()) ; }

    @Override
    public void httpDelete(Node graphName)            { doDelete(target(graphName)) ; }

    private boolean doDelete(String url)
    {
        try {
            HttpUriRequest httpDelete = new HttpDelete(url) ;
            exec(url, null, httpDelete, false) ;
            return true ;
        } catch (FusekiNotFoundException ex) { return false ; }
    }
    
    @Override
    public void httpPost(Graph data)                  { doPost(targetDefault(), data) ; }

    @Override
    public void httpPost(Node graphName, Graph data)  { doPost(target(graphName), data) ; }

    private void doPost(String url, Graph data)
    {
        HttpUriRequest httpPost = new HttpPost(url) ;
        exec(url, data, httpPost, false) ;
    }

    @Override
    public void httpPatch(Graph data)                 { throw new UnsupportedOperationException() ; }

    @Override
    public void httpPatch(Node graphName, Graph data) { throw new UnsupportedOperationException() ; }

    private String targetDefault()
    {
        return remote+"?"+paramDefault+"=" ;
    }

    private String target(Node name)
    {
        if ( ! name.isURI() )
            throw new FusekiException("Not a URI: "+name) ;
        return remote+"?"+paramGraph+"="+name.getURI() ;
    }

    static private HttpParams httpParams = createHttpParams() ;
    
    static private HttpParams createHttpParams()
    {
        HttpParams httpParams$ = new BasicHttpParams() ;
        // See DefaultHttpClient.createHttpParams
        HttpProtocolParams.setVersion(httpParams$,               HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams$,        HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(httpParams$,     true);
        HttpConnectionParams.setTcpNoDelay(httpParams$,          true);
        HttpConnectionParams.setSocketBufferSize(httpParams$,    32*1024);
        HttpProtocolParams.setUserAgent(httpParams$,             Fuseki.NAME+"/"+Fuseki.VERSION);
        return httpParams$;
    }
    
    private static String getHeader(HttpResponse response, String headerName)
    {
        Header h = response.getLastHeader(headerName) ;
        if ( h == null )
            return null ;
        return h.getValue() ;
    }

    private Graph exec(String targetStr, Graph graphToSend, HttpUriRequest httpRequest, boolean processBody)
    {
        DefaultHttpClient httpclient = new DefaultHttpClient(httpParams) ;
        
        if ( graphToSend != null )
        {
            // ??? httpRequest isa Post
            // Impedence mismatch - is there a better way?
            ByteArrayOutputStream out = new ByteArrayOutputStream() ;
            Model model = ModelFactory.createModelForGraph(graphToSend) ;
            model.write(out, WebContent.langNTriples) ;
            byte[] bytes = out.toByteArray() ;
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
            InputStreamEntity reqEntity = new InputStreamEntity(in, bytes.length) ;
            reqEntity.setContentType(WebContent.contentTypeNTriples) ;
            reqEntity.setContentEncoding(HTTP.UTF_8) ;
            HttpEntity entity = reqEntity ;
            ((HttpEntityEnclosingRequestBase)httpRequest).setEntity(entity) ;
        }
        TypedInputStream ts = null ;
        // httpclient.getParams().setXXX
        try {
	    if (getUser() != null && getPassword() != null)
	    {
		log.debug("HTTP Basic authentication with username: {}", getUser());
		UsernamePasswordCredentials creds = new UsernamePasswordCredentials(getUser(), new String(getPassword()));
		httpRequest.addHeader(new BasicScheme().authenticate(creds, httpRequest));
	    }
	    if (getApiKey() != null)
	    {
		log.debug("Authentication with API key param: {}", getApiKey());
		//httpclient.getParams().setParameter("apikey", getApiKey()); // Kasabi-specific
		httpRequest.getParams().setParameter("auth_token", getApiKey()); // Dydra-specific
	    }

            HttpResponse response = httpclient.execute(httpRequest) ;

            int responseCode = response.getStatusLine().getStatusCode() ;
            String responseMessage = response.getStatusLine().getReasonPhrase() ;
            
            if ( HttpSC.isRedirection(responseCode) )
                // Not implemented yet.
                throw FusekiRequestException.create(responseCode, responseMessage) ;

            // Other 400 and 500 - errors

            if ( HttpSC.isClientError(responseCode) || HttpSC.isServerError(responseCode) )
                throw FusekiRequestException.create(responseCode, responseMessage) ;

            if ( responseCode == HttpSC.NO_CONTENT_204) return null ;
            if ( responseCode == HttpSC.CREATED_201 ) return null ;
            
            if ( responseCode != HttpSC.OK_200 )
            {
                Log.warn(this, "Unexpected status code") ;
                throw FusekiRequestException.create(responseCode, responseMessage) ;
            }
            
            // May not have a body.
            String ct = getHeader(response, HttpNames.hContentType) ;
            if ( ct == null )
            {
                HttpEntity entity = response.getEntity() ;
                
                if (entity != null)
                {
                    InputStream instream = entity.getContent() ;
                    // Read to completion?
                    instream.close() ;
                }
                return null ;
            }
            
            // Tidy. See ConNeg / MediaType.
            String x = getHeader(response, HttpNames.hContentType) ;
            String y[] = x.split(";") ;
            String contentType = null ;
            if ( y[0] != null )
                contentType = y[0].trim();
            String charset = null ;
            if ( y.length > 1 && y[1] != null )
                charset = y[1].trim();

            // Get hold of the response entity
            HttpEntity entity = response.getEntity() ;

            if (entity != null)
            {
                InputStream instream = entity.getContent() ;
//                String mimeType = ConNeg.chooseContentType(request, rdfOffer, ConNeg.acceptRDFXML).getAcceptType() ;
//                String charset = ConNeg.chooseCharset(request, charsetOffer, ConNeg.charsetUTF8).getAcceptType() ;
                ts = new TypedInputStream(instream, contentType, charset) ;
            }
            Graph graph = GraphFactory.createGraphMem() ;
            if ( processBody )
                readGraph(graph, ts, null) ;
            if ( ts != null )
                ts.close() ;
            Graph graph2 = new UnmodifiableGraph(graph) ;
            return graph2 ;
        } catch (IOException ex)
        {
            httpRequest.abort() ;
            return null ;
        }
	catch (AuthenticationException ex)
        {
	    log.warn("Not authenticated", ex);

            httpRequest.abort() ;
            return null ;
        }
	finally
	{
            httpclient.getConnectionManager().shutdown();
	}
    }

    private void readGraph(Graph graph, TypedInputStream ts, String base)
    {
        // DRY - code in SPARQL_REST.parseBody
        
        // Yes - we ignore the charset.
        // Either it's XML and so the XML parser deals with it, or the 
        // language determines the charset and the parsers offer InputStreams.   
       
        Lang lang = FusekiLib.langFromContentType(ts.getMediaType()) ;
        if ( lang == null )
            throw new FusekiException("Unknown lang for "+ts.getMediaType()) ;
        Sink<Triple> sink = new SinkTriplesToGraph(graph) ;
        LangRIOT parser ;
        
        if ( lang.equals(Lang.RDFXML) )
            parser = LangRDFXML.create(ts, base, base, null, sink) ;
        else
            parser = RiotReader.createParserTriples(ts, lang, base, sink) ;
        parser.parse() ;
        IO.close(ts) ;
    }
    
    public char[] getPassword()
    {
	return password;
    }

    public String getUser()
    {
	return user;
    }

    public String getApiKey()
    {
	return apiKey;
    }

}

/*
 * (c) Copyright 2010 Epimorphics Ltd.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */