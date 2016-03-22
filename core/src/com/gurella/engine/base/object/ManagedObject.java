package com.gurella.engine.base.object;

import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.IdentityMap;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.gurella.engine.base.model.PropertyDescriptor;
import com.gurella.engine.base.object.ObjectSubscriptionAttachment.ObjectSubscription;
import com.gurella.engine.disposable.DisposablesService;
import com.gurella.engine.event.EventService;
import com.gurella.engine.pool.PoolService;
import com.gurella.engine.subscriptions.application.ApplicationEventSubscription;
import com.gurella.engine.utils.ImmutableArray;
import com.gurella.engine.utils.OrderedIdentitySet;
import com.gurella.engine.utils.SequenceGenerator;
import com.gurella.engine.utils.Uuid;
import com.gurella.engine.utils.Values;

public abstract class ManagedObject implements Comparable<ManagedObject> {
	transient int instanceId;
	@PropertyDescriptor(property = ManagedObjectUuidProperty.class)
	String uuid;

	@PropertyDescriptor(property = ManagedObjectPrefabProperty.class)
	PrefabReference prefab;

	transient ManagedObjectState state = ManagedObjectState.idle;

	private transient ManagedObject parent;
	private transient final OrderedIdentitySet<ManagedObject> _children = new OrderedIdentitySet<ManagedObject>();
	public transient final ImmutableArray<ManagedObject> children = _children.orderedItems();

	//TODO move attachments to static map so it consumes less memory
	private final transient IdentityMap<Object, Attachment<?>> attachments = new IdentityMap<Object, Attachment<?>>();

	public ManagedObject() {
		instanceId = SequenceGenerator.next();
	}

	public int getInstanceId() {
		return instanceId;
	}

	public String getUuid() {
		return uuid;
	}

	public String ensureUuid() {
		if (uuid == null) {
			uuid = Uuid.randomUuidString();
		}
		return uuid;
	}

	public PrefabReference getPrefab() {
		return prefab;
	}

	//// STATE

	public ManagedObjectState getState() {
		return state;
	}

	public boolean isDisposed() {
		return state == ManagedObjectState.disposed;
	}

	public boolean isActive() {
		return state == ManagedObjectState.active;
	}

	public boolean isInactive() {
		return state == ManagedObjectState.inactive;
	}

	public boolean isInitialized() {
		return state != ManagedObjectState.idle;
	}

	public final void activate() {
		Objects.activate(this);
	}

	void handleActivation() {
		if (!isActivationAllowed()) {
			return;
		}

		if (state == ManagedObjectState.idle) {
			init();
		}

		state = ManagedObjectState.active;
		attachAll();

		if (this instanceof ApplicationEventSubscription) {
			EventService.subscribe(this);
		}

		activated();
		Objects.activated(this);

		for (int i = 0; i < children.size(); i++) {
			ManagedObject child = children.get(i);
			child.handleActivation();
		}
	}

	protected boolean isActivationAllowed() {
		return parent == null || parent.isActive();
	}

	protected void init() {
	}

	protected void activated() {
	}

	public final void deactivate() {
		Objects.deactivate(this);
	}

	void handleDeactivation() {
		for (int i = 0; i < _children.size; i++) {
			ManagedObject child = children.get(i);
			if (child.state == ManagedObjectState.active) {
				child.handleDeactivation();
			}
		}

		state = ManagedObjectState.inactive;

		detachAll();
		deactivated();
		Objects.deactivated(this);

		if (this instanceof ApplicationEventSubscription) {
			EventService.unsubscribe(this);
		}
	}

	protected void deactivated() {
	}

	public void destroy() {
		Objects.destroy(this);
	}

	void handleDestruction() {
		if (state == ManagedObjectState.active) {
			handleDeactivation();
		}

		for (int i = 0; i < _children.size; i++) {
			ManagedObject child = children.get(i);
			child.handleDestruction();
		}

		if (parent != null) {
			parent._children.remove(this);
			parent.childRemoved(this);
			Objects.childRemoved(parent, this);
		}

		state = ManagedObjectState.disposed;
		clear();
		// TODO EventService.removeChannel(instanceId);

		if (this instanceof Poolable) {
			resetValues();
			PoolService.free(this);
		} else {
			DisposablesService.tryDispose(this);
		}
	}

	protected void clear() {
		clearAttachments();
		_children.clear();
		if (prefab != null) {
			prefab.free();
			prefab = null;
		}
	}

	protected void resetValues() {
		if (state != ManagedObjectState.disposed) {
			throw new GdxRuntimeException("Invalid state: " + state);
		}

		instanceId = SequenceGenerator.next();
		uuid = null;
		if (prefab != null) {
			prefab.free();
			prefab = null;
		}
		state = ManagedObjectState.idle;
		parent = null;
	}

	//// HIERARCHY

	public ManagedObject getParent() {
		return parent;
	}

	protected final void setParent(ManagedObject newParent) {
		Objects.reparent(this, newParent);
	}

