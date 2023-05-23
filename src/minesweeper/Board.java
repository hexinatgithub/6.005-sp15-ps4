/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Board represent a x columns, y rows minesweeper board to play with.
 * Game end if all square not have a bomb is dug. 
 * If user dig a square contain a bomb, game is not over, bomb will be explosion, 
 * and game can continue to play.
 * Game end all mutable operation will not have effect.
 * Board is thread safety.
 */
public class Board {
    
    // TODO: Abstraction function, rep invariant, rep exposure, thread safety
	
	/**
	 * Abstraction function:
	 * 	AF(columns, rows, grids) represent a columns x rows grids Board.
	 * rep invariant:
	 * 	all rows in grids length must be equal to columns. 
	 * rep exposure:
	 *  grids are defensive copy and Grid are immutable
	 * thread safety:
	 * 	all public method are monitor pattern
	 */
	
	public final int sizeX, sizeY;
	private final List<List<Grid>> grids;
    
    // TODO: Specify, test, and implement in problem 2
	
	/**
	 * Create a x columns, y rows Board, square Position in bombs contain a bomb.
	 * @param x Board columns size.
	 * @param y Board rows size.
	 * @param bombs contain square Position in Board contain a bomb. Position's x and y must
	 * greater than or equal to 0, and less than the board columns and rows size.
	 */
	public Board(int columns, int rows, List<Position> bombs) {
		this.sizeX = columns; 
		this.sizeY = rows;
		
		grids = new ArrayList<>();
		for (int i = 0; i < rows; i++) {
			grids.add(new ArrayList<>());
			
			for (int j = 0; j < columns; j++) {
				Position position = new Position(j, i);
				Grid grid = bombs.contains(position) ? new Grid(position, true) : 
					new Grid(position);
				grids.get(i).add(grid);
			}
		}
		
		for (Position position : bombs) {
			incrementNeighbors(position);
		}
		checkRep();
	}
	
