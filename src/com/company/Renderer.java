package com.company;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Scott Albertine
 */
public class Renderer {

	private final Frame window = new Frame("Minesweeper");
	private final int initialW = 1024;
	private final int initialH = 768;

	private int lastW = 0;
	private int lastH = 0;
	private int tileXSize;
	private int tileYSize;
	private Board board;
	private final AtomicBoolean displaying = new AtomicBoolean(false);
	private final AtomicBoolean needsWipe = new AtomicBoolean(true);

	public Renderer() {
		window.setBounds(0, 0, initialW, initialH);
		window.setVisible(true);
		window.addWindowListener(new SaneWindowAdapter());
		window.setIgnoreRepaint(true); //we'll repaint this manually, thanks
		window.createBufferStrategy(2); //with double buffering
		new Timer(16, this::render).start(); //render at 60fps
	}

	public void setBoard(Board board) {
		displaying.set(false);
		this.board = board;
		needsWipe.set(true);
		displaying.set(true);
	}

	private void render(ActionEvent e) {
		if (!displaying.get()) {
			return; //we're off, do nothing
		}

		Rectangle bounds = window.getBounds();
		int w = bounds.width;
		int h = bounds.height;

		//wipe on resize
		if ((w != lastW) || (h != lastH)) {
			lastW = w;
			lastH = h;
			tileXSize = w / board.xSize;
			tileYSize = h / board.ySize;
			needsWipe.set(true); //we're already rendering and thus are in the appropriate thread to wipe
		}

		BufferStrategy strategy = window.getBufferStrategy();
		Graphics g = strategy.getDrawGraphics();

		if (needsWipe.get()) {
			g.setColor(Color.white);
			g.fillRect(0, 0, w, h);
			Arrays.stream(board.tiles).flatMap(Arrays::stream).forEach((Tile tile) -> tile.drawn = false);
			needsWipe.set(false);
		}

		for (Tile[] row : board.tiles) {
			for (Tile tile : row) {
				if (tile.drawn) {
					continue;
				}
				if (tile.isFlag()) {
					g.setColor(Color.black);
				} else if (tile.isShowingNumber()) {
					g.setColor(Color.getHSBColor(tile.getNumber() / 8.0f, 1.0f, 1.0f));
				} else {
					continue;
				}
				g.fillRect(tile.x * tileXSize, tile.y * tileYSize, tileXSize, tileYSize);
				tile.drawn = true;
			}
		}

		g.dispose(); //be kind, dispose of our graphics object when we're done with it
		strategy.show(); //swap the buffer in
		Toolkit.getDefaultToolkit().sync(); //and show it
	}

	private static class SaneWindowAdapter extends WindowAdapter {
		public void windowClosing(WindowEvent e) {
			System.exit(0);
		}
	}
}
