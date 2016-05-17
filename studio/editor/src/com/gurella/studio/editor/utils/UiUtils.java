package com.gurella.studio.editor.utils;

import static com.gurella.studio.GurellaStudioPlugin.createFont;
import static com.gurella.studio.GurellaStudioPlugin.getToolkit;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class UiUtils {
	private static Font TEXT_FONT;

	public static Text createText(Composite parent) {
		FormToolkit toolkit = getToolkit();
		Text text = toolkit.createText(parent, "", SWT.SINGLE);
		toolkit.adapt(text, false, false);
		text.setData(FormToolkit.KEY_DRAW_BORDER, FormToolkit.TEXT_BORDER);
		text.setFont(getTextFont(text));
		return text;
	}

	public static Font getTextFont(Text text) {
		if (TEXT_FONT == null) {
			TEXT_FONT = createFont(FontDescriptor.createFrom(text.getFont()).increaseHeight(-1));
		}
		return TEXT_FONT;
	}

	public static Text createFloatWidget(Composite parent) {
		Text text = createText(parent);
		text.addVerifyListener(e -> verifyFloat(e, text.getText()));
		return text;
	}

	public static void verifyFloat(VerifyEvent e, String oldValue) {
		try {
			String newText = getNewText(e, oldValue);
			if (newText.length() > 0) {
				Float.parseFloat(newText);
			}
		} catch (Exception e2) {
			e.doit = false;
		}
	}

	private static String getNewText(VerifyEvent e, String oldValue) {
		return oldValue.substring(0, e.start) + e.text + oldValue.substring(e.end);
	}

	public static Text createIntegerWidget(Composite parent) {
		Text text = createText(parent);
		text.addVerifyListener(e -> verifyInteger(e, text.getText()));
		return text;
	}

	public static void verifyInteger(VerifyEvent e, String oldValue) {
		try {
			String newText = getNewText(e, oldValue);
			if (newText.length() > 0) {
				Integer.parseInt(newText);
			}
		} catch (Exception e2) {
			e.doit = false;
		}
	}

	public static Text createBigIntegerWidget(Composite parent) {
		Text text = createText(parent);
		text.addVerifyListener(e -> verifyBigInteger(e, text.getText()));
		return text;
	}

	@SuppressWarnings("unused")
	public static void verifyBigInteger(VerifyEvent e, String oldValue) {
		try {
			String newText = getNewText(e, oldValue);
			if (newText.length() > 0) {
				new BigInteger(newText);
			}
		} catch (Exception e2) {
			e.doit = false;
		}
	}

	public static Text createBigDecimalWidget(Composite parent) {
		Text text = createText(parent);
		text.addVerifyListener(e -> verifyBigDecimal(e, text.getText()));
		return text;
	}

	@SuppressWarnings("unused")
	public static void verifyBigDecimal(VerifyEvent e, String oldValue) {
		try {
			String newText = getNewText(e, oldValue);
			if (newText.length() > 0) {
				new BigDecimal(newText);
			}
		} catch (Exception e2) {
			e.doit = false;
		}
	}

	public static Text createLongWidget(Composite parent) {
		Text text = createText(parent);
		text.addVerifyListener(e -> verifyLong(e, text.getText()));
		return text;
	}

	public static void verifyLong(VerifyEvent e, String oldValue) {
		try {
			String newText = getNewText(e, oldValue);
			if (newText.length() > 0) {
				Long.parseLong(newText);
			}
		} catch (Exception e2) {
			e.doit = false;
		}
	}

	public static Text createByteWidget(Composite parent) {
		Text text = createText(parent);
		text.addVerifyListener(e -> verifyByte(e, text.getText()));
		return text;
	}

	public static void verifyByte(VerifyEvent e, String oldValue) {
		try {
			String newText = getNewText(e, oldValue);
			if (newText.length() > 0) {
				Byte.parseByte(newText);
			}
		} catch (Exception e2) {
			e.doit = false;
		}
	}

	public static Text createDoubleWidget(Composite parent) {
		Text text = createText(parent);
		text.addVerifyListener(e -> verifyDouble(e, text.getText()));
		return text;
	}

	public static void verifyDouble(VerifyEvent e, String oldValue) {
		try {
			String newText = getNewText(e, oldValue);
			if (newText.length() > 0) {
				Double.parseDouble(newText);
			}
		} catch (Exception e2) {
			e.doit = false;
		}
	}

	public static Text createShortWidget(Composite parent) {
		Text text = createText(parent);
		text.addVerifyListener(e -> verifyShort(e, text.getText()));
		return text;
	}

	public static void verifyShort(VerifyEvent e, String oldValue) {
		try {
			String newText = getNewText(e, oldValue);
			if (newText.length() > 0) {
				Short.parseShort(newText);
			}
		} catch (Exception e2) {
			e.doit = false;
		}
	}

	public static Text createCharacterWidget(Composite parent) {
		Text text = createText(parent);
		text.addVerifyListener(e -> verifyCharacter(e, text.getText()));
		return text;
	}

	public static void verifyCharacter(VerifyEvent e, String oldValue) {
		String newText = getNewText(e, oldValue);
		if (newText.length() > 1) {
			e.doit = false;
		}
	}

	public static <T extends Enum<T>> ComboViewer createEnumComboViewer(Composite parent, Class<T> enumType) {
		Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));

		ComboViewer comboViewer = new ComboViewer(combo);
		comboViewer.setContentProvider(new ArrayContentProvider());
		comboViewer.setLabelProvider(new LabelProvider());
		comboViewer.setInput(enumType.getEnumConstants());
		return comboViewer;
	}

	public static void paintBordersFor(Composite parent) {
		getToolkit().paintBordersFor(parent);
	}

	public static Composite createComposite(Composite parent) {
		return getToolkit().createComposite(parent);
	}

	public static Composite createComposite(Composite parent, int style) {
		return getToolkit().createComposite(parent, style);
	}

	public static Label createLabel(Composite parent, String text) {
		return getToolkit().createLabel(parent, text);
	}

	public static Label createLabel(Composite parent, String text, int style) {
		return getToolkit().createLabel(parent, text, style);
	}

	public static void adapt(Composite composite) {
		getToolkit().adapt(composite);
	}

	public static void verifyHexRgba(VerifyEvent e, String oldValue) {
		String newText = getNewText(e, oldValue);
		int length = newText.length();

		if (length == 0) {
			return;
		}

		String temp = newText.startsWith("#") ? newText.substring(1) : newText;
		if (temp.length() > 8) {
			e.doit = false;
			return;
		}

		try {
			Integer.parseUnsignedInt(temp, 16);
		} catch (Exception e2) {
			e.doit = false;
		}
	}

	public static void paintSimpleBorder(Composite composite, GC gc) {
		Rectangle area = composite.getClientArea();
		gc.setForeground(composite.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		gc.drawRectangle(0, 0, area.width - 1, area.height - 1);
	}
}