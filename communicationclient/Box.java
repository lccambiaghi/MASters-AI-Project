package communicationclient;

public class Box {
	private String _color;
	private char _letter;
	private int _x;
	private int _y;

	public Box(String color, char letter, int x, int y) {
		_color = color;
		_letter = letter;
		_x = x;
		_y = y;
	}

	public String getColor() {
		return _color;
	}

	public char getLetter() {
		return _letter
	}

	public int getXCoordinate() {
		return _x;
	}

	public int getYCoordinate() {
		return _y;
	}

}
