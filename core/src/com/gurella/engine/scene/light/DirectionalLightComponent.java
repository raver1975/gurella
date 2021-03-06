package com.gurella.engine.scene.light;

import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.gurella.engine.metatype.MetaTypeDescriptor;

@MetaTypeDescriptor(descriptiveName = "Directional Light")
public class DirectionalLightComponent extends LightComponent<DirectionalLight> {
	@Override
	protected DirectionalLight createLight() {
		DirectionalLight directionalLight = new DirectionalLight();
		directionalLight.direction.set(0, -1, 0);
		return directionalLight;
	}

	public Vector3 getDirection() {
		return light.direction;
	}

	public void setDirection(Vector3 direction) {
		light.direction.set(direction);
	}

	@Override
	public void reset() {
		super.reset();
		light.direction.set(0, -1, 0);
	}

	@Override
	public boolean equalAs(Object other) {
		if (other == this) {
			return true;
		} else if (other instanceof DirectionalLightComponent) {
			DirectionalLight otherLight = ((DirectionalLightComponent) other).light;
			return light.color.equals(otherLight.color) && light.direction.equals(otherLight.direction);
		} else {
			return false;
		}
	}
}
