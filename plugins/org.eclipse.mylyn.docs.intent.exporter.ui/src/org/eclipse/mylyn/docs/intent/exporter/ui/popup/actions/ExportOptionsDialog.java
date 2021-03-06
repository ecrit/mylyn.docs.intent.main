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
package org.eclipse.mylyn.docs.intent.exporter.ui.popup.actions;

import java.io.File;

import org.eclipse.core.resources.IContainer;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.mylyn.docs.intent.core.document.IntentDocument;
import org.eclipse.mylyn.docs.intent.core.document.IntentStructuredElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog displaying export options.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 */
public class ExportOptionsDialog extends Dialog {

	/**
	 * The dialog width.
	 */
	private static final int SHELL_WIDTH = 800;

	/**
	 * The dialog height.
	 */
	private static final int SHELL_HEIGHT = 300;

	/**
	 * Maximum size for the main export file name.
	 */
	private static final int TEXT_LIMIT = 255;

	/**
	 * Returns the name to use during intent document export.
	 * 
	 * @return the name to use during intent document export
	 */
	private String exportedIntentDocumentName = "Intent Documentation";

	/**
	 * The target folder location.
	 */
	private String targetFolderLocation;

	/**
	 * Description intentDocumentNameText area.
	 */
	private Text intentDocumentNameText;

	/**
	 * Text holding the export location.
	 */
	private Text exportLocationText;

	/**
	 * The {@link IntentStructuredElement} to export.
	 */
	private final IntentStructuredElement intentElement;

	/**
	 * Indicates whether export should show table of contents by default or not.
	 */
	private boolean shouldShowToc;

	/**
	 * Default constructor.
	 * 
	 * @param parent
	 *            the parent shell
	 * @param defaultTargetFolderLocation
	 *            the default location for the target folder
	 * @param intentElement
	 *            the intent element to launch the generation on
	 */
	public ExportOptionsDialog(Shell parent, String defaultTargetFolderLocation,
			IntentStructuredElement intentElement) {
		super(parent);
		targetFolderLocation = defaultTargetFolderLocation;
		this.intentElement = intentElement;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite)super.createDialogArea(parent);

		GridLayout gridLayout = new GridLayout(2, false);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalSpan = 1;

		// Allow to customize export folder
		Group group = new Group(composite, SWT.NONE);
		group.setText("Export folder location");

		Label label = new Label(group, SWT.NONE);
		label.setText("The location in which the HTML files will be stored.");
		new Label(group, SWT.NONE);
		exportLocationText = new Text(group, SWT.NONE);
		exportLocationText.setText(targetFolderLocation);
		exportLocationText.setLayoutData(gridData);

		Button button = new Button(group, SWT.NONE);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				IContainer[] openFolderSelection = WorkspaceResourceDialog.openFolderSelection(getShell(),
						"Select Export folder location", "message", false, null, null);
				if (openFolderSelection.length == 1) {
					exportLocationText.setText(new File(openFolderSelection[0].getLocationURI())
							.getAbsolutePath().toString());
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		group.setLayout(gridLayout);
		group.setLayoutData(gridData);

		// Allow to customize intent document name
		if (intentElement instanceof IntentDocument) {
			Group group2 = new Group(composite, SWT.NONE);
			group2.setText("Exported Document Name");
			Label label2 = new Label(group2, SWT.NONE);
			label2.setText("This name will be used as a title of the exported documentation.");
			new Label(group2, SWT.NONE);
			final GridData anyElementData = new GridData();
			anyElementData.horizontalAlignment = GridData.FILL;
			anyElementData.grabExcessHorizontalSpace = true;

			intentDocumentNameText = new Text(group2, SWT.BORDER);
			intentDocumentNameText.setTextLimit(TEXT_LIMIT);
			intentDocumentNameText.setLayoutData(anyElementData);
			intentDocumentNameText.setText(exportedIntentDocumentName);
			group2.setLayout(gridLayout);
			group2.setLayoutData(gridData);
		}

		shouldShowToc = true;
		Button shouldShowTOCButton = new Button(composite, SWT.CHECK);
		shouldShowTOCButton.setText("Show Table of Content by default");
		shouldShowTOCButton.setSelection(true);
		shouldShowTOCButton.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				shouldShowToc = !shouldShowToc;
			}

			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});
		return composite;
	}

	/**
	 * Returns the name to use during intent document export.
	 * 
	 * @return the name to use during intent document export
	 */
	public String getExportedIntentDocumentName() {
		return exportedIntentDocumentName;
	}

	/**
	 * Returns the target folder location.
	 * 
	 * @return the target folder location
	 */
	public String getTargetFolderLocation() {
		return targetFolderLocation;
	}

	/**
	 * Indicates whether Table of Contents should be shown by default.
	 * 
	 * @return true if the Table of Contents should be shown by default, false otherwise
	 */
	public boolean shouldShowTableOfContent() {
		return shouldShowToc;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		// create OK button and rename the Cancel button with Ignore label
		createButton(parent, IDialogConstants.OK_ID, "&" + IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void okPressed() {
		if (intentDocumentNameText != null) {
			if (intentDocumentNameText.getText() != null) {
				exportedIntentDocumentName = intentDocumentNameText.getText();
			} else {
				exportedIntentDocumentName = "Intent Documentation";
			}
		}
		if (exportLocationText.getText() != null) {
			targetFolderLocation = exportLocationText.getText();
		}
		super.okPressed();
	}

	@Override
	protected void configureShell(Shell newShell) {
		newShell.setMinimumSize(new Point(SHELL_WIDTH, SHELL_HEIGHT));
		super.configureShell(newShell);
		newShell.setText("Export Intent Documentation - as HTML");
	}

}
