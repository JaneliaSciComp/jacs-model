package org.janelia.rendering;

public enum Coordinate {
	X(0),
	Y(1),
	Z(2);
	
	private final int index;

	Coordinate(int indexParam) {
		this.index = indexParam;
	}

	public Coordinate fromIndex(int index) {
		switch (index % 3) {
			case 0: return X;
			case 1: return Y;
			case 2: return Z;
			default:
				return X;
		}
	}

	public int index() {return index;}

	public Coordinate next() {
		return fromIndex(index() + 1);
	}

	public Coordinate previous() {
		return fromIndex(index() -1);
	}

}
