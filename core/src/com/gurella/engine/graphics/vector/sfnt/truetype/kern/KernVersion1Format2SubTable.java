package com.gurella.engine.graphics.vector.sfnt.truetype.kern;

import com.gurella.engine.graphics.vector.sfnt.SfntDataType;

class KernVersion1Format2SubTable extends KernVersion1SubTable {
	public KernVersion1Format2SubTable(KernVersionTable parentTable, int offset) {
		super(parentTable, offset);
	}

	@Override
	public int getKerning(int leftGlyphId, int rightGlyphId) {
		int leftClassValue = getLeftClassValue(leftGlyphId);
		if (leftClassValue < 1) {
			return 0;
		}

		int rightClassValue = getRightClassValue(rightGlyphId);
		if (rightClassValue < 1) {
			return 0;
		}

		int arrayOffset = readUnsignedShort(KernVersion1Format2Offset.array);
		return readUnsignedShort(arrayOffset + leftClassValue + rightClassValue);
	}

	private int getLeftClassValue(int leftGlyphId) {
		int classTableOffset = readUnsignedShort(KernVersion1Format2Offset.leftClassTable);

		int firstGlyph = readUnsignedShort(classTableOffset);
		if (leftGlyphId < firstGlyph) {
			return 0;
		}

		int glyphIndex = leftGlyphId - firstGlyph;
		int nGlyphs = readUnsignedShort(classTableOffset + KernVersion1Format2ClassOffset.nGlyphs.offset);
		if (glyphIndex >= nGlyphs) {
			return 0;
		}

		return readUnsignedShort(classTableOffset + KernVersion1Format2ClassOffset.values.offset
				+ (glyphIndex * SfntDataType.unsignedShortValue.size));
	}

	private int getRightClassValue(int rightGlyphId) {
		int classTableOffset = readUnsignedShort(KernVersion1Format2Offset.rightClassTable);

		int firstGlyph = readUnsignedShort(classTableOffset);
		if (rightGlyphId < firstGlyph) {
			return 0;
		}

		int glyphIndex = rightGlyphId - firstGlyph;
		int nGlyphs = readUnsignedShort(classTableOffset + KernVersion1Format2ClassOffset.nGlyphs.offset);
		if (glyphIndex >= nGlyphs) {
			return 0;
		}

		return readUnsignedShort(classTableOffset + KernVersion1Format2ClassOffset.values.offset
				+ (glyphIndex * SfntDataType.unsignedShortValue.size));
	}

	private enum KernVersion1Format2Offset implements Offset {
		length(0), format(4), coverage(5), tupleIndex(6), rowWidth(8), leftClassTable(10), rightClassTable(12), array(14);

		private final int offset;

		private KernVersion1Format2Offset(int offset) {
			this.offset = offset;
		}

		@Override
		public int getOffset() {
			return offset;
		}
	}

	private enum KernVersion1Format2ClassOffset implements Offset {
		firstGlyph(0), nGlyphs(2), values(4);

		private final int offset;

		private KernVersion1Format2ClassOffset(int offset) {
			this.offset = offset;
		}

		@Override
		public int getOffset() {
			return offset;
		}
	}
}