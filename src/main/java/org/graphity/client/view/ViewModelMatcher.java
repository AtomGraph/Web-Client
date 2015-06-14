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
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.core.MediaType;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.header.MediaTypes;
import com.sun.jersey.core.reflection.AnnotatedMethod;
import com.sun.jersey.core.reflection.MethodList;

/**
 *
 * @author Martynas Jusevičius <martynas@graphity.org>
 */
public class ViewModelMatcher
{

	/**
	 * @param viewModel
	 * @param class1
	 * @param cr
	 * @return
	 */
	public static <T extends Annotation> AnnotatedMethod findMethod(
			ViewModel viewModel, String httpMethod, ClientResponse cr) {

		MethodList methodList = viewModel.getMethodsForHttpMethod(httpMethod);
		for (AnnotatedMethod method : methodList) {
			System.out.println("Checked Method: "
					+ method.getMethod().getName());

			//if (!isStatusMatch(method, cr.getStatus())) {
			//	continue;
			//}

			// TODO: ordering issues! This solution here picks the first method
			// that matches, not necessarily the best match
                        MediaType contentType = cr.getType();
                        if (contentType != null) {
                            if (!isMediaTypeMatch(MediaTypes.createMediaTypes(method
                                            .getAnnotation(Consumes.class).value()), contentType)) {
                                    continue;
                            }
                        }

			return method;
		}

		return null;
	}

        /*
	private static boolean isStatusMatch(AnnotatedMethod method, int status) {

		if (!method.isAnnotationPresent(Status.class)) {
			return true;
		}
		Status s = method.getAnnotation(Status.class);
		for (int value : s.value()) {
			if (value == status) {
				return true;
			}
		}
		return false;
	}
        */
        
	private static boolean isMediaTypeMatch(List<MediaType> consumes,
			MediaType contentType) {
		for (MediaType mt : consumes) {
			if (mt.isCompatible(contentType)) {
				return true;
			}
		}
		return false;

	}

}
