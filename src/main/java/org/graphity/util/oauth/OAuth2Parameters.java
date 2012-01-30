/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util.oauth;

import java.net.URI;

/**
 *
 * @author Pumba
 */
public class OAuth2Parameters extends com.sun.jersey.oauth.signature.OAuthParameters
{
    public static final String CLIENT_ID = "client_id";
    public static final String REDIRECT_URI = "redirect_uri";

    public OAuth2Parameters clientId(String clientId)
    {
	put(CLIENT_ID, clientId);
	return this;
    }
    
    public OAuth2Parameters redirectURI(URI redirectUri)
    {
	put(REDIRECT_URI, redirectUri.toString());
	return this;
    }

}
