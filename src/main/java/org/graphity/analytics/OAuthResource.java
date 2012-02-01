/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.analytics;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.spi.resource.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.graphity.util.oauth.OAuth2Parameters;
import org.graphity.util.oauth.OAuth2Secrets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Pumba
 */
//@Singleton
@Path("oauth")
public class OAuthResource // implements ClientFilter
{
    // http://code.google.com/apis/accounts/docs/OAuth2WebServer.html
    // http://developers.facebook.com/docs/authentication/
    
    private static final String TOKEN_ENDPOINT = "http://developers.facebook.com/docs/authentication/";
    private static final Logger log = LoggerFactory.getLogger(OAuthResource.class);
    public static final String CODE = "code";
    
    private @Context UriInfo uriInfo = null;
    private @Context @QueryParam("code") String authCode = null;
    private @Context @QueryParam("error") String error = null;
    private @Context @QueryParam("error_reason") String reason = null;

    OAuth2Parameters params = new OAuth2Parameters().clientId("121081534640971");
    OAuth2Secrets secrets = new OAuth2Secrets().clientSecret("ccfc3ca9c5dbabdda321a68a89560355"); // new OAuthSecrets().consumerSecret(CONSUMER_SECRET);


    /*
     * http://YOUR_URL?code=A_CODE_GENERATED_BY_SERVER
     * http://YOUR_URL?error_reason=user_denied&
     *	    error=access_denied&error_description=The+user+denied+your+request.
     */
    @GET
    public String getAuthCode()
    {
	if (authCode == null)
	{
	    if (error != null)
		log.warn("OAuth 2.0 authentication failed. Error: {} Reason: {}", error, reason); // throw WAE?
	}
	else
	    log.trace("Received OAuth 2.0 authentication code: {}", authCode);
	    
	return getAccessToken();
    }
    
    /*
     * https://graph.facebook.com/oauth/access_token?
     *	    client_id=YOUR_APP_ID&redirect_uri=YOUR_URL&
     *	    client_secret=YOUR_APP_SECRET&code=THE_CODE_FROM_ABOVE
     */
    public String getAccessToken()
    {    
	WebResource resource = Client.create().resource(TOKEN_ENDPOINT).
		queryParam(OAuth2Parameters.CLIENT_ID, params.getClientId()).
		queryParam(OAuth2Parameters.REDIRECT_URI, uriInfo.getAbsolutePath().toString()).
		queryParam(OAuth2Secrets.CLIENT_SECRET, secrets.getClientSecret()).
		queryParam(CODE, authCode);	
	
	String token = resource.get(String.class);
	log.trace("Received OAuth access token: {}", token);
	return token;
    }
}
