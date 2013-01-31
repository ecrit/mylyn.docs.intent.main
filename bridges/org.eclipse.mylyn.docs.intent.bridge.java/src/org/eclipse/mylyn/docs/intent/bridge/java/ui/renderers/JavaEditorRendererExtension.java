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
package org.eclipse.mylyn.docs.intent.bridge.java.ui.renderers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceReference;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.mylyn.docs.intent.bridge.java.Classifier;
import org.eclipse.mylyn.docs.intent.bridge.java.Field;
import org.eclipse.mylyn.docs.intent.bridge.java.Method;
import org.eclipse.mylyn.docs.intent.bridge.java.resource.factory.JavaResourceFactory;
import org.eclipse.mylyn.docs.intent.bridge.java.util.JavaBridgeUtils;
import org.eclipse.mylyn.docs.intent.client.ui.editor.renderers.IEditorRendererExtension;
import org.eclipse.mylyn.docs.intent.client.ui.logger.IntentUiLogger;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ExternalContentReference;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

/**
 * An {@link IEditorRendererExtension} customizing the way java elements referenced through
 * {@link ExternalContentReference}s are displayed and opened by the Intent hyperlink detector.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 */
public class JavaEditorRendererExtension implements IEditorRendererExtension {

	/**
	 * Default constructor.
	 */
	public JavaEditorRendererExtension() {
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.client.ui.editor.renderers.IEditorRendererExtension#isRendererFor(org.eclipse.mylyn.docs.intent.core.modelingunit.ExternalContentReference)
	 */
	public boolean isRendererFor(ExternalContentReference externalContentReference) {
		return JavaBridgeUtils.isHandledByJavaBridge(URI.createURI(externalContentReference.getUri()
				.toString().trim()));
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.client.ui.editor.renderers.IEditorRendererExtension#openEditor(org.eclipse.mylyn.docs.intent.core.modelingunit.ExternalContentReference)
	 */
	public boolean openEditor(ExternalContentReference externalContentReference) {
		URI javaElementURI = URI.createURI(externalContentReference.getUri().toString().trim());

		// Step 1: open editor
		IFile javaFile = ResourcesPlugin.getWorkspace().getRoot()
				.getFile(new Path(javaElementURI.trimFragment().toString()));
		FileEditorInput editorInput = new FileEditorInput(javaFile);
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry()
				.getDefaultEditor(javaElementURI.trimFragment().lastSegment());
		IEditorPart openedEditor = null;
		if (desc != null) {
			try {
				openedEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
						.openEditor(editorInput, desc.getId());
			} catch (PartInitException e) {
				IntentUiLogger.logError(e);
			}
		}

		// Step 2: select element described by URI (if any)
		if (openedEditor != null && javaElementURI.hasFragment()) {
			updateOpenedEditorSelection(openedEditor, javaFile, javaElementURI);
		}
		// Always return true so that Intent does not try to open an editor with default behavior
		return true;
	}

	/**
	 * Selects the java element (e.g. method, field...) described by the given {@link URI}.
	 * 
	 * @param openedEditor
	 *            the editor to update
	 * @param javaFile
	 *            the java file
	 * @param javaElementURI
	 *            the {@link URI} of the element to select
	 */
	private void updateOpenedEditorSelection(IEditorPart openedEditor, IFile javaFile, URI javaElementURI) {
		EObject eJavaElement = new JavaResourceFactory().createResource(javaElementURI.trimFragment())
				.getEObject(javaElementURI.fragment());
		try {
			IType javaType = ((ICompilationUnit)JavaCore.create(javaFile)).getTypes()[0];

			ISourceReference matchingElement = null;
			if (eJavaElement instanceof Classifier) {
				matchingElement = javaType;
			} else if (eJavaElement instanceof Method) {
				for (IMethod method : javaType.getMethods()) {
					if (method.getElementName().equals(((Method)eJavaElement).getSimpleName())) {
						// Todo consider parameters
						matchingElement = method;
					}
				}
			} else if (eJavaElement instanceof Field) {
				matchingElement = javaType.getField(((Field)eJavaElement).getName());
			}
			if (matchingElement != null) {
				ITextSelection textSelection = new TextSelection(
						matchingElement.getSourceRange().getOffset(), matchingElement.getSourceRange()
								.getLength());
				openedEditor.getEditorSite().getSelectionProvider().setSelection(textSelection);
			}
		} catch (JavaModelException e) {
			// Silent catch, element will not be selected
		}
	}
}