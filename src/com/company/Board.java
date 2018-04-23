package com.company;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.company.Utils.pickOne;

/**
 * @author Scott Albertine
 */
@SuppressWarnings({"JavaDoc", "FieldNotUsedInToString", "HardcodedLineSeparator"})
public class Board {
	//TODO: this can be an actual 2 dimensional array for speed
	public List<List<Tile>> tiles;
	private final int xSize;
	private final int ySize;
	public int mineCount;

	public Board(int xSize, int ySize, int mineCount) {
		tiles = new ArrayList<>(xSize);
		this.xSize = xSize;
		this.ySize = ySize;
		this.mineCount = mineCount;
		createTiles();
		makeNeighborGraph();
		addBombs();
		calculateTileCounts();
	}

	private void createTiles() {
		for (int x = 0; x < xSize; x++) {
			List<Tile> row = new ArrayList<>(ySize);
			tiles.add(row);
			for (int y = 0; y < ySize; y++) {
				row.add(new Tile(x, y));
			}
		}
	}

	private void makeNeighborGraph() {
		for (List<Tile> row : tiles) {
			for (Tile tile : row) {
				int x = tile.x;
				int y = tile.y;
				tile.neighbors = Stream.of(getTile(x - 1, y - 1),
										   getTile(x - 1, y),
										   getTile(x - 1, y + 1),
										   getTile(x, y - 1),
										   getTile(x, y + 1),
										   getTile(x + 1, y - 1),
										   getTile(x + 1, y),
										   getTile(x + 1, y + 1))
									   .filter(Objects::nonNull)
									   .collect(Collectors.toList());
			}
		}
	}

	private void addBombs() {
		int i = 0;
		while (i < mineCount) {
			if (getRandomTile().addBomb()) {
				i++;
			}
		}
	}

	private void calculateTileCounts() {
		tiles.stream().flatMap(List::stream).forEach(Tile::calculateCount);
	}

	public Tile getRandomTile() {
		return pickOne(pickOne(tiles));
	}

	private Tile getTile(int x, int y) {
		return ((x < 0) || (x >= xSize) || (y < 0) || (y >= ySize)) ? null : tiles.get(x).get(y);
	}

	public List<List<Tile>> getTiles() {
		return tiles;
	}

	public int getTilesRemaining() {
		return (int) tiles.stream().flatMap(List::stream).filter(Tile::isBlank).count();
	}

	public List<Tile> findBorder() {
		return tiles.stream().flatMap(List::stream).filter(Tile::isBorder).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return tiles.stream().map(Board::rowToString).collect(Collectors.joining("\n", "", "\n"));
	}

	private static String rowToString(Collection<Tile> row) {
		return row.stream().map(Tile::toString).collect(Collectors.joining());
	}
}
