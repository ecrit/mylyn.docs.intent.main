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
package org.eclipse.mylyn.docs.intent.compare.match;

import com.google.common.collect.Maps;

import java.util.Map;

import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.mylyn.docs.intent.compare.match.EditionDistance.CountingDiffEngine;
import org.eclipse.mylyn.docs.intent.compare.utils.LocationDistanceUtils;
import org.eclipse.mylyn.docs.intent.compare.utils.StringDistanceUtils;
import org.eclipse.mylyn.docs.intent.core.document.IntentDocument;
import org.eclipse.mylyn.docs.intent.core.document.IntentStructuredElement;
import org.eclipse.mylyn.docs.intent.core.document.descriptionunit.DescriptionBloc;
import org.eclipse.mylyn.docs.intent.core.document.descriptionunit.DescriptionUnit;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ContributionInstruction;
import org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstruction;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnit;
import org.eclipse.mylyn.docs.intent.core.modelingunit.StructuralFeatureAffectation;
import org.eclipse.mylyn.docs.intent.markup.markup.BlockContent;
import org.eclipse.mylyn.docs.intent.markup.markup.MarkupPackage;
import org.eclipse.mylyn.docs.intent.markup.markup.Text;
import org.eclipse.mylyn.docs.intent.markup.serializer.WikiTextSerializer;
import org.eclipse.mylyn.docs.intent.serializer.IntentSerializer;

/**
 * An implementation of a diff engine which count and measure the detected changes into Intent documents.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public class IntentCountingDiffEngine extends CountingDiffEngine {

	/**
	 * Weight of location differences.
	 */
	private static final double LOCALIZATION_DISTANCE_WEIGHT = 0.15;

	/**
	 * Weight of identifier differences.
	 */
	private static final double IDENTIFIER_DISTANCE_WEIGHT = 0.85;

	/**
	 * A map keeping a cache of each element and its serialized form.
	 */
	private Map<EObject, String> serializationCache = Maps.newLinkedHashMap();

	/**
	 * A map keeping a cache of each element and its complete level.
	 */
	private Map<EObject, String> levelCache = Maps.newLinkedHashMap();

	/**
	 * Constructor.
	 * 
	 * @param editionDistance
	 *            for instanciation
	 * @param maxDistance
	 *            the max distance
	 */
	public IntentCountingDiffEngine(
			org.eclipse.mylyn.docs.intent.compare.match.EditionDistance editionDistance, double maxDistance) {
		editionDistance.super(maxDistance, editionDistance.fakeComparison);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.compare.match.EditionDistance.CountingDiffEngine#measureDifferences(org.eclipse.emf.compare.Comparison,
	 *      org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject)
	 */
	@Override
	public double measureDifferences(Comparison comparisonInProgress, EObject a, EObject b) {
		if (a instanceof IntentDocument && b instanceof IntentDocument) {
			return 0; // root element
		}

		Double distance = null;

		// the default localization distance
		Double locationDistance = getLocationDistance(a, b);

		// the semantic distance: in the best case, a title or feature id. If not available, the
		// element serialization
		Double identifierDistance = getIdentifierDistance(a, b);
		if (identifierDistance == null) {
			identifierDistance = getSerializationDistance(a, b);
		}

		if (identifierDistance != null && locationDistance != null) {
			distance = (double)(identifierDistance * IDENTIFIER_DISTANCE_WEIGHT + locationDistance
					* LOCALIZATION_DISTANCE_WEIGHT);
		} else if (identifierDistance != null) {
			distance = identifierDistance;
		} else if (locationDistance != null) {
			distance = locationDistance;
		} else {
			distance = super.measureDifferences(comparisonInProgress, a, b);
		}
		return distance;
	}

	/**
	 * Returns the distance between document elements by comparing their locations.
	 * 
	 * @param a
	 *            the first element
	 * @param b
	 *            the second element
	 * @return the distance between two strings
	 */
	private Double getLocationDistance(EObject a, EObject b) {
		Double distance = null;
		String fragmentA = LocationDistanceUtils.computeLevel(a);
		String fragmentB = LocationDistanceUtils.computeLevel(b);
		levelCache.put(a, fragmentA);
		levelCache.put(b, fragmentB);
		if (fragmentA != null && fragmentB != null) {
			distance = StringDistanceUtils.getStringDistance(fragmentA, fragmentB);
		}
		return distance;
	}

	/**
	 * Returns the distance between document elements by comparing their identifiers.
	 * 
	 * @param a
	 *            the first element
	 * @param b
	 *            the second element
	 * @return the distance between two strings
	 */
	private Double getIdentifierDistance(EObject a, EObject b) {
		Double distance = null;
		String identifierA = null;
		String identifierB = null;
		if (a instanceof IntentStructuredElement && b instanceof IntentStructuredElement) {
			identifierA = getTitle((IntentStructuredElement)a);
			identifierB = getTitle((IntentStructuredElement)b);
		} else if (a instanceof StructuralFeatureAffectation && b instanceof StructuralFeatureAffectation) {
			identifierA = ((StructuralFeatureAffectation)a).getName();
			identifierB = ((StructuralFeatureAffectation)b).getName();
		} else if (a instanceof InstanciationInstruction && b instanceof InstanciationInstruction) {
			identifierA = ((InstanciationInstruction)a).getName();
			identifierB = ((InstanciationInstruction)b).getName();
		} else if (a instanceof ContributionInstruction && b instanceof ContributionInstruction) {
			identifierA = ((ContributionInstruction)a).getContributionReference().getIntentHref();
			identifierB = ((ContributionInstruction)b).getContributionReference().getIntentHref();
		}
		if (identifierA != null && identifierB != null) {
			distance = StringDistanceUtils.getStringDistance(identifierA, identifierB);
		}
		return distance;
	}

	/**
	 * Returns the distance between document elements by comparing their serialization.
	 * 
	 * @param a
	 *            the first element
	 * @param b
	 *            the second element
	 * @return the distance between two strings
	 */
	private Double getSerializationDistance(EObject a, EObject b) {
		Double distance = null;
		String serializedA = serialize(a);
		String serializedB = serialize(b);
		if (serializedA != null && serializedB != null) {
			distance = StringDistanceUtils.getStringDistance(serializedA, serializedB);
		}
		return distance;
	}

	/**
	 * Serializes the given element.
	 * 
	 * @param root
	 *            the element to serialize
	 * @return the serialized version
	 */
	private String serialize(EObject root) {
		if (serializationCache.get(root) == null) {
			String res = "";
			if (root.eClass().getEPackage().equals(MarkupPackage.eINSTANCE)) {
				res = new WikiTextSerializer().serialize(root);
			} else if (root instanceof ModelingUnit || root instanceof DescriptionUnit
					|| root instanceof IntentStructuredElement) {
				res = new IntentSerializer().serialize(root);
			} else if (root instanceof DescriptionBloc) {
				DescriptionBloc bloc = (DescriptionBloc)root;
				res = serialize(bloc.getDescriptionBloc());
			}
			serializationCache.put(root, res);
		}
		return serializationCache.get(root);
	}

	/**
	 * Returns the formatted title of the given element.
	 * 
	 * @param element
	 *            the formatted title
	 * @return the formatted title of the given element
	 */
	private static String getTitle(IntentStructuredElement element) {
		String title = null;
		if (element.getTitle() != null && !element.getTitle().getContent().isEmpty()) {
			BlockContent content = element.getTitle().getContent().get(0);
			if (content instanceof Text) {
				title = ((Text)content).getData();
			}
		}
		return title;
	}
}
