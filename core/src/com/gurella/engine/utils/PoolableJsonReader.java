package com.gurella.engine.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BaseJsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonValue.ValueType;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.SerializationException;
import com.badlogic.gdx.utils.StreamUtils;
import com.badlogic.gdx.utils.StringBuilder;
import com.gurella.engine.pool.PoolService;

public class PoolableJsonReader implements BaseJsonReader {
	private JsonValuePool valuesPool;
	private char[] tempChars = PoolService.obtainCharArray(1024, 0.5f);

	public PoolableJsonReader() {
		this.valuesPool = new JsonValuePool();
	}

	public PoolableJsonReader(JsonValuePool valuesPool) {
		this.valuesPool = valuesPool;
	}

	public JsonValue parse(String json) {
		char[] data = json.toCharArray();
		return parse(data, 0, data.length);
	}

	public JsonValue parse(StringBuilder json) {
		char[] chars = json.chars;
		return parse(chars, 0, chars.length);
	}

	public JsonValue parse(char[] json) {
		return parse(json, 0, json.length);
	}

	public JsonValue parse(Reader reader) {
		try {
			int offset = 0;
			while (true) {
				int length = reader.read(tempChars, offset, tempChars.length - offset);
				if (length == -1) {
					break;
				}

				if (length == 0) {
					char[] newData = PoolService.obtainCharArray(tempChars.length * 2, 0.5f);
					System.arraycopy(tempChars, 0, newData, 0, tempChars.length);
					PoolService.free(tempChars);
					tempChars = newData;
				} else {
					offset += length;
				}
			}
			return parse(tempChars, 0, offset);
		} catch (IOException ex) {
			throw new SerializationException(ex);
		} finally {
			StreamUtils.closeQuietly(reader);
		}
	}

	@Override
	public JsonValue parse(InputStream input) {
		try {
			return parse(new InputStreamReader(input, "UTF-8"));
		} catch (IOException ex) {
			throw new SerializationException(ex);
		} finally {
			StreamUtils.closeQuietly(input);
		}
	}

	@Override
	public JsonValue parse(FileHandle file) {
		try {
			return parse(file.reader("UTF-8"));
		} catch (Exception ex) {
			throw new SerializationException("Error parsing file: " + file, ex);
		}
	}

