package com.gurella.engine.graphics.render.shader.template;

public class MinNode extends EvaluateNode {
	public MinNode(boolean preprocessed, String expression) {
		super(preprocessed, expression);
	}

	@Override
	protected float evaluate(float first, float second) {
		return Math.min(first, second);
	}

	@Override
	protected String getOperatorString() {
		return " min ";
	}
}
