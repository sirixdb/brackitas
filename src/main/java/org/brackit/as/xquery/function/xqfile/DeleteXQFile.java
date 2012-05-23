/*
 * [New BSD License]
 * Copyright (c) 2011-2012, Brackit Project Team <info@brackit.org>  
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Brackit Project Team nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.brackit.as.xquery.function.xqfile;

import java.io.File;

import javax.servlet.ServletContext;

import org.brackit.as.context.BaseAppContext;
import org.brackit.as.http.HttpConnector;
import org.brackit.as.xquery.ASQueryContext;
import org.brackit.xquery.QueryContext;
import org.brackit.xquery.QueryException;
import org.brackit.xquery.atomic.Atomic;
import org.brackit.xquery.atomic.Bool;
import org.brackit.xquery.atomic.QNm;
import org.brackit.xquery.function.AbstractFunction;
import org.brackit.xquery.module.StaticContext;
import org.brackit.xquery.util.annotation.FunctionAnnotation;
import org.brackit.xquery.xdm.Sequence;
import org.brackit.xquery.xdm.Signature;
import org.brackit.xquery.xdm.type.AtomicType;
import org.brackit.xquery.xdm.type.Cardinality;
import org.brackit.xquery.xdm.type.SequenceType;

/**
 * 
 * @author Henrique Valer
 * 
 */
@FunctionAnnotation(description = "Deletes the specified XQuery file under "
		+ "the file path name ($filePathName). The file path name starts from "
		+ "the applications directory, by default: src/main/resources/apps.", parameters = "$filePathName")
public class DeleteXQFile extends AbstractFunction {

	public static final QNm DEFAULT_NAME = new QNm(XqfileFun.XQFILE_NSURI,
			XqfileFun.XQFILE_PREFIX, "delete");

	public DeleteXQFile() {
		this(DEFAULT_NAME);
	}

	public DeleteXQFile(QNm name) {
		super(name, new Signature(new SequenceType(AtomicType.BOOL,
				Cardinality.One), new SequenceType(AtomicType.STR,
				Cardinality.One)), true);
	}

	public DeleteXQFile(QNm name, Signature signature) {
		super(name, signature, true);
	}

	@Override
	public Sequence execute(StaticContext sctx, QueryContext ctx,
			Sequence[] args) throws QueryException {
		try {
			String fPathName = ((Atomic) args[0]).atomize().stringValue()
					.trim();
			fPathName = (fPathName.startsWith("/")) ? fPathName.substring(1)
					: fPathName;
			String app = fPathName.split("/")[0];
			String base = String.format("%s/%s", HttpConnector.APPS_PATH,
					fPathName);
			ServletContext servletCtx;
			try {
				servletCtx = ((ASQueryContext) ctx).getReq()
						.getServletContext();
				((BaseAppContext) servletCtx.getAttribute(app))
						.unregister(fPathName);
			} catch (NullPointerException e) {
			}
			return new Bool(new File(HttpConnector.class.getClassLoader()
					.getResource(base).toURI()).delete());
		} catch (Exception e) {
			throw new QueryException(e, XqfileFun.XQFILE_DELETE_INT_ERROR, e
					.getMessage());
		}
	}
}