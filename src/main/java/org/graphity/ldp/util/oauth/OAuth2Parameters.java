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

package org.graphity.ldp.util.oauth;

import java.util.HashMap;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
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
