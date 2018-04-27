package com.company;

import java.util.concurrent.ExecutionException;

@SuppressWarnings({"MagicNumber", "JavaDoc", "InfiniteLoopStatement", "UseOfSystemOutOrSystemErr"})
public enum Main {
	;

	public static void main(String... args) throws InterruptedException, ExecutionException {
		int xSize = Integer.parseInt(args[0]);
		int ySize = Integer.parseInt(args[1]);
		int mineCount = Integer.parseInt(args[2]);
    	int wins = 0;
    	while (true) {
    		Board mainBoard = new Board(xSize, ySize, mineCount);
    		Player player = new Player(mainBoard);
    		player.go();
    		wins++;
    		System.out.println(mainBoard);
    		System.out.println("wins: " + wins);
        }
    }
}
