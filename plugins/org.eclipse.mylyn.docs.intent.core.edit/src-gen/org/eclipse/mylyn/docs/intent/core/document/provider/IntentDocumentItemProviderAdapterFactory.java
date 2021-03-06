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
package org.eclipse.mylyn.docs.intent.core.document.provider;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.edit.provider.ChangeNotifier;
import org.eclipse.emf.edit.provider.ComposeableAdapterFactory;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
import org.eclipse.emf.edit.provider.IChangeNotifier;
import org.eclipse.emf.edit.provider.IDisposable;
import org.eclipse.emf.edit.provider.IEditingDomainItemProvider;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.edit.provider.IItemPropertySource;
import org.eclipse.emf.edit.provider.INotifyChangedListener;
import org.eclipse.emf.edit.provider.IStructuredItemContentProvider;
import org.eclipse.emf.edit.provider.ITreeItemContentProvider;

import org.eclipse.mylyn.docs.intent.core.document.util.IntentDocumentAdapterFactory;

/**
 * This is the factory that is used to provide the interfaces needed to support Viewers.
 * The adapters generated by this factory convert EMF adapter notifications into calls to {@link #fireNotifyChanged fireNotifyChanged}.
 * The adapters also support Eclipse property sheets.
 * Note that most of the adapters are shared among multiple instances.
 * <!-- begin-user-doc -->
 * <!-- end-user-doc -->
 * @generated
 */
public class IntentDocumentItemProviderAdapterFactory extends IntentDocumentAdapterFactory implements ComposeableAdapterFactory, IChangeNotifier, IDisposable {
	/**
	 * This keeps track of the root adapter factory that delegates to this adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ComposedAdapterFactory parentAdapterFactory;

	/**
	 * This is used to implement {@link org.eclipse.emf.edit.provider.IChangeNotifier}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IChangeNotifier changeNotifier = new ChangeNotifier();

	/**
	 * This keeps track of all the supported types checked by {@link #isFactoryForType isFactoryForType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected Collection<Object> supportedTypes = new ArrayList<Object>();

	/**
	 * This constructs an instance.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public IntentDocumentItemProviderAdapterFactory() {
		supportedTypes.add(IEditingDomainItemProvider.class);
		supportedTypes.add(IStructuredItemContentProvider.class);
		supportedTypes.add(ITreeItemContentProvider.class);
		supportedTypes.add(IItemLabelProvider.class);
		supportedTypes.add(IItemPropertySource.class);
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IntentGenericElementItemProvider intentGenericElementItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.mylyn.docs.intent.core.document.IntentGenericElement}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createIntentGenericElementAdapter() {
		if (intentGenericElementItemProvider == null) {
			intentGenericElementItemProvider = new IntentGenericElementItemProvider(this);
		}

		return intentGenericElementItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.mylyn.docs.intent.core.document.IntentSection} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IntentSectionItemProvider intentSectionItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.mylyn.docs.intent.core.document.IntentSection}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createIntentSectionAdapter() {
		if (intentSectionItemProvider == null) {
			intentSectionItemProvider = new IntentSectionItemProvider(this);
		}

		return intentSectionItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.mylyn.docs.intent.core.document.IntentDocument} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IntentDocumentItemProvider intentDocumentItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.mylyn.docs.intent.core.document.IntentDocument}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createIntentDocumentAdapter() {
		if (intentDocumentItemProvider == null) {
			intentDocumentItemProvider = new IntentDocumentItemProvider(this);
		}

		return intentDocumentItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.mylyn.docs.intent.core.document.IntentReferenceInstruction} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected IntentReferenceInstructionItemProvider intentReferenceInstructionItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.mylyn.docs.intent.core.document.IntentReferenceInstruction}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createIntentReferenceInstructionAdapter() {
		if (intentReferenceInstructionItemProvider == null) {
			intentReferenceInstructionItemProvider = new IntentReferenceInstructionItemProvider(this);
		}

		return intentReferenceInstructionItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.mylyn.docs.intent.core.document.LabelDeclaration} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected LabelDeclarationItemProvider labelDeclarationItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.mylyn.docs.intent.core.document.LabelDeclaration}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createLabelDeclarationAdapter() {
		if (labelDeclarationItemProvider == null) {
			labelDeclarationItemProvider = new LabelDeclarationItemProvider(this);
		}

		return labelDeclarationItemProvider;
	}

	/**
	 * This keeps track of the one adapter used for all {@link org.eclipse.mylyn.docs.intent.core.document.LabelReferenceInstruction} instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected LabelReferenceInstructionItemProvider labelReferenceInstructionItemProvider;

	/**
	 * This creates an adapter for a {@link org.eclipse.mylyn.docs.intent.core.document.LabelReferenceInstruction}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter createLabelReferenceInstructionAdapter() {
		if (labelReferenceInstructionItemProvider == null) {
			labelReferenceInstructionItemProvider = new LabelReferenceInstructionItemProvider(this);
		}

		return labelReferenceInstructionItemProvider;
	}

	/**
	 * This returns the root adapter factory that contains this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ComposeableAdapterFactory getRootAdapterFactory() {
		return parentAdapterFactory == null ? this : parentAdapterFactory.getRootAdapterFactory();
	}

	/**
	 * This sets the composed adapter factory that contains this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setParentAdapterFactory(ComposedAdapterFactory parentAdapterFactory) {
		this.parentAdapterFactory = parentAdapterFactory;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object type) {
		return supportedTypes.contains(type) || super.isFactoryForType(type);
	}

	/**
	 * This implementation substitutes the factory itself as the key for the adapter.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Adapter adapt(Notifier notifier, Object type) {
		return super.adapt(notifier, this);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object adapt(Object object, Object type) {
		if (isFactoryForType(type)) {
			Object adapter = super.adapt(object, type);
			if (!(type instanceof Class<?>) || (((Class<?>)type).isInstance(adapter))) {
				return adapter;
			}
		}

		return null;
	}

	/**
	 * This adds a listener.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void addListener(INotifyChangedListener notifyChangedListener) {
		changeNotifier.addListener(notifyChangedListener);
	}

	/**
	 * This removes a listener.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void removeListener(INotifyChangedListener notifyChangedListener) {
		changeNotifier.removeListener(notifyChangedListener);
	}

	/**
	 * This delegates to {@link #changeNotifier} and to {@link #parentAdapterFactory}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void fireNotifyChanged(Notification notification) {
		changeNotifier.fireNotifyChanged(notification);

		if (parentAdapterFactory != null) {
			parentAdapterFactory.fireNotifyChanged(notification);
		}
	}

	/**
	 * This disposes all of the item providers created by this factory. 
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void dispose() {
		if (intentGenericElementItemProvider != null)
			intentGenericElementItemProvider.dispose();
		if (intentSectionItemProvider != null)
			intentSectionItemProvider.dispose();
		if (intentDocumentItemProvider != null)
			intentDocumentItemProvider.dispose();
		if (intentReferenceInstructionItemProvider != null)
			intentReferenceInstructionItemProvider.dispose();
		if (labelDeclarationItemProvider != null)
			labelDeclarationItemProvider.dispose();
		if (labelReferenceInstructionItemProvider != null)
			labelReferenceInstructionItemProvider.dispose();
	}

}
