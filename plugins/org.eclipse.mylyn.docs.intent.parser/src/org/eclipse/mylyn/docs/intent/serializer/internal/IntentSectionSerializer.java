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
package org.eclipse.mylyn.docs.intent.serializer.internal;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.mylyn.docs.intent.core.document.IntentDocument;
import org.eclipse.mylyn.docs.intent.core.document.IntentSection;
import org.eclipse.mylyn.docs.intent.parser.IntentKeyWords;
import org.eclipse.mylyn.docs.intent.serializer.descriptionunit.DescriptionUnitSerializer;

/**
 * Serialize an Intent section, and maintain an mapping between serialized elements and their position in
 * their serialized form.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 */
public final class IntentSectionSerializer {

	/**
	 * IntentSectionSerializer constructor.
	 */
	private IntentSectionSerializer() {

	}

	/**
	 * Return the serialized form of the given element.
	 * 
	 * @param section
	 *            the element to serialize
	 * @param serializer
	 *            the dispatcher to call for serializing sub-elements
	 * @return the serialized form of the given element
	 */
	public static String serialize(IntentSection section, IntentElementSerializer serializer) {
		StringBuilder renderedForm = new StringBuilder();
		renderedForm.append(serializer.tabulation());

		int initalOffset = serializer.getCurrentOffset();
		if (section.eContainer() instanceof IntentDocument) {
			renderedForm.append(IntentKeyWords.INTENT_KEYWORD_CHAPTER);
		} else {
			renderedForm.append(IntentKeyWords.INTENT_KEYWORD_SECTION);
		}
		int initialLength = renderedForm.length();
		renderedForm.append(IntentKeyWords.INTENT_WHITESPACE);

		// Section Title
		if (section.getTitle() != null) {
			DescriptionUnitSerializer descriptionUnitSerializer = new DescriptionUnitSerializer(
					new IntentDocumentSerializerSwitch(serializer));
			renderedForm.append(descriptionUnitSerializer.serializeSectionTitle(section.getTitle(),
					initalOffset + renderedForm.length()));
			serializer.getPositionManager().addIntentPositionManagerInformations(
					descriptionUnitSerializer.getPositionManager());
			renderedForm.append(IntentKeyWords.INTENT_WHITESPACE);
		}
		renderedForm.append(IntentKeyWords.INTENT_KEYWORD_OPEN);

		serializer.setCurrentIndendationLevel(serializer.getCurrentIndendationLevel() + 1);

		// Contents : subSection and Units
		for (EObject content : section.getIntentContent()) {
			if (content instanceof IntentSection) {
				renderedForm.append(IntentKeyWords.INTENT_LINEBREAK);
			}
			serializer.setCurrentOffset(initalOffset + renderedForm.length());
			renderedForm.append(serializer.serialize(content));
		}

		serializer.setCurrentIndendationLevel(serializer.getCurrentIndendationLevel() - 1);
		renderedForm.append(serializer.tabulation() + IntentKeyWords.INTENT_KEYWORD_CLOSE
				+ IntentKeyWords.INTENT_LINEBREAK);
		serializer.setCurrentOffset(initalOffset + renderedForm.length());
		serializer.setPositionForElement(section, initalOffset, renderedForm.length(), initialLength);
		return renderedForm.toString();
	}
}
