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
package org.eclipse.mylyn.docs.intent.core.query;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
import org.eclipse.mylyn.docs.intent.core.document.IntentChapter;
import org.eclipse.mylyn.docs.intent.core.document.IntentDocument;
import org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement;
import org.eclipse.mylyn.docs.intent.core.document.IntentSection;
import org.eclipse.mylyn.docs.intent.core.document.IntentStructuredElement;
import org.eclipse.mylyn.docs.intent.core.document.IntentSubSectionContainer;
import org.eclipse.mylyn.docs.intent.core.genericunit.GenericUnit;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ExternalContentReference;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnit;

/**
 * Class that provides useful services on Intent element.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 */
public final class IntentHelper {

	/**
	 * IntentHelper constructor.
	 */
	private IntentHelper() {

	}

	/**
	 * Indicates if the given element is an Intent element that can be hold by an IntentEditor.
	 * 
	 * @param element
	 *            the element to determine if it can be opened by an IntentEditor
	 * @return true if the given element is an Intent element that can be hold by an IntentEditor, false
	 *         otherwise
	 */
	public static boolean canBeOpenedByIntentEditor(Object element) {
		return (element instanceof IntentStructuredElement) || (element instanceof GenericUnit);
	}

	/**
	 * Returns all the compilation status associated to the given elements and its sons.
	 * 
	 * @param element
	 *            the element to inspect
	 * @return all the compilation status associated to the given elements and its sons
	 */
	public static EList<CompilationStatus> getAllStatus(IntentGenericElement element) {
		EList<CompilationStatus> allCompilationStatus = new BasicEList<CompilationStatus>();
		allCompilationStatus.addAll(element.getCompilationStatus());
		for (EObject object : element.eContents()) {
			if (object instanceof IntentGenericElement) {
				allCompilationStatus.addAll(getAllStatus((IntentGenericElement)object));
			}
		}
		return allCompilationStatus;
	}

	/**
	 * Indicates if the given root contain the given searchedElement.
	 * 
	 * @param root
	 *            the root to study
	 * @param searchedElement
	 *            the searchedElement
	 * @return true if the given root contain the given searchedElement, false otherwise
	 */
	public static boolean containsElement(IntentGenericElement root, IntentGenericElement searchedElement) {
		boolean containsElement = root == searchedElement;
		if (!containsElement) {
			Iterator<EObject> containedElementsIterator = root.eContents().iterator();
			while (!containsElement && containedElementsIterator.hasNext()) {
				EObject containedElement = containedElementsIterator.next();
				if (containedElement instanceof IntentGenericElement) {
					containsElement = containsElement((IntentGenericElement)containedElement, searchedElement);
				}
			}
		}
		return containsElement;
	}

	/**
	 * Returns all {@link ExternalContentReference}s contained in the given root.
	 * 
	 * @return all {@link ExternalContentReference}s contained in the given root
	 */
	public static Collection<ExternalContentReference> getAllExternalContentReferences(
			IntentGenericElement root) {
		Collection<ExternalContentReference> externalContentReferences = new LinkedHashSet<ExternalContentReference>();
		if (root instanceof IntentDocument) {
			for (IntentChapter chapter : ((IntentDocument)root).getChapters()) {
				externalContentReferences.addAll(getAllExternalContentReferences(chapter));
			}
		} else {
			if (root instanceof IntentSubSectionContainer) {
				for (IntentSection section : ((IntentSubSectionContainer)root).getSubSections()) {
					externalContentReferences.addAll(getAllExternalContentReferences(section));
				}
			}

			if (root instanceof IntentSection) {
				for (ModelingUnit unit : ((IntentSection)root).getModelingUnits()) {
					externalContentReferences.addAll(getAllExternalContentReferences(unit));
				}
			}

			if (root instanceof ModelingUnit) {
				externalContentReferences.addAll(Sets.newLinkedHashSet(Iterables.filter(
						((ModelingUnit)root).getInstructions(), ExternalContentReference.class)));
			}
		}
		return externalContentReferences;
	}
}
