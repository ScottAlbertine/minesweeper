package com.company;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Scott Albertine
 */
@SuppressWarnings({"JavaDoc", "UseOfSystemOutOrSystemErr", "BooleanMethodNameMustStartWithQuestion"})
public class Player {

	private static final long MAX_LAYOUTS = 5000000L;

	private final Board board;

	public Player(Board board) {
		this.board = board;
	}

	public void go() throws InterruptedException {
		//click on a random tile to start
		board.getRandomTile().click();
		while (!board.findBorder().isEmpty()) {
			System.out.println(board);
			//Thread.sleep(100L);
			if (trySureClick()) {
				continue;
			}

			tryRiskyClick();
		}
	}

	private boolean trySureClick() {
		List<Tile> border = board.findBorder();
		//TODO: this can be a cool streamy thing
		for (Tile tile : border) {
			for (Tile neighbor : tile.neighbors) {
				if (neighbor.isShowingNumber()) {
					int num = neighbor.getNumber();
					long bombs = neighbor.neighbors.stream().filter(Tile::isFlag).count();
					long possibleBombs = neighbor.neighbors.stream().filter(Tile::isBlank).count();
					if ((bombs + possibleBombs) == num) {
						tile.flag();
						board.mineCount--;
						return true;
					}
					if (bombs == num) {
						tile.click();
						return true;
					}
				}
			}
		}
		return false;
	}

	private void tryRiskyClick() {
		//get the list of tiles we actually want to look at, aka, the list of sane moves
		List<Tile> border = board.findBorder();

		//Get the list of their neighbors that we actually want to check for sanity, when we try a layout
		Set<Tile> neighborsToCheck =
				border.stream()
					  .map(Tile::getNeighbors)
					  .flatMap(List::stream)
					  .filter(Tile::isShowingNumber)
					  .collect(Collectors.toSet());


		//TODO: cap this by the number of mines left
		//iterate through all layouts of bomb/no bomb for the border (maybe trim this by number of bombs left?)
		int length = border.size();
		long totalPossibleLayouts = (long) Math.pow(2L, length);
		boolean exhaustive = true;
		if (totalPossibleLayouts > MAX_LAYOUTS) {
			totalPossibleLayouts = MAX_LAYOUTS;
			exhaustive = false;
		}

		System.out.println("total possible layouts: " + totalPossibleLayouts);

		int[] scores = new int[length];
		for (int layoutNum = 0; layoutNum < totalPossibleLayouts; layoutNum++) {
			boolean[] layout = exhaustive ? Utils.bitfield(layoutNum, length)
										  : Utils.randomBitfield(length);
			setupPossibilityCheck(border, layout);
			if (isPossible(neighborsToCheck)) {
				for (int i = 0; i < length; i++) {
					if (layout[i]) {
						scores[i] += 1;
					}
				}
			}
		}

		Tile lowestChanceTile = border.get(0);
		int lowestChance = scores[0];
		for (int i = 0; i < length; i++) {
			int score = scores[i];
			if (score < lowestChance) {
				lowestChance = score;
				lowestChanceTile = border.get(i);
			}
		}

		//check if the lowest chance tile is better than a random other tile
		double guessRatio = getGuessRatio();
		double borderRisk = (double) lowestChance / totalPossibleLayouts;
		System.out.println("lowest border risk: " + borderRisk);
		System.out.println("guessing would be: " + guessRatio);
		if (borderRisk > guessRatio) {
			//make a random guess
			while (true) {
				Tile tile = board.getRandomTile();
				//yes, this is O(n), but n here is suuuuper small compared to elsewhere
				if (tile.isBlank() && !border.contains(tile)) {
					tile.click();
					return;
				}
			}
		}
		//click on the lowest chance tile if we haven't already clicked on this time
		lowestChanceTile.click();
	}

	private double getGuessRatio() {
		return (double) board.mineCount / board.getTilesRemaining();
	}

	private void setupPossibilityCheck(List<Tile> border, boolean... layout) {
		//TODO: this could be a board method?
		Arrays.stream(board.getTiles()).flatMap(Arrays::stream).forEach(Tile::clearTempState);

		for (int i = 0; i < border.size(); i++) {
			if (layout[i]) {
				border.get(i).tempFlag();
			}
		}
	}

	private static boolean isPossible(Iterable<Tile> neighborsToCheck) {
		for (Tile neighbor : neighborsToCheck) {
			int expectedBombs = neighbor.getNumber();
			long bombs = neighbor.neighbors.stream().filter(Tile::isTempFlagged).count();
			if (bombs != expectedBombs) {
				return false;
			}
		}
		return true;
	}

}
