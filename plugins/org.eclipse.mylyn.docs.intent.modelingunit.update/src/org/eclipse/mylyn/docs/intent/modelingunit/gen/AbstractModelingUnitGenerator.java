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
package org.eclipse.mylyn.docs.intent.modelingunit.gen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
import org.eclipse.mylyn.docs.intent.core.modelingunit.AbstractValue;
import org.eclipse.mylyn.docs.intent.core.modelingunit.AffectationOperator;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ContributionInstruction;
import org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstruction;
import org.eclipse.mylyn.docs.intent.core.modelingunit.InstanciationInstructionReference;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitFactory;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitInstructionReference;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitPackage;
import org.eclipse.mylyn.docs.intent.core.modelingunit.NativeValue;
import org.eclipse.mylyn.docs.intent.core.modelingunit.NewObjectValue;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ReferenceValue;
import org.eclipse.mylyn.docs.intent.core.modelingunit.StructuralFeatureAffectation;
import org.eclipse.mylyn.docs.intent.core.modelingunit.TypeReference;

/**
 * Utility which generates modeling units from existing models.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public abstract class AbstractModelingUnitGenerator {

	/**
	 * The repository adapter.
	 */
	protected RepositoryAdapter repositoryAdapter;

	/**
	 * The {@link ResourceSet} used by this generator.
	 */
	protected ResourceSet resourceSet = new ResourceSetImpl();

	/**
	 * Lists of the new objects.
	 */
	private List<EObject> newObjects;

	/**
	 * Creates a modeling unit updater.
	 * 
	 * @param repositoryAdapter
	 *            the repository adapter
	 */
	public AbstractModelingUnitGenerator(RepositoryAdapter repositoryAdapter) {
		this.repositoryAdapter = repositoryAdapter;
	}

	/**
	 * Sets the new objects lists, i.e. objects created during the same process which can reference each
	 * other.
	 * 
	 * @param newObjects
	 *            the objects available during the creation process
	 */
	public void setNewObjects(List<EObject> newObjects) {
		this.newObjects = newObjects;
	}

	/**
	 * Generates a contribution to the given instantiation instruction. The contribution build the given root
	 * model fragment.
	 * 
	 * @param instanciation
	 *            the instantiation to contribute
	 * @return the contribution
	 */
	public ContributionInstruction generateContribution(InstanciationInstruction instanciation) {
		ContributionInstruction contribution = ModelingUnitFactory.eINSTANCE.createContributionInstruction();
		contribution.setLineBreak(true);
		ModelingUnitInstructionReference ref = ModelingUnitFactory.eINSTANCE
				.createModelingUnitInstructionReference();
		ref.setReferencedInstruction(instanciation);
		if (instanciation.getName() == null) {
			// if there is no reference available we generate a new one
			EObject generated = getGeneratedElement(instanciation);
			instanciation.setName(getReferenceName(generated));
		}
		ref.setIntentHref(instanciation.getName());
		contribution.setContributionReference(ref);
		return contribution;
	}

	/**
	 * Generates the instructions which build the given {@link EObject} and its attributes.
	 * 
	 * @param root
	 *            the root {@link EObject}
	 * @return the generated {@link InstanciationInstruction}
	 */
	public InstanciationInstruction generateInstanciation(EObject root) {
		InstanciationInstruction instanciation = ModelingUnitFactory.eINSTANCE
				.createInstanciationInstruction();
		instanciation.setName(getReferenceName(root)); // we set the previously generated reference name

		TypeReference typeReference = ModelingUnitFactory.eINSTANCE.createTypeReference();
		typeReference.setTypeName(root.eClass().getName());
		typeReference.setResolvedType(root.eClass());

		instanciation.setMetaType(typeReference);

		for (EStructuralFeature feature : root.eClass().getEAllStructuralFeatures()) {
			if (!filter(feature)) {
				instanciation.getStructuralFeatures().addAll(
						generateAffectations(feature, root.eGet(feature)));
			}
		}
		return instanciation;
	}

	/**
	 * Generates the {@link StructuralFeatureAffectation} which build the affectation.
	 * 
	 * @param feature
	 *            the feature to set
	 * @param workingCopyValue
	 *            the value to set, can be a collection
	 * @return the affectation instruction
	 */
	public List<StructuralFeatureAffectation> generateAffectations(EStructuralFeature feature,
			Object workingCopyValue) {
		List<StructuralFeatureAffectation> affectations = new ArrayList<StructuralFeatureAffectation>();
		if (workingCopyValue instanceof Collection<?>) {
			for (Object singleValue : (Collection<?>)workingCopyValue) {
				StructuralFeatureAffectation affectation = generateSingleAffectation(feature, singleValue);
				if (affectation != null) {
					affectations.add(affectation);
				}
			}
		} else {
			StructuralFeatureAffectation affectation = generateSingleAffectation(feature, workingCopyValue);
			if (affectation != null) {
				affectations.add(affectation);
			}
		}
		return affectations;
	}

	/**
	 * Generates the {@link StructuralFeatureAffectation} which build the affectation.
	 * 
	 * @param feature
	 *            the feature to set
	 * @param workingCopyValue
	 *            the single value to set
	 * @return the affectation instruction
	 */
	public StructuralFeatureAffectation generateSingleAffectation(EStructuralFeature feature,
			Object workingCopyValue) {
		StructuralFeatureAffectation affectation = null;
		if (workingCopyValue != null
				&& !(feature.getDefaultValue() != null && workingCopyValue.equals(feature.getDefaultValue()))) {
			affectation = ModelingUnitFactory.eINSTANCE.createStructuralFeatureAffectation();
			affectation.setLineBreak(true);
			affectation.setName(feature.getName());
			if (feature.isMany()) {
				affectation.setUsedOperator(AffectationOperator.MULTI_VALUED_AFFECTATION);
			} else {
				affectation.setUsedOperator(AffectationOperator.SINGLE_VALUED_AFFECTATION);
			}
			AbstractValue generateValue = generateValue(feature, workingCopyValue);
			if (generateValue != null) {
				affectation.getValues().add(generateValue);
			} else {
				affectation = null;
			}
		}
		return affectation;
	}

	/**
	 * Sets the correct value in the given value instruction.
	 * 
	 * @param valueInstruction
	 *            the value instruction
	 * @param newValue
	 *            the value to set
	 * @return true if the value has been set
	 */
	public boolean setValue(AbstractValue valueInstruction, Object newValue) {
		boolean res = true;
		switch (valueInstruction.eClass().getClassifierID()) {
			case ModelingUnitPackage.NATIVE_VALUE:
				((NativeValue)valueInstruction).setValue("\"" + newValue.toString() + "\"");
				break;
			case ModelingUnitPackage.NEW_OBJECT_VALUE:
				((NewObjectValue)valueInstruction).setValue(generateInstanciation((EObject)newValue));
				break;
			case ModelingUnitPackage.REFERENCE_VALUE:
				if (newValue instanceof EDataType) {
					InstanciationInstructionReference instanciationRef = ModelingUnitFactory.eINSTANCE
							.createInstanciationInstructionReference();
					instanciationRef.setInstanceName(((EDataType)newValue).getName());
					((ReferenceValue)valueInstruction).setReferenceType((EDataType)newValue);
					((ReferenceValue)valueInstruction).setInstanciationReference(instanciationRef);
				} else {
					InstanciationInstructionReference reference = ModelingUnitFactory.eINSTANCE
							.createInstanciationInstructionReference();

					InstanciationInstruction instanciation = getExistingInstanciationFor((EObject)newValue);
					if (instanciation != null) {
						if (instanciation.getName() == null) {
							// if there is no reference available we generate a new one
							instanciation.setName(getReferenceName(getGeneratedElement(instanciation)));
						}
						reference.setInstanceName(instanciation.getName());
						((ReferenceValue)valueInstruction).setInstanciationReference(reference);
					} else if (newObjects != null && newObjects.contains(newValue)) {
						reference.setInstanceName(getReferenceName((EObject)newValue));
						((ReferenceValue)valueInstruction).setInstanciationReference(reference);
					} else {
						res = false;
					}
				}
				break;
			default:
				break;
		}
		return res;
	}

	/**
	 * Generates the {@link Value} which build the value.
	 * 
	 * @param feature
	 *            the feature to set
	 * @param value
	 *            the value
	 * @return the value instruction
	 */
	private AbstractValue generateValue(EStructuralFeature feature, Object value) {
		AbstractValue res = null;
		if (feature instanceof EAttribute) {
			res = ModelingUnitFactory.eINSTANCE.createNativeValue();
		} else if (feature instanceof EReference) {
			if (((EReference)feature).isContainment()) {
				res = ModelingUnitFactory.eINSTANCE.createNewObjectValue();
			} else {
				res = ModelingUnitFactory.eINSTANCE.createReferenceValue();
			}
		}
		if (setValue(res, value)) {
			return res;
		} else {
			return null;
		}
	}

	/**
	 * Filters the feature to generate as affectations.
	 * 
	 * @param feature
	 *            the feature
	 * @return true if an affectation must be generated
	 */
	protected boolean filter(EStructuralFeature feature) {
		boolean isUnsettable = feature instanceof EReference && ((EReference)feature).isContainment()
				&& feature.isUnsettable();
		return !feature.isChangeable() || feature.isDerived() || isUnsettable || feature.isTransient();
	}

	/**
	 * Retrieves the object generated by the given instanciation.
	 * 
	 * @param instanciation
	 *            the instanciation instruction
	 * @return the generated object
	 */
	protected abstract EObject getGeneratedElement(InstanciationInstruction instanciation);

	/**
	 * Retrieves, if exists, the instanciation of the given working copy object in the existing modeling unit.
	 * 
	 * @param o
	 *            the object
	 * @return the instanciation instruction or null
	 */
	protected abstract InstanciationInstruction getExistingInstanciationFor(EObject o);

	/**
	 * Generates a reference name.
	 * 
	 * @param eObject
	 *            the object to reference
	 * @return the id
	 */
	protected abstract String getReferenceName(EObject eObject);
}
