/**
 *  Copyright 2012 Martynas Jusevičius <martynas@graphity.org>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.graphity.util.locator;

import com.hp.hpl.jena.util.TypedStream;
import org.graphity.ldp.util.oauth.OAuth2Parameters;
import org.graphity.ldp.util.oauth.OAuth2Secrets;
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
