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

package org.graphity.client.view;

import java.lang.annotation.Annotation;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;

import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;
import com.sun.jersey.core.reflection.MethodList.Filter;
import java.util.Arrays;
import javax.ws.rs.HttpMethod;

/**
 * 
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class ViewModel
{    

    Class<?> type;

    public ViewModel(Class<?> type)
    {
        this.type = type;
    }

    public String[] getConsumesFor(String method)
    {
        List<String> consumes = new LinkedList<>();
        MethodList methodList = new MethodList(this.type);

        for (AnnotatedMethod m : methodList.hasAnnotation(Consumes.class)) {
            String[] c = m.getAnnotation(Consumes.class).value();
            consumes.addAll(Arrays.asList(c));
        }

        String[] a = new String[consumes.size()];
        return consumes.toArray(a);

    }

    public MethodList getMethodsForHttpMethod(final String httpMethod)
    {
        MethodList methodList = new MethodList(this.type);
        return methodList.filter(new Filter() {
            @Override
            public boolean keep(AnnotatedMethod m) {
                for (Annotation a : m.getAnnotations()) {
                    if (a.annotationType().getAnnotation(HttpMethod.class) != null) {
                        HttpMethod hm = a.annotationType().getAnnotation(HttpMethod.class);
                        if (hm.value().equalsIgnoreCase(httpMethod)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }
    
}
