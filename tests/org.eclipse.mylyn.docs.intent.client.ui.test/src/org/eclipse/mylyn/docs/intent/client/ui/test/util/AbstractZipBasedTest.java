/*******************************************************************************
 * Copyright (c) 2011 Obeo.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Obeo - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.docs.intent.client.ui.test.util;

import java.math.BigInteger;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.mylyn.docs.intent.client.ui.ide.builder.IntentNature;
import org.eclipse.mylyn.docs.intent.client.ui.ide.builder.ToggleNatureAction;
import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndex;
import org.eclipse.mylyn.docs.intent.core.compiler.TraceabilityIndexEntry;

/**
 * Tests the Intent demo, part 1: navigation behavior.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public abstract class AbstractZipBasedTest extends AbstractIntentUITest {

	private String location;

	private String projectName;

	private static final String BUNDLE_NAME = "org.eclipse.mylyn.docs.intent.client.ui.test";

	private static final int TIME_TO_WAIT = 300;

	private static final int RECENT_COMPILATION_DELAY = 60000;

	private static final long TIME_OUT_DELAY = 10000;

	/**
	 * Constructor.
	 * 
	 * @param zipLocation
	 *            the files to expand
	 * @param intentProjectName
	 *            the project name
	 */
	public AbstractZipBasedTest(String zipLocation, String intentProjectName) {
		this.location = zipLocation;
		this.projectName = intentProjectName;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		// Step 1 : import the demo projects
		WorkspaceUtils.unzipAllProjects(BUNDLE_NAME, location, new NullProgressMonitor());

		intentProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);

		// workaround hudson issue
		if (waitForNature()) {
			ToggleNatureAction.toggleNature(intentProject);
			waitForAllOperationsInUIThread();
			assertFalse(waitForNature());
		}

		// Step 2 : setting the intent repository
		// and wait its complete initialization
		setUpRepository(intentProject);
		boolean repositoryInitialized = false;
		long startTime = System.currentTimeMillis();
		boolean timeOutDetected = false;
		while (!repositoryInitialized && !timeOutDetected) {
			try {
				Resource resource = repositoryAdapter
						.getResource(IntentLocations.TRACEABILITY_INFOS_INDEX_PATH);
				// We ensure that the compiler did its work less that one minute ago
				repositoryInitialized = resource != null
						&& !resource.getContents().isEmpty()
						&& isRecentTraceabilityIndex((TraceabilityIndex)resource.getContents().iterator()
								.next());
				timeOutDetected = System.currentTimeMillis() - startTime > TIME_OUT_DELAY;
				Thread.sleep(TIME_TO_WAIT);
			} catch (WrappedException e) {
				// Try again
			}
		}

		assertFalse("The Intent clients have not been launched although the project has been imported",
				timeOutDetected);
		registerRepositoryListener();
		repositoryListener.clearPreviousEntries();
		waitForSynchronizer();
	}

	private boolean waitForNature() throws RepositoryConnectionException, CoreException, InterruptedException {
		boolean timeOutDetected = false;
		long startTime = System.currentTimeMillis();
		// while the project does not have the correct nature or is unaccessible, the repository is null
		while (!intentProject.hasNature(IntentNature.NATURE_ID) && !timeOutDetected) {
			timeOutDetected = System.currentTimeMillis() - startTime > TIME_OUT_DELAY;
			Thread.sleep(TIME_TO_WAIT);
		}
		return timeOutDetected;
	}

	/**
	 * Indicates if the given traceability index is recent or is old.
	 * 
	 * @param traceabilityIndex
	 *            the traceability index to test
	 * @return true if the given traceability index has been compiled less than a minute ago, false otherwise
	 */
	private boolean isRecentTraceabilityIndex(TraceabilityIndex traceabilityIndex) {
		if (traceabilityIndex.getEntries().size() > 0) {
			final TraceabilityIndexEntry entry = traceabilityIndex.getEntries().iterator().next();
			BigInteger compilationTime = entry.getCompilationTime();
			return compilationTime.doubleValue() > (System.currentTimeMillis() - RECENT_COMPILATION_DELAY);
		}
		return false;
	}

}
