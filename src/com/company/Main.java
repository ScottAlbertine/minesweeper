package com.company;

import java.util.concurrent.ExecutionException;

@SuppressWarnings({"MagicNumber", "JavaDoc", "InfiniteLoopStatement", "UseOfSystemOutOrSystemErr"})
public enum Main {
	;

	public static void main(String... args) throws InterruptedException, ExecutionException {
    	int wins = 0;
    	while (true) {
    		Board mainBoard = new Board(50, 203, 2000);
    		Player player = new Player(mainBoard);
    		player.go();
    		wins++;
    		System.out.println(mainBoard);
    		System.out.println("wins: " + wins);
        }
    }
}
