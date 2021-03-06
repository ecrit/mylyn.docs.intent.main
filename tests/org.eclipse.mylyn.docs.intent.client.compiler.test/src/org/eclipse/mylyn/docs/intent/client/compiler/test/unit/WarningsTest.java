/*******************************************************************************
 * Copyright (c) 2010, 2011 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.docs.intent.client.compiler.test.unit;

import org.eclipse.mylyn.docs.intent.client.compiler.test.util.AbstractIntentCompilerTest;
import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatusSeverity;
import org.junit.Test;

/**
 * Tests the correct behavior of Intent compiler warnings.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public class WarningsTest extends AbstractIntentCompilerTest {
	/**
	 * Checks that the compiler is able to validate the generated resources and raise warnings if validation
	 * issue occurs.
	 */
	@Test
	public void testMissingURI() {
		compile("dataTests/warnings/missingURI.intent");
		checkCompilationStatus(CompilationStatusSeverity.WARNING,
				"-The namespace URI 'null' is not well formed\n");
	}

	/**
	 * Checks that the compiler is able to validate the generated resources and raise warnings if validation
	 * issue occurs.
	 */
	@Test
	public void testMissingPrefix() {
		compile("dataTests/warnings/missingPrefix.intent");
		checkCompilationStatus(CompilationStatusSeverity.WARNING,
				"-The namespace prefix 'null' is not well formed\n");
	}
}
