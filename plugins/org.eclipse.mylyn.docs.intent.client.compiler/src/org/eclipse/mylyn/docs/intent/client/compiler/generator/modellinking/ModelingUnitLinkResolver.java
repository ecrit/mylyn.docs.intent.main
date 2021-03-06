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
package org.eclipse.mylyn.docs.intent.client.compiler.generator.modellinking;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.mylyn.docs.intent.client.compiler.errors.InvalidReferenceException;
import org.eclipse.mylyn.docs.intent.client.compiler.errors.PackageNotFoundResolveException;
import org.eclipse.mylyn.docs.intent.client.compiler.errors.PackageRegistrationException;
import org.eclipse.mylyn.docs.intent.client.compiler.errors.ResolveException;
import org.eclipse.mylyn.docs.intent.client.compiler.utils.IntentCompilerInformationHolder;
import org.eclipse.mylyn.docs.intent.core.document.UnitInstruction;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ContributionInstruction;
import org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstruction;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitInstruction;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitInstructionReference;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ReferenceValue;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ResourceDeclaration;
import org.eclipse.mylyn.docs.intent.core.modelingunit.StructuralFeatureAffectation;

/**
 * Modeling UnitLinker : entity used to resolve reference in the modelingUnits or between generated elements.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 */
public class ModelingUnitLinkResolver {

	/**
	 * The package registry to use for resolving links.
	 */
	private EPackage.Registry packageRegistry;

	/**
	 * The package registry to use for invalid elements.
	 */
	private EPackage.Registry invalidPackageRegistry;

	/**
	 * The information holder to use for register the generated elements.
	 */
	private IntentCompilerInformationHolder informationHolder;

	/**
	 * MetaTypeLinkResolver constructor.
	 * 
	 * @param packageRegistry
	 *            the repository package registry
	 * @param informationHolder
	 *            the information holder to use for register the generated elements
	 */
	public ModelingUnitLinkResolver(EPackage.Registry packageRegistry,
			IntentCompilerInformationHolder informationHolder) {
		this.packageRegistry = packageRegistry;
		this.informationHolder = informationHolder;
		this.invalidPackageRegistry = new EPackageRegistryImpl();
	}

	/**
	 * Returns the structuralFeature described by the given structuralFeatureAffectation, in the scope of the
	 * given eClass.
	 * 
	 * @param affectation
	 *            the structuralFeatureAffectation to inspect
	 * @param eClass
	 *            the eClass containing the described structuralFeatures
	 * @return the structuralFeature described by the given structuralFeatureAffectation, in the scope of the
	 *         given eClass
	 * @throws ResolveException
	 *             if the given eClass has no feature named has described in the StructuralFeatureAffectation
	 */
	public EStructuralFeature resolveEStructuralFeature(StructuralFeatureAffectation affectation,
			EClass eClass) throws ResolveException {

		String featureHRef = affectation.getName();

		try {
			EStructuralFeature foundFeature = eClass.getEStructuralFeature(featureHRef);
			if (foundFeature != null) {
				return foundFeature;
			}
		} catch (NullPointerException e) {
			// A resolve exception will be thrown
		}
		throw new ResolveException(affectation, "The feature " + featureHRef + " doesn't exists in "
				+ eClass.getName());
	}

	/**
	 * Returns the eClassifier described by the given textual reference, exploring all the packages registered
	 * in the package registry.
	 * 
	 * @param instruction
	 *            the instruction containing the eClassifier to resolve
	 * @param href
	 *            the textual reference of an eClassifier
	 * @return the eClass described by the given textual reference
	 * @throws ResolveException
	 *             if the given reference doesn't match any class of any package
	 */
	public EClassifier resolveEClassifierUsingPackageRegistry(UnitInstruction instruction, String href)
			throws ResolveException {

		// We get all the packages to inspect from the package repository
		List<EPackage> packageToInspect = new ArrayList<EPackage>();
		for (String packageURI : this.packageRegistry.keySet()) {
			packageToInspect.add(this.packageRegistry.getEPackage(packageURI));
		}

		EClassifier resolvedClassifier = null;
		// For each package, we try to resolve the reference
		for (EPackage ePackage : packageToInspect) {
			try {
				resolvedClassifier = resolveEClassifierUsingPackage(this.packageRegistry, instruction,
						ePackage.getNsURI(), href);
			} catch (PackageNotFoundResolveException e) {
				// As the nsURI is directly given by an registered ePackage, this exception cannot appen.
			}
			if (resolvedClassifier != null) {
				break;
			}
		}

		if (resolvedClassifier == null) {
			createResolveException(instruction, href);
		}
		return resolvedClassifier;
	}

