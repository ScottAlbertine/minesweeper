package com.company;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * @author Scott Albertine
 */
@SuppressWarnings({"JavaDoc", "UseOfSystemOutOrSystemErr", "BooleanMethodNameMustStartWithQuestion"})
public class Player {

	private static final Boolean[] BLANK_LAYOUT = new Boolean[0];

	private final Board board;
	private final ThreadPoolExecutor pool = Utils.MAIN_POOL;

	public Player(Board board) {
		this.board = board;
	}

	public void go() throws InterruptedException, ExecutionException {
		board.getRandomTile().click(); //click on a random tile to start
		while (true) {
			//get the list of tiles we actually want to look at, aka, the list of sane moves
			List<Tile> border = board.findBorder();
			if (border.isEmpty()) {
				break;
			}
			if (trySureClick(border)) {
				continue;
			}
			tryRiskyClick(border);
		}
	}

	private boolean trySureClick(Iterable<Tile> border) {
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

	private void tryRiskyClick(List<Tile> border) throws ExecutionException, InterruptedException {
		//Get the list of neighbors that we actually want to check for sanity, when we try a layout
		Set<Tile> neighborsToCheck =
				border.parallelStream()
					  .map(Tile::getNeighbors)
					  .flatMap(List::stream)
					  .filter(Tile::isShowingNumber)
					  .collect(Collectors.toSet());

		int length = border.size();
		System.out.println("difficulty: " + length);
		//an inverse array, which makes it easy to go from item -> index, rather than index -> item
		Map<Tile, Integer> inverseBorder = IntStream.range(0, length)
													.parallel()
													.boxed()
													.collect(Collectors.toMap(border::get, Function.identity()));
		//fancy way to create a populated array of atomic longs initialized to 0
		AtomicLong[] scores = LongStream.range(0, length)
										.parallel()
										.mapToObj((long i) -> new AtomicLong(0))
										.toArray(AtomicLong[]::new);
		pool.submit(new SolveJob(inverseBorder, neighborsToCheck, BLANK_LAYOUT, scores, pool));

		//put a tracer through the pool, repeatedly, so we don't busywait on the pool being empty
		//remember, there's always going to be 1 active count here, it's me, the tracer thread.
		System.out.print("pool queue size:");
		while (!pool.submit(() -> (pool.getActiveCount() == 1) && pool.getQueue().isEmpty()).get()) {
			//this is neat, we don't need a body here at all.
			System.out.print(" " + pool.getQueue().size());
		}
		System.out.println();

		System.out.println("Scores: " + Arrays.stream(scores)
											  .map(AtomicLong::get)
											  .map(Object::toString)
											  .collect(Collectors.joining(" ")));

		boolean unClicked = true;
		Tile lowestChanceTile = border.get(0);
		long lowestChance = scores[0].get();
		for (int i = 0; i < length; i++) {
			long score = scores[i].get();
			//click on all 0 chance tiles while we're in here, for speed's sake
			if (score == 0L) {
				border.get(i).click();
				unClicked = false;
				continue;
			}
			if (score < lowestChance) {
				lowestChance = score;
				lowestChanceTile = border.get(i);
			}
		}

		if (unClicked) { //click on the lowest chance tile if we haven't already clicked on this turn
			lowestChanceTile.click();
		}
	}

}