	public JsonValue parse(char[] data, int offset, int length) {
		int cs, p = offset, pe = length, eof = pe, top = 0;
		int[] stack = new int[4];

		int s = 0;
		Array<String> names = new Array<String>(8);
		boolean needsUnescape = false, stringIsName = false, stringIsUnquoted = false;
		RuntimeException parseRuntimeEx = null;

		try {

			// line 97 "JsonReader.java"
			{
				cs = json_start;
				top = 0;
			}

			// line 103 "JsonReader.java"
			{
				int _klen;
				int _trans = 0;
				int _acts;
				int _nacts;
				int _keys;
				int _goto_targ = 0;

				_goto: while (true) {
					switch (_goto_targ) {
					case 0:
						if (p == pe) {
							_goto_targ = 4;
							continue _goto;
						}
						if (cs == 0) {
							_goto_targ = 5;
							continue _goto;
						}
						//$FALL-THROUGH$
					case 1:
						_match: do {
							_keys = _json_key_offsets[cs];
							_trans = _json_index_offsets[cs];
							_klen = _json_single_lengths[cs];
							if (_klen > 0) {
								int _lower = _keys;
								int _mid;
								int _upper = _keys + _klen - 1;
								while (true) {
									if (_upper < _lower)
										break;

									_mid = _lower + ((_upper - _lower) >> 1);
									if (data[p] < _json_trans_keys[_mid])
										_upper = _mid - 1;
									else if (data[p] > _json_trans_keys[_mid])
										_lower = _mid + 1;
									else {
										_trans += (_mid - _keys);
										break _match;
									}
								}
								_keys += _klen;
								_trans += _klen;
							}

							_klen = _json_range_lengths[cs];
							if (_klen > 0) {
								int _lower = _keys;
								int _mid;
								int _upper = _keys + (_klen << 1) - 2;
								while (true) {
									if (_upper < _lower)
										break;

									_mid = _lower + (((_upper - _lower) >> 1) & ~1);
									if (data[p] < _json_trans_keys[_mid])
										_upper = _mid - 2;
									else if (data[p] > _json_trans_keys[_mid + 1])
										_lower = _mid + 2;
									else {
										_trans += ((_mid - _keys) >> 1);
										break _match;
									}
								}
								_trans += _klen;
							}
						} while (false);

						_trans = _json_indicies[_trans];
						cs = _json_trans_targs[_trans];

						if (_json_trans_actions[_trans] != 0) {
							_acts = _json_trans_actions[_trans];
							_nacts = _json_actions[_acts++];
							while (_nacts-- > 0) {
								switch (_json_actions[_acts++]) {
								case 0:
								// line 104 "JsonReader.rl"
								{
									stringIsName = true;
								}
									break;
								case 1:
								// line 107 "JsonReader.rl"
								{
									String value = new String(data, s, p - s);
									if (needsUnescape)
										value = unescape(value);
									outer: if (stringIsName) {
										stringIsName = false;
										names.add(value);
									} else {
										String name = names.size > 0 ? names.pop() : null;
										if (stringIsUnquoted) {
											if (value.equals("true")) {
												bool(name, true);
												break outer;
											} else if (value.equals("false")) {
												bool(name, false);
												break outer;
											} else if (value.equals("null")) {
												string(name, null);
												break outer;
											}
											boolean couldBeDouble = false, couldBeLong = true;
											outer2: for (int i = s; i < p; i++) {
												switch (data[i]) {
												case '0':
												case '1':
												case '2':
												case '3':
												case '4':
												case '5':
												case '6':
												case '7':
												case '8':
												case '9':
												case '-':
												case '+':
													break;
												case '.':
												case 'e':
												case 'E':
													couldBeDouble = true;
													couldBeLong = false;
													break;
												default:
													couldBeDouble = false;
													couldBeLong = false;
													break outer2;
												}
											}
											if (couldBeDouble) {
												try {
													number(name, Double.parseDouble(value), value);
													break outer;
												} catch (NumberFormatException ignored) {
												}
											} else if (couldBeLong) {
												try {
													number(name, Long.parseLong(value), value);
													break outer;
												} catch (NumberFormatException ignored) {
												}
											}
										}
										string(name, value);
									}
									stringIsUnquoted = false;
									s = p;
								}
									break;
								case 2:
								// line 181 "JsonReader.rl"
								{
									String name = names.size > 0 ? names.pop() : null;
									startObject(name);
									{
										if (top == stack.length) {
											int[] newStack = new int[stack.length * 2];
											System.arraycopy(stack, 0, newStack, 0, stack.length);
											stack = newStack;
										}
										{
											stack[top++] = cs;
											cs = 5;
											_goto_targ = 2;
											continue _goto;
										}
									}
								}
								case 3:
								// line 187 "JsonReader.rl"
								{
									pop();
									{
										cs = stack[--top];
										_goto_targ = 2;
										continue _goto;
									}
								}
								case 4:
								// line 192 "JsonReader.rl"
								{
									String name = names.size > 0 ? names.pop() : null;
									startArray(name);
									{
										if (top == stack.length) {
											int[] newStack = new int[stack.length * 2];
											System.arraycopy(stack, 0, newStack, 0, stack.length);
											stack = newStack;
										}
										{
											stack[top++] = cs;
											cs = 23;
											_goto_targ = 2;
											continue _goto;
										}
									}
								}
								case 5:
								// line 198 "JsonReader.rl"
								{
									pop();
									{
										cs = stack[--top];
										_goto_targ = 2;
										continue _goto;
									}
								}
								case 6:
								// line 203 "JsonReader.rl"
								{
									int start = p - 1;
									if (data[p++] == '/') {
										while (p != eof && data[p] != '\n')
											p++;
										p--;
									} else {
										while (p + 1 < eof && data[p] != '*' || data[p + 1] != '/') {
											p++;
										}
										p++;
									}
								}
									break;
								case 7:
								// line 216 "JsonReader.rl"
								{
									s = p;
									needsUnescape = false;
									stringIsUnquoted = true;
									if (stringIsName) {
										outer: while (true) {
											switch (data[p]) {
											case '\\':
												needsUnescape = true;
												break;
											case '/':
												if (p + 1 == eof)
													break;
												char c = data[p + 1];
												if (c == '/' || c == '*')
													break outer;
												break;
											case ':':
											case '\r':
											case '\n':
												break outer;
											}
											p++;
											if (p == eof)
												break;
										}
									} else {
										outer: while (true) {
											switch (data[p]) {
											case '\\':
												needsUnescape = true;
												break;
											case '/':
												if (p + 1 == eof)
													break;
												char c = data[p + 1];
												if (c == '/' || c == '*')
													break outer;
												break;
											case '}':
											case ']':
											case ',':
											case '\r':
											case '\n':
												break outer;
											}
											p++;
											if (p == eof)
												break;
										}
									}
									p--;
									while (Character.isWhitespace(data[p])) {
										p--;
									}
								}
									break;
								case 8:
								// line 270 "JsonReader.rl"
								{
									s = ++p;
									needsUnescape = false;
									outer: while (true) {
										switch (data[p]) {
										case '\\':
											needsUnescape = true;
											p++;
											break;
										case '"':
											break outer;
										}
										// if (debug) System.out.println("quotedChar: '" + data[p] + "'");
										p++;
										if (p == eof) {
											break;
										}
									}
									p--;
								}
									break;
								// line 408 "JsonReader.java"
								}
							}
						}
						//$FALL-THROUGH$
					case 2:
						if (cs == 0) {
							_goto_targ = 5;
							continue _goto;
						}
						if (++p != pe) {
							_goto_targ = 1;
							continue _goto;
						}
						//$FALL-THROUGH$
					case 4:
						if (p == eof) {
							int __acts = _json_eof_actions[cs];
							int __nacts = _json_actions[__acts++];
							while (__nacts-- > 0) {
								switch (_json_actions[__acts++]) {
								case 1:
								// line 107 "JsonReader.rl"
								{
									String value = new String(data, s, p - s);
									if (needsUnescape)
										value = unescape(value);
									outer: if (stringIsName) {
										stringIsName = false;
										names.add(value);
									} else {
										String name = names.size > 0 ? names.pop() : null;
										if (stringIsUnquoted) {
											if (value.equals("true")) {
												bool(name, true);
												break outer;
											} else if (value.equals("false")) {
												bool(name, false);
												break outer;
											} else if (value.equals("null")) {
												string(name, null);
												break outer;
											}
											boolean couldBeDouble = false, couldBeLong = true;
											outer2: for (int i = s; i < p; i++) {
												switch (data[i]) {
												case '0':
												case '1':
												case '2':
												case '3':
												case '4':
												case '5':
												case '6':
												case '7':
												case '8':
												case '9':
												case '-':
												case '+':
													break;
												case '.':
												case 'e':
												case 'E':
													couldBeDouble = true;
													couldBeLong = false;
													break;
												default:
													couldBeDouble = false;
													couldBeLong = false;
													break outer2;
												}
											}
											if (couldBeDouble) {
												try {
													number(name, Double.parseDouble(value), value);
													break outer;
												} catch (NumberFormatException ignored) {
												}
											} else if (couldBeLong) {
												try {
													number(name, Long.parseLong(value), value);
													break outer;
												} catch (NumberFormatException ignored) {
												}
											}
										}
										string(name, value);
									}
									stringIsUnquoted = false;
									s = p;
								}
									break;
								// line 506 "JsonReader.java"
								}
							}
						}
						//$FALL-THROUGH$
					case 5:
					}
					break;
				}
			}

			// line 306 "JsonReader.rl"

		} catch (RuntimeException ex) {
			parseRuntimeEx = ex;
		}

		JsonValue root = this.root;
		this.root = null;
		current = null;
		lastChild.clear();

		if (p < pe) {
			int lineNumber = 1;
			for (int i = 0; i < p; i++)
				if (data[i] == '\n')
					lineNumber++;
			int start = Math.max(0, p - 32);
			throw new SerializationException("Error parsing JSON on line " + lineNumber + " near: "
					+ new String(data, start, p - start) + "*ERROR*" + new String(data, p, Math.min(64, pe - p)),
					parseRuntimeEx);
		} else if (elements.size != 0) {
			JsonValue element = elements.peek();
			elements.clear();
			if (element != null && element.isObject())
				throw new SerializationException("Error parsing JSON, unmatched brace.");
			else
				throw new SerializationException("Error parsing JSON, unmatched bracket.");
		} else if (parseRuntimeEx != null) {
			throw new SerializationException("Error parsing JSON: " + new String(data), parseRuntimeEx);
		}
		return root;
	}