	/**
	 * Returns the eClass described by the given textual reference, exploring each package associated to the
	 * given list of nsURIs.
	 * 
	 * @param instruction
	 *            the instruction containing the eClassifier to resolve
	 * @param packageURIs
	 *            the URI associated to the packages
	 * @param href
	 *            the textual reference to an EClass
	 * @throws PackageNotFoundResolveException
	 *             if on of the given nsURIs doesn't match to any package of the packageRegistry
	 * @return the eClass described by the given textual reference, exploring all the package with the given
	 * @throws ResolveException
	 *             if the given reference doesn't match any class of any package nsURI
	 */
	public EClassifier resolveEClassifierUsingPackage(UnitInstruction instruction, List<String> packageURIs,
			String href) throws ResolveException, PackageNotFoundResolveException {
		EClassifier resolvedClass = null;
		for (String nsURI : packageURIs) {
			resolvedClass = resolveEClassifierUsingPackage(this.packageRegistry, instruction, nsURI, href);
			if (resolvedClass != null) {
				break;
			}
		}
		if (resolvedClass == null) {
			createResolveException(instruction, href);
		}
		return resolvedClass;
	}

	/**
	 * Throws a resolve exception with an accurate message if possible.
	 * 
	 * @param instruction
	 *            the creation instruction
	 * @param href
	 *            the element name
	 */
	private void createResolveException(UnitInstruction instruction, String href) {
		String message = "The Entity " + href + " cannot be resolved";

		// We get all the packages to inspect from the package repository
		List<EPackage> packageToInspect = new ArrayList<EPackage>();
		for (String packageURI : this.invalidPackageRegistry.keySet()) {
			packageToInspect.add(this.invalidPackageRegistry.getEPackage(packageURI));
		}

		EClassifier resolvedClassifier = null;
		// For each package, we try to resolve the reference
		for (EPackage ePackage : packageToInspect) {
			try {
				resolvedClassifier = resolveEClassifierUsingPackage(this.invalidPackageRegistry, instruction,
						ePackage.getNsURI(), href);
			} catch (PackageNotFoundResolveException e) {
				// As the nsURI is directly given by an registered ePackage, this exception cannot appen.
			}
			if (resolvedClassifier != null) {
				break;
			}
		}

		if (resolvedClassifier != null) {
			message = "The Entity " + href + " exists in the " + resolvedClassifier.getEPackage().getName()
					+ " EPackage but its definition is invalid";
		}

		throw new ResolveException(instruction, message);
	}

	/**
	 * * Returns the eClassifier described by the given textual reference, exploring the package with the
	 * given nsURI.
	 * 
	 * @param packageRegistry
	 *            the registry to inspect
	 * @param instruction
	 *            the instruction containing the eClassifier to resolve
	 * @param nsURI
	 *            the URI of the package
	 * @param href
	 *            the textual reference to an EClassifier
	 * @throws PackageNotFoundResolveException
	 *             if the given nsURI doesn't match to any package of the packageRegistry
	 * @return the eClass described by the given textual reference, exploring the package with the given nsURI
	 */
	private static EClassifier resolveEClassifierUsingPackage(EPackage.Registry packageRegistry,
			UnitInstruction instruction, String nsURI, String href) throws PackageNotFoundResolveException {

		EPackage ePackage = packageRegistry.getEPackage(nsURI);

		if (ePackage == null) {
			throw new PackageNotFoundResolveException(instruction, "The package with nsURI \"" + nsURI
					+ "\" cannot be found. ");
		}

		// resolving ePackage from qualified name if necessary
		String classifierName = href;
		if (href.contains(".")) {
			classifierName = href.substring(href.lastIndexOf('.') + 1);
			String packageQualifiedName = href.substring(0, href.lastIndexOf('.'));
			if (!getQualifiedName(ePackage).equals(packageQualifiedName)) {
				return null;
			}
		}

		EClassifier resolvedClass = null;
		EClassifier foundClassifier = ePackage.getEClassifier(classifierName);
		if (foundClassifier != null) {
			resolvedClass = foundClassifier;
			if (instruction instanceof ReferenceValue) {
				((ReferenceValue)instruction).setReferenceType(resolvedClass);
			}
		}

		return resolvedClass;
	}

