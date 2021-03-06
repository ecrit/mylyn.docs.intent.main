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

package org.eclipse.mylyn.docs.intent.client.compiler.generator.modelgeneration;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.mylyn.docs.intent.client.compiler.errors.InvalidValueException;
import org.eclipse.mylyn.docs.intent.client.compiler.generator.modellinking.ModelingUnitLinkResolver;
import org.eclipse.mylyn.docs.intent.core.compiler.CompilationMessageType;
import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatus;
import org.eclipse.mylyn.docs.intent.core.compiler.CompilationStatusSeverity;
import org.eclipse.mylyn.docs.intent.core.compiler.CompilerFactory;
import org.eclipse.mylyn.docs.intent.core.modelingunit.NativeValue;
import org.eclipse.mylyn.docs.intent.core.modelingunit.StructuralFeatureAffectation;

/**
 * Returns the value described by the given {@link NativeValueGenerator} instruction.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 */
public final class NativeValueGenerator {

	/**
	 * ReferenceValueGenerator constructor.
	 */
	private NativeValueGenerator() {

	}

	/**
	 * Generates the object described in the given {@link NativeValue}.
	 * 
	 * @param valueInstruction
	 *            the {@link NativeValue} instruction to inspect
	 * @param linkResolver
	 *            the entity used in order to resolve links
	 * @param modelingUnitGenerator
	 *            the dispatcher to use for calling the correct generator on sub-elements
	 * @return the object described in the given instanciationInstruction
	 */
	public static Object generate(NativeValue valueInstruction, ModelingUnitLinkResolver linkResolver,
			ModelingUnitGenerator modelingUnitGenerator) {

		ModelingUnitGenerator.clearCompilationStatus(valueInstruction);

		// We first get the type of the value to generate
		String typeName = ((StructuralFeatureAffectation)valueInstruction.eContainer()).getMetaType()
				.getTypeName();

		Object generatedValue = generateCorrectValueAccordingToType(valueInstruction, linkResolver, typeName);

		if (generatedValue == null) {
			throw new InvalidValueException(valueInstruction, "The value " + valueInstruction.getValue()
					+ " is invalid for type " + typeName);
		}
		return generatedValue;
	}

	/**
	 * Generates the value described in the given {@link NativeValue} instruction, according to its type.
	 * 
	 * @param valueInstruction
	 *            the {@link NativeValue} describing the value to generate
	 * @param linkResolver
	 *            the linkResolver to use to determine the correct type of this value
	 * @param typeName
	 *            this value's type name
	 * @return the value described in the given NativeValueForStructuralFeature instruction, or null if no
	 *         value can be generated.
	 */
	private static Object generateCorrectValueAccordingToType(NativeValue valueInstruction,
			ModelingUnitLinkResolver linkResolver, String typeName) {
		valueInstruction.getCompilationStatus().clear();
		// According to this type, we generated the value differently
		Object generatedValue = null;
		try {
			if ("EInt".equals(typeName)) {
				generatedValue = generateEInt(valueInstruction.getValue());
			}
			if ("EString".equals(typeName)) {
				generatedValue = generateEString(valueInstruction.getValue());
			}
			if ("EBoolean".equals(typeName)) {
				generatedValue = generateEBoolean(valueInstruction.getValue());
			}
		} catch (NumberFormatException nfe) {
			CompilationStatus status = CompilerFactory.eINSTANCE.createCompilationStatus();
			status.setMessage("Invalid value for " + valueInstruction.getValue() + " : expecting " + typeName);
			status.setSeverity(CompilationStatusSeverity.WARNING);
			status.setTarget(valueInstruction);
			status.setType(CompilationMessageType.INVALID_VALUE_ERROR);
			valueInstruction.getCompilationStatus().add(status);
		}

		if (generatedValue == null) {
			// Specific case of an EEnum value
			// We first resolve the EEnum
			EClassifier type = linkResolver
					.resolveEClassifierUsingPackageRegistry(valueInstruction, typeName);
			if (type instanceof EEnum) {
				EEnum typeAsEnum = (EEnum)type;
				// Then we call the specific methods of this EEnum to get the correct literal.
				EEnumLiteral eEnumLiteralByLiteral = typeAsEnum
						.getEEnumLiteralByLiteral(removeQuotes(valueInstruction.getValue()));
				if (eEnumLiteralByLiteral != null) {
					generatedValue = eEnumLiteralByLiteral.getInstance();
				} else {
					CompilationStatus status = CompilerFactory.eINSTANCE.createCompilationStatus();
					status.setMessage("Invalid literal value '" + valueInstruction.getValue()
							+ "' : expecting one of " + typeAsEnum.getELiterals());
					status.setSeverity(CompilationStatusSeverity.ERROR);
					status.setTarget(valueInstruction);
					status.setType(CompilationMessageType.INVALID_VALUE_ERROR);
					valueInstruction.getCompilationStatus().add(status);
				}
			} else if (type instanceof EDataType) {
				EDataType typeAsDataType = (EDataType)type;
				if (typeAsDataType.getInstanceClass().equals(Integer.class)) {
					generatedValue = generateEInt(valueInstruction.getValue());
				}
				if (typeAsDataType.getInstanceClass().equals(String.class)) {
					generatedValue = generateEString(valueInstruction.getValue());
				}
				if (typeAsDataType.getInstanceClass().equals(Boolean.class)) {
					generatedValue = generateEBoolean(valueInstruction.getValue());
				}
			}
		}
		return generatedValue;
	}

	/**
	 * Generate a String corresponding to the given value.
	 * 
	 * @param value
	 *            the value to convert
	 * @return a String corresponding to the given value
	 */
	private static Integer generateEInt(String value) {
		return Integer.parseInt(removeQuotes(value));
	}

	/**
	 * Generate a String corresponding to the given value.
	 * 
	 * @param value
	 *            the value to convert
	 * @return a String corresponding to the given value
	 */
	private static String generateEString(String value) {
		return removeQuotes(value);
	}

	/**
	 * Generate a Boolean corresponding to the given value.
	 * 
	 * @param value
	 *            the value to convert
	 * @return a Boolean corresponding to the given value
	 */
	private static Boolean generateEBoolean(String value) {
		return Boolean.parseBoolean(removeQuotes(value));
	}

	/**
	 * Returns a String corresponding to the given value from which we removed the quotes.
	 * 
	 * @param value
	 *            the value to treat
	 * @return a String corresponding to the given value from which we removed the quotes
	 */
	private static String removeQuotes(String value) {
		return value.replace("\"", "");
	}
}