	void reparent(ManagedObject newParent) {
		if (parent == newParent) {
			return;
		}

		validateReparent(newParent);

		ManagedObject oldParent = parent;
		if (oldParent != null) {
			oldParent._children.remove(this);
			oldParent.childRemoved(this);
			Objects.childRemoved(oldParent, this);
		}

		parent = newParent;
		if (newParent != null) {
			newParent._children.add(this);
			newParent.childAdded(this);
			updateStateByParent();
			Objects.childAdded(newParent, this);
		}

		parentChanged(oldParent, newParent);
		Objects.parentChanged(this, oldParent, newParent);
	}

	private void updateStateByParent() {
		ManagedObjectState parentState = parent.state;
		if (state == ManagedObjectState.active && parentState != ManagedObjectState.active) {
			handleDeactivation();
		} else if (state.ordinal() < ManagedObjectState.active.ordinal() && parentState == ManagedObjectState.active) {
			handleActivation();
		}
	}

	protected void validateReparent(ManagedObject newParent) {
		if (state == ManagedObjectState.disposed) {
			throw new GdxRuntimeException("Object is disposed.");
		}

		if (newParent != null && newParent.state == ManagedObjectState.disposed) {
			throw new GdxRuntimeException("Parent is disposed.");
		}

		// TODO detect cycles
		if (newParent == this) {
			throw new GdxRuntimeException("Parent can't be 'this'.");
		}
	}

	protected void childRemoved(@SuppressWarnings("unused") ManagedObject child) {
		// TODO Auto-generated method stub
	}

	protected void childAdded(@SuppressWarnings("unused") ManagedObject child) {
		// TODO Auto-generated method stub
	}

	@SuppressWarnings("unused")
	protected void parentChanged(ManagedObject oldParent, ManagedObject newParent) {
		// TODO Auto-generated method stub
	}

	//// ATTACHMENTS
	protected void attach(Attachment<?> attachment) {
		Object value = attachment.value;
		if (value == null) {
			throw new GdxRuntimeException("Attachment value must be non null.");
		}

		attachments.put(value, attachment);
		if (isActive()) {
			attachment.attach();
		}
	}

	private void attachAll() {
		for (Attachment<?> attachment : attachments.values()) {
			attachment.attach();
		}
	}
	
	protected void detach(Attachment<?> attachment) {
		detach(attachment.value);
	}

	protected void detach(Object value) {
		Attachment<?> attachment = attachments.remove(value);
		if (attachment != null) {
			if (isActive()) {
				attachment.detach();
			}

			if (attachment instanceof Poolable) {
				PoolService.free(attachment);
			} else {
				DisposablesService.tryDispose(attachment);
			}
		}
	}

	private void detachAll() {
		for (Attachment<?> attachment : attachments.values()) {
			attachment.detach();
		}
	}

	private void clearAttachments() {
		for (Attachment<?> attachment : attachments.values()) {
			if (attachment instanceof Poolable) {
				PoolService.free(attachment);
			} else {
				DisposablesService.tryDispose(attachment);
			}
		}
		attachments.clear();
	}

	protected void subscribe(Object subscriber) {
		if (subscriber == null) {
			throw new NullPointerException("subscriber is null.");
		}

		attach(SubscriptionAttachment.obtain(subscriber));
	}

	protected void unsubscribe(Object subscriber) {
		if (subscriber == null) {
			throw new NullPointerException("subscriber is null.");
		}
		detach(subscriber);
	}

	protected void subscribeTo(ManagedObject object, Object subscriber) {
		if (object == null || subscriber == null) {
			throw new NullPointerException("object or subscriber is null.");
		}
		attach(ObjectSubscriptionAttachment.obtain(object.instanceId, subscriber));
	}

	protected void unsubscribeFrom(ManagedObject object, Object subscriber) {
		if (object == null || subscriber == null) {
			throw new NullPointerException("object or subscriber is null.");
		}
		ObjectSubscription objectSubscription = ObjectSubscription.obtain(object.instanceId, subscriber);
		detach(objectSubscription);
		objectSubscription.free();
	}

	public void subscribeTo(Object subscriber) {
		if (subscriber == null) {
			throw new NullPointerException("subscriber is null.");
		}
		attach(ObjectSubscriptionAttachment.obtain(instanceId, subscriber));
	}

	public void unsubscribeFrom(Object subscriber) {
		if (subscriber == null) {
			throw new NullPointerException("subscriber is null.");
		}
		ObjectSubscription objectSubscription = ObjectSubscription.obtain(instanceId, subscriber);
		detach(objectSubscription);
		objectSubscription.free();
	}

	@Override
	public int hashCode() {
		return instanceId;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (other == null) {
			return false;
		}
		if (getClass() != other.getClass()) {
			return false;
		}

		return instanceId == ((ManagedObject) other).instanceId;
	}

	@Override
	public int compareTo(ManagedObject other) {
		return Values.compare(instanceId, other.instanceId);
	}
}
