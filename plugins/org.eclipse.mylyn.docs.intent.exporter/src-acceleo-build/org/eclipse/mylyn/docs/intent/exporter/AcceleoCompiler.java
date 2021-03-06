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
package org.eclipse.mylyn.docs.intent.exporter;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.acceleo.common.IAcceleoConstants;
import org.eclipse.acceleo.parser.compiler.AbstractAcceleoCompiler;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.BasicMonitor;
import org.eclipse.emf.common.util.Monitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.mylyn.docs.intent.core.document.IntentDocumentPackage;
import org.eclipse.mylyn.docs.intent.core.document.descriptionunit.DescriptionUnitPackage;
import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitPackage;
import org.eclipse.mylyn.docs.intent.markup.markup.MarkupPackage;
import org.eclipse.mylyn.docs.intent.markup.wikigen.WikigenPackage;

/**
 * The Acceleo Stand Alone compiler.
 * 
 * @author <a href="mailto:stephane.begaudeau@obeo.fr">Stephane Begaudeau</a>
 * @since 3.1
 */
public class AcceleoCompiler extends AbstractAcceleoCompiler {

	/**
	 * The entry point of the compilation.
	 * 
	 * @param args
	 *            The arguments used in the compilation: the source folder, the output folder, a boolean
	 *            indicating if we should use binary resource serialization and finally the dependencies of
	 *            the project.
	 */
	public static void main(String[] args) {
		if (args.length < 3) {
			throw new IllegalArgumentException("Missing parameters"); //$NON-NLS-1$
		}
		AcceleoCompiler acceleoCompiler = new AcceleoCompiler();
		acceleoCompiler.setSourceFolder(args[0]);
		acceleoCompiler.setOutputFolder(args[1]);
		acceleoCompiler.setBinaryResource(Boolean.valueOf(args[2]).booleanValue());
		if (args.length == 4 && args[3] != null && !"".equals(args[3])) { //$NON-NLS-1$
			acceleoCompiler.setDependencies(args[3]);
		}
		acceleoCompiler.doCompile(new BasicMonitor());
	}

	/**
	 * Launches the compilation of the mtl files in the generator.
	 * 
	 * @param monitor
	 *            the progress monitor
	 * @see org.eclipse.acceleo.parser.compiler.AbstractAcceleoCompiler#doCompile(org.eclipse.emf.common.util.Monitor)
	 */
	@Override
	public void doCompile(Monitor monitor) {
		super.doCompile(monitor);
	}

	/**
	 * Registers the packages of the metamodels used in the generator.
	 * 
	 * @see org.eclipse.acceleo.parser.compiler.AbstractAcceleoCompiler#registerPackages()
	 */
	@Override
	protected void registerPackages() {
		super.registerPackages();
		/*
		 * If you want to add the other packages used by your generator, for example UML:
		 * org.eclipse.emf.ecore.EPackage.Registry.put(UMLPackage.eNS_URI, UMLPackage.eINSTANCE);
		 */
		org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.put(MarkupPackage.eNS_URI, MarkupPackage.eINSTANCE);
		org.eclipse.emf.ecore.EPackage.Registry.INSTANCE
				.put(WikigenPackage.eNS_URI, WikigenPackage.eINSTANCE);
		org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.put(IntentDocumentPackage.eNS_URI,
				IntentDocumentPackage.eINSTANCE);
		org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.put(ModelingUnitPackage.eNS_URI,
				ModelingUnitPackage.eINSTANCE);
		org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.put(DescriptionUnitPackage.eNS_URI,
				DescriptionUnitPackage.eINSTANCE);
		org.eclipse.emf.ecore.EPackage.Registry.INSTANCE.put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
	}

	/**
	 * Registers the resource factories.
	 * 
	 * @see org.eclipse.acceleo.parser.compiler.AbstractAcceleoCompiler#registerResourceFactories()
	 */
	@Override
	protected void registerResourceFactories() {
		super.registerResourceFactories();
		/*
		 * If you want to add other resource factories, for example if your metamodel uses a specific
		 * serialization:
		 * org.eclipse.emf.ecore.resource.Resource.Factory.Registry.getExtensionToFactoryMap().put
		 * (UMLResource.FILE_EXTENSION, UMLResource.Factory.INSTANCE);
		 */
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.acceleo.parser.compiler.AbstractAcceleoCompiler#computeDependencies(java.util.List,
	 *      java.util.Map)
	 */
	protected void computeDependencies(List<URI> dependenciesURIs, Map<URI, URI> mapURIs) {
		// USED TO FIX COMPILER WITH TYCHO
		Iterator<String> identifiersIt = dependenciesIDs.iterator();
		for (Iterator<File> dependenciesIt = dependencies.iterator(); dependenciesIt.hasNext()
				&& identifiersIt.hasNext();) {
			File requiredFolder = dependenciesIt.next();
			String identifier = identifiersIt.next();
			if (requiredFolder != null && requiredFolder.exists() && requiredFolder.isDirectory()) {
				List<File> emtlFiles = new ArrayList<File>();
				members(emtlFiles, requiredFolder, IAcceleoConstants.EMTL_FILE_EXTENSION);
				for (File emtlFile : emtlFiles) {
					String requiredFolderAbsolutePath = requiredFolder.getAbsolutePath();
					if (requiredFolderAbsolutePath.endsWith("target/classes")) {
						// using tycho
						String[] splited = requiredFolderAbsolutePath.split("\\/");
						StringBuffer buf = new StringBuffer(requiredFolderAbsolutePath.length());
						for (int i = 0; i < splited.length - 3; i++) {
							buf.append(splited[i]);
							buf.append("/");
						}
						requiredFolderAbsolutePath = buf.toString();
						String emtlAbsolutePath = emtlFile.getAbsolutePath();
						URI emtlFileURI = URI.createFileURI(emtlAbsolutePath);
						dependenciesURIs.add(emtlFileURI);
						String emtlModifiedPath = emtlAbsolutePath.replaceAll("target\\/|classes\\/", "");
						IPath relativePath = new Path(emtlModifiedPath.substring(requiredFolderAbsolutePath
								.length()));
						URI relativeURI = URI.createPlatformPluginURI(relativePath.toString(), false);
						mapURIs.put(emtlFileURI, relativeURI);
					} else {
						// normal behavior
						String emtlAbsolutePath = emtlFile.getAbsolutePath();
						URI emtlFileURI = URI.createFileURI(emtlAbsolutePath);
						dependenciesURIs.add(emtlFileURI);
						IPath relativePath = new Path(identifier).append(emtlAbsolutePath
								.substring(requiredFolderAbsolutePath.length()));
						mapURIs.put(emtlFileURI, URI.createPlatformPluginURI(relativePath.toString(), false));
					}
				}
			}
		}
	}
}
