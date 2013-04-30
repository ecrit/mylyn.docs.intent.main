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
package org.eclipse.mylyn.docs.intent.client.indexer.tocmaker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.mylyn.docs.intent.core.document.IntentDocument;
import org.eclipse.mylyn.docs.intent.core.document.IntentSection;
import org.eclipse.mylyn.docs.intent.core.document.IntentStructuredElement;
import org.eclipse.mylyn.docs.intent.core.indexer.INDEX_ENTRY_TYPE;
import org.eclipse.mylyn.docs.intent.core.indexer.IntentIndex;
import org.eclipse.mylyn.docs.intent.core.indexer.IntentIndexEntry;
import org.eclipse.mylyn.docs.intent.core.indexer.IntentIndexerFactory;
import org.eclipse.mylyn.docs.intent.core.query.StructuredElementHelper;

/**
 * Entity used to make the index (table of content) of an Intent document.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 */
public class TocMaker {

	/**
	 * Replace the given index content with the given document's table of contents.
	 * 
	 * @param index
	 *            the index to update
	 * @param document
	 *            the Intent document
	 */
	public void computeIndex(IntentIndex index, IntentDocument document) {

		// Purge Index
		List<EObject> toRemove = new ArrayList<EObject>();
		for (Iterator<EObject> iterator = index.eAllContents(); iterator.hasNext();) {
			EObject eo = iterator.next();
			if (eo instanceof IntentIndexEntry) {
				IntentIndexEntry iie = (IntentIndexEntry)eo;
				if (iie.getReferencedElement() == null || iie.getReferencedElement().eResource() == null) {
					toRemove.add(iie);
				}
			}
		}
		for (EObject o : toRemove) {
			EcoreUtil.remove(o);
		}

		// Step 1: use an existing index entry if any
		IntentIndexEntry documentIndexEntry = null;
		if (index.getEntries().isEmpty()) {
			documentIndexEntry = IntentIndexerFactory.eINSTANCE.createIntentIndexEntry();
			index.getEntries().add(documentIndexEntry);
		} else {
			documentIndexEntry = index.getEntries().get(0);
		}

		// Step 2: compute the title
		String title = StructuredElementHelper.getTitle(document);
		if ((title == null) || (title.length() < 1)) {
			title = "Document";
		}

		// Step 3: setting index entry informations
		documentIndexEntry.setName(title);
		documentIndexEntry.setReferencedElement(document);
		documentIndexEntry.setType(INDEX_ENTRY_TYPE.INTENT_DOCUMENT);

		// Step 4: for each chapter contained in this document
		for (IntentSection chapter : document.getSubSections()) {
			IntentIndexEntry candidateChapterEntry = null;
			// Step 4.1: we use an existing index entry if any
			for (IntentIndexEntry candidate : documentIndexEntry.getSubEntries()) {
				if (((IntentStructuredElement)candidate.getReferencedElement()) != null) {
					String completeLevel = ((IntentStructuredElement)candidate.getReferencedElement())
							.getCompleteLevel();
					if (completeLevel != null && completeLevel.equals(chapter.getCompleteLevel())) {
						candidateChapterEntry = candidate;
					}
				}
			}
			// Step 4.2: and compute the entry for this chapter
			computeEntryForChapter(documentIndexEntry, candidateChapterEntry, chapter);
		}
	}

	/**
	 * Returns the IndexEntry describing the given chapter's table of contents.
	 * 
	 * @param documentIndexEntry
	 *            the {@link IntentIndexEntry} of the document containing the given chapter
	 * @param candidateChapterEntry
	 *            the candidate {@link IntentIndexEntry} to use for building this entry (can be null)
	 * @param chapter
	 *            the chapter to consider
	 * @return the entry corresponding to the given chapter's toc
	 */
	private IntentIndexEntry computeEntryForChapter(IntentIndexEntry documentIndexEntry,
			IntentIndexEntry candidateChapterEntry, IntentSection chapter) {
		// Step 1: use an existing index entry if any
		IntentIndexEntry chapterEntry = candidateChapterEntry;
		if (candidateChapterEntry == null) {
			chapterEntry = IntentIndexerFactory.eINSTANCE.createIntentIndexEntry();
			documentIndexEntry.getSubEntries().add(chapterEntry);
		}

		// Step 2: compute the title
		String title = StructuredElementHelper.getTitle(chapter);
		if ((title == null) || (title.length() < 1)) {
			title = "Untitled Chapter";
		}

		// Step 3: setting index entry informations
		chapterEntry.setName(chapter.getCompleteLevel() + " " + title);
		chapterEntry.setReferencedElement(chapter);
		chapterEntry.setType(INDEX_ENTRY_TYPE.INTENT_CHAPTER);

		// Step 4: for each section contained in this chapter
		for (IntentSection section : chapter.getSubSections()) {
			// Step 4.1: we use an existing index entry if any
			IntentIndexEntry candidateSectionEntry = null;
			for (IntentIndexEntry candidate : chapterEntry.getSubEntries()) {
				if (((IntentStructuredElement)candidate.getReferencedElement()) != null) {
					String completeLevel = ((IntentStructuredElement)candidate.getReferencedElement())
							.getCompleteLevel();
					if (completeLevel != null && completeLevel.equals(section.getCompleteLevel())) {
						candidateSectionEntry = candidate;
					}
				}
			}

			// Step 4.2: and compute the entry for this section
			computeEntryForSection(chapterEntry, candidateSectionEntry, section);
		}
		return chapterEntry;
	}

	/**
	 * Returns the IndexEntry describing the given section's table of contents.
	 * 
	 * @param containerIndexEntry
	 *            the {@link IntentIndexEntry} of the element containing the given section
	 * @param candidateSectionEntry
	 *            the candidate {@link IntentIndexEntry} to use for building this entry (can be null)
	 * @param section
	 *            the section to consider
	 * @return the entry corresponding to the given chapter's toc
	 */
	private IntentIndexEntry computeEntryForSection(IntentIndexEntry containerIndexEntry,
			IntentIndexEntry candidateSectionEntry, IntentSection section) {
		// Step 1: use an existing index entry if any
		IntentIndexEntry sectionEntry = candidateSectionEntry;
		if (candidateSectionEntry == null) {
			sectionEntry = IntentIndexerFactory.eINSTANCE.createIntentIndexEntry();
			containerIndexEntry.getSubEntries().add(sectionEntry);
		}

		// Step 2: compute the title
		String title = StructuredElementHelper.getTitle(section);
		if ((title == null) || (title.length() < 1)) {
			title = "Untitled Section";
		}

		// Step 3: setting index entry informations
		sectionEntry.setName(section.getCompleteLevel() + " " + title);
		sectionEntry.setReferencedElement(section);
		sectionEntry.setType(INDEX_ENTRY_TYPE.INTENT_SECTION);

		// Step 4: for each sub-section contained in this section
		for (IntentSection subSection : section.getSubSections()) {
			// Step 4.1: we use an existing index entry if any
			IntentIndexEntry candidateSubSectionEntry = null;

			for (IntentIndexEntry candidate : sectionEntry.getSubEntries()) {
				IntentStructuredElement referencedElement = (IntentStructuredElement)candidate
						.getReferencedElement();
				if (referencedElement != null
						&& referencedElement.getCompleteLevel().equals(subSection.getCompleteLevel())) {
					candidateSubSectionEntry = candidate;
				}
			}

			// Step 4.2: and compute the entry for this sub-section
			computeEntryForSection(sectionEntry, candidateSubSectionEntry, subSection);
		}
		return sectionEntry;
	}

}
