/*******************************************************************************
 * Copyright (c) 2006, 2010 Wind River Systems, Inc., IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation - https://bugs.eclipse.org/bugs/show_bug.cgi?id=22712
 *     Anton Leherbauer (Wind River Systems) - [painting] Long lines take too long to display when "Show Whitespace Characters" is enabled - https://bugs.eclipse.org/bugs/show_bug.cgi?id=196116
 *     Anton Leherbauer (Wind River Systems) - [painting] Whitespace characters not drawn when scrolling to right slowly - https://bugs.eclipse.org/bugs/show_bug.cgi?id=206633
 *     Tom Eicher (Avaloq Evolution AG) - block selection mode
 *     Alex Lagarde (Obeo) - used to drawing lines before and after each Modeling Unit in an Intent editor.
 *******************************************************************************/
package org.eclipse.mylyn.docs.intent.client.ui.editor.scanner;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.mylyn.docs.intent.client.ui.editor.configuration.ColorManager;
import org.eclipse.mylyn.docs.intent.client.ui.editor.configuration.IntentColorConstants;
import org.eclipse.mylyn.docs.intent.parser.IntentKeyWords;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.StyledTextContent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

/**
 * A painter for drawing decoration lines before and after each modeling unit.
 * 
 * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
 * @since 3.3
 */
public class ModelingUnitDecorationPainter implements IPainter, PaintListener {

	/**
	 * Maximum number of lines of an element.
	 */
	private static final int MAXIMUM_ELEMENT_LINES = 50;

	/**
	 * Indicates the space between the end of the decoration and the corner of the decorated editor.
	 */
	private static final int RIGHT_DECORATION_PADDING = 30;

	/**
	 * Indicates the space between the beginning of the decoration and the beginning of the decorated element.
	 */
	private static final int LEFT_DECORATION_PADDING = 5;

	/** Indicates whether this painter is active. */
	private boolean fIsActive;

	/** The source viewer this painter is attached to. */
	private ITextViewer fTextViewer;

	/** The viewer's widget. */
	private StyledText fTextWidget;

	/** Tells whether the advanced graphics sub system is available. */
	private final boolean fIsAdvancedGraphicsPresent;

	/**
	 * The color manager to use for this decoration.
	 */
	private ColorManager colorManager;

	/**
	 * Previous drawRequest's x.
	 */
	private int previousX;

	/**
	 * Previous drawRequest's y.
	 */
	private int previousY;

	/**
	 * Previous drawRequest's width.
	 */
	private int previousWidht;

	/**
	 * Previous drawRequest's height.
	 */
	private int previousHeight;

