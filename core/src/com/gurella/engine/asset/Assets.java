package com.gurella.engine.asset;

import java.util.StringTokenizer;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectSet;
import com.gurella.engine.utils.Values;

public class Assets {
	private static final String filePathDelimiters = "\\/";
	private static final char unixFilePathDelimiter = '/';
	private static final char windowsFilePathDelimiter = '\\';
	private static final char fileExtensionDelimiter = '.';
	private static final char localFileType = 'l';
	private static final char absoluteFileType = 'a';
	private static final char externalFileType = 'e';
	private static final char classpathFileType = 'c';
	private static final char internalFileType = 'i';
	private static final char fileInfoDelimiter = ':';

	private static final ObjectSet<Class<?>> assetTypes = new ObjectSet<Class<?>>();

	static {
		AssetType[] values = AssetType.values();
		for (int i = 0; i < values.length; i++) {
			Class<?> assetType = values[i].assetType;
			if (assetType != null) {
				assetTypes.add(assetType);
			}
		}
	}

	private Assets() {
	}

	public static boolean isAsset(Object obj) {
		return obj != null && isAssetType(obj.getClass());
	}

	public static boolean isAssetType(Class<?> type) {
		Class<?> temp = type;
		while (temp != null && temp != Object.class) {
			if (assetTypes.contains(temp)) {
				return true;
			}
			temp = temp.getSuperclass();
		}
		return false;
	}

	public static String getFileExtension(final String fileName) {
		if (fileName == null) {
			return "";
		} else {
			int index = fileName.lastIndexOf(fileExtensionDelimiter);
			return index > 0 ? fileName.substring(index + 1) : "";
		}
	}

	public static AssetType getAssetType(final String fileName) {
		String extension = getFileExtension(fileName);
		if (Values.isBlank(extension)) {
			return null;
		}

		extension = extension.toLowerCase();
		AssetType[] values = AssetType.values();
		for (int i = 0, n = values.length; i < n; i++) {
			AssetType type = values[i];
			if (type.isValidExtension(extension)) {
				return type;
			}
		}
		return null;
	}

	public static <T> Class<T> getAssetClass(final String fileName) {
		AssetType type = getAssetType(fileName);
		return type == null ? null : Values.cast(type.assetType);
	}

	public static FileHandle getFileHandle(String path) {
		boolean hasFileTypeInfo = hasFileTypeInfo(path);
		char fileTypeInfo = hasFileTypeInfo ? path.charAt(0) : getDefaultFileTypeInfo(path);
		String pathExtract = hasFileTypeInfo ? path.substring(3) : path;
		Files files = Gdx.files;
		switch (fileTypeInfo) {
		case internalFileType:
			return files.internal(pathExtract);
		case classpathFileType:
			return files.classpath(pathExtract);
		case externalFileType:
			return files.external(pathExtract);
		case absoluteFileType:
			return files.absolute(pathExtract);
		case localFileType:
			return files.local(pathExtract);
		default:
			return files.internal(pathExtract);
		}
	}

	private static char getDefaultFileTypeInfo(String path) {
		if (path.length() < 1) {
			return internalFileType;
		} else {
			char firstChar = path.charAt(0);
			return firstChar == unixFilePathDelimiter || firstChar == windowsFilePathDelimiter ? absoluteFileType
					: internalFileType;
		}
	}

	private static boolean hasFileTypeInfo(String path) {
		if (path.length() < 3) {
			return false;
		}

		if (path.charAt(1) != fileInfoDelimiter || path.charAt(2) != fileInfoDelimiter) {
			return false;
		}

		char typeInfo = path.charAt(0);
		return typeInfo == classpathFileType || typeInfo == internalFileType || typeInfo == externalFileType
				|| typeInfo == absoluteFileType || typeInfo == localFileType;
	}

	public static FileHandle getRelativeFileHandle(FileHandle file, String path) {
		StringTokenizer tokenizer = new StringTokenizer(path, filePathDelimiters);
		FileHandle result = file.parent();
		while (tokenizer.hasMoreElements()) {
			String token = tokenizer.nextToken();
			if (token.equals(".."))
				result = result.parent();
			else {
				result = result.child(token);
			}
		}
		return result;
	}
}
