package com.gurella.studio.editor.model.extension;

import static com.gurella.engine.utils.Values.cast;
import static com.gurella.studio.editor.model.extension.style.SwtWidgetStyle.extractComboStyle;
import static com.gurella.studio.editor.model.extension.style.SwtWidgetStyle.extractLabelStyle;
import static com.gurella.studio.editor.model.extension.style.SwtWidgetStyle.extractSimpleControlStyle;
import static com.gurella.studio.editor.model.extension.style.SwtWidgetStyle.extractSimpleScrollableStyle;
import static com.gurella.studio.editor.model.extension.style.SwtWidgetStyle.extractSpinnerStyle;
import static com.gurella.studio.editor.model.extension.style.SwtWidgetStyle.extractTableStyle;
import static com.gurella.studio.editor.model.extension.style.SwtWidgetStyle.extractToolBarStyle;
import static com.gurella.studio.editor.model.extension.style.SwtWidgetStyle.extractTreeStyle;
import static com.gurella.studio.editor.model.extension.style.SwtWidgetStyle.getSwtStyle;
import static com.gurella.studio.editor.model.extension.style.SwtWidgetStyle.length;

import java.io.InputStream;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import com.badlogic.gdx.graphics.Color;
import com.gurella.engine.editor.ui.EditorButton;
import com.gurella.engine.editor.ui.EditorButton.ArrowDirection;
import com.gurella.engine.editor.ui.EditorCombo.ComboStyle;
import com.gurella.engine.editor.ui.EditorComposite;
import com.gurella.engine.editor.ui.EditorComposite.CompositeStyle;
import com.gurella.engine.editor.ui.EditorControl;
import com.gurella.engine.editor.ui.EditorDateTime.CalendarStyle;
import com.gurella.engine.editor.ui.EditorDateTime.DateStyle;
import com.gurella.engine.editor.ui.EditorDateTime.DateTimeLength;
import com.gurella.engine.editor.ui.EditorDateTime.DropDownDateStyle;
import com.gurella.engine.editor.ui.EditorDateTime.TimeStyle;
import com.gurella.engine.editor.ui.EditorExpandBar;
import com.gurella.engine.editor.ui.EditorFont;
import com.gurella.engine.editor.ui.EditorGroup.GroupStyle;
import com.gurella.engine.editor.ui.EditorImage;
import com.gurella.engine.editor.ui.EditorLabel;
import com.gurella.engine.editor.ui.EditorLabel.LabelStyle;
import com.gurella.engine.editor.ui.EditorLabel.SeparatorStyle;
import com.gurella.engine.editor.ui.EditorLink.LinkStyle;
import com.gurella.engine.editor.ui.EditorList;
import com.gurella.engine.editor.ui.EditorLogLevel;
import com.gurella.engine.editor.ui.EditorProgressBar.ProgressBarStyle;
import com.gurella.engine.editor.ui.EditorSash.SashStyle;
import com.gurella.engine.editor.ui.EditorScale.ScaleStyle;
import com.gurella.engine.editor.ui.EditorSlider.SliderStyle;
import com.gurella.engine.editor.ui.EditorSpinner.SpinnerStyle;
import com.gurella.engine.editor.ui.EditorTabFolder;
import com.gurella.engine.editor.ui.EditorTable.TableStyle;
import com.gurella.engine.editor.ui.EditorText;
import com.gurella.engine.editor.ui.EditorToolBar.ToolBarStyle;
import com.gurella.engine.editor.ui.EditorTree.TreeStyle;
import com.gurella.engine.editor.ui.EditorUi;
import com.gurella.engine.editor.ui.style.WidgetStyle;
import com.gurella.studio.GurellaStudioPlugin;
import com.gurella.studio.editor.model.extension.style.SwtWidgetStyle;

//TODO import methods from UiUtils
public class SwtEditorUi implements EditorUi {
	public static final SwtEditorUi instance = new SwtEditorUi();

	private SwtEditorUi() {
	}

	@Override
	public void log(EditorLogLevel level, String message) {
		GurellaStudioPlugin.log(level, message);
	}

	@Override
	public void logError(Throwable t, String message) {
		GurellaStudioPlugin.log(t, message);
	}

	@Override
	public EditorImage createImage(InputStream imageStream) {
		return new SwtEditorImage(new Image(getDisplay(), imageStream));
	}

	@Override
	public SwtEditorFont createFont(String name, int height, boolean bold, boolean italic) {
		Font font = createSwtFont(name, height, bold, italic);
		return font == null ? null : new SwtEditorFont(font);
	}

	public Font createSwtFont(String name, int height, boolean bold, boolean italic) {
		return FontDescriptor.createFrom(name, height, getFontStyle(bold, italic)).createFont(getDisplay());
	}

