/*******************************************************************************
 * Copyright (c) 2011 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.docs.intent.client.ui.test.unit.demo.compilation;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditor;
import org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditorDocument;
import org.eclipse.mylyn.docs.intent.client.ui.editor.annotation.IntentAnnotationMessageType;
import org.eclipse.mylyn.docs.intent.client.ui.test.unit.demo.AbstractDemoTest;

/**
 * Tests the Intent demo, part 2: compilation behavior.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public class CompileTest extends AbstractDemoTest {

	private static final String ERROR_TEXT_PATTERN = "nsPrefixx = \"match\";";

	private static final String NO_ERROR_TEXT_PATTERN = "nsPrefix = \"match\";";

	private static final String COMPILATION_ERROR_MESSAGE = "The feature nsPrefixx doesn't exists in EPackage";

	private static final String INFOS_TEXT_PATTERN = "nsURI = \"\";";

	private static final String NO_INFOS_TEXT_PATTERN = "nsURI = \"http://www.eclipse.org/emf/compare/match/1.1\";";

	private static final String COMPILATION_INFO_MESSAGE = "-The namespace URI '' is not well formed";

	private IntentEditor editor;

	private IntentEditorDocument document;

	/**
	 * Ensures that compilation errors are detected and can be fixed.
	 */
	public void testCompilationErrors() {
		// Step 1 : opening an editor on the document
		editor = openIntentEditor(getIntentDocument().getChapters().get(3).getSubSections().get(0));
		document = (IntentEditorDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());

		// Step 2 : update section by adding incorrect content
		String initialContent = document.get();
		String newContent = initialContent.replaceFirst(NO_ERROR_TEXT_PATTERN, ERROR_TEXT_PATTERN);
		document.set(newContent);
		editor.doSave(new NullProgressMonitor());
		waitForAllOperationsInUIThread();

		// Step 3 : ensure that the compilation error has been detected
		assertEquals(
				true,
				hasIntentAnnotation(editor, IntentAnnotationMessageType.COMPILER_ERROR,
						COMPILATION_ERROR_MESSAGE, true));

		// Step 4 : fix the error by resetting the content
		document.set(initialContent);
		editor.doSave(new NullProgressMonitor());
		waitForAllOperationsInUIThread();

		// Step 5 : ensure that the compilation error no longer exists
		assertEquals(
				false,
				hasIntentAnnotation(editor, IntentAnnotationMessageType.COMPILER_ERROR,
						COMPILATION_ERROR_MESSAGE, true));
	}

	// TODO test compilation warnings ?

	/**
	 * Ensures that compilation infos are detected and can be fixed.
	 */
	public void testCompilationInfos() {
		// Step 1 : opening an editor on the document
		editor = openIntentEditor(getIntentDocument().getChapters().get(3).getSubSections().get(0));
		document = (IntentEditorDocument)editor.getDocumentProvider().getDocument(editor.getEditorInput());

		// Step 2 : update section by adding incorrect content
		String initialContent = document.get();
		String newContent = initialContent.replaceFirst(NO_INFOS_TEXT_PATTERN, INFOS_TEXT_PATTERN);
		document.set(newContent);
		editor.doSave(new NullProgressMonitor());
		waitForAllOperationsInUIThread();

		// Step 3 : ensure that the compilation info has been detected
		assertEquals(
				true,
				hasIntentAnnotation(editor, IntentAnnotationMessageType.COMPILER_INFO,
						COMPILATION_INFO_MESSAGE, true));

		// Step 4 : fix the info by resetting the content
		document.set(initialContent);
		editor.doSave(new NullProgressMonitor());
		waitForAllOperationsInUIThread();

		// Step 5 : ensure that the compilation info no longer exists
		assertEquals(
				false,
				hasIntentAnnotation(editor, IntentAnnotationMessageType.COMPILER_INFO,
						COMPILATION_INFO_MESSAGE, true));
	}
}