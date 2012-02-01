/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util.locator;

import com.hp.hpl.jena.util.TypedStream;
import org.graphity.util.LocatorLinkedData;
import org.graphity.util.oauth.OAuth2Parameters;
import org.graphity.util.oauth.OAuth2Secrets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pumba
 */
public class LocatorLinkedDataOAuth2 extends LocatorLinkedData
{
    private static Logger log = LoggerFactory.getLogger(LocatorLinkedDataOAuth2.class) ;
    
    // http://developers.gigya.com/020_Developer_Guide/85_REST/OAuth2
    // http://jersey.java.net/nonav/apidocs/1.10/contribs/jersey-oauth/oauth-signature/com/sun/jersey/oauth/signature/OAuthParameters.html
    // http://jersey.java.net/nonav/apidocs/latest/contribs/jersey-oauth/oauth-client/com/sun/jersey/oauth/client/OAuthClientFilter.html

    private OAuth2Parameters params = null;
    private OAuth2Secrets secrets = null; // new OAuthSecrets().consumerSecret(CONSUMER_SECRET);
    private String accessToken = null;

    public LocatorLinkedDataOAuth2(OAuth2Parameters params)
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

}
