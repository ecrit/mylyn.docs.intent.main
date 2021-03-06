/*******************************************************************************
 * Copyright (c) 2012 Obeo.

 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.docs.intent.modelingunit.update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.Match;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.mylyn.docs.intent.collab.common.query.TraceabilityInformationsQuery;
import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
import org.eclipse.mylyn.docs.intent.compare.utils.EMFCompareUtils;
import org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement;
import org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstruction;
import org.eclipse.mylyn.docs.intent.modelingunit.gen.AbstractModelingUnitGenerator;

/**
 * Utility which updates modeling units according to existing models.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public abstract class AbstractModelingUnitUpdater extends AbstractModelingUnitGenerator {

	/**
	 * The common traceability query.
	 */
	protected TraceabilityInformationsQuery query;

	/**
	 * Last used index (use to associated identifier to created elements.
	 */
	private int lastIndex = -1;

	/**
	 * Associate {@link EObject}s with their intent reference identifier.
	 */
	private Map<EObject, String> referenceNames = new HashMap<EObject, String>();

	/**
	 * The mapping of working copy objects, compiled objects.
	 */
	private Map<EObject, EObject> match = new HashMap<EObject, EObject>();

	/**
	 * Creates a modeling unit updater.
	 * 
	 * @param repositoryAdapter
	 *            the repository adapter
	 */
	public AbstractModelingUnitUpdater(RepositoryAdapter repositoryAdapter) {
		super(repositoryAdapter);
		query = new TraceabilityInformationsQuery(repositoryAdapter);
	}

	/**
	 * Initializes the match map from the given resources.
	 * 
	 * @param compiledResource
	 *            the compiled resource
	 * @param workingCopyResource
	 *            the working copy resource
	 */
	protected void includeMatch(Resource compiledResource, Resource workingCopyResource) {
		try {
			for (Match matchElement : EMFCompareUtils.compare(compiledResource, workingCopyResource)
					.getMatches()) {
				collectAllMatches(matchElement);
			}
			// CHECKSTYLE:OFF we ignore comparison errors
		} catch (Exception e) {
			// CHECKSTYLE :ON
			e.printStackTrace();
		}
	}

	/**
	 * Fills the match map from the given match element.
	 * 
	 * @param matchElement
	 *            the match element
	 */
	private void collectAllMatches(Match matchElement) {
		EObject workingCopyObject = matchElement.getRight();
		EObject compiledObject = matchElement.getLeft();
		match.put(workingCopyObject, compiledObject);
		for (Match subMatchElement : matchElement.getAllSubmatches()) {
			EObject subWorkingCopyObject = subMatchElement.getRight();
			EObject subCompiledObject = subMatchElement.getLeft();
			match.put(subWorkingCopyObject, subCompiledObject);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.modelingunit.gen.AbstractModelingUnitGenerator#getExistingInstanciationFor(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	protected InstanciationInstruction getExistingInstanciationFor(EObject o) {
		EObject compiledObject = match.get(o);
		if (compiledObject != null) {
			// working copy object
			return query.getInstanciation(compiledObject);
		} else {
			// compiled object
			return query.getInstanciation(o);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.modelingunit.gen.AbstractModelingUnitGenerator#getGeneratedElement(org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstruction)
	 */
	@Override
	protected EObject getGeneratedElement(InstanciationInstruction instanciation) {
		return query.getInstance(instanciation);
	}

	/**
	 * Load the object at the given uri.
	 * 
	 * @param uri
	 *            the uri
	 * @return the loaded object
	 */
	protected EObject getWorkingCopyEObject(String uri) {
		if (uri == null) {
			return null;
		}
		return resourceSet.getEObject(URI.createURI(uri), true);
	}

	/**
	 * Returns the container of the given type from a root element.
	 * 
	 * @param object
	 *            the root
	 * @param classifierId
	 *            the classifier ids to consider
	 * @return the parent instruction
	 */
	public static IntentGenericElement getContainer(IntentGenericElement object, int... classifierId) {
		List<Integer> ids = new ArrayList<Integer>();
		for (Integer id : classifierId) {
			ids.add(id);
		}

		EObject tmp = object;
		while (tmp != null && !ids.contains(tmp.eClass().getClassifierID())) {
			tmp = tmp.eContainer();
		}
		if (tmp != null && ids.contains(tmp.eClass().getClassifierID())) {
			return (IntentGenericElement)tmp;
		}
		return null;
	}

	/**
	 * Removes the given element from its container, deletes the container if empty.
	 * 
	 * @param element
	 *            the element
	 */
	protected void removeFromContainer(EObject element) {
		EObject container = element.eContainer();
		EcoreUtil.delete(element);
		if (container != null && container.eContents().isEmpty()) {
			removeFromContainer(container);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.modelingunit.gen.AbstractModelingUnitGenerator#getReferenceName(org.eclipse.emf.ecore.EObject)
	 */
	@Override
	protected String getReferenceName(EObject eObject) {
		String res = referenceNames.get(eObject);
		if (res == null) {
			if (lastIndex < 0) {
				// we have to init the index from existing instanciations
				String regex = "REF[0-9]+";
				for (InstanciationInstruction instanciation : query.getInstanciations()) {
					String name = instanciation.getName();
					if (name != null && name.matches(regex)) {
						int current = Integer.valueOf(name.substring(3));
						lastIndex = Math.max(lastIndex, current);
					}
				}
			}
			lastIndex++;
			res = "REF" + lastIndex;
			referenceNames.put(eObject, res);
		}
		return res;
	}
}
