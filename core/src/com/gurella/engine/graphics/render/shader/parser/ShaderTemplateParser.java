package com.gurella.engine.graphics.render.shader.parser;

import static com.gurella.engine.graphics.render.shader.parser.ShaderParserBlockType.ifdef;
import static com.gurella.engine.graphics.render.shader.parser.ShaderParserBlockType.ifdefContent;
import static com.gurella.engine.graphics.render.shader.parser.ShaderParserBlockType.include;
import static com.gurella.engine.graphics.render.shader.parser.ShaderParserBlockType.insertPiece;
import static com.gurella.engine.graphics.render.shader.parser.ShaderParserBlockType.multiLineComment;
import static com.gurella.engine.graphics.render.shader.parser.ShaderParserBlockType.none;
import static com.gurella.engine.graphics.render.shader.parser.ShaderParserBlockType.piece;
import static com.gurella.engine.graphics.render.shader.parser.ShaderParserBlockType.pieceContent;
import static com.gurella.engine.graphics.render.shader.parser.ShaderParserBlockType.singleLineComment;
import static com.gurella.engine.graphics.render.shader.parser.ShaderParserBlockType.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.StreamUtils;
import com.gurella.engine.graphics.render.shader.template.ShaderTemplate;
import com.gurella.engine.pool.PoolService;
import com.gurella.engine.utils.Values;

public class ShaderTemplateParser implements Poolable {
	private static final int maxBlockTestChar = 12;
	private static final int dataSize = 1024;

	private static final char[] endTest = "@end".toCharArray();
	private final char[] endTemp = new char[4];

	private static final char[] includeTest = "@include".toCharArray();
	private final char[] includeTemp = new char[8];

	private static final char[] pieceTest = "@piece".toCharArray();
	private final char[] pieceTemp = new char[6];

	private static final char[] insertpieceTest = "@insertpiece".toCharArray();
	private final char[] insertpieceTemp = new char[12];

	private static final char[] ifdefTest = "@ifdef".toCharArray();
	private final char[] ifdefTemp = new char[6];

	private static final char[] multiLineCommentStartTest = "/*".toCharArray();
	private static final char[] singleLineCommentStartTest = "//".toCharArray();
	private final char[] commentStartTemp = new char[2];

	private final char[] data = new char[dataSize];

	private Array<ShaderParserBlock> blockStack = new Array<ShaderParserBlock>();
	private Array<ShaderParserBlock> rootBlocks = new Array<ShaderParserBlock>();
	private Array<ShaderParserBlock> allBlocks = new Array<ShaderParserBlock>();

	private StringBuffer currentText = new StringBuffer();

	private int potencialBlockStart = -1;
	private boolean parenthesisOpened;
	private int numIfdefExpressionParenthesis;