	// line 548 "JsonReader.java"
	private static byte[] init__json_actions_0() {
		return new byte[] { 0, 1, 1, 1, 2, 1, 3, 1, 4, 1, 5, 1, 6, 1, 7, 1, 8, 2, 0, 7, 2, 0, 8, 2, 1, 3, 2, 1, 5 };
	}

	private static final byte _json_actions[] = init__json_actions_0();

	private static short[] init__json_key_offsets_0() {
		return new short[] { 0, 0, 11, 13, 14, 16, 25, 31, 37, 39, 50, 57, 64, 73, 74, 83, 85, 87, 96, 98, 100, 101,
				103, 105, 116, 123, 130, 141, 142, 153, 155, 157, 168, 170, 172, 174, 179, 184, 184 };
	}

	private static final short _json_key_offsets[] = init__json_key_offsets_0();

	private static char[] init__json_trans_keys_0() {
		return new char[] { 13, 32, 34, 44, 47, 58, 91, 93, 123, 9, 10, 42, 47, 34, 42, 47, 13, 32, 34, 44, 47, 58, 125,
				9, 10, 13, 32, 47, 58, 9, 10, 13, 32, 47, 58, 9, 10, 42, 47, 13, 32, 34, 44, 47, 58, 91, 93, 123, 9, 10,
				9, 10, 13, 32, 44, 47, 125, 9, 10, 13, 32, 44, 47, 125, 13, 32, 34, 44, 47, 58, 125, 9, 10, 34, 13, 32,
				34, 44, 47, 58, 125, 9, 10, 42, 47, 42, 47, 13, 32, 34, 44, 47, 58, 125, 9, 10, 42, 47, 42, 47, 34, 42,
				47, 42, 47, 13, 32, 34, 44, 47, 58, 91, 93, 123, 9, 10, 9, 10, 13, 32, 44, 47, 93, 9, 10, 13, 32, 44,
				47, 93, 13, 32, 34, 44, 47, 58, 91, 93, 123, 9, 10, 34, 13, 32, 34, 44, 47, 58, 91, 93, 123, 9, 10, 42,
				47, 42, 47, 13, 32, 34, 44, 47, 58, 91, 93, 123, 9, 10, 42, 47, 42, 47, 42, 47, 13, 32, 47, 9, 10, 13,
				32, 47, 9, 10, 0 };
	}

