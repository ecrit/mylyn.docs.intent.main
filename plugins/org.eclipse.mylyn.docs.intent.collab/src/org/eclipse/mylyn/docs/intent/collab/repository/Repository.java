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
package org.eclipse.mylyn.docs.intent.collab.repository;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryClient;
import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryStructurer;

/**
 * Abstract representation of the repository.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 */
public interface Repository {

	/**
	 * Registers the given client as an user of this repository.
	 * 
	 * @param client
	 *            the client to register
	 */
	void register(RepositoryClient client);

	/**
	 * Unregisters the given client ; if no client are registered, close the current session.
	 * 
	 * @param client
	 *            the client to unregister
	 */
	void unregister(RepositoryClient client);

	/**
	 * Returns a session connected to the repository ; if no session is currently in use, create a new
	 * session.
	 * 
	 * @return a session connected to the repository
	 * @throws RepositoryConnectionException
	 *             if a connection to the repository cannot be made.
	 */
	Object getOrCreateSession() throws RepositoryConnectionException;

	/**
	 * Closes the session opened with the getOrCreateSession method.
	 * 
	 * @throws RepositoryConnectionException
	 *             if a connection to the repository cannot be made.
	 */
	void closeSession() throws RepositoryConnectionException;

	/**
	 * Returns the packageRegistry associated to the repository.
	 * 
	 * @return the packageRegistry associated to the repository
	 * @throws RepositoryConnectionException
	 *             if a connection to the repository cannot be made.
	 */
	EPackage.Registry getPackageRegistry() throws RepositoryConnectionException;

	/**
	 * Creates a repository adapter.
	 * 
	 * @return the repository adapter
	 */
	RepositoryAdapter createRepositoryAdapter();

	/**
	 * Sets the structurer.
	 * 
	 * @param structurer
	 *            the Repository structurer
	 */
	void setRepositoryStructurer(RepositoryStructurer structurer);

	/**
	 * Returns the identifier of this repository (e.g. 'myRepository' for a workspace repository,
	 * 'cdo:/myRepository' for a cdo repository...).
	 * 
	 * @return the identifier of this repository (e.g. 'myRepository' for a workspace repository,
	 *         'cdo:/myRepository' for a cdo repository...)
	 */
	String getIdentifier();

	/**
	 * Returns the {@link URI} of this repository.
	 * 
	 * @return the {@link URI} of this repository
	 */
	URI getRepositoryURI();

	/**
	 * Returns the location of the repository.
	 * 
	 * @return the location of the repository
	 */
	String getRepositoryLocation();

}
