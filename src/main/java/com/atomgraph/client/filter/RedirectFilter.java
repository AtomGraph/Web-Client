/*
 * Copyright 2018 Martynas Jusevičius <martynas@atomgraph.com>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.atomgraph.client.filter;

import java.io.IOException;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.core.Response;

/**
 *
 * @author Martynas Jusevičius {@literal <martynas@atomgraph.com>}
 */
public class RedirectFilter implements ClientResponseFilter
{

//    public Response filter(ClientResponseContext cr)
//    {
//        //ClientHandler ch = getNext();
//        //Response resp = ch.handle(cr);
//
//        if (cr.getStatusInfo().getFamily() != Response.Status.Family.REDIRECTION) return resp;
//
//        String target = resp.getHeaders().getFirst(HttpHeaders.LOCATION);
//        cr.setURI(UriBuilder.fromUri(target).build());
//        return ch.handle(cr);
//    }

    @Override
    public void filter(ClientRequestContext req, ClientResponseContext resp) throws IOException
    {
        if (resp.getStatusInfo().getFamily() == Response.Status.Family.REDIRECTION)
        {
            //resp.getHeaders().add(k, v);
        }
    }

}
