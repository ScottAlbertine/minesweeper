package com.company;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author Scott Albertine
 */
@SuppressWarnings({"JavaDoc", "UseOfSystemOutOrSystemErr", "BooleanMethodNameMustStartWithQuestion"})
public class Player {

	private final Board board;
	private final ThreadPoolExecutor pool = Utils.MAIN_POOL;

	public Player(Board board) {
		this.board = board;
	}

	public void go() throws InterruptedException, ExecutionException {
		//click on a random tile to start
		board.getRandomTile().click();
		while (!board.findBorder().isEmpty()) {
			System.out.println(board);
			Thread.sleep(20L);
			if (trySureClick()) {
				continue;
			}
			tryRiskyClick();
			Thread.sleep(1000L);
		}
	}

	private boolean trySureClick() {
		for (Tile tile : board.findBorder()) {
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

	private void tryRiskyClick() throws ExecutionException, InterruptedException {
		//get the list of tiles we actually want to look at, aka, the list of sane moves
		List<Tile> border = board.findBorder();

		//Get the list of their neighbors that we actually want to check for sanity, when we try a layout
		Set<Tile> neighborsToCheck =
				border.stream()
					  .map(Tile::getNeighbors)
					  .flatMap(List::stream)
					  .filter(Tile::isShowingNumber)
					  .collect(Collectors.toSet());

		int length = border.size();
		System.out.println("difficulty: " + length);
		//fancy way to create a populated array of atomic longs initialized to 0
		AtomicLong[] scores = LongStream.range(0, length)
										.mapToObj((long i) -> new AtomicLong(0))
										.toArray(AtomicLong[]::new);
		pool.submit(new SolveJob(board, border, neighborsToCheck, new Boolean[length], 0, scores, pool));

		//put a tracer through the pool, repeatedly, so we don't busywait on the pool being empty
		//remember, there's always going to be 1 active count here, it's me, the tracer thread.
		System.out.print("pool queue size:");
		while (!pool.submit(() -> (pool.getActiveCount() == 1) && pool.getQueue().isEmpty()).get()) {
			//this is neat, we don't need a body here at all.
			System.out.print(" " + pool.getQueue().size());
		}
		System.out.println();

		Tile lowestChanceTile = border.get(0);
		long lowestChance = scores[0].get();
		for (int i = 0; i < length; i++) {
			long score = scores[i].get();
			if (score < lowestChance) {
				lowestChance = score;
				lowestChanceTile = border.get(i);
			}
		}

		//check if the lowest chance tile is better than a random other tile
		double guessRatio = (double) board.mineCount / board.getTilesRemaining();
		double borderRisk = lowestChance / Math.pow(2, length);
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

}