	protected static int getFontStyle(boolean bold, boolean italic) {
		int style = bold ? SWT.BOLD : 0;
		style |= italic ? SWT.ITALIC : SWT.NORMAL;
		return style;
	}

	@Override
	public SwtEditorFont createFont(EditorFont initial, int height, boolean bold, boolean italic) {
		Font oldFont = ((SwtEditorFont) initial).font;
		Font font = createSwtFont(oldFont, height, bold, italic);
		return font == null ? null : new SwtEditorFont(font);
	}

	protected Font createSwtFont(Font oldFont, int height, boolean bold, boolean italic) {
		if (oldFont == null) {
			return null;
		}

		int style = getFontStyle(bold, italic);
		Font font = FontDescriptor.createFrom(oldFont).setHeight(height).setStyle(style).createFont(getDisplay());
		return font;
	}

	@Override
	public SwtEditorFont createFont(EditorControl control, int height, boolean bold, boolean italic) {
		Font oldFont = ((SwtEditorControl<?>) control).widget.getFont();
		Font font = createSwtFont(oldFont, height, bold, italic);
		return font == null ? null : new SwtEditorFont(font);
	}

	public static Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	public static Color toGdxColor(org.eclipse.swt.graphics.Color color) {
		return new Color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f,
				color.getAlpha() / 255f);
	}

	public static SwtEditorComposite createComposite(Composite parent) {
		return new SwtEditorComposite(parent);
	}

	@Override
	public SwtEditorComposite createComposite(EditorComposite parent) {
		return new SwtEditorComposite(cast(parent), SWT.NONE);
	}

	@Override
	public SwtEditorComposite createComposite(EditorComposite parent, CompositeStyle style) {
		return new SwtEditorComposite(cast(parent), extractSimpleScrollableStyle(style));
	}

	@Override
	public SwtEditorGroup createGroup(EditorComposite parent) {
		return new SwtEditorGroup(cast(parent), SWT.NONE);
	}

	@Override
	public SwtEditorGroup createGroup(EditorComposite parent, GroupStyle style) {
		return new SwtEditorGroup(cast(parent), extractSimpleScrollableStyle(style));
	}

	@Override
	public EditorLabel createLabel(EditorComposite parent) {
		return new SwtEditorLabel(cast(parent), SWT.NONE);
	}

	@Override
	public EditorLabel createLabel(EditorComposite parent, LabelStyle style) {
		return new SwtEditorLabel(cast(parent), extractLabelStyle(style));
	}

	@Override
	public EditorLabel createLabel(EditorComposite parent, String text) {
		return new SwtEditorLabel(cast(parent), text, SWT.NONE);
	}

	@Override
	public EditorLabel createLabel(EditorComposite parent, String text, LabelStyle style) {
		return new SwtEditorLabel(cast(parent), text, extractLabelStyle(style));
	}

	@Override
	public SwtEditorLabel createSeparator(EditorComposite parent, boolean vertical) {
		return new SwtEditorLabel(cast(parent), SWT.SEPARATOR | orientation(vertical));
	}

	@Override
	public SwtEditorLabel createSeparator(EditorComposite parent, SeparatorStyle style) {
		return new SwtEditorLabel(cast(parent), SWT.SEPARATOR | orientation(style.vertical) | extractLabelStyle(style));
	}

	protected int orientation(boolean vertical) {
		return vertical ? SWT.VERTICAL : SWT.HORIZONTAL;
	}

	@Override
	public SwtEditorLink createLink(EditorComposite parent) {
		return new SwtEditorLink(cast(parent), SWT.NONE);
	}

	@Override
	public SwtEditorLink createLink(EditorComposite parent, LinkStyle style) {
		return new SwtEditorLink(cast(parent), extractSimpleControlStyle(style));
	}

	@Override
	public SwtEditorLink createLink(EditorComposite parent, String text) {
		SwtEditorLink link = createLink(parent);
		link.setText(text);
		return link;
	}

	@Override
	public SwtEditorLink createLink(EditorComposite parent, String text, LinkStyle style) {
		SwtEditorLink link = createLink(parent, style);
		link.setText(text);
		return link;
	}

	@Override
	public SwtEditorProgressBar createProgressBar(EditorComposite parent, boolean vertical, boolean smooth,
			boolean indeterminate) {
		int style = orientation(vertical);
		if (smooth) {
			style |= SWT.SMOOTH;
		}
		if (indeterminate) {
			style |= SWT.INDETERMINATE;
		}
		return new SwtEditorProgressBar(cast(parent), style);
	}

	@Override
	public SwtEditorProgressBar createProgressBar(EditorComposite parent, ProgressBarStyle style) {
		int result = orientation(style.vertical) | extractSimpleControlStyle(style);
		if (style.smooth) {
			result |= SWT.SMOOTH;
		}
		if (style.indeterminate) {
			result |= SWT.INDETERMINATE;
		}
		return new SwtEditorProgressBar(cast(parent), result);
	}

	@Override
	public SwtEditorSash createSash(EditorComposite parent, boolean vertical, boolean smooth) {
		int style = orientation(vertical);
		if (smooth) {
			style |= SWT.SMOOTH;
		}
		return new SwtEditorSash(cast(parent), style);
	}

	@Override
	public SwtEditorSash createSash(EditorComposite parent, SashStyle style) {
		int result = orientation(style.vertical) | extractSimpleControlStyle(style);
		if (style.smooth) {
			result |= SWT.SMOOTH;
		}
		return new SwtEditorSash(cast(parent), result);
	}

	@Override
	public SwtEditorScale createScale(EditorComposite parent, boolean vertical) {
		return new SwtEditorScale(cast(parent), orientation(vertical));
	}

	@Override
	public SwtEditorScale createScale(EditorComposite parent, ScaleStyle style) {
		return new SwtEditorScale(cast(parent), orientation(style.vertical) | extractSimpleControlStyle(style));
	}

	@Override
	public SwtEditorSlider createSlider(EditorComposite parent, boolean vertical) {
		return new SwtEditorSlider(cast(parent), orientation(vertical));
	}

	@Override
	public SwtEditorSlider createSlider(EditorComposite parent, SliderStyle style) {
		return new SwtEditorSlider(cast(parent), orientation(style.vertical) | extractSimpleControlStyle(style));
	}

	@Override
	public SwtEditorList createList(EditorComposite parent, boolean multi, WidgetStyle<? super EditorList>... styles) {
		int style = multi ? SWT.MULTI : SWT.SINGLE;
		return new SwtEditorList(cast(parent), getSwtStyle(style, styles));
	}

	@Override
	public SwtEditorText createText(EditorComposite parent, WidgetStyle<? super EditorText>... styles) {
		return new SwtEditorText(cast(parent), getSwtStyle(SWT.SINGLE, styles));
	}

	@Override
	public SwtEditorText createTextArea(EditorComposite parent, WidgetStyle<? super EditorText>... styles) {
		return new SwtEditorText(cast(parent), getSwtStyle(SWT.MULTI, styles));
	}

	@Override
	public SwtEditorCombo createCombo(EditorComposite parent) {
		return new SwtEditorCombo(cast(parent), SWT.DROP_DOWN);
	}

	@Override
	public SwtEditorCombo createCombo(EditorComposite parent, ComboStyle style) {
		return new SwtEditorCombo(cast(parent), SWT.DROP_DOWN | extractComboStyle(style));
	}

	@Override
	public SwtEditorDateTime createDate(EditorComposite parent, DateTimeLength length) {
		return new SwtEditorDateTime(cast(parent), SWT.DATE | length(length));
	}

	@Override
	public SwtEditorDateTime createDate(EditorComposite parent, DateStyle style) {
		return new SwtEditorDateTime(cast(parent),
				SWT.DATE | SwtWidgetStyle.length(style.length) | extractSimpleScrollableStyle(style));
	}

	@Override
	public SwtEditorDateTime createDropDownDate(EditorComposite parent) {
		return new SwtEditorDateTime(cast(parent), SWT.DATE | SWT.DROP_DOWN);
	}

	@Override
	public SwtEditorDateTime createDropDownDate(EditorComposite parent, DropDownDateStyle style) {
		return new SwtEditorDateTime(cast(parent), SWT.DATE | SWT.DROP_DOWN | extractSimpleScrollableStyle(style));
	}

	@Override
	public SwtEditorDateTime createTime(EditorComposite parent, DateTimeLength length) {
		return new SwtEditorDateTime(cast(parent), SWT.TIME | length(length));
	}

	@Override
	public SwtEditorDateTime createTime(EditorComposite parent, TimeStyle style) {
		return new SwtEditorDateTime(cast(parent), SWT.TIME | extractSimpleScrollableStyle(style));
	}

	@Override
	public SwtEditorDateTime createCalendar(EditorComposite parent) {
		return new SwtEditorDateTime(cast(parent), SWT.CALENDAR);
	}

	@Override
	public SwtEditorDateTime createCalendar(EditorComposite parent, CalendarStyle style) {
		return new SwtEditorDateTime(cast(parent), SWT.CALENDAR | extractSimpleScrollableStyle(style));
	}

	@Override
	public SwtEditorSpinner createSpinner(EditorComposite parent) {
		return new SwtEditorSpinner(cast(parent), SWT.NONE);
	}

	@Override
	public SwtEditorSpinner createSpinner(EditorComposite parent, SpinnerStyle style) {
		return new SwtEditorSpinner(cast(parent), extractSpinnerStyle(style));
	}

	@Override
	public SwtEditorTabFolder createTabFolder(EditorComposite parent, boolean top,
			WidgetStyle<? super EditorTabFolder>... styles) {
		return new SwtEditorTabFolder(cast(parent), getSwtStyle(styles));
	}

	@Override
	public SwtEditorExpandBar createExpandBar(EditorComposite parent, boolean verticalScroll,
			WidgetStyle<? super EditorExpandBar>... styles) {
		return new SwtEditorExpandBar(cast(parent), getSwtStyle(verticalScroll ? SWT.V_SCROLL : 0, styles));
	}

	@Override
	public SwtEditorButton createCheckBox(EditorComposite parent, WidgetStyle<? super EditorButton>... styles) {
		return new SwtEditorButton(cast(parent), getSwtStyle(SWT.CHECK, styles));
	}

	@Override
	public SwtEditorButton createCheckBox(EditorComposite parent, String text,
			WidgetStyle<? super EditorButton>... styles) {
		return new SwtEditorButton(cast(parent), text, getSwtStyle(SWT.CHECK, styles));
	}

	@Override
	public EditorButton createButton(EditorComposite parent, String text, WidgetStyle<? super EditorButton>... styles) {
		return new SwtEditorButton(cast(parent), text, getSwtStyle(SWT.PUSH, styles));
	}

	@Override
	public EditorButton createButton(EditorComposite parent, WidgetStyle<? super EditorButton>... styles) {
		return new SwtEditorButton(cast(parent), getSwtStyle(SWT.PUSH, styles));
	}

	@Override
	public EditorButton createToggleButton(EditorComposite parent, String text,
			WidgetStyle<? super EditorButton>... styles) {
		return new SwtEditorButton(cast(parent), text, getSwtStyle(SWT.TOGGLE, styles));
	}

	@Override
	public EditorButton createToggleButton(EditorComposite parent, WidgetStyle<? super EditorButton>... styles) {
		return new SwtEditorButton(cast(parent), getSwtStyle(SWT.TOGGLE, styles));
	}

	@Override
	public EditorButton createRadioButton(EditorComposite parent, String text,
			WidgetStyle<? super EditorButton>... styles) {
		return new SwtEditorButton(cast(parent), text, getSwtStyle(SWT.RADIO, styles));
	}

	@Override
	public EditorButton createRadioButton(EditorComposite parent, WidgetStyle<? super EditorButton>... styles) {
		return new SwtEditorButton(cast(parent), getSwtStyle(SWT.RADIO, styles));
	}

	@Override
	public EditorButton createArrowButton(EditorComposite parent, ArrowDirection arrowDirection,
			WidgetStyle<? super EditorButton>... styles) {
		return new SwtEditorButton(cast(parent), getSwtStyle(SWT.ARROW | getArrowStyle(arrowDirection), styles));
	}

	public static int getArrowStyle(ArrowDirection arrowDirection) {
		switch (arrowDirection) {
		case UP:
			return SWT.UP;
		case DOWN:
			return SWT.DOWN;
		case LEFT:
			return SWT.LEFT;
		case RIGHT:
			return SWT.RIGHT;
		default:
			throw new IllegalArgumentException();
		}
	}

	@Override
	public SwtEditorMenu createMenu(EditorControl parent) {
		return new SwtEditorMenu((SwtEditorControl<?>) parent);
	}

	@Override
	public SwtEditorToolBar createToolBar(EditorComposite parent, boolean vertical) {
		return new SwtEditorToolBar(cast(parent), orientation(vertical));
	}

	@Override
	public SwtEditorToolBar createToolBar(EditorComposite parent, boolean vertical, ToolBarStyle style) {
		return new SwtEditorToolBar(cast(parent), orientation(vertical) | extractToolBarStyle(style));
	}

	@Override
	public SwtEditorTable createTable(EditorComposite parent) {
		return new SwtEditorTable(cast(parent), SWT.SINGLE | SWT.FULL_SELECTION);
	}

	@Override
	public SwtEditorTable createTable(EditorComposite parent, TableStyle style) {
		return new SwtEditorTable(cast(parent), extractTableStyle(style));
	}

	@Override
	public SwtEditorTree createTree(EditorComposite parent) {
		return new SwtEditorTree(cast(parent), SWT.SINGLE | SWT.FULL_SELECTION);
	}

	@Override
	public SwtEditorTree createTree(EditorComposite parent, TreeStyle style) {
		return new SwtEditorTree(cast(parent), extractTreeStyle(style));
	}
}
