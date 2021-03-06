package com.gurella.engine.scene.tag;

import com.badlogic.gdx.utils.Bits;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.gurella.engine.pool.PoolService;
import com.gurella.engine.scene.Scene;
import com.gurella.engine.scene.SceneNode;
import com.gurella.engine.scene.SceneNodeComponent;
import com.gurella.engine.scene.BuiltinSceneSystem;
import com.gurella.engine.scene.manager.ComponentManager.ComponentFamily;
import com.gurella.engine.subscriptions.scene.ComponentActivityListener;
import com.gurella.engine.subscriptions.scene.tag.TagActivityListener;
import com.gurella.engine.utils.ArrayExt;
import com.gurella.engine.utils.ImmutableArray;
import com.gurella.engine.utils.OrderedIdentitySet;

//TODO EntitySubscription -> TagSubscription
public class TagManager extends BuiltinSceneSystem implements ComponentActivityListener, TagActivityListener {
	private static final ComponentFamily tagComponentFamily = ComponentFamily.fromComponentType(TagComponent.class);

	private final IntMap<OrderedIdentitySet<SceneNode>> nodesByTag = new IntMap<OrderedIdentitySet<SceneNode>>();
	private final IntMap<FamilyNodes> families = new IntMap<FamilyNodes>();

	public TagManager(Scene scene) {
		super(scene);
	}

	@Override
	protected void serviceActivated() {
		scene.componentManager.registerComponentFamily(tagComponentFamily);
	}

	@Override
	protected void serviceDeactivated() {
		scene.componentManager.unregisterComponentFamily(tagComponentFamily);
		for (FamilyNodes familyNodes : families.values()) {
			PoolService.free(familyNodes);
		}
		families.clear();
		nodesByTag.clear();
	}

	@Override
	public void onComponentActivated(SceneNodeComponent component) {
		if (component instanceof TagComponent) {
			TagComponent tagComponent = (TagComponent) component;
			updateFamilies(tagComponent);
			Bits tags = tagComponent._tags;

			for (int tagId = tags.nextSetBit(0); tagId >= 0; tagId = tags.nextSetBit(tagId + 1)) {
				onTagAdded(tagComponent, Tag.valueOf(tagId));
			}
		}
	}

	private void updateFamilies(TagComponent component) {
		for (FamilyNodes familyNodes : families.values()) {
			familyNodes.handle(component);
		}
	}

	@Override
	public void onComponentDeactivated(SceneNodeComponent component) {
		if (component instanceof TagComponent) {
			TagComponent tagComponent = (TagComponent) component;
			removeFromFamilies(tagComponent);
			Bits tags = tagComponent._tags;

			for (int tagId = tags.nextSetBit(0); tagId >= 0; tagId = tags.nextSetBit(tagId + 1)) {
				onTagRemoved(tagComponent, Tag.valueOf(tagId));
			}
		}
	}

	private void removeFromFamilies(TagComponent component) {
		SceneNode node = component.getNode();
		for (FamilyNodes familyNodes : families.values()) {
			familyNodes.remove(node);
		}
	}

	public void registerFamily(TagFamily tagFamily) {
		int familyId = tagFamily.id;
		if (families.containsKey(familyId)) {
			return;
		}

		FamilyNodes familyNodes = PoolService.obtain(FamilyNodes.class);
		familyNodes.family = tagFamily;
		families.put(familyId, familyNodes);

		if (scene == null) {
			return;
		}

		ImmutableArray<TagComponent> components = scene.componentManager.getComponents(tagComponentFamily);
		for (int i = 0; i < components.size(); i++) {
			familyNodes.handle(components.get(i));
		}
	}

	public void unregisterFamily(TagFamily family) {
		FamilyNodes familyNodes = families.remove(family.id);
		if (familyNodes != null) {
			PoolService.free(familyNodes);
		}
	}

	private OrderedIdentitySet<SceneNode> getNodesByTag(int tagId) {
		OrderedIdentitySet<SceneNode> nodes = nodesByTag.get(tagId);

		if (nodes == null) {
			nodes = new OrderedIdentitySet<SceneNode>();
			nodesByTag.put(tagId, nodes);
		}

		return nodes;
	}

	@Override
	public void onTagAdded(TagComponent component, Tag tag) {
		int tagId = tag.id;
		getNodesByTag(tagId).add(component.getNode());
	}

	@Override
	public void onTagRemoved(TagComponent component, Tag tag) {
		int tagId = tag.id;
		nodesByTag.get(tagId).remove(component.getNode());
	}

	public ImmutableArray<SceneNode> getNodes(Tag tag) {
		int tagId = tag.id;
		OrderedIdentitySet<SceneNode> nodes = nodesByTag.get(tagId);
		return nodes == null ? ImmutableArray.<SceneNode> empty() : nodes.orderedItems();
	}

	public boolean belongsToFamily(SceneNode node, TagFamily family) {
		return getNodes(family).contains(node, true);
	}

	public ImmutableArray<SceneNode> getNodes(TagFamily family) {
		FamilyNodes familyNodes = families.get(family.id);
		return familyNodes == null ? ImmutableArray.<SceneNode> empty() : familyNodes.nodes.immutable();
	}

	public SceneNode getSingleNodeByTag(Tag tag) {
		int tagId = tag.id;
		OrderedIdentitySet<SceneNode> nodes = nodesByTag.get(tagId);

		if (nodes == null || nodes.size == 0) {
			return null;
		} else {
			return nodes.get(0);
		}
	}

	private static class FamilyNodes implements Poolable {
		private TagFamily family;
		private final ArrayExt<SceneNode> nodes = new ArrayExt<SceneNode>();

		private void handle(TagComponent component) {
			SceneNode node = component.getNode();
			boolean belongsToFamily = family.matches(component);
			boolean containsNode = nodes.contains(node, true);
			if (belongsToFamily && !containsNode) {
				nodes.add(node);
			} else if (!belongsToFamily && containsNode) {
				nodes.removeValue(node, true);
			}
		}

		private void remove(SceneNode node) {
			nodes.removeValue(node, true);
		}

		@Override
		public void reset() {
			family = null;
			nodes.clear();
		}
	}
}