	private static final char _json_trans_keys[] = init__json_trans_keys_0();

	private static byte[] init__json_single_lengths_0() {
		return new byte[] { 0, 9, 2, 1, 2, 7, 4, 4, 2, 9, 7, 7, 7, 1, 7, 2, 2, 7, 2, 2, 1, 2, 2, 9, 7, 7, 9, 1, 9, 2, 2,
				9, 2, 2, 2, 3, 3, 0, 0 };
	}

	private static final byte _json_single_lengths[] = init__json_single_lengths_0();

	private static byte[] init__json_range_lengths_0() {
		return new byte[] { 0, 1, 0, 0, 0, 1, 1, 1, 0, 1, 0, 0, 1, 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 1, 0, 0,
				1, 0, 0, 0, 1, 1, 0, 0 };
	}

	private static final byte _json_range_lengths[] = init__json_range_lengths_0();

	private static short[] init__json_index_offsets_0() {
		return new short[] { 0, 0, 11, 14, 16, 19, 28, 34, 40, 43, 54, 62, 70, 79, 81, 90, 93, 96, 105, 108, 111, 113,
				116, 119, 130, 138, 146, 157, 159, 170, 173, 176, 187, 190, 193, 196, 201, 206, 207 };
	}

	private static final short _json_index_offsets[] = init__json_index_offsets_0();