	/**
	 * Generate a random new board with sizeX columns and sizeY rows.
	 * @param start with a random board with width sizeX.
	 * @param start with a random board with height sizeY.
	 * @return a random new board with sizeX columns and sizeY rows.
	 */
	static public Board random(int sizeX, int sizeY) {
		List<Position> bombs = new ArrayList<>();
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				if (Math.random() < 0.25) {
					bombs.add(new Position(x, y));
				}
			}
		}
        System.out.println(bombs.size());
        System.out.println(sizeX * sizeY);
        System.out.println((double) bombs.size() / (sizeX * sizeY));
		return new Board(sizeX, sizeY, bombs);
	}
	
	/**
	 * Generate a new board loaded from a file.
	 * @return a new board loaded from a file.
	 * @throws IOException if read from disk error.
	 */
	static public Board load(File file) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			List<Position> bombs = new ArrayList<>();
			int sizeX, sizeY;
			String line; String[] vals;
			
			line = reader.readLine();
			vals = line.split(" ");
			if (vals.length != 2) throw new FileImproperlyFormatted();
			sizeX = Integer.parseInt(vals[0]);
			sizeY = Integer.parseInt(vals[1]);
			
			for (int y = 0; y < sizeY; y++) {
				line = reader.readLine();
				if (line == null) throw new FileImproperlyFormatted();
				vals = line.split(" ");
				if (vals.length != sizeX) throw new FileImproperlyFormatted();
				
				for (int x = 0; x < sizeX; x++) {
					if (vals[x].equals("1")) {
						bombs.add(new Position(x, y));
					}
				}
			}
			if (reader.readLine() != null) {
				throw new FileImproperlyFormatted();
			}
			
			return new Board(sizeX, sizeY, bombs);
		}
	}
	
	private void checkRep() {
		for (List<Grid> list : grids) {
			assert list.size() == sizeX;
		}
	}
	
	/**
	 * Dig change square x,y’s state to dug if square x,y’s state is untouched.
	 * If square x,y contains a bomb, change it so that it contains no bomb.
	 * If the square x,y has no neighbor squares with bombs, then for each of x,y’s 
	 * untouched neighbor squares, change said square to dug and repeat this step
	 * (not the entire DIG procedure) recursively for said neighbor square unless 
	 * said neighbor square was already dug before said change.
	 * @param position's x and y must greater than or equal to 0, and less than
	 * the board size.
	 * @return whether square x,y contain a bomb.
	 */
	synchronized public boolean dig(Position position) {
		Grid grid = getGrid(position);
		boolean isBomb = grid.dig();
		
		if (isBomb) {
			decrementNeighbors(position);
		}
		
		if (grid.count == 0) {
			crossNeighbors(position, p -> {
				if (getGrid(p).state.equals(State.Untouched))
					dig(p);
			});
		}
		
		checkRep();
		return isBomb;
	}
	
	/**
	 * Flag change the square x,y to flagged state if is in the untouched state.
	 * @param position's x and y must greater than or equal to 0, and less than
	 * the board size.
	 */
	synchronized public void flag(Position position) {
		Grid grid = getGrid(position);
		grid.flag();
		checkRep();
	}
	
	/**
	 * Deflag change square x,y to untouched state if square is in the flagged state.
	 * @param position's x and y must greater than or equal to 0, and less than
	 * the board size.
	 */
	synchronized public void deflag(Position position) {
		Grid grid = getGrid(position);
		grid.deflag();
		checkRep();
	}
	
	/**
	 * Inspect square x,y state.
	 * @param position's x and y must greater than or equal to 0, and less than
	 * the board size.
	 * @return square x,y state.
	 */
	synchronized public State inspect(Position position) {
		return getGrid(position).state;
	}
	
	/**
	 * Game over if all square contain a bomb is flagged and all other square
	 * is dug.
	 * @return whether game is over.
	 */
	synchronized public boolean isGameOver() {
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				Grid grid = getGrid(new Position(x, y));
				
				if (!grid.isBomb && !grid.state.isDug()) {
					return false;
				}
			}
		}
		return true;
	}
    
	/**
	 * @return a String consists of a series of newline-separated rows of space-separated 
	 * characters, thereby giving a grid representation of the board’s state with exactly 
	 * one char for each square. The mapping of characters is as follows:
	 * 	"-" for squares with state untouched .
	 * 	"F" for squares with state flagged .
	 * 	" " (space) for squares with state dug and 0 neighbors that have a bomb.
	 * 	integer COUNT in range [1-8] for squares with state dug and COUNT neighbors that have a bomb.
	 */
	@Override synchronized public String toString() {
		String[] rows = new String[this.sizeY];
		List<String> square = new ArrayList<>();
		for (int y = 0; y < sizeY; y++) {
			for (int x = 0; x < sizeX; x++) {
				square.add(inspect(new Position(x, y)).toString());
			}
			rows[y] = String.join(" ", square);
			square.clear();
		}
		return String.join("\n", rows);
	}
	
	/**
	 * Get Grid x,y.
	 * @param position's x and y must greater than or equal to 0, and less than
	 * the board size.
	 * @return Grid x,y.
	 */
	private Grid getGrid(Position position) {
		return grids.get(position.y).get(position.x);
	}
	
	/**
	 * Increment middle square neighbors bombs count.
	 * @param middle square position.
	 */
	private void incrementNeighbors(final Position middle) {
		neighbors(middle, p -> getGrid(p).increment());
	}

	/**
	 * Decrement middle square neighbors bombs count.
	 * @param middle square position.
	 */
	private void decrementNeighbors(final Position middle) {
		neighbors(middle, p -> getGrid(p).decrement());
	}

	/**
	 * For every neighbors nearby middle, apply function f
	 * @param middle square to find out the neighbors
	 * @param f to called for every neighbors
	 */
	private void neighbors(Position middle, Consumer<Position> f) {
		final Function<Position, Boolean> isNeighbor = p -> inBoard(p) && !p.equals(middle);
		final Position lt = new Position(middle.x-1, middle.y-1);
		
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				
				final Position inspect = new Position(lt.x+j, lt.y+i);
				if (isNeighbor.apply(inspect)) {
					f.accept(inspect);
				}
			}
		}
	}
	
	/**
	 * For every cross neighbors nearby middle, apply function f.
	 * Cross neighbors is in the left, right, top, bottom relative to
	 * middle.
	 * @param middle square to find out the cross neighbors
	 * @param f to called for every cross neighbors
	 */
	private void crossNeighbors(Position middle, Consumer<Position> f) {
		final List<Position> cross = List.of(
				new Position(middle.x-1, middle.y),
				new Position(middle.x+1, middle.y),
				new Position(middle.x, middle.y-1),
				new Position(middle.x, middle.y+1));
		for (Position pos : cross) {
			if (inBoard(pos)) f.accept(pos);
		}
	}
	
	/**
	 * @param p square position in Board.
	 * @return whether position p is in Board.
	 */
	private boolean inBoard(Position p) {
		return p.x >= 0 && p.x < sizeX && p.y >= 0 && p.y < sizeY;
	}

	/**
	 * State represent a Grid state in Board.
	 * The mapping of characters is as follows:
	 * 	"-" for squares with state untouched .
	 * 	"F" for squares with state flagged .
	 * 	" " (space) for squares with state dug and 0 neighbors that have a bomb.
	 * 	integer COUNT in range [1-8] for squares with state dug and COUNT neighbors that have a bomb.
	 */
	static public class State {
		
		static public final State Flagged = new State("F");
		static public final State Space = new State(" ");
		static public final State Untouched = new State("-");
		
		private final String s;
		
		static public State count(int count) {
			if (count == 0) {
				return Space;
			}
			return new State(String.valueOf(count));
		}
		
		private State(String symbol) {
			s = symbol;
		}
		
		public boolean isDug() {
			return !this.equals(State.Flagged) && !this.equals(State.Untouched);
		}
	
		@Override public boolean equals(Object thaObject) {
			if (thaObject instanceof State) {
				State that = (State) thaObject;
				return this.s.equals(that.s);
			}
			return false;
		}
		
		@Override public String toString() {
			return s;
		}
	}
	
	/**
	 * Grid represent a grid in Board.
	 */
	private class Grid {
		
		/**
		 * Abstraction function:
		 * 	AF(position, state, count, isBomb) represent a grid in Board at x,y position
		 *  with state, how many bombs nearby in count and whether is a bomb.
		 * rep invariant:
		 * 	If grid is a bomb, state must not be Dug.
		 * 	If state is Dug, must not be bomb.
		 * rep exposure:
		 *  position are final, all fields are immutable.
		 */
		
		private final Position position;
		
		private State state = State.Untouched;
		
		private int count = 0;
		
		private boolean isBomb = false;
		
		private Grid(Position position, boolean bomb) {
			this.position = position;
			this.isBomb = bomb;
			checkRep();
		}
		
		private Grid(Position position) {
			this.position = position;
			checkRep();
		}
		
		private void checkRep() {
			if (isBomb) assert state != State.Space;
			if (state == State.Space) assert !isBomb;
			assert count >= 0;
		}
		
		private boolean dig() {
			boolean result = false;
			if (state.equals(State.Untouched)) {
				result = isBomb;
				state = State.count(count);
				isBomb = false;
				checkRep();
			}
			return result;
		}
		
		private void flag() {
			if (state.equals(State.Untouched)) {
				state = State.Flagged;
				checkRep();
			}
		}
		
		private void deflag() {
			if (state.equals(State.Flagged)) {
				state = State.Untouched;
				checkRep();
			}
		}
		
		private void decrement() {
			if (count > 0) {
				count--;
				if (state.isDug()) state = State.count(count);
			}
			checkRep();
		}
		
		private void increment() {
			count++;
			if (state.isDug()) state = State.count(count);
			checkRep();
		}
		
		@Override public boolean equals(Object thatObject) {
			if (thatObject instanceof Grid) {
				Grid that = (Grid) thatObject;
				return this.position.equals(that.position) && this.state.equals(that.state) &&
						this.count == that.count && this.isBomb == that.isBomb;
 			}
			return false;
		}
		
	}
	
	/**
	 * Position represent a square location in Board.
	 * Position is immutable.
	 */
	public static class Position {
		public final int x, y;
		
		/**
		 * Abstraction function:
		 * 	AF(x, y) represent square x,y location in Board.
		 * rep invariant:
		 * 	true
		 * rep exposure:
		 *  x, y are final and immutable
		 */
		
		/**
		 * @param x greater than or equal to 0, and less than the board columns size.
		 * @param y greater than or equal to 0, and less than the board rows size.
		 */
		public Position(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override public boolean equals(Object thatObject) {
			if (thatObject instanceof Position) {
				Position that = (Position) thatObject;
				return that.x == this.x && that.y == this.y;
			}
			return false;
		}
	}
	
	public static class FileImproperlyFormatted extends RuntimeException {
		private static final long serialVersionUID = -5007129787557668752L;
	}
}
