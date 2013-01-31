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
package org.eclipse.mylyn.docs.intent.client.ui.internal.renderers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.mylyn.docs.intent.client.ui.editor.renderers.IEditorRendererExtension;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ExternalContentReference;

/**
 * Registry containing all Lock Strategy extensions that have been parsed from the
 * {@link IEditorRendererExtensionRegistryListener#RENDERER_EXTENSION_POINT} extension point.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 */
public final class IEditorRendererExtensionRegistry {

	/**
	 * The registered {@link ISaveDialogExtension}s.
	 */
	private static final Map<IEditorRendererExtension, Collection<IEditorRendererExtensionDescriptor>> EXTENSIONS = Maps
			.newHashMap();

	/**
	 * Utility classes don't need a default constructor.
	 */
	private IEditorRendererExtensionRegistry() {

	}

	/**
	 * Adds an extension to the registry, with the given behavior.
	 * 
	 * @param extensionDescriptor
	 *            The extension that is to be added to the registry
	 */
	public static void addExtension(IEditorRendererExtensionDescriptor extensionDescriptor) {
		IEditorRendererExtension extension = extensionDescriptor.getSynchronizerExtension();
		if (EXTENSIONS.get(extension) == null) {
			EXTENSIONS.put(extension, new HashSet<IEditorRendererExtensionDescriptor>());
		}
		EXTENSIONS.get(extension).add(extensionDescriptor);
	}

	/**
	 * Removes all extensions from the registry. This will be called at plugin stopping.
	 */
	public static void clearRegistry() {
		EXTENSIONS.clear();
	}

	/**
	 * Returns a copy of the registered extensions list.
	 * 
	 * @return A copy of the registered extensions list.
	 */
	public static Collection<IEditorRendererExtensionDescriptor> getRegisteredExtensions() {
		Set<IEditorRendererExtensionDescriptor> registeredExtensions = Sets.newHashSet();
		for (Collection<IEditorRendererExtensionDescriptor> extensions : EXTENSIONS.values()) {
			registeredExtensions.addAll(extensions);
		}
		return registeredExtensions;
	}

	/**
	 * Returns all the registered {@link IEditorRendererExtension}s defined for the given
	 * {@link ExternalContentReference}.
	 * 
	 * @param externalContentReference
	 *            the {@link ExternalContentReference} to render
	 * @return all the registered {@link IEditorRendererExtension}s defined for the given
	 *         {@link ExternalContentReference}
	 */
	public static Collection<IEditorRendererExtension> getEditorRendererExtensions(
			ExternalContentReference externalContentReference) {
		Set<IEditorRendererExtension> registeredExtensions = Sets.newHashSet();
		for (Collection<IEditorRendererExtensionDescriptor> extensions : EXTENSIONS.values()) {
			for (IEditorRendererExtensionDescriptor descriptor : extensions) {
				if (descriptor.getSynchronizerExtension().isRendererFor(externalContentReference)) {
					registeredExtensions.add(descriptor.getSynchronizerExtension());
				}
			}
		}
		return registeredExtensions;
	}

	/**
	 * Removes a phantom from the registry.
	 * 
	 * @param extensionClassName
	 *            Qualified class name of the sync element which corresponding phantom is to be removed from
	 *            the registry.
	 */
	public static void removeExtension(String extensionClassName) {
		for (IEditorRendererExtensionDescriptor extension : getRegisteredExtensions()) {
			if (extension.getExtensionClassName().equals(extensionClassName)) {
				EXTENSIONS.get(extension).clear();
			}
		}
	}

}