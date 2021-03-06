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
package org.eclipse.mylyn.docs.intent.client.synchronizer.factory;

import org.eclipse.emf.compare.AttributeChange;
import org.eclipse.emf.compare.Diff;
import org.eclipse.emf.compare.DifferenceKind;
import org.eclipse.emf.compare.ReferenceChange;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ResourceDeclaration;

/**
 * Provide messages created from a given Diff.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public final class SynchronizerMessageProvider {

	/**
	 * Constant used to separate synchronization messages.
	 */
	private static final String SYNC_MESSAGES_SEPARATOR = " : ";

	/**
	 * Constant used to prefix synchronization messages.
	 */
	private static final String SYNC_MESSAGES_BEGINNING = "The ";

	/**
	 * Constant to identify whitespace.
	 */
	private static final String SYNC_MESSAGES_WHITESPACE = " ";

	/**
	 * Constant to identify the documentation elements.
	 */
	private static final String SYNC_MESSAGES_INTERNAL_MODEL = "Documentation";

	/**
	 * Constant to identify the working copy elements.
	 */
	private static final String SYNC_MESSAGES_EXTERNAL_MODEL = "Working Copy";

	/**
	 * SynchronizerMessageProvider constructor.
	 */
	private SynchronizerMessageProvider() {
	}

	/**
	 * Create a message from the given {@link Diff}.
	 * 
	 * @param diff
	 *            the {@link Diff} used to create the returned message
	 * @return a message created from the given {@link Diff} element
	 */
	public static String createMessageFromDiff(Diff diff) {
		String returnedMessage = null;

		if (diff instanceof ReferenceChange) {
			ReferenceChange referenceChange = (ReferenceChange)diff;
			if (referenceChange.getReference().isContainment()) {
				returnedMessage = createMessageFromContainmentChange(referenceChange);
			} else {
				returnedMessage = createMessageFromReferenceChange(referenceChange);
			}
		} else if (diff instanceof AttributeChange) {
			returnedMessage = createMessageFromAttributeChange((AttributeChange)diff);
		}

		if (returnedMessage == null) {
			returnedMessage = diff.toString();
		}
		return returnedMessage;
	}

	/**
	 * Create a message from the given Difference element.
	 * 
	 * @param diff
	 *            the Diff used to create the returned message
	 * @return a message created from the given ReferenceChange element
	 */
	private static String createMessageFromContainmentChange(ReferenceChange diff) {
		String returnedMessage = SYNC_MESSAGES_BEGINNING + diff.getValue().eClass().getName()
				+ SYNC_MESSAGES_WHITESPACE + SynchonizerEObjectNameGetter.computeObjectName(diff.getValue());
		switch (diff.getKind().getValue()) {
			case DifferenceKind.ADD_VALUE:
				returnedMessage += " is defined in the " + SYNC_MESSAGES_INTERNAL_MODEL
						+ " model<br/>but not in the " + SYNC_MESSAGES_EXTERNAL_MODEL + " model.";
				break;
			case DifferenceKind.DELETE_VALUE:
				returnedMessage += " is defined in the " + SYNC_MESSAGES_EXTERNAL_MODEL
						+ " model<br/>but not in the " + SYNC_MESSAGES_INTERNAL_MODEL + " model.";
				break;
			default:
				returnedMessage = createMessageFromReferenceChange(diff);
				break;
		}
		return returnedMessage;
	}

	/**
	 * Create a message from the given Difference element.
	 * 
	 * @param diff
	 *            the Diff used to create the returned message
	 * @return a message created from the given ReferenceChange element
	 */
	private static String createMessageFromReferenceChange(ReferenceChange diff) {
		String valueSignature = diff.getValue().eClass().getName() + SYNC_MESSAGES_WHITESPACE
				+ SynchonizerEObjectNameGetter.computeObjectName(diff.getValue());

		String returnedMessage = null;
		String signature = "reference '" + diff.getReference().getName() + "'";

		switch (diff.getKind().getValue()) {
			case DifferenceKind.ADD_VALUE:
				returnedMessage = SYNC_MESSAGES_BEGINNING + valueSignature + " has been added to the "
						+ signature;
				break;
			case DifferenceKind.DELETE_VALUE:
				returnedMessage = SYNC_MESSAGES_BEGINNING + valueSignature + " has been removed from the "
						+ signature;
				break;
			case DifferenceKind.MOVE_VALUE:
				returnedMessage = "The order of the values of the " + signature + " has changed";
				break;
			case DifferenceKind.CHANGE_VALUE:
				returnedMessage = SYNC_MESSAGES_BEGINNING + signature;
				EObject element = diff.getMatch().getRight();

				if (element != null) {

					String elementLabel = SynchonizerEObjectNameGetter.computeObjectName(element);
					if (elementLabel != null) {
						returnedMessage += " in " + elementLabel;
					}
					returnedMessage += " has changed.<br/>";
					returnedMessage += SYNC_MESSAGES_INTERNAL_MODEL
							+ SYNC_MESSAGES_SEPARATOR
							+ SynchonizerEObjectNameGetter.computeObjectName(diff.getValue())
							+ "<br/>"
							+ SYNC_MESSAGES_EXTERNAL_MODEL
							+ SYNC_MESSAGES_SEPARATOR
							+ SynchonizerEObjectNameGetter.computeObjectName((EObject)element.eGet(diff
									.getReference()));

				}
				break;
			default:
				break;
		}
		return returnedMessage;
	}

	/**
	 * Create a message from the given Difference element.
	 * 
	 * @param diff
	 *            the Diff used to create the returned message
	 * @return a message created from the given ReferenceChange element
	 */
	private static String createMessageFromAttributeChange(AttributeChange diff) {
		String returnedMessage = null;
		String signature = "attribute '" + diff.getAttribute().getName() + "'";

		switch (diff.getKind().getValue()) {
			case DifferenceKind.ADD_VALUE:
				returnedMessage = SYNC_MESSAGES_BEGINNING + diff.getValue() + " has been added to "
						+ signature;
				break;
			case DifferenceKind.DELETE_VALUE:
				returnedMessage = SYNC_MESSAGES_BEGINNING + diff.getValue() + " has been removed from "
						+ signature;
				break;
			case DifferenceKind.MOVE_VALUE:
				returnedMessage = "The order of the values of " + signature + " has changed";
				break;
			case DifferenceKind.CHANGE_VALUE:
				returnedMessage = SYNC_MESSAGES_BEGINNING + signature;
				EObject element = diff.getMatch().getRight();
				String elementLabel = SynchonizerEObjectNameGetter.computeObjectName(element);
				if (elementLabel != null) {
					returnedMessage += " in " + elementLabel;
				}
				returnedMessage += " has changed.<br/>";
				returnedMessage += SYNC_MESSAGES_INTERNAL_MODEL + SYNC_MESSAGES_SEPARATOR + diff.getValue()
						+ "<br/>" + SYNC_MESSAGES_EXTERNAL_MODEL + SYNC_MESSAGES_SEPARATOR
						+ element.eGet(diff.getAttribute());
				break;
			default:
				break;
		}
		return returnedMessage;
	}

	/**
	 * Creates an error message indicating that the given resourceDeclaration hasn't been found externally.
	 * 
	 * @param resourceDeclaration
	 *            the resourceDeclaration that hasn't been found externally
	 * @return an error message indicating that the given resourceDeclaration hasn't been found externally
	 */
	public static String createMessageForNullExternalResource(ResourceDeclaration resourceDeclaration) {
		String returnedMessage = "";
		if (resourceDeclaration.getUri() != null) {
			returnedMessage += "Cannot locate Resource at URI : " + resourceDeclaration.getUri().toString();
		}
		returnedMessage += '.';
		return returnedMessage;
	}

	/**
	 * Creates an error message indicating that the given resourceDeclaration has been found externally but is
	 * empty.
	 * 
	 * @param resourceDeclaration
	 *            the resourceDeclaration that has been found externally but is empty
	 * @return an error message
	 */
	public static String createMessageForEmptyExternalResource(ResourceDeclaration resourceDeclaration) {
		String returnedMessage = "";
		if (resourceDeclaration.getUri() != null) {
			returnedMessage += "The Resource at URI : " + resourceDeclaration.getUri().toString()
					+ " is empty";
		}
		returnedMessage += '.';
		return returnedMessage;
	}

	/**
	 * Creates an error message indicating that the given resourceDeclaration is declared in the document,
	 * found externally, but is empty.
	 * 
	 * @param resourceDeclaration
	 *            the resourceDeclaration that has been found externally but is empty
	 * @return an error message
	 */
	public static String createMessageForEmptyInternalResource(ResourceDeclaration resourceDeclaration) {
		String returnedMessage = "";
		if (resourceDeclaration.getUri() != null) {
			returnedMessage += "The Resource " + resourceDeclaration.getName() + " is empty";
		}
		returnedMessage += '.';
		return returnedMessage;
	}
}
