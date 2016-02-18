package com.gurella.engine.scene.bullet;

import static com.gurella.engine.scene.behaviour.BehaviourEvents.onPhysicsSimulationEnd;
import static com.gurella.engine.scene.behaviour.BehaviourEvents.onPhysicsSimulationStart;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.btBroadphaseInterface;
import com.badlogic.gdx.physics.bullet.collision.btCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btCollisionDispatcher;
import com.badlogic.gdx.physics.bullet.collision.btCollisionObjectArray;
import com.badlogic.gdx.physics.bullet.collision.btDbvtBroadphase;
import com.badlogic.gdx.physics.bullet.collision.btDefaultCollisionConfiguration;
import com.badlogic.gdx.physics.bullet.collision.btDispatcher;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.gurella.engine.application.events.ApplicationUpdateSignal.ApplicationUpdateListener;
import com.gurella.engine.application.events.CommonUpdatePriority;
import com.gurella.engine.disposable.DisposablesService;
import com.gurella.engine.scene.Scene;
import com.gurella.engine.scene.SceneListener;
import com.gurella.engine.scene.SceneNodeComponent;
import com.gurella.engine.scene.SceneSystem;
import com.gurella.engine.scene.behaviour.BehaviourComponent;
import com.gurella.engine.scene.event.EventManager;
import com.gurella.engine.utils.ImmutableArray;

//TODO attach listeners on activate
public class BulletPhysicsSystem extends SceneSystem implements SceneListener, ApplicationUpdateListener {
	static {
		Bullet.init();
	}

	private btCollisionConfiguration collisionConfig;
	private btDispatcher dispatcher;
	private btBroadphaseInterface broadphase;
	private btConstraintSolver constraintSolver;

	private btDynamicsWorld dynamicsWorld;

	private CollisionTrackingInternalTickCallback tickCallback;

	private Vector3 gravity = new Vector3(0, -10f, 0);
	private EventManager eventManager;

	public BulletPhysicsSystem() {
		collisionConfig = DisposablesService.add(new btDefaultCollisionConfiguration());
		dispatcher = DisposablesService.add(new btCollisionDispatcher(collisionConfig));
		broadphase = DisposablesService.add(new btDbvtBroadphase());
		constraintSolver = DisposablesService.add(new btSequentialImpulseConstraintSolver());

		dynamicsWorld = DisposablesService
				.add(new btDiscreteDynamicsWorld(dispatcher, broadphase, constraintSolver, collisionConfig));
		dynamicsWorld.setGravity(gravity);

		tickCallback = DisposablesService.add(new CollisionTrackingInternalTickCallback());
		tickCallback.attach(dynamicsWorld, false);
	}

	@Override
	protected void activated() {
		Scene scene = getScene();
		tickCallback.scene = scene;
		eventManager = scene.eventManager;
		ImmutableArray<SceneNodeComponent> components = scene.activeComponents;
		for (int i = 0; i < components.size(); i++) {
			componentActivated(components.get(i));
		}
	}

	@Override
	protected void deactivated() {
		int numCollisionObjects = dynamicsWorld.getNumCollisionObjects();
		btCollisionObjectArray collisionObjectArray = dynamicsWorld.getCollisionObjectArray();

		for (int i = 0; i < numCollisionObjects; i++) {
			dynamicsWorld.removeCollisionObject(collisionObjectArray.at(i));
		}

		tickCallback.clear();
		eventManager = null;
	}

	@Override
	public int getPriority() {
		return CommonUpdatePriority.PHYSICS;
	}

	@Override
	public void update() {
		dispatchSimulationStartEvent();
		dynamicsWorld.stepSimulation(Gdx.graphics.getDeltaTime(), 5, 1f / 60f);
		dispatchSimulationEndEvent();
	}

	private void dispatchSimulationStartEvent() {
		for (BehaviourComponent behaviourComponent : eventManager.getListeners(onPhysicsSimulationStart)) {
			behaviourComponent.onPhysicsSimulationStart(dynamicsWorld);
		}
	}

	private void dispatchSimulationEndEvent() {
		for (BehaviourComponent behaviourComponent : eventManager.getListeners(onPhysicsSimulationEnd)) {
			behaviourComponent.onPhysicsSimulationEnd(dynamicsWorld);
		}
	}

	@Override
	protected void resetted() {
		super.resetted();
		dynamicsWorld.clearForces();
		tickCallback.clear();
	}

	@Override
	public void componentAdded(SceneNodeComponent component) {
	}

	@Override
	public void componentRemoved(SceneNodeComponent component) {
	}

	@Override
	public void componentActivated(SceneNodeComponent component) {
		if (component instanceof BulletPhysicsRigidBodyComponent) {
			BulletPhysicsRigidBodyComponent rigidBodyComponent = (BulletPhysicsRigidBodyComponent) component;
			dynamicsWorld.addRigidBody(rigidBodyComponent.rigidBody);
		}
	}

	@Override
	public void componentDeactivated(SceneNodeComponent component) {
		if (component instanceof BulletPhysicsRigidBodyComponent) {
			BulletPhysicsRigidBodyComponent rigidBodyComponent = (BulletPhysicsRigidBodyComponent) component;
			dynamicsWorld.removeRigidBody(rigidBodyComponent.rigidBody);
		}
	}
}