	/**
	 * Creates a new painter for the given text viewer.
	 * 
	 * @param textViewer
	 *            the text viewer the painter should be attached to
	 * @param colorManager
	 *            the color Manager to use for getting colors.
	 */
	public ModelingUnitDecorationPainter(ITextViewer textViewer, ColorManager colorManager) {
		super();
		fTextViewer = textViewer;
		fTextWidget = textViewer.getTextWidget();
		GC gc = new GC(fTextWidget);
		gc.setAdvanced(true);
		fIsAdvancedGraphicsPresent = gc.getAdvanced();
		this.colorManager = colorManager;
		gc.dispose();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.text.IPainter#dispose()
	 */
	public void dispose() {
		fTextViewer = null;
		fTextWidget = null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.text.IPainter#paint(int)
	 */
	public void paint(int reason) {
		IDocument document = fTextViewer.getDocument();
		if (document == null) {
			deactivate(false);
			return;
		}
		if (!fIsActive) {
			fIsActive = true;
			fTextWidget.addPaintListener(this);
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.text.IPainter#deactivate(boolean)
	 */
	public void deactivate(boolean redraw) {
		if (fIsActive) {
			fIsActive = false;
			fTextWidget.removePaintListener(this);
			if (redraw) {
				redrawAll();
			}
		}
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.text.IPainter#setPositionManager(org.eclipse.jface.text.IPaintPositionManager)
	 */
	public void setPositionManager(IPaintPositionManager manager) {
		// no need for a position manager
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
	 */
	public void paintControl(PaintEvent event) {
		if (fTextWidget != null) {
			handleDrawRequest(event.gc, event.x, event.y, event.width, event.height);

			// launch a redrawn to avoid leaving old painted decorations when editor is resized
			if (!isEqualToLastDrawRequest(fTextWidget.getClientArea().x, fTextWidget.getClientArea().y,
					fTextWidget.getClientArea().width, fTextWidget.getClientArea().height)) {
				fTextWidget.redraw(fTextWidget.getClientArea().x, fTextWidget.getClientArea().y,
						fTextWidget.getClientArea().width, fTextWidget.getClientArea().height, false);
			}
		}
	}

	/**
	 * Handles the given draw request by redrawing decoration lines on modeling unit (only if needed).
	 * 
	 * @param gc
	 *            the {@link GC} to use
	 * @param x
	 *            the draw request's x
	 * @param y
	 *            the draw request's y
	 * @param width
	 *            the draw request's width
	 * @param height
	 *            the draw request's height
	 */
	private void handleDrawRequest(GC gc, int x, int y, int width, int height) {
		int startLine = fTextWidget.getLineIndex(y);
		int endLine = fTextWidget.getLineIndex(y + height - 1);
		if (startLine <= endLine && startLine < fTextWidget.getLineCount()) {
			if (fIsAdvancedGraphicsPresent) {
				int alpha = gc.getAlpha();
				gc.setAlpha(100);
				drawLineRange(gc, startLine, endLine, x, width, true);
				gc.setAlpha(alpha);
			} else {
				drawLineRange(gc, startLine, endLine, x, width, false);
			}
		}
	}

	/**
	 * Draw the given line range.
	 * 
	 * @param gc
	 *            the GC
	 * @param startLine
	 *            first line number
	 * @param endLine
	 *            last line number (inclusive)
	 * @param x
	 *            the X-coordinate of the drawing range
	 * @param w
	 *            the width of the drawing range
	 * @param usingAlpha
	 *            indicates if alpha should be used
	 */
	private void drawLineRange(GC gc, int startLine, int endLine, int x, int w, boolean usingAlpha) {
		final int viewPortWidth = fTextWidget.getClientArea().width;
		try {
			for (int line = startLine; line <= endLine; line++) {
				int lineOffset = fTextWidget.getOffsetAtLine(line);
				// line end offset including line delimiter
				int lineEndOffset;
				if (line < fTextWidget.getLineCount() - 1) {
					lineEndOffset = fTextWidget.getOffsetAtLine(line + 1);
				} else {
					lineEndOffset = fTextWidget.getCharCount();
				}
				// line length excluding line delimiter
				int lineLength = lineEndOffset - lineOffset;
				while (lineLength > 0) {
					char c = fTextWidget.getTextRange(lineOffset + lineLength - 1, 1).charAt(0);
					if (c != '\r' && c != '\n') {
						break;
					}
					--lineLength;
				}
				// compute coordinates of last character on line
				Point endOfLine = fTextWidget.getLocationAtOffset(lineOffset + lineLength);
				if (x - endOfLine.x > viewPortWidth) {
					// line is not visible
					continue;
				}
				// Y-coordinate of line
				int y = fTextWidget.getLinePixel(line);
				// compute first visible char offset
				int startOffset;
				try {
					startOffset = fTextWidget.getOffsetAtLocation(new Point(x, y)) - 1;
					if (startOffset - 2 <= lineOffset) {
						startOffset = lineOffset;
					}
				} catch (IllegalArgumentException iae) {
					startOffset = lineOffset;
				}
				// compute last visible char offset
				int endOffset;
				if (x + w >= endOfLine.x) {
					// line end is visible
					endOffset = lineEndOffset;
				} else {
					try {
						endOffset = fTextWidget.getOffsetAtLocation(new Point(x + w - 1, y)) + 1;
						if (endOffset + 2 >= lineEndOffset) {
							endOffset = lineEndOffset;
						}
					} catch (IllegalArgumentException iae) {
						endOffset = lineEndOffset;
					}
				}
				// draw character range
				if (endOffset > startOffset) {
					drawDecoration(gc, startOffset, endOffset, usingAlpha);
				}
			}
		} catch (IllegalArgumentException e) {
			// Silent catch: decoration line will not be painted
		} catch (SWTException e) {
			// Silent catch: decoration line will not be painted
		} catch (StringIndexOutOfBoundsException e) {
			// Silent catch: decoration line will not be painted
		}
	}

	/**
	 * Draw characters of content range.
	 * 
	 * @param gc
	 *            the GC
	 * @param startOffset
	 *            inclusive start index
	 * @param endOffset
	 *            exclusive end index
	 * @param usingAlpha
	 *            indicates if alpha should be used
	 */
	private void drawDecoration(GC gc, int startOffset, int endOffset, boolean usingAlpha) {
		StyledTextContent content = fTextWidget.getContent();

		int muOpeningLineOffset = 0;
		int maxMuLineSize = 0;
		int maxMuLineSizeOffset = 0;
		// We don't consider the first and the last lines
		int beginLine = Math.max(1, content.getLineAtOffset(startOffset) - MAXIMUM_ELEMENT_LINES);
		int endLine = Math.min(content.getLineCount() - 2, content.getLineAtOffset(endOffset)
				+ MAXIMUM_ELEMENT_LINES);

		for (int i = beginLine; i < endLine; i++) {
			String line = content.getLine(i);

			if (line.length() > maxMuLineSize) {
				maxMuLineSize = line.length();
				maxMuLineSizeOffset = content.getOffsetAtLine(i + 1) - 1;
			}
			if (line.contains(IntentKeyWords.MODELING_UNIT_BEGIN)) {
				// If the current line declares the beginning of a modeling unit
				// We get beginning offset of the previous line
				// int beginingOffset = content.getOffsetAtLine(i - 1);
				// We get the offset at end of this line
				// muOpeningLineOffset = beginingOffset + content.getLine(i - 1).length();
				muOpeningLineOffset = content.getOffsetAtLine(i)
						+ line.indexOf(IntentKeyWords.MODELING_UNIT_BEGIN);
			}
			if (line.contains(IntentKeyWords.MODELING_UNIT_END.replace("\n", ""))) {
				if (content.getLineAtOffset(muOpeningLineOffset) != i) {
					// If the current line declares the end of a modeling unit
					// We get beginning offset of the following line
					int beginingOffset = content.getOffsetAtLine(i + 1);

					// We decorate the modeling unit starting at the muOpeningLineOffset and ending at he end
					// offset
					this.drawDecorationRectangle(gc, muOpeningLineOffset,
							beginingOffset + content.getLine(i + 1).length(), maxMuLineSizeOffset, usingAlpha);
				}

			}
		}

	}

	/**
	 * Redraw all of the text widgets visible content.
	 */
	private void redrawAll() {
		fTextWidget.redraw();
	}

	/**
	 * Decorate the element located at the given offsets by drawling a rectangle delimiting this element.
	 * 
	 * @param gc
	 *            the GC
	 * @param beginOffset
	 *            the modeling unit begin offset
	 * @param endOffset
	 *            the modeling unit end offset
	 * @param maxMuLineSizeOffset
	 *            the end offset of the biggest line of the element to decorate (in number of characters) ;
	 *            used to determine the width of the rectangle
	 * @param usingAlpha
	 *            indicates if alpha should be used
	 */
	private void drawDecorationRectangle(GC gc, int beginOffset, int endOffset, int maxMuLineSizeOffset,
			boolean usingAlpha) {
		int horizontalBarMaximum = 0;
		if (fTextWidget.getHorizontalBar() != null) {
			horizontalBarMaximum = fTextWidget.getHorizontalBar().getMaximum();
		}
		int decorationLineLength = Math.max(fTextWidget.getSize().x, horizontalBarMaximum);
		gc.setAlpha(100);
		gc.setForeground(colorManager.getColor(IntentColorConstants.getMuDecorationLineForeground()));
		gc.setBackground(colorManager.getColor(IntentColorConstants.getMuDecorationBackground()));
		Point beginPos = fTextWidget.getLocationAtOffset(beginOffset);
		Point endPos = fTextWidget.getLocationAtOffset(endOffset);
		gc.drawRoundRectangle(beginPos.x - LEFT_DECORATION_PADDING, beginPos.y, decorationLineLength
				- beginPos.x - RIGHT_DECORATION_PADDING, endPos.y - beginPos.y, 10, 10);

	}

	/**
	 * Indicates if the given draw request is equal to the previous one.
	 * 
	 * @param x
	 *            new draw request's x
	 * @param y
	 *            new draw request's y
	 * @param width
	 *            new draw request's width
	 * @param height
	 *            new draw request's height
	 * @return true if the given draw request is equal to the previous one, false otherwise
	 */
	private boolean isEqualToLastDrawRequest(int x, int y, int width, int height) {
		boolean isEqualToLastDrawRequest = previousX == x && previousY == y && previousWidht == width
				&& previousHeight == height;
		this.previousX = x;
		this.previousY = y;
		this.previousWidht = width;
		this.previousHeight = height;
		return isEqualToLastDrawRequest;
	}
}
