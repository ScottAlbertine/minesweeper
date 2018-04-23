package com.company;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.company.Utils.pickOne;

/**
 * @author Scott Albertine
 */
@SuppressWarnings({"JavaDoc", "FieldNotUsedInToString", "HardcodedLineSeparator", "NumericCastThatLosesPrecision"})
public class Board {
	private final Tile[][] tiles;
	private final int xSize;
	private final int ySize;
	public int mineCount;

	public Board(int xSize, int ySize, int mineCount) {
		tiles = new Tile[xSize][ySize];
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
			for (int y = 0; y < ySize; y++) {
				tiles[x][y] = new Tile(x, y);
			}
		}
	}

	private void makeNeighborGraph() {
		for (Tile[] row : tiles) {
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
		Arrays.stream(tiles).flatMap(Arrays::stream).forEach(Tile::calculateCount);
	}

	public Tile getRandomTile() {
		return pickOne(pickOne(tiles));
	}

	private Tile getTile(int x, int y) {
		return ((x < 0) || (x >= xSize) || (y < 0) || (y >= ySize)) ? null : tiles[x][y];
	}

	public Tile[][] getTiles() {
		return tiles;
	}

	public int getTilesRemaining() {
		return (int) Arrays.stream(tiles).flatMap(Arrays::stream).filter(Tile::isBlank).count();
	}

	public List<Tile> findBorder() {
		return Arrays.stream(tiles).flatMap(Arrays::stream).filter(Tile::isBorder).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return Arrays.stream(tiles).map(Board::rowToString).collect(Collectors.joining("\n", "", "\n"));
	}

	private static String rowToString(Tile... row) {
		return Arrays.stream(row).map(Tile::toString).collect(Collectors.joining());
	}
}
