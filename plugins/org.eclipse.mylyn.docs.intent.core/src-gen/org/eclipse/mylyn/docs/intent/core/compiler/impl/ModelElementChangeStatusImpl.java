/**
 * Copyright (c) 2010, 2012 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 */
package org.eclipse.mylyn.docs.intent.core.compiler.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;

import org.eclipse.mylyn.docs.intent.core.compiler.CompilerPackage;
import org.eclipse.mylyn.docs.intent.core.compiler.ModelElementChangeStatus;
import org.eclipse.mylyn.docs.intent.core.compiler.SynchronizerChangeState;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Model Element Change Status</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link org.eclipse.mylyn.docs.intent.core.compiler.impl.ModelElementChangeStatusImpl#getChangeState <em>Change State</em>}</li>
 *   <li>{@link org.eclipse.mylyn.docs.intent.core.compiler.impl.ModelElementChangeStatusImpl#getCompiledParent <em>Compiled Parent</em>}</li>
 *   <li>{@link org.eclipse.mylyn.docs.intent.core.compiler.impl.ModelElementChangeStatusImpl#getCompiledElement <em>Compiled Element</em>}</li>
 *   <li>{@link org.eclipse.mylyn.docs.intent.core.compiler.impl.ModelElementChangeStatusImpl#getWorkingCopyParentURIFragment <em>Working Copy Parent URI Fragment</em>}</li>
 *   <li>{@link org.eclipse.mylyn.docs.intent.core.compiler.impl.ModelElementChangeStatusImpl#getWorkingCopyElementURIFragment <em>Working Copy Element URI Fragment</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 */
public class ModelElementChangeStatusImpl extends SynchronizerCompilationStatusImpl implements ModelElementChangeStatus {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ModelElementChangeStatusImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return CompilerPackage.Literals.MODEL_ELEMENT_CHANGE_STATUS;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SynchronizerChangeState getChangeState() {
		return (SynchronizerChangeState)eGet(
				CompilerPackage.Literals.MODEL_ELEMENT_CHANGE_STATUS__CHANGE_STATE, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setChangeState(SynchronizerChangeState newChangeState) {
		eSet(CompilerPackage.Literals.MODEL_ELEMENT_CHANGE_STATUS__CHANGE_STATE, newChangeState);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EObject getCompiledParent() {
		return (EObject)eGet(CompilerPackage.Literals.MODEL_ELEMENT_CHANGE_STATUS__COMPILED_PARENT, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCompiledParent(EObject newCompiledParent) {
		eSet(CompilerPackage.Literals.MODEL_ELEMENT_CHANGE_STATUS__COMPILED_PARENT, newCompiledParent);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EObject getCompiledElement() {
		return (EObject)eGet(CompilerPackage.Literals.MODEL_ELEMENT_CHANGE_STATUS__COMPILED_ELEMENT, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setCompiledElement(EObject newCompiledElement) {
		eSet(CompilerPackage.Literals.MODEL_ELEMENT_CHANGE_STATUS__COMPILED_ELEMENT, newCompiledElement);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getWorkingCopyParentURIFragment() {
		return (String)eGet(
				CompilerPackage.Literals.MODEL_ELEMENT_CHANGE_STATUS__WORKING_COPY_PARENT_URI_FRAGMENT, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWorkingCopyParentURIFragment(String newWorkingCopyParentURIFragment) {
		eSet(CompilerPackage.Literals.MODEL_ELEMENT_CHANGE_STATUS__WORKING_COPY_PARENT_URI_FRAGMENT,
				newWorkingCopyParentURIFragment);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String getWorkingCopyElementURIFragment() {
		return (String)eGet(
				CompilerPackage.Literals.MODEL_ELEMENT_CHANGE_STATUS__WORKING_COPY_ELEMENT_URI_FRAGMENT, true);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setWorkingCopyElementURIFragment(String newWorkingCopyElementURIFragment) {
		eSet(CompilerPackage.Literals.MODEL_ELEMENT_CHANGE_STATUS__WORKING_COPY_ELEMENT_URI_FRAGMENT,
				newWorkingCopyElementURIFragment);
	}

} //ModelElementChangeStatusImpl
