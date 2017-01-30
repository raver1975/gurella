package com.gurella.engine.asset2.persister;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.gurella.engine.asset2.Assets;
import com.gurella.engine.asset2.AssetsManager;

public class AssetsPersister {
	private final AssetsManager manager;
	private final ObjectMap<Class<?>, PersisterInfo<?>> persisters = new ObjectMap<Class<?>, PersisterInfo<?>>();

	static {
		// TODO
		// register(Scene.class, true, new JsonObjectPersister<Scene>(Scene.class));
		// register(SceneNode.class, true, new JsonObjectPersister<SceneNode>(SceneNode.class));
		// register(MaterialDescriptor.class, true, new
		// JsonObjectPersister<MaterialDescriptor>(MaterialDescriptor.class));
		// register(ManagedObject.class, true, new JsonObjectPersister<ManagedObject>(ManagedObject.class));
		// register(ApplicationConfig.class, true, new JsonObjectPersister<ApplicationConfig>(ApplicationConfig.class));
		// Class<AssetProperties<?>> propertiesClass = Values.cast(AssetProperties.class);
		// register(propertiesClass, true, new JsonObjectPersister<AssetProperties<?>>(propertiesClass));
	}

	public AssetsPersister(AssetsManager manager) {
		this.manager = manager;
	}

	public <T> void register(Class<T> type, boolean derivable, AssetPersister<T> persister) {
		synchronized (persisters) {
			persisters.put(type, new PersisterInfo<T>(derivable, persister));
		}
	}

	public <T> AssetPersister<T> get(T asset) {
		return get(asset.getClass());
	}

	private <T> AssetPersister<T> get(Class<? extends Object> type) {
		synchronized (persisters) {
			@SuppressWarnings("unchecked")
			PersisterInfo<T> info = (PersisterInfo<T>) persisters.get(type);
			if (info != null) {
				return info.persister;
			}

			for (Entry<Class<?>, PersisterInfo<?>> entry : persisters.entries()) {
				PersisterInfo<?> derivedInfo = entry.value;
				if (derivedInfo.derivable && ClassReflection.isAssignableFrom(entry.key, type)) {
					@SuppressWarnings("unchecked")
					AssetPersister<T> derivedPersister = (AssetPersister<T>) derivedInfo.persister;
					persisters.put(type, info);
					return derivedPersister;
				}
			}
		}

		return null;
	}

	public <T> void persist(FileHandle handle, T asset) {
		AssetPersister<T> persister = get(asset);
		if (persister == null) {
			throw new IllegalArgumentException("Can't find persister for asset type: " + asset.getClass());
		} else {
			persister.persist(manager, handle, asset);
		}
	}

	private static class PersisterInfo<T> {
		private final boolean derivable;
		private final AssetPersister<T> persister;

		PersisterInfo(boolean strict, AssetPersister<T> persister) {
			this.derivable = strict;
			this.persister = persister;
		}
	}
}