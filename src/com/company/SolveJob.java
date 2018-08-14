package com.company;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Scott Albertine
 */
public class SolveJob implements Runnable {

	private final Map<Tile, Integer> inverseBorder;
	private final Set<Tile> neighborsToCheck;
	private final Boolean[] layout;
	private final AtomicLong[] scores;
	private final ExecutorService pool;

	public SolveJob(Map<Tile, Integer> inverseBorder, Set<Tile> neighborsToCheck, Boolean[] layout, AtomicLong[] scores, ExecutorService pool) {
		this.inverseBorder = inverseBorder;
		this.neighborsToCheck = neighborsToCheck;
		this.layout = layout;
		this.scores = scores;
		this.pool = pool;
	}

	public void run() {
		if (!isPossible()) {
			return;
		}
		//we're possible here

		if (layout.length == inverseBorder.size()) { //the entire border is full, this layout is fully possible, add its scores
			for (int i = 0; i < layout.length; i++) {
				if (layout[i]) {
					scores[i].incrementAndGet();
				}
			}
			return;
		}
		//we are not full, create our two kids and add them to the thread pool

		Boolean[] layout1 = new Boolean[layout.length + 1];
		Boolean[] layout2 = new Boolean[layout.length + 1];
		System.arraycopy(layout, 0, layout1, 0, layout.length);
		System.arraycopy(layout, 0, layout2, 0, layout.length);
		layout1[layout.length] = true;
		layout2[layout.length] = false;
		pool.submit(new SolveJob(inverseBorder, neighborsToCheck, layout1, scores, pool));
		pool.submit(new SolveJob(inverseBorder, neighborsToCheck, layout2, scores, pool));
	}

	private boolean isPossible() {
		for (Tile neighbor : neighborsToCheck) {
			int expectedBombs = neighbor.getNumber();
			int maxBombs = 8;
			int minBombs = 0;
			for (Tile doubleNeighbor : neighbor.neighbors) {
				if (doubleNeighbor.isFlag()) {
					minBombs++;
				} else if (doubleNeighbor.isShowingNumber()) {
					maxBombs--;
				} else {
					Integer i = inverseBorder.get(doubleNeighbor);
					if ((i != null) && (i < layout.length)) { //prevents null pointer and index oob exceptions
						if (layout[i]) {
							minBombs++;
						} else {
							maxBombs--;
						}
					}
				}
			}
			if ((expectedBombs < minBombs) || (expectedBombs > maxBombs)) {
				return false;
			}
		}
		return true;
	}

}