	private static byte[] init__json_indicies_0() {
		return new byte[] { 1, 1, 2, 3, 4, 3, 5, 3, 6, 1, 0, 7, 7, 3, 8, 3, 9, 9, 3, 11, 11, 12, 13, 14, 3, 15, 11, 10,
				16, 16, 17, 18, 16, 3, 19, 19, 20, 21, 19, 3, 22, 22, 3, 21, 21, 24, 3, 25, 3, 26, 3, 27, 21, 23, 28,
				29, 28, 28, 30, 31, 32, 3, 33, 34, 33, 33, 13, 35, 15, 3, 34, 34, 12, 36, 37, 3, 15, 34, 10, 16, 3, 36,
				36, 12, 3, 38, 3, 3, 36, 10, 39, 39, 3, 40, 40, 3, 13, 13, 12, 3, 41, 3, 15, 13, 10, 42, 42, 3, 43, 43,
				3, 28, 3, 44, 44, 3, 45, 45, 3, 47, 47, 48, 49, 50, 3, 51, 52, 53, 47, 46, 54, 55, 54, 54, 56, 57, 58,
				3, 59, 60, 59, 59, 49, 61, 52, 3, 60, 60, 48, 62, 63, 3, 51, 52, 53, 60, 46, 54, 3, 62, 62, 48, 3, 64,
				3, 51, 3, 53, 62, 46, 65, 65, 3, 66, 66, 3, 49, 49, 48, 3, 67, 3, 51, 52, 53, 49, 46, 68, 68, 3, 69, 69,
				3, 70, 70, 3, 8, 8, 71, 8, 3, 72, 72, 73, 72, 3, 3, 3, 0 };
	}

	private static final byte _json_indicies[] = init__json_indicies_0();

	private static byte[] init__json_trans_targs_0() {
		return new byte[] { 35, 1, 3, 0, 4, 36, 36, 36, 36, 1, 6, 5, 13, 17, 22, 37, 7, 8, 9, 7, 8, 9, 7, 10, 20, 21,
				11, 11, 11, 12, 17, 19, 37, 11, 12, 19, 14, 16, 15, 14, 12, 18, 17, 11, 9, 5, 24, 23, 27, 31, 34, 25,
				38, 25, 25, 26, 31, 33, 38, 25, 26, 33, 28, 30, 29, 28, 26, 32, 31, 25, 23, 2, 36, 2 };
	}

	private static final byte _json_trans_targs[] = init__json_trans_targs_0();

	private static byte[] init__json_trans_actions_0() {
		return new byte[] { 13, 0, 15, 0, 0, 7, 3, 11, 1, 11, 17, 0, 20, 0, 0, 5, 1, 1, 1, 0, 0, 0, 11, 13, 15, 0, 7, 3,
				1, 1, 1, 1, 23, 0, 0, 0, 0, 0, 0, 11, 11, 0, 11, 11, 11, 11, 13, 0, 15, 0, 0, 7, 9, 3, 1, 1, 1, 1, 26,
				0, 0, 0, 0, 0, 0, 11, 11, 0, 11, 11, 11, 1, 0, 0 };
	}

	private static final byte _json_trans_actions[] = init__json_trans_actions_0();