	public ShaderTemplate parse(Reader reader) {
		try {
			return parseSafely(reader);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}

	protected ShaderTemplate parseSafely(Reader reader) throws IOException {
		while (true) {
			int length = reader.read(data);
			if (length < 1) {
				finish();
				return extractShaderTemplate();
			} else {
				parse(data, length);
			}
		}
	}

	private ShaderTemplate extractShaderTemplate() {
		ShaderTemplate shaderTemplate = new ShaderTemplate();
		for (int i = 0, n = rootBlocks.size; i < n; i++) {
			rootBlocks.get(i).initTemplate(shaderTemplate);
		}
		return shaderTemplate;
	}

	protected void finish() {
		if (!areCurrentValuesEmpty(0) && blockStack.size == 0) {
			ShaderParserBlock textSourceBlock = PoolService.obtain(ShaderParserBlock.class);
			textSourceBlock.type = text;
			textSourceBlock.value.append(currentText);
			rootBlocks.add(textSourceBlock);
			allBlocks.add(textSourceBlock);
		}
	}

	private boolean areCurrentValuesEmpty(int end) {
		int length = currentText.length();
		/*if (length == 0) {
			return true;
		}

		for (int i = 0; i < length - end; i++) {
			if (!Values.isWhitespace(currentText.charAt(i))) {
				return false;
			}
		}
		return true;*/
		return length - end < 1;
	}

	public void parse(char[] data, int length) {
		ShaderParserBlock current = getCurrentBlock();
		ShaderParserBlockType type = current == null ? none : current.type;

		for (int i = 0; i < length; i++) {
			char c = data[i];
			currentText.append(c);

			switch (type) {
			case singleLineComment:
				if ('\n' == c || '\r' == c) {
					type = pop(0);
					i--;
				}
				break;
			case multiLineComment:
				if ('/' == c && currentText.charAt(currentText.length() - 2) == '*') {
					type = pop(2);
				}
				break;
			case include:
			case insertPiece:
				if (parenthesisOpened) {
					if (')' == c) {
						type = pop(1);
					}
				} else {
					currentText.setLength(currentText.length() - 1);
					if ('(' == c) {
						parenthesisOpened = true;
					}
				}
				break;
			case ifdef:
				if (parenthesisOpened) {
					if ('(' == c) {
						numIfdefExpressionParenthesis++;
					} else if (')' == c) {
						numIfdefExpressionParenthesis--;
						if (numIfdefExpressionParenthesis == 0) {
							type = push(1, ifdefContent);
						}
					}
				} else {
					currentText.setLength(currentText.length() - 1);
					if ('(' == c) {
						parenthesisOpened = true;
					}
				}
				break;
			case piece:
				if (parenthesisOpened) {
					if (')' == c) {
						type = push(1, pieceContent);
						// type = pop(4);
					}
				} else {
					currentText.setLength(currentText.length() - 1);
					if ('(' == c) {
						parenthesisOpened = true;
					}
				}
				break;
			case ifdefContent:
			case pieceContent:
				if (isBlockClosed(c)) {
					type = pop(4);
					type = pop(0);
				} else {
					type = checkBlockStart(type, c);
				}
				break;
			case none:
			case text:
				type = checkBlockStart(type, c);
				break;
			default:
				break;
			}
		}
	}

	protected boolean isBlockClosed(char c) {
		return potencialBlockStart > -1 && 'd' == c && testLast(endTest, endTemp);
	}

	private ShaderParserBlockType checkBlockStart(ShaderParserBlockType type, char c) {
		switch (c) {
		case '@':
			potencialBlockStart = currentText.length() - 1;
			return type;
		case '/':
			if (testLast(singleLineCommentStartTest, commentStartTemp)) {
				return startBlock(singleLineCommentStartTest, singleLineComment);
			} else {
				return type;
			}
		case '*':
			if (testLast(multiLineCommentStartTest, commentStartTemp)) {
				return startBlock(multiLineCommentStartTest, multiLineComment);
			} else {
				return type;
			}
		default:
			if (currentText.length() - potencialBlockStart > maxBlockTestChar) {
				potencialBlockStart = -1;
				return type;
			} else if (blockStack.size == 0 && testLast(includeTest, includeTemp)) {
				parenthesisOpened = false;
				return startBlock(includeTest, include);
			} else if (blockStack.size == 0 && testLast(pieceTest, pieceTemp)) {
				parenthesisOpened = false;
				return startBlock(pieceTest, piece);
			} else if (testLast(insertpieceTest, insertpieceTemp)) {
				parenthesisOpened = false;
				return startBlock(insertpieceTest, insertPiece);
			} else if (testLast(ifdefTest, ifdefTemp)) {
				parenthesisOpened = false;
				numIfdefExpressionParenthesis = 1;
				return startBlock(ifdefTest, ifdef);
			} else {
				return type;
			}
		}
	}

	private ShaderParserBlockType startBlock(char[] startedType, ShaderParserBlockType blockType) {
		ShaderParserBlock newBlock = PoolService.obtain(ShaderParserBlock.class);
		newBlock.type = blockType;
		int currLen = currentText.length();
		int testLen = startedType.length;
		ShaderParserBlock current = getCurrentBlock();

		if (currLen == testLen) {
			if (current != null) {
				pop(0);
				current = getCurrentBlock();
			}
		} else if (!areCurrentValuesEmpty(testLen)) {
			ShaderParserBlock textSourceBlock = PoolService.obtain(ShaderParserBlock.class);
			textSourceBlock.type = text;
			textSourceBlock.value.append(currentText, 0, currLen - testLen);
			if (current == null) {
				rootBlocks.add(textSourceBlock);
			} else {
				current.children.add(textSourceBlock);
			}
			allBlocks.add(textSourceBlock);
		}

		currentText.setLength(0);
		blockStack.add(newBlock);
		if (current == null) {
			rootBlocks.add(newBlock);
		} else {
			current.children.add(newBlock);
		}
		allBlocks.add(newBlock);

		return newBlock.type;
	}

	private ShaderParserBlock getCurrentBlock() {
		return blockStack.size == 0 ? null : blockStack.peek();
	}

	private ShaderParserBlockType pop(int valuesSub) {
		ShaderParserBlock current = blockStack.peek();
		ShaderParserBlockType type = current.type;

		if (type == pieceContent || type == ifdefContent) {
			ShaderParserBlock textSourceBlock = PoolService.obtain(ShaderParserBlock.class);
			textSourceBlock.type = text;
			textSourceBlock.value.append(currentText, 0, currentText.length() - valuesSub);
			current.children.add(textSourceBlock);
		} else {
			current.value.append(currentText, 0, currentText.length() - valuesSub);
		}

		currentText.setLength(0);
		blockStack.pop();
		potencialBlockStart = -1;

		current = getCurrentBlock();
		return current == null ? none : current.type;
	}

	private ShaderParserBlockType push(int valuesSub, ShaderParserBlockType blockType) {
		ShaderParserBlock newBlock = PoolService.obtain(ShaderParserBlock.class);
		newBlock.type = blockType;
		ShaderParserBlock current = blockStack.peek();
		current.value.append(currentText, 0, currentText.length() - valuesSub);
		currentText.setLength(0);
		blockStack.add(newBlock);
		current.children.add(newBlock);
		potencialBlockStart = -1;
		return newBlock.type;
	}

	private boolean testLast(char[] testVal, char[] temp) {
		int currLen = currentText.length();
		int testLen = testVal.length;
		if (currLen < testLen) {
			return false;
		}

		currentText.getChars(currLen - testLen, currLen, temp, 0);
		return Arrays.equals(testVal, temp);
	}

	@Override
	public void reset() {
		blockStack.clear();
		rootBlocks.clear();
		PoolService.freeAll(allBlocks);

		currentText.setLength(0);

		potencialBlockStart = -1;
		parenthesisOpened = false;
	}

	public static void main(String[] args) {
		String name = "com/gurella/engine/graphics/render/shader/parser/TestParser.glsl";
		InputStream input = ShaderTemplateParser.class.getClassLoader().getResourceAsStream(name);
		ShaderTemplateParser parser = new ShaderTemplateParser();

		try {
			parser.parse(new InputStreamReader(input, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		parser.printBlocks();
		System.out.print("\n\n\n\n\n");
		ShaderTemplate template = parser.extractShaderTemplate();
		System.out.print(template.toString());
		System.out.print("\n\n\n\n\n");
		StringBuilder builder = new StringBuilder();
		template.generate(builder);
		System.out.print(builder);
		parser.reset();
	}

	private void printBlocks() {
		for (ShaderParserBlock shaderParserBlock : rootBlocks) {
			System.out.print(shaderParserBlock.toString());
			System.out.print("\n");
		}
	}
}
