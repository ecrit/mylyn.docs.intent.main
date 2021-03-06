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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.mylyn.docs.intent.client.ui.IntentEditorActivator;
import org.eclipse.mylyn.docs.intent.client.ui.ide.builder.IntentNature;
import org.eclipse.mylyn.docs.intent.client.ui.ide.builder.ToggleNatureAction;
import org.eclipse.mylyn.docs.intent.client.ui.preferences.IntentPreferenceConstants;
import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;

/**
 * Tests the Intent demo, part 1: navigation behavior.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public abstract class AbstractZipBasedTest extends AbstractIntentUITest {

	/**
	 * Test plugin ID.
	 */
	private static final String BUNDLE_NAME = "org.eclipse.mylyn.docs.intent.client.ui.test";

	/**
	 * Time to wait before checking again the nature of an {@link org.eclipse.core.resources.IProject}.
	 */
	private static final int TIME_TO_WAIT_FOR_NATURE = 300;

	/**
	 * Timeout after which a project should be considered as not having the Intent nature.
	 */
	private static final long TIME_OUT_NATURE_DELAY = 60000;

	/**
	 * Location of the archive file.
	 */
	private String location;

	/**
	 * Intent project name.
	 */
	private String projectName;

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
		// Disabling preview mechanism for performances improvements
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(IntentEditorActivator.PLUGIN_ID);
		node.putBoolean(IntentPreferenceConstants.SHOW_PREVIEW_PAGE, false);
		// activate logging
		node.putBoolean(IntentPreferenceConstants.ACTIVATE_ADVANCE_LOGGING, true);

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
		registerRepositoryListener();
		repositoryListener.clearPreviousEntries();
		waitForSynchronizer();
	}

	/**
	 * Waits for the intent project to have the Intent nature.
	 * 
	 * @return true if the intent project has the Intent nature, false otherwise
	 * @throws RepositoryConnectionException
	 *             if repository cannot be properly accessed
	 * @throws CoreException
	 *             if issues occur while getting project natures
	 * @throws InterruptedException
	 *             if cannot make the thread sleep
	 */
	private boolean waitForNature() throws RepositoryConnectionException, CoreException, InterruptedException {
		boolean timeOutDetected = false;
		long startTime = System.currentTimeMillis();
		// while the project does not have the correct nature or is unaccessible, the repository is null
		while (!intentProject.hasNature(IntentNature.NATURE_ID) && !timeOutDetected) {
			timeOutDetected = System.currentTimeMillis() - startTime > TIME_OUT_NATURE_DELAY;
			Thread.sleep(TIME_TO_WAIT_FOR_NATURE);
		}
		return timeOutDetected;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(IntentEditorActivator.PLUGIN_ID);
		node.putBoolean(IntentPreferenceConstants.ACTIVATE_ADVANCE_LOGGING, false);
		super.tearDown();
	}

}
