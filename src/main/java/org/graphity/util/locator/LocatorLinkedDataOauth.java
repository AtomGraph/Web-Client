/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util.locator;

import com.hp.hpl.jena.util.TypedStream;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.oauth.client.OAuthClientFilter;
import com.sun.jersey.oauth.signature.OAuthParameters;
import com.sun.jersey.oauth.signature.OAuthSecrets;
import org.graphity.util.LocatorLinkedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pumba
 */
public class LocatorLinkedDataOauth extends LocatorLinkedData
{
    private static Logger log = LoggerFactory.getLogger(LocatorLinkedDataOauth.class) ;
    
    // http://developers.gigya.com/020_Developer_Guide/85_REST/OAuth2
    // http://jersey.java.net/nonav/apidocs/1.10/contribs/jersey-oauth/oauth-signature/com/sun/jersey/oauth/signature/OAuthParameters.html
    // http://jersey.java.net/nonav/apidocs/latest/contribs/jersey-oauth/oauth-client/com/sun/jersey/oauth/client/OAuthClientFilter.html

    private OAuthParameters params = null;
    private OAuthSecrets secrets = null; // new OAuthSecrets().consumerSecret(CONSUMER_SECRET);
    private String accessToken = null;

    public LocatorLinkedDataOauth(OAuthParameters params)
    {
	super();
	this.params = params;

//params.readRequest(null);
//OAuthRequest req = new OAuthRequest();
//OAuthSignatureMethod
    }

    @Override
    public TypedStream open(String filenameOrURI)
    {
	/*
	if (apiKey != null)
	{
	    UriBuilder.fromUri(filenameOrURI).queryParam("", values)
	}
	 */
		
	return super.open(filenameOrURI);
    }

    public String getAccessToken()
    {
	//WebClient client = 
	Client client = Client.create();
	WebResource resource = client.resource("https://www.facebook.com/dialog/oauth");
	OAuthClientFilter filter = new OAuthClientFilter(client.getProviders(), params, secrets); // secrets
	client.addFilter(filter);
	String token = resource.get(String.class);
	log.trace("Received OAuth access token: {}", token);
	return token;
    }

}
