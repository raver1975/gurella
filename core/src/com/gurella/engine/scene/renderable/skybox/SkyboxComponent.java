package com.gurella.engine.scene.renderable.skybox;

import com.badlogic.gdx.graphics.Cubemap;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.CubemapAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Disposable;
import com.gurella.engine.base.model.ModelDescriptor;
import com.gurella.engine.graphics.render.GenericBatch;
import com.gurella.engine.scene.renderable.RenderableComponent;

@ModelDescriptor(descriptiveName = "Skybox")
public class SkyboxComponent extends RenderableComponent implements Disposable {
	Cubemap cubemap;

	private Model boxModel;
	private ModelInstance boxInstance;

	public SkyboxComponent() {

		boxModel = createModel();
		boxInstance = new ModelInstance(boxModel);
	}

	private Model createModel() {
		ModelBuilder modelBuilder = new ModelBuilder();
		Model model = modelBuilder.createBox(1, 1, 1,
				new Material(new CubemapAttribute(CubemapAttribute.EnvironmentMap, cubemap)),
				VertexAttributes.Usage.Position);
		return model;
	}

	@Override
	protected void updateGeometry() {
		// TODO Auto-generated method stub
	}

	@Override
	protected void doRender(GenericBatch batch) {
		batch.render(boxInstance, SkyboxShader.getInstance());
	}

	@Override
	protected void doGetBounds(BoundingBox bounds) {
		// TODO Auto-generated method stub
	}

	@Override
	protected boolean doGetIntersection(Ray ray, Vector3 intersection) {
		return false;
	}

	@Override
	public void dispose() {
		boxModel.dispose();
		cubemap.dispose();
	}
}
