/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.atomgraph.client.filter.request;

import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

/**
 * A filter to support HTTP method replacing of a POST request to a request
 * utilizing another HTTP method for the case where proxies or HTTP
 * servers would otherwise block that HTTP method.
 * <p>
 * This filter may be used to replace a POST request with a PUT, DELETE or GET
 * request.
 * <p>
 * Replacement will occur if the request method is POST and there exists either
 * a request header "X-HTTP-Method-Override", or
 * a query parameter "_method" with a non-empty value. That value
 * will be the HTTP method that replaces the POST method. In addition to that,
 * when replacing the POST method with GET, the filter will convert the form parameters
 * to query parameters. If the filter is configured to look for both the X-HTTP-Method-Override
 * header as well as the _method query parameter (the default setting), both are present in the
 * request and they differ, the filter returns {@link Status#BAD_REQUEST} response.
 * <p>
 * When an application is deployed as a Servlet or Filter this Jersey filter can be
 * registered using the following initialization parameter:
 * <blockquote><pre>
 *     &lt;init-param&gt;
 *         &lt;param-name&gt;com.sun.jersey.spi.container.ContainerRequestFilters&lt;/param-name&gt;
 *         &lt;param-value&gt;com.sun.jersey.api.container.filter.PostReplaceFilter&lt;/param-value&gt;
 *     &lt;/init-param&gt
 * </pre></blockquote>
 * <p>
 * The filter can be configured using the com.sun.jersey.api.container.filter.PostReplaceFilterConfig property.
 * See {@link #PROPERTY_POST_REPLACE_FILTER_CONFIG} for the supported property values.
 *
 * @author Paul.Sandoz@Sun.Com
 * @author Martin Matula
 * @author Fredy Nagy
 * @author Florian Hars (florian@hars.de)
 *
 * @see com.sun.jersey.api.container.filter
 */
public class PostReplaceFilter implements ContainerRequestFilter
{
    
    public static final String METHOD_PARAM = "_method";

    /**
     * Property that may contain a comma-separated list of the configuration flags to be set on this filter. {@link ConfigFlag} enum lists the allowed config flags.
     * If this property is not set, the default value "HEADER,QUERY" will be used to initialize the filter.
     * <p>
     * When an application is deployed as a servlet or filter this property can be set using the following initialization parameter:
     * <blockquote><pre>
     *     &lt;init-param&gt;
     *         &lt;param-name&gt;com.sun.jersey.api.container.filter.PostReplaceFilterConfig&lt;/param-name&gt;
     *         &lt;param-value&gt;HEADER&lt;/param-value&gt;
     *     &lt;/init-param&gt
     * </pre></blockquote>
     * The above setting would cause the filter would only look at the X-HTTP-Method-Override header and ignore _method query parameter.
     */
    public static final String PROPERTY_POST_REPLACE_FILTER_CONFIG = "com.sun.jersey.api.container.filter.PostReplaceFilterConfig";

    private final int config;

    /**
     * Enum representing configuration flags that can be set on the filter. Each literal of this enum is a flag that can either be turned on (included in the config) or not.
     */
    public enum ConfigFlag {

        /** If added to the config, causes the filter to look for a method override in the X-HTTP-Method-Override header */
        HEADER(1),
        /** If added to the config, causes the filter to look for a method override in the _method query parameter */
        QUERY(2);
        private final int flag;

        private ConfigFlag(int flag) {
            this.flag = flag;
        }

        /**
         * Returns the numeric value of the bit corresponding to this flag.
         * @return numeric value of this flag
         */
        public int getFlag() {
            return flag;
        }

        /**
         * Returns true if the bit corresponding to this flag is set in a given integer value
         * @param config Integer value to check for the bit corresponding to this flag
         * @return true if the passed value has the bit corresponding to this flag set
         */
        public boolean isPresentIn(int config) {
            return (config & flag) == flag;
        }
    }

