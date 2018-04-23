package com.company;

@SuppressWarnings({"MagicNumber", "JavaDoc", "InfiniteLoopStatement", "UseOfSystemOutOrSystemErr"})
public enum Main {
	;

	public static void main(String... args) throws InterruptedException {
    	int wins = 0;
    	while (true) {
    		Board mainBoard = new Board(30, 30, 150);
    		Player player = new Player(mainBoard);
    		player.go();
    		wins++;
    		System.out.println(mainBoard);
    		System.out.println("wins: " + wins);
        }
    }
}
