/*
 * Copyright (C) 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.graphity.analytics;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
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
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
//@Singleton
@Path("oauth")
public class OAuthResource // implements ClientFilter
{
    // http://code.google.com/apis/accounts/docs/OAuth2WebServer.html
    // http://developers.facebook.com/docs/authentication/
    
    private static final String TOKEN_ENDPOINT = "https://graph.facebook.com/oauth/access_token";
    private static final Logger log = LoggerFactory.getLogger(OAuthResource.class);
    public static final String CODE = "code";
    //public static final String ERROR = "code";
    
    private @Context UriInfo uriInfo = null;
    private @Context @QueryParam(CODE) String authCode = null;
    private @Context @QueryParam("error") String error = null;
    private @Context @QueryParam("error_reason") String reason = null;
    //private String accessToken = null;
    //private String expires = null;
    

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
	// http://blogs.oracle.com/enterprisetechtips/entry/consuming_restful_web_services_with
	WebResource resource = Client.create().resource(TOKEN_ENDPOINT).
		queryParam(OAuth2Parameters.CLIENT_ID, params.getClientId()).
		queryParam(OAuth2Parameters.REDIRECT_URI, uriInfo.getAbsolutePath().toString()).
		queryParam(OAuth2Secrets.CLIENT_SECRET, secrets.getClientSecret()).
		queryParam(CODE, authCode);

	/*
	MultivaluedMap<String, String> queryParams = resource.accept(MediaType.APPLICATION_FORM_URLENCODED).
	    get(new GenericType<MultivaluedMap<String, String>>() {});
	
	//MultivaluedMap queryParams = new MultivaluedMapImpl();
	//MultivaluedMap<String, String> queryParams = resource.get(new GenericType<MultivaluedMap<String, String>>() {});
	//ClientResponse response = resource.get(ClientResponse.class);
	//String accessToken = queryParams.getFirst("access_token");
	//String expires = queryParams.getFirst("expires");
	*/
	
	// any clever way to parse this as MediaType.APPLICATION_FORM_URLENCODED ??
	String[] parts = resource.get(String.class).split("&");
	String accessToken = parts[0].split("=")[1]; // value after access_token=
	String expires = parts[1].split("=")[1]; // value after expires=

	log.trace("Received OAuth access token: {} Expires: {}", accessToken, expires);
	return accessToken;
    }
}
