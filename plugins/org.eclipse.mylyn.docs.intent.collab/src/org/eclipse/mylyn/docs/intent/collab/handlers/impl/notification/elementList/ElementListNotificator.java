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
package org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.elementList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryObjectHandler;
import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.IntentCommand;
import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
import org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator;
import org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification;

/**
 * Notificator listening for the changes that occurs on the given list of elements.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 */
public class ElementListNotificator implements Notificator {

	/**
	 * Set of the listened EObjects : if any change occurs on an element of this set, this Notificator will
	 * prevent its associated RepositoryObjectHandlers.
	 */
	private final Set<EObject> listenedObjects;

	/**
	 * List of RepositoryObectHandlers to notify if any change occurs on any listened object.
	 */
	private Set<RepositoryObjectHandler> listeningHandlers;

	/**
	 * Adapter to attach to every watched object.
	 */
	private final ElementListAdapter elementAdapter;

	/**
	 * ElementListNotification constructor.
	 * 
	 * @param listenedObjects
	 *            the objects to listen
	 * @param repositoryAdapter
	 *            the repository adapter
	 */
	public ElementListNotificator(Set<EObject> listenedObjects, RepositoryAdapter repositoryAdapter) {
		this.listenedObjects = listenedObjects;
		this.listeningHandlers = new LinkedHashSet<RepositoryObjectHandler>();
		this.elementAdapter = new ElementListAdapter(this);

		initialize(listeningHandlers, repositoryAdapter);
	}

	/**
	 * ElementListNotification constructor.
	 * 
	 * @param listenedObjects
	 *            the objects to listen
	 * @param listAdapter
	 *            the adapter to use (must inherit from ElementListAdapter)
	 * @param repositoryAdapter
	 *            the repository adapter
	 */
	public ElementListNotificator(Set<EObject> listenedObjects, ElementListAdapter listAdapter,
			RepositoryAdapter repositoryAdapter) {
		this.listenedObjects = listenedObjects;
		this.listeningHandlers = new LinkedHashSet<RepositoryObjectHandler>();
		this.elementAdapter = listAdapter;
		listAdapter.setNotificator(this);

		initialize(listeningHandlers, repositoryAdapter);
	}

	/**
	 * Register itself as an eAdapter for all the listened objects and configure the repository to be notified
	 * by this eAdapter.
	 * 
	 * @param handlersToRegister
	 *            handlers that listens this Notificator.
	 * @param repositoryAdapter
	 *            the repository adapter
	 */
	public void initialize(Set<RepositoryObjectHandler> handlersToRegister,
			RepositoryAdapter repositoryAdapter) {
		// Step 1 : we register the created eAdapter as an adapter of all listened objects
		repositoryAdapter.execute(new IntentCommand() {

			public void execute() {
				for (EObject objectToListen : listenedObjects) {
					objectToListen.eAdapters().add(elementAdapter);
				}
			}
		});

		// Step 2 : we add the listening Handlers to the listeningHandlers list.
		for (RepositoryObjectHandler listeningHandler : handlersToRegister) {
			addRepositoryObjectHandler(listeningHandler);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator#stop()
	 */
	public void stop() {
		for (EObject objectToListen : listenedObjects) {
			try {
				objectToListen.eAdapters().remove(elementAdapter);
			} catch (NullPointerException npe) {
				// If the view has already been close, nothing to do.
			} catch (IllegalStateException ise) {
				// If the view has already been close, nothing to do.
			}
			listeningHandlers.clear();
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator#addRepositoryObjectHandler(org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryObjectHandler)
	 */
	public void addRepositoryObjectHandler(RepositoryObjectHandler listener) {
		listeningHandlers.add(listener);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator#removeRepositoryObjectHandler(org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryObjectHandler)
	 */
	public void removeRepositoryObjectHandler(RepositoryObjectHandler listener) {
		listeningHandlers.remove(listener);
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator#notifyHandlers(org.eclipse.mylyn.docs.intent.collab.handlers.notification.RepositoryChangeNotification)
	 */
	public void notifyHandlers(RepositoryChangeNotification changeNotification) {
		for (RepositoryObjectHandler handler : listeningHandlers) {
			handler.handleChangeNotification(changeNotification);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator#addObjectsToListen(java.util.List)
	 */
	public void addObjectsToListen(List<EObject> objectsToHandle) {
		for (EObject objectToListen : objectsToHandle) {
			if (!listenedObjects.contains(objectToListen)) {
				objectToListen.eAdapters().add(elementAdapter);
				listenedObjects.add(objectToListen);

			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator#removeObjectsToListen(java.util.List)
	 */
	public void removeObjectsToListen(List<Object> objectsToRemove) {
		for (Object objectToRemove : objectsToRemove) {
			if (objectToRemove instanceof EObject) {
				EObject eObjectToRemove = (EObject)objectToRemove;
				try {
					eObjectToRemove.eAdapters().remove(elementAdapter);
					listenedObjects.remove(eObjectToRemove);
				} catch (NullPointerException npe) {
					// If the view has already been close, nothing to do.
				} catch (IllegalStateException ise) {
					// If the view has already been close, nothing to do.
				}
			}
		}

	}

}
