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

package org.graphity.util;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class ModelUtils
{

    public static long hashModel( Model m ) {
    	long result = 0;
    	ExtendedIterator<Triple> it = m.getGraph().find(Node.ANY, Node.ANY, Node.ANY);
		while (it.hasNext()) result ^= hashTriple( it.next() );
    	return result;
	}

	public static long hashTriple(Triple t) {
		long result = 0;
		Node S = t.getSubject(), P = t.getPredicate(), O = t.getObject();
		if (!S.isBlank()) result = (long) S.hashCode() << 32;
		if (!P.isBlank()) result ^= (long) P.hashCode() << 16;
		if (!O.isBlank()) result ^= (long) O.hashCode();
		return result;
	}

}