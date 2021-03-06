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
package org.eclipse.mylyn.docs.intent.client.ui.test.unit.project;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.docs.intent.client.ui.test.util.AbstractIntentUITest;

/**
 * Tests the correct behavior of Intent projects.
 * 
 * @author <a href="mailto:william.piers@obeo.fr">William Piers</a>
 */
public class ProjectTest extends AbstractIntentUITest {

	/**
	 * {@inheritDoc}
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setUpIntentProject("intentProject", "data/unit/documents/editorupdates/changeEditorUpdateTest.intent");
		openIntentEditor();
	}

	/**
	 * Ensures that a project can be closed.
	 */
	public void testDeleteProject() {
		// already managed by tearDown()
	}

	/**
	 * Ensures that a project can be closed.
	 */
	public void testCloseProject() {
		try {
			intentProject.close(new NullProgressMonitor());
		} catch (CoreException e) {
			fail(e.getMessage());
		}
	}

	// TODO fix or remove incorrect test: the repository is not the same after toggling
	// /**
	// * Ensures that the project nature can be activated/deactivated.
	// */
	// public void testToggleNature() {
	// ToggleNatureAction.toggleNature(intentProject);
	//
	// waitForAllOperationsInUIThread();
	//
	// ToggleNatureAction.toggleNature(intentProject);
	//
	// waitForAllOperationsInUIThread();
	//
	// openIntentEditor();
	// }

}