    /**
     * Initializes this filter with {@link #PROPERTY_POST_REPLACE_FILTER_CONFIG} property value from the application resource config.
     * If the property has no value, both {@link ConfigFlag#HEADER} and {@link ConfigFlag#QUERY} will be added to the config.
     *
     * @param rc resource config (injected by Jersey)
     */
    public PostReplaceFilter(@Context ResourceConfig rc) {
        this(configStringToConfig((String) rc.getProperty(PROPERTY_POST_REPLACE_FILTER_CONFIG)));
    }

    /**
     * Initializes this filter with config flags.
     * @param configFlags Config flags to initialize the filter with. If no config flags are passed,
     * both {@link ConfigFlag#HEADER} and {@link ConfigFlag#QUERY} will be added to the config.
     */
    public PostReplaceFilter(ConfigFlag... configFlags) {
        int c = 0;
        for (ConfigFlag cf : configFlags) {
            c |= cf.getFlag();
        }
        if (c == 0) {
            c = 3;
        }
        this.config = c;
    }

    /**
     * Converts a config string (from the init parameters) to an array of config flags.
     *
     * @param configString config string
     * @return array of ConfigFlag objects
     */
    private static ConfigFlag[] configStringToConfig(String configString) {
        if (configString == null) {
            return new ConfigFlag[0];
        }

        String[] parts = configString.toUpperCase().split(",");
        ArrayList<ConfigFlag> result = new ArrayList<>(parts.length);
        for (String part : parts) {
            part = part.trim();
            if (part.length() > 0) {
                try {
                    result.add(ConfigFlag.valueOf(part));
                } catch (IllegalArgumentException e) {
                    Logger.getLogger(PostReplaceFilter.class.getName()).log(Level.WARNING, "Invalid config flag for " + PROPERTY_POST_REPLACE_FILTER_CONFIG + " property: {0}", part.trim());
                }
            }
        }
        return result.toArray(new ConfigFlag[result.size()]);
    }

    /**
     * Returns parameter value in a normalized form (uppercase, trimmed and null if empty string) considering the config flags.
     *
     * @param configFlag Config flag to look for (if set in the config, this method returns the param value, if not set, this method returns null).
     * @param paramsMap Map to retrieve the parameter from.
     * @param paramName Name of the parameter to retrieve.
     * @return Normalized parameter value. Never returns an empty string - converts it to null.
     */
    private String getParamValue(ConfigFlag configFlag, MultivaluedMap<String, String> paramsMap, String paramName) {
        String value = configFlag.isPresentIn(config) ? paramsMap.getFirst(paramName) : null;
        if (value == null) {
            return null;
        }
        value = value.trim();
        return value.length() == 0 ? null : value.toUpperCase();
    }

    @Override
    public ContainerRequest filter(ContainerRequest request) {
        if (!request.getMethod().equalsIgnoreCase("POST")) {
            return request;
        }

        String header = getParamValue(ConfigFlag.HEADER, request.getRequestHeaders(), "X-HTTP-Method-Override");
        String query = getParamValue(ConfigFlag.QUERY, request.getQueryParameters(), METHOD_PARAM);

        String override;
        if (header == null) {
            override = query;
            URI originalRequestUri = request.getRequestUriBuilder().replaceQueryParam(METHOD_PARAM, null).build();
            request.setUris(request.getBaseUri(), originalRequestUri);
        } else {
            override = header;
            if (query != null && !query.equals(header)) {
                throw new WebApplicationException(Response.status(Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Inconsistent POST override.\nX-HTTP-Method-Override: " + header + "\n_method: " + query).build());
            }
        }
        if (override == null) {
            return request;
        }
        request.setMethod(override);
        if (override.equals("GET")) {
            if (MediaTypes.typeEquals(MediaType.APPLICATION_FORM_URLENCODED_TYPE, request.getMediaType())) {
                UriBuilder ub = request.getRequestUriBuilder().replaceQuery(request.getEntity(String.class));
                request.setUris(request.getBaseUri(), ub.build());
            }
        }
        return request;
    }
}