	/**
	 * Returns the qualified name of the ePackage.
	 * 
	 * @param ePackage
	 *            the ePackage
	 * @return the qualified name of the ePackage
	 */
	private static String getQualifiedName(EPackage ePackage) {
		String res = ePackage.getName();
		EPackage tmp = (EPackage)ePackage.eContainer();
		while (tmp != null) {
			res = tmp.getName() + '.' + res;
			tmp = (EPackage)tmp.eContainer();
		}
		return res;
	}

	/**
	 * Resolve an unresolved reference using the informationHolder's informations.
	 * 
	 * @param instruction
	 *            the instruction that makes this reference (used to construct error message).
	 * @param searchedType
	 *            the type of the searched element (can be null if unknown)
	 * @param referencedValue
	 *            the textual value of the reference
	 * @return the element matching this reference
	 * @throws InvalidReferenceException
	 *             if the element cannot be found in the generated element list
	 */
	public EObject resolveReferenceInElementList(UnitInstruction instruction, EClassifier searchedType,
			String referencedValue) throws InvalidReferenceException {
		EObject foundReference = (EObject)informationHolder.getCreatedInstanceByName(searchedType,
				referencedValue);

		if (foundReference == null) {
			throw new InvalidReferenceException(instruction, "The reference " + referencedValue
					+ " cannot be resolved. ");
		}

		UnitInstruction instanciationInstruction = informationHolder
				.getInstanciationInstructionByCreatedElement(foundReference);
		if (instanciationInstruction instanceof ModelingUnitInstruction
				&& instruction instanceof ContributionInstruction) {
			((ContributionInstruction)instruction).getContributionReference().setReferencedInstruction(
					(ModelingUnitInstruction)instanciationInstruction);
		} else if (instanciationInstruction instanceof InstanciationInstruction) {
			if (instruction instanceof ReferenceValue) {
				((ReferenceValue)instruction).getInstanciationReference().setInstanciation(
						(InstanciationInstruction)instanciationInstruction);
				((ReferenceValue)instruction)
						.setReferenceType(((InstanciationInstruction)instanciationInstruction).getMetaType()
								.getResolvedType());
			} else if (instruction instanceof ResourceDeclaration) {
				ResourceDeclaration resourceDeclaration = (ResourceDeclaration)instruction;
				for (ModelingUnitInstructionReference reference : resourceDeclaration.getContent()) {
					if (referencedValue.equals(reference.getIntentHref())) {
						reference.setReferencedInstruction((ModelingUnitInstruction)instanciationInstruction);
					}
				}
			}
		}

		return foundReference;

	}

	/**
	 * Register the given ePackage in the package registry.
	 * 
	 * @param instruction
	 *            the instruction that makes this reference (used to construct error message)
	 * @param generatedPackage
	 *            the ePackage to register
	 */
	public void registerInPackageRegistry(UnitInstruction instruction, EPackage generatedPackage) {
		// If no package exception has been registered, we throw this exception
		if (this.packageRegistry == null) {
			throw new PackageRegistrationException(instruction, "the generated package \""
					+ generatedPackage.getNsURI()
					+ "\" cannot be registered (maybe because of an invalid connection to the repository)");
		}
		packageRegistry.put(generatedPackage.getNsURI(), generatedPackage);
	}

	/**
	 * Register the given ePackage in the package registry.
	 * 
	 * @param instruction
	 *            the instruction that makes this reference (used to construct error message)
	 * @param generatedPackage
	 *            the ePackage to register
	 */
	public void registerAsInvalidPackage(UnitInstruction instruction, EPackage generatedPackage) {
		invalidPackageRegistry.put(generatedPackage.getNsURI(), generatedPackage);
	}

	/**
	 * Erases old entries if necessary.
	 * 
	 * @param ePackage
	 *            the ePackage to unregister
	 */
	public void unregisterEPackage(EPackage ePackage) {
		packageRegistry.remove(ePackage.getNsURI());
		invalidPackageRegistry.remove(ePackage.getNsURI());
		informationHolder.getCurrentImportedPackages().remove(ePackage.getNsURI());
	}
}
