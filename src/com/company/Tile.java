package com.company;

import java.util.List;

import static com.company.TileState.*;

/**
 * @author Scott Albertine
 */
@SuppressWarnings({"JavaDoc", "FieldNotUsedInToString", "UseOfSystemOutOrSystemErr", "CallToSystemExit", "NumericCastThatLosesPrecision"})
public class Tile {
	public List<Tile> neighbors;
	public int x;
	public int y;

	private TileState state;
	private int num;
	private boolean hasBomb;

	public Tile(int x, int y) {
		this.x = x;
		this.y = y;
		state = BLANK;
	}

	/**
	 * Add a bomb to this tile.
	 *
	 * @return True if the bomb was added successfully, false if there was already a bomb here.
	 */
	public boolean addBomb() {
		if (hasBomb) {
			return false;
		}
		hasBomb = true;
		return true;
	}

	public void calculateCount() {
		if (hasBomb) {
			return;
		}
		num = (int) neighbors.stream().filter(Tile::isBomb).count();
	}

	public void flag() {
		if (!hasBomb) {
			System.out.println("false flag on " + x + ',' + y);
			throw new DeadException();
		}
		state = FLAG;
	}

	public void click() {
		if (hasBomb) {
			System.out.println("You died when you clicked on " + x + ',' + y);
			throw new DeadException();
		}
		state = NUMBER;
		//click all neighboring blank tiles if you're a zero, cause there's no bomb danger
		//TODO: this causes stack overflows on large, sparsely populated maps, handle that.
		if (num == 0) {
			for (Tile neighbor : neighbors) {
				if (neighbor.isBlank()) {
					neighbor.click();
				}
			}
		}
	}

	public int getNumber() {
		return num;
	}

	public boolean isShowingNumber() {
		return state == NUMBER;
	}

	// for convenience of a streaming op
	private boolean isBomb() {
		return hasBomb;
	}

	public boolean isBlank() {
		return state == BLANK;
	}

	public boolean isFlag() {
		return state == FLAG;
	}

	public boolean isBorder() {
		if (state == BLANK) {
			return neighbors.stream().anyMatch(Tile::isShowingNumber);
		}
		return false;
	}

	public List<Tile> getNeighbors() {
		return neighbors;
	}

	@Override
	public String toString() {
		switch (state) {
			case BLANK:
				return " ";
			case FLAG:
				return "F";
			case NUMBER:
				return String.valueOf(num);
		}
		return " ";
	}
}
