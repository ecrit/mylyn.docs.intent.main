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
package org.eclipse.mylyn.docs.intent.collab.common.internal.logger;

import com.google.common.collect.Sets;

import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionDelta;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.Platform;
import org.eclipse.mylyn.docs.intent.collab.common.logger.IIntentLogger.LogType;
import org.eclipse.mylyn.docs.intent.collab.common.logger.IntentLogger;

/**
 * This listener allows to be aware of contribution changes against the
 * {@link IntentLoggerRegistryListener#INTENT_LOGGER_EXTENSION_POINT} extension point.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 */
public class IntentLoggerRegistryListener implements IRegistryChangeListener {

	/** Name of the extension point to parse for extensions. */
	public static final String INTENT_LOGGER_EXTENSION_POINT = "org.eclipse.mylyn.docs.intent.collab.common.intentlogger"; //$NON-NLS-1$

	/** Name of the extension point's tag. */
	private static final String LOGGER_TAG = "logger"; //$NON-NLS-1$

	/**
	 * Register this listener and parse initial contributions.
	 */
	public void init() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		registry.addRegistryChangeListener(this, INTENT_LOGGER_EXTENSION_POINT);
		parseInitialContributions();
	}

	/**
	 * Parses a single extension contribution.
	 * 
	 * @param extension
	 *            Parses the given extension and adds its contribution to the registry.
	 */
	private void parseExtension(IExtension extension) {
		final IConfigurationElement[] configElements = extension.getConfigurationElements();
		for (IConfigurationElement elem : configElements) {
			if (LOGGER_TAG.equals(elem.getName())) {
				try {
					IntentLoggerRegistry.addLogger(new IntentLoggerDescriptor(elem));
				} catch (IllegalArgumentException e) {
					IntentLogger.getInstance().log(LogType.ERROR,
							"Could not create logger " + elem.getValue(), e);
				}
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.core.runtime.IRegistryEventListener#added(org.eclipse.core.runtime.IExtensionPoint[])
	 */
	public void added(IExtensionPoint[] extensionPoints) {
		for (IExtensionPoint extensionPoint : extensionPoints) {
			for (IExtension extension : extensionPoint.getExtensions()) {
				parseExtension(extension);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
	 */
	public void registryChanged(IRegistryChangeEvent event) {
		Set<IExtension> addedExtensions = Sets.newLinkedHashSet();
		for (IExtensionDelta extensionDelta : event.getExtensionDeltas()) {
			addedExtensions.add(extensionDelta.getExtension());
		}
		added(addedExtensions.toArray(new IExtension[addedExtensions.size()]));
	}

	/**
	 * Behavior when the given extensions are added.
	 * 
	 * @param extensions
	 *            the added extensions
	 */
	public void added(IExtension[] extensions) {
		for (IExtension extension : extensions) {
			parseExtension(extension);
		}
	}

	/**
	 * Though this listener reacts to the extension point changes, there could have been contributions before
	 * it's been registered. This will parse these initial contributions.
	 */
	public void parseInitialContributions() {
		final IExtensionRegistry registry = Platform.getExtensionRegistry();

		for (IExtension extension : registry.getExtensionPoint(INTENT_LOGGER_EXTENSION_POINT).getExtensions()) {
			parseExtension(extension);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.core.runtime.IRegistryEventListener#removed(org.eclipse.core.runtime.IExtension[])
	 */
	public void removed(IExtension[] extensions) {
		for (IExtension extension : extensions) {
			final IConfigurationElement[] configElements = extension.getConfigurationElements();
			for (IConfigurationElement elem : configElements) {
				if (LOGGER_TAG.equals(elem.getName())) {
					final String extensionClassName = elem
							.getAttribute(IntentLoggerDescriptor.LOGGER_CLASS_NAME);
					IntentLoggerRegistry.removeExtension(extensionClassName);
				}
			}
		}
	}

	/**
	 * Remove this listener and flush the contributions registry.
	 */
	public void dispose() {
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		registry.removeRegistryChangeListener(this);
		IntentLoggerRegistry.clearContributedLoggers();
	}
}
