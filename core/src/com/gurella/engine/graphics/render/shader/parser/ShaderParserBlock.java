package com.gurella.engine.graphics.render.shader.parser;

import static com.gurella.engine.graphics.render.shader.parser.ShaderParserBlockType.multiLineComment;
import static com.gurella.engine.graphics.render.shader.parser.ShaderParserBlockType.text;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.gurella.engine.graphics.render.shader.template.IfdefNode;
import com.gurella.engine.graphics.render.shader.template.InsertPieceNode;
import com.gurella.engine.graphics.render.shader.template.PieceNode;
import com.gurella.engine.graphics.render.shader.template.ShaderTemplate;
import com.gurella.engine.graphics.render.shader.template.ShaderTemplateNode;
import com.gurella.engine.graphics.render.shader.template.TextNode;

class ShaderParserBlock implements Poolable {
	ShaderParserBlockType type;
	StringBuffer value = new StringBuffer();
	Array<ShaderParserBlock> children = new Array<ShaderParserBlock>();

	void initTemplate(ShaderTemplateNode node) {
		switch (type) {
		case singleLineComment:
		case multiLineComment:
		case none:
			return;
		case include:
			if (node instanceof ShaderTemplate) {
				((ShaderTemplate) node).addDependency(value.toString());
			}
			return;
		case insertPiece:
			node.addChild(new InsertPieceNode(value.toString()));
			return;
		case text:
			if (value.length() > 0) {
				node.addChild(new TextNode(value.toString()));
			}
			return;
		case piece:
			if (node instanceof ShaderTemplate) {
				PieceNode piece = new PieceNode(value.toString());
				((ShaderTemplate) node).addPiece(piece);
				initTemplateChildren(piece);
			}
			return;
		case pieceContent:
			initTemplateChildren(node);
			return;
		case ifdef:
			IfdefNode ifdef = new IfdefNode(BooleanExpressionParser.parse(value));
			node.addChild(ifdef);
			initTemplateChildren(ifdef);
			return;
		case ifdefContent:
			initTemplateChildren(node);
			return;
		default:
			throw new IllegalArgumentException();
		}
	}

	private void initTemplateChildren(ShaderTemplateNode node) {
		for (int i = 0, n = children.size; i < n; i++) {
			ShaderParserBlock child = children.get(i);
			child.initTemplate(node);
		}
	}

	@Override
	public void reset() {
		value.setLength(0);
		children.clear();
	}

	@Override
	public String toString() {
		return toString(0);
	}

	public String toString(int indent) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			builder.append('\t');
		}

		builder.append(type.name());
		builder.append(": {");
		builder.append(toStringValue());
		builder.append(toStringChildren(indent + 1));

		if (children.size > 0) {
			builder.append("\n");
			for (int i = 0; i < indent; i++) {
				builder.append('\t');
			}
		}

		builder.append("}");
		return builder.toString();
	}

	protected String toStringValue() {
		return type == text || type == multiLineComment ? value.toString().replace("\n", "\\n") : value.toString();
	}

	private String toStringChildren(int indent) {
		if (children.size == 0) {
			return "";
		}

		StringBuilder builder = new StringBuilder();
		for (ShaderParserBlock child : children) {
			builder.append("\n");
			builder.append(child.toString(indent));
		}
		return builder.toString();
	}
}