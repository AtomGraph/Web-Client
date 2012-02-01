/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.graphity.util.oauth;

/**
 *
 * @author Pumba
 */
public class OAuth2Secrets
{
    public static final String CLIENT_SECRET = "client_secret";
    public static final String ACESS_TOKEN = "access_token";

    private String clientSecret = null;
    
    public OAuth2Secrets clientSecret(String clientSecret)
    {
	this.clientSecret = clientSecret;
	return this;
    }
    
    public String getClientSecret()
    {
	return clientSecret;
    }
}
