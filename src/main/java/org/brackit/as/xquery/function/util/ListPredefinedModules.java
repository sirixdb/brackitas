/*
 * [New BSD License]
 * Copyright (c) 2011, Brackit Project Team <info@brackit.org>  
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of the <organization> nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.brackit.as.xquery.function.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.brackit.as.annotation.FunctionAnnotation;
import org.brackit.as.annotation.ModuleAnnotation;
import org.brackit.xquery.QueryContext;
import org.brackit.xquery.QueryException;
import org.brackit.xquery.XQuery;
import org.brackit.xquery.atomic.QNm;
import org.brackit.xquery.function.AbstractFunction;
import org.brackit.xquery.module.Functions;
import org.brackit.xquery.module.StaticContext;
import org.brackit.xquery.xdm.Function;
import org.brackit.xquery.xdm.Sequence;
import org.brackit.xquery.xdm.Signature;

/**
 * @author Roxana Zapata
 * 
 */
@FunctionAnnotation(description = "List predefined modules.", parameters = "")
public class ListPredefinedModules extends AbstractFunction {

	public ListPredefinedModules(QNm name, Signature signature) {
		super(name, signature, true);
	}

	public Sequence execute(StaticContext sctx, QueryContext ctx,
			Sequence[] args) throws QueryException {

		Set<Entry<String, Module>> results = getPredefinedModules().entrySet();

		if (results.size() > 0) {
			String result = "<Modules>";

			for (Iterator iterator = results.iterator(); iterator.hasNext();) {
				Entry<String, Module> module = (Entry<String, Module>) iterator
						.next();

				result += "<Module>";
				result += "<Name>" + module.getValue().name + "</Name>";
				result += "<Description>" + module.getValue().description
						+ "</Description>";
				result += "<NsURI>" + module.getValue().nsURI + "</NsURI>";
				result += "</Module>";
			}

			result += "</Modules>";

			XQuery xquery = new XQuery(result);

			return xquery.execute(ctx);
		}
		XQuery xquery = new XQuery("<Modules> no modules found! </Modules>");

		return xquery.execute(ctx);

	}

	private Map<String, Module> getPredefinedModules() {

		Module module;
		Map<String, Module> result = new HashMap<String, Module>();
		
		Iterator<Function[]> i = Functions.getPredefinedFunctions().values().iterator();

		while (i.hasNext()) {
			Function[] f = i.next();
			String description;

			for (int j = 0; j < f.length; j++) {

				ModuleAnnotation annotation = f[j].getClass().getAnnotation(
						ModuleAnnotation.class);

				if (annotation != null) {
					description = annotation.description();
				} else {
					description = "No description present";
				}

				module = new Module(f[j].getName().getPrefix(), f[j].getName()
						.getNamespaceURI(), description);
				if (!result.containsKey(module.getName())) {
					result.put(module.getName(), module);
				} else {
					if (result.get(module.name).description
							.equalsIgnoreCase("No description present")
							&& !module.description.equalsIgnoreCase("No description present")) {
						result.put(module.getName(), module);
					}

				}
			}
		}
		return result;
	}

	private static class Module {

		private final String name;
		private final String nsURI;
		private final String description;

		private Module(String name, String nsURI, String description) {
			this.name = name;
			this.nsURI = nsURI;
			this.description = description;
		}

		public String getName() {
			return this.name;
		}

		public String getNsURI() {
			return this.nsURI;
		}

		@Override
		public boolean equals(Object module) {
			if (module instanceof Module) {
				if (((Module) module).getName().equals(this.getName())
						&& ((Module) module).getNsURI().equals(this.getNsURI())) {
					return true;
				}
			}

			return false;
		};

	}

}
