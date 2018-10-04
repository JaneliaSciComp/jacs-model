package org.janelia.model.rendering;

public enum CoordinateAxis {
	X(0),
	Y(1),
	Z(2);
	
	private final int index;

	CoordinateAxis(int indexParam) {
		this.index = indexParam;
	}

	public CoordinateAxis fromIndex(int index) {
		switch (index % 3) {
			case 0: return X;
			case 1: return Y;
			case 2: return Z;
			default:
				return X;
		}
	}

	public int index() {return index;}

	public CoordinateAxis next() {
		return fromIndex(index() + 1);
	}

	public CoordinateAxis previous() {
		return fromIndex(index() -1);
	}

}