	private static byte[] init__json_eof_actions_0() {
		return new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 1, 0, 0, 0 };
	}

	private static final byte _json_eof_actions[] = init__json_eof_actions_0();

	static final int json_start = 1;
	static final int json_first_final = 35;
	static final int json_error = 0;

	static final int json_en_object = 5;
	static final int json_en_array = 23;
	static final int json_en_main = 1;

	// line 337 "JsonReader.rl"

	private final Array<JsonValue> elements = new Array<JsonValue>(8);
	private final Array<JsonValue> lastChild = new Array<JsonValue>(8);
	private JsonValue root, current;

	private void addChild(String name, JsonValue child) {
		child.setName(name);
		if (current == null) {
			current = child;
			root = child;
		} else if (current.isArray() || current.isObject()) {
			child.parent = current;
			if (current.size == 0)
				current.child = child;
			else {
				JsonValue last = lastChild.pop();
				last.next = child;
				child.prev = last;
			}
			lastChild.add(child);
			current.size++;
		} else
			root = current;
	}

	protected void startObject(String name) {
		JsonValue value = valuesPool.obtain();
		value.setType(ValueType.object);
		if (current != null) {
			addChild(name, value);
		}
		elements.add(value);
		current = value;
	}

	protected void startArray(String name) {
		JsonValue value = valuesPool.obtain();
		value.setType(ValueType.array);
		if (current != null) {
			addChild(name, value);
		}
		elements.add(value);
		current = value;
	}

	protected void pop() {
		root = elements.pop();
		if (current.size > 0) {
			lastChild.pop();
		}
		current = elements.size > 0 ? elements.peek() : null;
	}

	protected void string(String name, String value) {
		JsonValue jsonValue = valuesPool.obtain();
		jsonValue.set(value);
		addChild(name, jsonValue);
	}

	protected void number(String name, double value, String stringValue) {
		JsonValue jsonValue = valuesPool.obtain();
		jsonValue.set(value, stringValue);
		addChild(name, jsonValue);
	}

	protected void number(String name, long value, String stringValue) {
		JsonValue jsonValue = valuesPool.obtain();
		jsonValue.set(value, stringValue);
		addChild(name, jsonValue);
	}

	protected void bool(String name, boolean value) {
		JsonValue jsonValue = valuesPool.obtain();
		jsonValue.set(value);
		addChild(name, jsonValue);
	}

	private static String unescape(String value) {
		int length = value.length();
		StringBuilder buffer = new StringBuilder(length + 16);
		for (int i = 0; i < length;) {
			char c = value.charAt(i++);
			if (c != '\\') {
				buffer.append(c);
				continue;
			}
			if (i == length)
				break;
			c = value.charAt(i++);
			if (c == 'u') {
				buffer.append(Character.toChars(Integer.parseInt(value.substring(i, i + 4), 16)));
				i += 4;
				continue;
			}
			switch (c) {
			case '"':
			case '\\':
			case '/':
				break;
			case 'b':
				c = '\b';
				break;
			case 'f':
				c = '\f';
				break;
			case 'n':
				c = '\n';
				break;
			case 'r':
				c = '\r';
				break;
			case 't':
				c = '\t';
				break;
			default:
				throw new SerializationException("Illegal escaped character: \\" + c);
			}
			buffer.append(c);
		}
		return buffer.toString();
	}

	public void free(JsonValue value) {
		valuesPool.freeHierarchy(value);
	}

	public static class JsonValuePool extends Pool<JsonValue> {
		@Override
		protected JsonValue newObject() {
			return new JsonValue(true);
		}

		@Override
		protected void reset(JsonValue value) {
			value.size = 0;
			value.child = null;
			value.next = null;
			value.prev = null;
			value.parent = null;
			value.name = null;
			value.set(0.0, null);
			value.setType(ValueType.nullValue);
		}

		void freeHierarchy(JsonValue value) {
			for (JsonValue child = value.child; child != null; child = child.next) {
				freeHierarchy(child);
			}
			free(value);
		}
	}
}
