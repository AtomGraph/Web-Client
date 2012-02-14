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

package org.graphity.manager.locator;

import com.hp.hpl.jena.util.TypedStream;
import org.graphity.util.oauth.OAuth2Parameters;
import org.graphity.util.oauth.OAuth2Secrets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
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
