package com.company;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Scott Albertine
 */
public class Renderer implements Runnable {

	private final Frame window = new Frame("Minesweeper");
	private final int initialW = 1024;
	private final int initialH = 768;

	private int lastW = 0;
	private int lastH = 0;
	private int tileXSize;
	private int tileYSize;
	private Board board;
	private AtomicBoolean displaying = new AtomicBoolean(false);
	private AtomicBoolean needsWipe = new AtomicBoolean(true);

	public Renderer() {
		window.setBounds(0, 0, initialW, initialH);
		window.setVisible(true);
		window.addWindowListener(new SaneWindowAdapter());
		new Thread(this).start(); //start the run() method in our own thread
	}

	public void run() {
		while (true) {
			if (displaying.get()) {
				if (needsWipe.get()) {
					wipe();
				}
				render(); //assumes we have at least one frame per board, which if we don't, the fuck're we doing anyway?
			}
			try {
				Thread.sleep(16L); //60 fps
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void setBoard(Board board) {
		displaying.set(false);
		this.board = board;
		needsWipe.set(true);
		displaying.set(true);
	}

	private void wipe() {
		Graphics g = window.getGraphics();
		Rectangle bounds = window.getBounds();
		g.setColor(Color.white);
		g.fillRect(0, 0, bounds.width, bounds.height);
		needsWipe.set(false);
	}

	private void render() {
		Rectangle bounds = window.getBounds();
		int w = bounds.width;
		int h = bounds.height;

		//wipe on resize
		if ((w != lastW) || (h != lastH)) {
			wipe(); //we're already rendering and thus are in the appropriate thread to wipe
			lastW = w;
			lastH = h;
			tileXSize = w / board.xSize;
			tileYSize = h / board.ySize;
		}

		//TODO: see what happens if we parallelize this call? probably makes our render thread wayyy too heavy
		Graphics g = window.getGraphics();
		for (Tile[] row : board.tiles) {
			for (Tile tile : row) {
				if (tile.isFlag()) {
					g.setColor(Color.black);
				} else if (tile.isShowingNumber()) {
					g.setColor(Color.getHSBColor(tile.getNumber() / 8.0f, 1.0f, 1.0f));
				} else {
					continue;
				}
				g.fillRect(tile.x * tileXSize, tile.y * tileYSize, tileXSize, tileYSize);
			}
		}
	}

	private static class SaneWindowAdapter extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
}
