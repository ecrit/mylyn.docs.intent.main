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
package org.eclipse.mylyn.docs.intent.parser.modelingunit.serializer.internal;

import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitInstructionReference;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ResourceDeclaration;
import org.eclipse.mylyn.docs.intent.parser.modelingunit.serializer.ModelingUnitSerializer;

/**
 * Returns the serialized form of the given ModelingUnit ResourceDeclaration element.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 */
public final class ResourceDeclarationSerializer {

	/**
	 * ResourceDeclarationSerializer constructor.
	 */
	private ResourceDeclarationSerializer() {

	}

	/**
	 * Return the textual form of the given ModelingUnit ResourceDeclaration.
	 * 
	 * @param resourceDeclaration
	 *            the element to serialize
	 * @param dispatcher
	 *            the ModelingUnitElementDispatcher to call
	 * @return the textual form of the given ModelingUnit ResourceDeclaration.
	 */
	public static String render(ResourceDeclaration resourceDeclaration,
			ModelingUnitElementDispatcher dispatcher) {
		StringBuilder renderedForm = new StringBuilder();
		int initialOffset = dispatcher.getCurrentOffset();
		renderedForm.append("Resource" + ModelingUnitSerializer.WHITESPACE);
		int declarationLength = renderedForm.length();
		if (resourceDeclaration.getName() != null && resourceDeclaration.getName().length() > 0) {
			renderedForm.append(resourceDeclaration.getName());
			declarationLength = renderedForm.length();
			renderedForm.append(ModelingUnitSerializer.WHITESPACE);
		}

		renderedForm.append("{" + ModelingUnitSerializer.LINE_BREAK);

		if (resourceDeclaration.getUri() != null) {
			renderedForm.append("URI" + ModelingUnitSerializer.WHITESPACE + "="
					+ ModelingUnitSerializer.WHITESPACE + ModelingUnitSerializer.QUOTE
					+ resourceDeclaration.getUri() + ModelingUnitSerializer.QUOTE + ';'
					+ ModelingUnitSerializer.LINE_BREAK);
		}

		if (resourceDeclaration.getContentType() != null) {
			renderedForm.append("contentType" + ModelingUnitSerializer.WHITESPACE + "="
					+ ModelingUnitSerializer.WHITESPACE + resourceDeclaration.getContentType() + ';'
					+ ModelingUnitSerializer.LINE_BREAK);
		}

		for (ModelingUnitInstructionReference content : resourceDeclaration.getContent()) {
			renderedForm.append("content" + ModelingUnitSerializer.WHITESPACE + "+="
					+ ModelingUnitSerializer.WHITESPACE);

			dispatcher.setCurrentOffset(initialOffset + renderedForm.length());
			renderedForm.append(dispatcher.doSwitch(content) + ';' + ModelingUnitSerializer.LINE_BREAK);
		}

		renderedForm.append("}");
		if (resourceDeclaration.isLineBreak()) {
			renderedForm.append(ModelingUnitSerializer.LINE_BREAK);
		}
		dispatcher.getPositionManager().setPositionForInstruction(resourceDeclaration, initialOffset,
				renderedForm.length(), declarationLength);
		dispatcher.setCurrentOffset(initialOffset + renderedForm.length());
		return renderedForm.toString();
	}
}
