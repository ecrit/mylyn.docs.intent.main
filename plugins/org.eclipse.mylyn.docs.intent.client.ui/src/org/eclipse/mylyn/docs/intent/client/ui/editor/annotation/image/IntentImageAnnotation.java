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
package org.eclipse.mylyn.docs.intent.client.ui.editor.annotation.image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.mylyn.docs.intent.collab.common.repository.IntentRepositoryManager;
import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * An {@link org.eclipse.jface.text.source.Annotation} allowing the
 * {@link org.eclipse.mylyn.docs.intent.client.ui.editor.IntentEditor} to display images corresponding to an
 * image link in pure documentation zones.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 */
public class IntentImageAnnotation extends AbstractIntentImageAnnotation {

	private org.eclipse.mylyn.docs.intent.markup.markup.Image imageLink;

	/**
	 * Default constructor.
	 * 
	 * @param imageLink
	 *            a reference to an image
	 */
	public IntentImageAnnotation(org.eclipse.mylyn.docs.intent.markup.markup.Image imageLink) {
		super();
		this.imageLink = imageLink;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.mylyn.docs.intent.client.ui.editor.annotation.image.AbstractIntentImageAnnotation#doCreateImage()
	 */
	@Override
	protected Image doCreateImage() {
		String imagePath = imageLink.getUrl();
		// TODO enable this live rendering of images once position of markup.Image will be exact
		// return doCreateImageFromURL(imagePath);
		return null;
	}

	private Image doCreateImageFromURL(String imagePath) {
		// Case 1: URL is a web URL
		try {
			InputStream fileInputStream;
			if (imagePath.startsWith("http")) {
				fileInputStream = new URL(imagePath).openStream();
			} else {
				// Case 2: URL is project-relative

				if (imagePath.startsWith("./")) {
					URI uri = imageLink.eResource().getURI();
					if (uri.isPlatformResource()) {
						Repository repository = IntentRepositoryManager.INSTANCE
								.getRepository(uri.segment(1));
						imagePath = repository.getRepositoryLocation() + imagePath.replaceFirst("./", "");
					}
				}
				// Case 3 (default): URL is absolute
				fileInputStream = new FileInputStream(imagePath);

			}
			return new Image(Display.getDefault(), fileInputStream);
		} catch (FileNotFoundException e) {
			// Silent catch: image will not be created
		} catch (RepositoryConnectionException e) {
			// Silent catch: image will not be created
		} catch (CoreException e) {
			// Silent catch: image will not be created
		} catch (MalformedURLException e) {
			// Silent catch: image will not be created
		} catch (IOException e) {
			// Silent catch: image will not be created
		}
		return null;
	}

}
