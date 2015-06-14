/*
 * Copyright 2015 Martynas Jusevičius <martynas@graphity.org>.
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

package org.graphity.client.provider;

import org.graphity.client.view.ViewModelMatcher;
import org.graphity.client.view.ViewModel;
import org.graphity.client.view.ViewModeller;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.async.AsyncClientHandler;
import com.sun.jersey.client.proxy.ViewProxy;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import java.util.concurrent.Future;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class ViewProxyProvider implements com.sun.jersey.client.proxy.ViewProxyProvider
{

    @Override
    public <T> ViewProxy<T> proxy(Client client, Class<T> c)
    {
        return new ViewProxy<T>()
        {

            @Override
            public T view(Class<T> type, ClientRequest request, ClientHandler handler)
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.                
            }

            @Override
            public T view(T v, ClientRequest request, ClientHandler handler)
            {
                ViewModel viewModel = ViewModeller.createViewModel(v.getClass());

                processRequest(viewModel, request);

                ClientResponse response = handler.handle(request);

                return processResponse(viewModel, v, request, response);
            }

            @Override
            public Future<T> asyncView(Class<T> type, ClientRequest request, AsyncClientHandler handler)
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Future<T> asyncView(T v, ClientRequest request, AsyncClientHandler handler)
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public T view(Class<T> type, ClientResponse response)
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public T view(T v, ClientResponse cr)
            {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
            
            protected void processRequest(ViewModel viewModel, ClientRequest request)
            {
                String[] consumes = viewModel.getConsumesFor(request.getMethod());
                // overwriting any previous accept() call
                if (consumes.length > 0) {
                    request.getHeaders().remove("Accept");
                    for (String consume : consumes) {
                        request.getHeaders().add("Accept", consume);

                    }
                }
            }
            
            private T processResponse(ViewModel viewModel, T v, ClientRequest request, ClientResponse response)
            {
                AnnotatedMethod m = ViewModelMatcher.findMethod(viewModel, request.getMethod(),
                        response);
                /*
                if (m == null) {
                    if (response.getClientResponseStatus().getFamily() == Response.Status.Family.CLIENT_ERROR) {
                        throw new ClientErrorException(response);
                    } else if (response.getClientResponseStatus().getFamily() == Response.Status.Family.SERVER_ERROR) {
                        throw new ServerErrorException(response);
                    } else {
                        throw new ResponseNotHandledByViewException(response);
                    }
                }

                // During this call, a fag should be set that indicates
                // whether response or output stream has been injected.
                // If not injected, we can close the response in the
                // finally.
                Object[] args = ArgumentInjector.makeArgs(client, m, response,
                        request.getMethod().equals("GET") ? request.getURI() : null);

                invoke(m.getMethod(), v, args);
                return v;
                */
                
                return null;
            }
            
        };
    }
    
}