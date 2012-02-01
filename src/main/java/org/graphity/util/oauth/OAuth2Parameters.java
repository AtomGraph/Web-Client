/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util.oauth;

import java.util.HashMap;

/**
 *
 * @author Pumba
 */
public class OAuth2Parameters extends HashMap<String, String>
{
    public static final String CLIENT_ID = "client_id";
    public static final String REDIRECT_URI = "redirect_uri";

    public OAuth2Parameters clientId(String clientId)
    {
	put(CLIENT_ID, clientId);
	return this;
    }
 
    public String getClientId()
    {
	return get(CLIENT_ID);
    }
    
    public OAuth2Parameters redirectURI(String redirectUri)
    {
	put(REDIRECT_URI, redirectUri);
	return this;
    }

    public String redirectURI()
    {
	return get(REDIRECT_URI);
    }
}
