package com.gurella.studio.editor;

import org.eclipse.jface.resource.ColorDescriptor;
import org.eclipse.jface.resource.DeviceResourceManager;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class GurellaStudioPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "com.gurella.studio.editor"; //$NON-NLS-1$

	private static GurellaStudioPlugin plugin;

	private static DeviceResourceManager resourceManager;
	private static FormToolkit toolkit;

	public GurellaStudioPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		Display display = getDisplay();
		toolkit = new FormToolkit(display);
		resourceManager = new DeviceResourceManager(display);
	}

	private static Display getDisplay() {
		Display display = Display.getCurrent();
		return display == null ? PlatformUI.getWorkbench().getDisplay() : display;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
		toolkit.dispose();
		resourceManager.dispose();
	}

	/**
	 * Returns the shared instance
	 */
	public static GurellaStudioPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 *
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	public static DeviceResourceManager getResourceManager() {
		return resourceManager;
	}

	public static Image createImage(String path) {
		return resourceManager.createImage(getImageDescriptor(path));
	}

	public static Image createImageWithDefault(String path) {
		return resourceManager.createImageWithDefault(getImageDescriptor(path));
	}

	public static Image createImage(ImageDescriptor descriptor) {
		return resourceManager.createImage(descriptor);
	}

	public static Image createImageWithDefault(ImageDescriptor descriptor) {
		return resourceManager.createImageWithDefault(descriptor);
	}

	public static void destroyImage(String path) {
		resourceManager.destroyImage(getImageDescriptor(path));
	}

	public static void destroyImage(ImageDescriptor descriptor) {
		resourceManager.destroyImage(descriptor);
	}

	public static Color createColor(ColorDescriptor descriptor) {
		return resourceManager.createColor(descriptor);
	}

	public static Color createColor(RGB descriptor) {
		return resourceManager.createColor(descriptor);
	}

	public static Color createColor(int red, int green, int blue) {
		return resourceManager.createColor(new RGB(red, green, blue));
	}

	public static Color createColor(int red, int green, int blue, int alpha) {
		return resourceManager.createColor(new RGBAColorDescriptor(red, green, blue, alpha));
	}

	public static void destroyColor(RGB descriptor) {
		resourceManager.destroyColor(descriptor);
	}

	public static void destroyColor(int red, int green, int blue) {
		resourceManager.destroyColor(new RGB(red, green, blue));
	}

	public static void destroyColor(int red, int green, int blue, int alpha) {
		resourceManager.destroyColor(new RGBAColorDescriptor(red, green, blue, alpha));
	}

	public static void destroyColor(ColorDescriptor descriptor) {
		resourceManager.destroyColor(descriptor);
	}

	public static Font createFont(FontDescriptor descriptor) {
		return resourceManager.createFont(descriptor);
	}

	public static void destroyFont(FontDescriptor descriptor) {
		resourceManager.destroyFont(descriptor);
	}

	public static FormToolkit getToolkit() {
		return toolkit;
	}
}