/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import minesweeper.Board.Position;
import minesweeper.Board.State;

/**
 * TODO: Description
 */
public class BoardTest {
    
    // TODO: Testing strategy
	/**
	 *  dig(), flag(), deflag(), inspect():
	 *  	partition on square state:
	 *  		untouched, flagged, dug
	 *  
	 *  dig():
	 *  	partition on bomb:
	 *  		contain a bomb
	 *  		contain no bomb
	 *  	partition on dig deep:
	 *  		dig with no neighbor state changed
	 *  		dig one deep further
	 *  		dig two deep further
	 *  		dig more than two deep further
	 *  	partition on position:
	 *  		top-left, top-right
	 *  		bottom-left, bottom-right, 
	 *  		in top edge
	 *  		in left edge
	 *  		in right edge
	 *  		in bottom edge
	 *  		middle
	 *  	partition on cross product between 
	 *  		dig deep and position
	 *  	partition on dig further encounter square was already dug
	 *  
	 *  isGameOver(), dig(), flag(), deflag():
	 *  	partition on game state
	 *  
	 *  toString():
	 *  	partition on some square untouched
	 *  	partition on some square flagged
	 *  	partition on neighbors that have a bomb:
	 *  		no neighbors have a bomb
	 *  		COUNT [1-8)
	 *  		COUNT 8
	 *  	partition on top-left, top-right,
	 *  		bottom-left, bottom-right,
	 *  		in top edge, in left edge,
	 *  		in right edge, in bottom edge,
	 *  		touched square
	 *  		and neighbors have a bomb or not
	 */
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    // TODO: Tests
    
    /**
     * covers untouched, flagged, dug
     */
    @Test
    public void testDig() {
    	Board board = new Board(5, 5, List.of(new Position(2, 3)));
    	board.dig(new Position(2, 2));
    	assertEquals("expected square dug", State.count(1), board.inspect(new Position(2, 2)));
    	board.flag(new Position(2, 2));
    	assertEquals("expected square dug", State.count(1), board.inspect(new Position(2, 2)));
    	board.flag(new Position(1,  1));
    	board.dig(new Position(1, 1));
    	assertEquals("expected square flagged", State.Flagged, board.inspect(new Position(1, 1)));
    }
    
    /**
     * covers contain a or no bomb
     */
    @Test
    public void testDigABomb() {
    	String[] expected = makeInitExpectStrings(10, 10);
    	Board board = new Board(10, 10, List.of(new Position(2, 3)));
    	assertFalse("expected dig no bomb", board.dig(new Position(2, 2)));
    	expected[2] = "- - 1 - - - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertTrue("expected dig a bomb", board.dig(new Position(2, 3)));
    	for (int i = 0; i < 10; i++) expected[i] = "                   ";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	assertTrue("expected game over", board.isGameOver());
    }
    
    /**
     * covers top-left, top-right, bottom-left, bottom-right
     * 		dig with no neighbor state changed
     */
    @Test
    public void testDigBoardCornerWithNoNeighborChanged() {
    	String[] expected = makeInitExpectStrings(10, 10);
    	
    	/**

		 construct board and dig x marked square
		 x*------*x
		 ----------
		 ----------
		 ----------
		 ----------
		 ----------
		 ----------
		 ----------
		 *--------*
		 x--------x
	   	 */
    	Board board = new Board(10, 10, List.of(
    			new Position(1, 0),
    			new Position(0, 8),
    			new Position(8, 0),
    			new Position(9, 8)));
    	assertFalse("expected dig no bomb", board.dig(new Position(0, 0)));
    	expected[0] = "1 - - - - - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(9, 0)));
    	expected[0] = "1 - - - - - - - - 1";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(0, 9)));
    	expected[9] = "1 - - - - - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(9, 9)));
    	expected[9] = "1 - - - - - - - - 1";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    }
    
    /**
     * covers top-left, top-right, bottom-left, bottom-right
     * 		dig one deep further
     */
    @Test
    public void testDigBoardCornerWithOneDeepFurther() {
    	String[] expected = makeInitExpectStrings(10, 10);
    	
    	/**
    	 
		 construct board and dig x marked square
		 x--*-----x
		 ------*---
		 -------*--
		 -*------*-
		 ----------
		 ----------
		 **------*-
		 ------*---
		 ---*------
		 x-----*--x
    	 */
    	Board board = new Board(10, 10, List.of(
    			new Position(3, 0),
    			new Position(6, 1),
    			new Position(7, 2),
    			new Position(1, 3), new Position(8, 3),
    			new Position(0, 6), new Position(1, 6), new Position(8, 6),
    			new Position(6, 7),
    			new Position(3, 8),
    			new Position(6, 9)));
    	assertFalse("expected dig no bomb", board.dig(new Position(0, 0)));
    	expected[0] = "    1 - - - - - - -";
    	expected[1] = "    1 - - - - - - -";
    	expected[2] = "1 1 - - - - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(9, 0)));
    	expected[0] = "    1 - - - - 1    ";
    	expected[1] = "    1 - - - - - 1  ";
    	expected[2] = "1 1 - - - - - - - 1";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(0, 9)));
    	expected[7] = "2 2 - - - - - - - -";
    	expected[8] = "    1 - - - - - - -";
    	expected[9] = "    1 - - - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(9, 9)));
    	expected[7] = "2 2 - - - - - - 1 1";
    	expected[8] = "    1 - - - - 2    ";
    	expected[9] = "    1 - - - - 1    ";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    }
    
    /**
     * covers top-left, top-right, bottom-left, bottom-right
     * 		dig two deep further
     */
    @Test
    public void testDigBoardCornerWithTwoDeepFurther() {
    	String[] expected = makeInitExpectStrings(10, 10);
    	
    	/**
    	 
		 construct board and dig x marked square
		 x---*----x
		 -----*----
		 -------*--
		 -*------*-
		 ----------
		 **------*-
		 ----*-----
		 -----*----
		 ----*-----
		 x----*---x
    	 */
    	Board board = new Board(10, 10, List.of(
    			new Position(4, 0),
    			new Position(5, 1),
    			new Position(7, 2),
    			new Position(1, 3), new Position(8, 3),
    			new Position(0, 5), new Position(1, 5), new Position(8, 5),
    			new Position(4, 6),
    			new Position(5, 7),
    			new Position(4, 8),
    			new Position(5, 9)));
    	assertFalse("expected dig no bomb", board.dig(new Position(0, 0)));
    	expected[0] = "      1 - - - - - -";
    	expected[1] = "      1 - - - - - -";
    	expected[2] = "1 1 1 - - - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(9, 0)));
    	expected[0] = "      1 - - 1      ";
    	expected[1] = "      1 - - - 1 1  ";
    	expected[2] = "1 1 1 - - - - - - 1";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(0, 9)));
    	expected[6] = "2 2 1 - - - - - - -";
    	expected[7] = "      2 - - - - - -";
    	expected[8] = "      1 - - - - - -";
    	expected[9] = "      1 - - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(9, 9)));
    	expected[6] = "2 2 1 - - - - 1 1 1";
    	expected[7] = "      2 - - 1      ";
    	expected[8] = "      1 - - 2      ";
    	expected[9] = "      1 - - 1      ";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    }
    
    /**
     * covers top-left, top-right, bottom-left, bottom-right
     * 		dig more than two deep further
     * 		dig further encounter square was already dug
     */
    @Test
    public void testDigBoardCornerWithMoreThanTwoDeepFurther() {
    	String[] expected = makeInitExpectStrings(10, 10);
    	
    	/**
		 construct board and dig x marked square
		 x----*---x
		 ----------
		 ----------
		 ---*------
		 *---------
		 ----------
		 ----*-----
		 ----------
		 ----------
		 x--------x
    	 */
    	Board board = new Board(10, 10, List.of(
    			new Position(5, 0),
    			new Position(3, 3),
    			new Position(0, 4),
    			new Position(4, 6)));
    	assertFalse("expected dig no bomb", board.dig(new Position(0, 0)));
    	expected[0] = "        1 - - - - -";
    	expected[1] = "        1 - - - - -";
    	expected[2] = "    1 1 - - - - - -";
    	expected[3] = "1 1 - - - - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(0, 9)));
    	expected[0] = "        1 - 1      ";
    	expected[1] = "        1 1 1      ";
    	expected[2] = "    1 1 1          ";
    	expected[3] = "1 1 - - 1          ";
    	expected[4] = "- - 1 - 1          ";
    	expected[5] = "1 1   1 - 1        ";
    	expected[6] = "      1 - 1        ";
    	expected[7] = "      1 1 1        ";
    	expected[8] = "                   ";
    	expected[9] = "                   ";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(9, 0)));
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	assertFalse("expected dig no bomb", board.dig(new Position(9, 9)));
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    }
    
    /**
     * covers in top edge, in left edge, 
     * 	in right edge, in bottom edge
     * 	dig with no neighbor state changed
     */
    @Test
    public void testDigBoardEdgeWithNoNeighborChanged() {
    	String[] expected = makeInitExpectStrings(10, 10);
    	
    	/**
		construct board and dig x marked square
		--------x-
		--------*x
		x---------
		*---------
		----------
		----------
		----------
		----------
		-----*----
		----x-----
    	*/
    	Board board = new Board(10, 10, List.of(
    			new Position(8, 1),
    			new Position(0, 3),
    			new Position(5, 8)));
    	assertFalse("expected dig no bomb", board.dig(new Position(8, 0)));
    	expected[0] = "- - - - - - - - 1 -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(9, 1)));
    	expected[1] = "- - - - - - - - - 1";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(0, 2)));
    	expected[2] = "1 - - - - - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(4, 9)));
    	expected[9] = "- - - - 1 - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    }
    
    /**
     * covers in top edge, in left edge, 
     * 	in right edge, in bottom edge
     *  dig one deep further
     */
    @Test
    public void testDigBoardEdgeWithOneDeepFurther() {
    	String[] expected = makeInitExpectStrings(10, 10);
    	
    	/** 
		construct board and dig x marked square
		-----x----
		x-*-----*-
		----*-*---
		*---------
		----------
		--------*-
		----------
		-*--------
		-------*-x
		-x--*-----
    	 */
    	Board board = new Board(10, 10, List.of(
    			new Position(2, 1), new Position(8, 1),
    			new Position(4, 2), new Position(6, 2),
    			new Position(0, 3),
    			new Position(8, 5),
    			new Position(1, 7),
    			new Position(7, 8),
    			new Position(4, 9)));
    	assertFalse("expected dig no bomb", board.dig(new Position(5, 0)));
    	expected[0] = "- - - 1       1 - -";
    	expected[1] = "- - - - 1 2 1 - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(0, 1)));
    	expected[0] = "  1 - 1       1 - -";
    	expected[1] = "  1 - - 1 2 1 - - -";
    	expected[2] = "1 - - - - - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(1, 9)));
    	expected[8] = "1 1 1 - - - - - - -";
    	expected[9] = "      1 - - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(9, 8)));
    	expected[6] = "- - - - - - - - - 1";
    	expected[7] = "- - - - - - - - 1  ";
    	expected[8] = "1 1 1 - - - - - 1  ";
    	expected[9] = "      1 - - - - 1  ";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    }
    
    /**
     * covers top-left, top-right, bottom-left, bottom-right
     * 		dig two deep further
     */
    @Test
    public void testDigBoardEdgeWithTwoDeepFurther() {
    	String[] expected = makeInitExpectStrings(10, 10);
    	
    	/**
    	 
		construct board and dig x marked square
		----x----*
		-*-----*--
		----------
		-*--*-*--x
		----------
		x--*----*-
		--------*-
		-*--------
		-----*----
		-------x--
    	 */
    	Board board = new Board(10, 10, List.of(
    			new Position(9, 0),
    			new Position(1, 1), new Position(7, 1),
    			new Position(1, 3), new Position(4, 3), new Position(6, 3),
    			new Position(3, 5), new Position(8, 5),
    			new Position(8, 6),
    			new Position(1, 7),
    			new Position(5, 8)));	
    	assertFalse("expected dig no bomb", board.dig(new Position(4, 0)));
    	expected[0] = "- - 1       1 - - -";
    	expected[1] = "- - 1       1 - - -";
    	expected[2] = "- - - 1 1 2 - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(9, 3)));
    	expected[1] = "- - 1       1 - - 1";
    	expected[2] = "- - - 1 1 2 - - 1  ";
    	expected[3] = "- - - - - - - 1    ";
    	expected[4] = "- - - - - - - - 1 1";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(0, 5)));
    	expected[4] = "1 1 - - - - - - 1 1";
    	expected[5] = "    1 - - - - - - -";
    	expected[6] = "1 1 - - - - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(7, 9)));
    	expected[7] = "- - - - - - - 1 1 1";
    	expected[8] = "- - - - - - 1      ";
    	expected[9] = "- - - - - - 1      ";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    }
    
    /**
     * covers top-left, top-right, bottom-left, bottom-right
     * 		dig more than two deep further
     * 		dig further encounter square was already dug
     */
    @Test
    public void testDigBoardEdgeWithMoreThanTwoDeepFurther() {
    	String[] expected = makeInitExpectStrings(10, 10);

    	/**
    	
    	construct this board
		-----x----
		----------
		----*----x
		x---------
		----------
		-------*--
		----------
		---*------
		----------
		-------*--
    	 */
    	Board board = new Board(10, 10, List.of(
    			new Position(4, 2),
    			new Position(7, 5),
    			new Position(3, 7),
    			new Position(7, 9)));
    	assertFalse("expected dig no bomb", board.dig(new Position(5, 0)));
    	expected[0] = "                   ";
    	expected[1] = "      1 1 1        ";
    	expected[2] = "      1 - 1        ";
    	expected[3] = "      1 1 1        ";
    	expected[4] = "            1 1 1  ";
    	expected[5] = "            1 - 1  ";
    	expected[6] = "    1 1 1   1 1 1  ";
    	expected[7] = "    1 - 1          ";
    	expected[8] = "    1 1 1   1 1 1  ";
    	expected[9] = "            1 - 1  ";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(9, 2)));
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertFalse("expected dig no bomb", board.dig(new Position(0, 3)));
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    	
    	assertTrue("expected dig no bomb", board.dig(new Position(7, 9)));
    	expected[8] = "    1 1 1          ";
    	expected[9] = "                   ";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    }
    
    /**
     * covers middle
     * 	dig with no neighbor state changed
     */
    @Test
    public void testDigMiddleWithNoNeighborChanged() {
    	String[] expected = makeInitExpectStrings(10, 10);
    	
    	/**
		construct board and dig x marked square
		---------*
		----------
		----------
		----*-----
		---x------
		----------
		----------
		-------*--
		----------
		----------
    	*/
    	Board board = new Board(10, 10, List.of(
    			new Position(9, 0),
    			new Position(4, 3),
    			new Position(7, 7)));
    	assertFalse("expected dig no bomb", board.dig(new Position(3, 4)));
    	expected[4] = "- - - 1 - - - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    }
    
    /**
     * covers middle
     *  dig one deep further
     */
    @Test
    public void testDigMiddleWithOneDeepFurther() {
    	String[] expected = makeInitExpectStrings(10, 10);
    	
    	/** 
		construct board and dig x marked square
		---------*
		----------
		----*-----
		---------- 
		-----x----
		---*---*--
		-----*----
		-------*--
		----------
		----------
    	 */
    	Board board = new Board(10, 10, List.of(
    			new Position(9, 0),
    			new Position(4, 2),
    			new Position(3, 5), new Position(7, 5),
    			new Position(5, 6),
    			new Position(7, 7)));
    	assertFalse("expected dig no bomb", board.dig(new Position(5, 4)));
    	expected[3] = "- - - - - 1 - - - -";
    	expected[4] = "- - - - 1   1 - - -";
    	expected[5] = "- - - - - 1 - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    }
    
    /**
     * covers middle
     *  dig two deep further
     */
    @Test
    public void testDigMiddleWithTwoDeepFurther() {
    	String[] expected = makeInitExpectStrings(10, 10);
    	
    	/** 
		construct board and dig x marked square
		----------
		-----*----
		---*------
		----------
		-----x-*--
		--*-------
		----------
		-----*----
		----------
		----------
    	 */
    	Board board = new Board(10, 10, List.of(
    			new Position(5, 1),
    			new Position(3, 2),
    			new Position(7, 4),
    			new Position(2, 5),
    			new Position(5, 7)));
    	assertFalse("expected dig no bomb", board.dig(new Position(5, 4)));
    	expected[2] = "- - - - - 1 - - - -";
    	expected[3] = "- - - - 1   1 - - -";
    	expected[4] = "- - - 1     1 - - -";
    	expected[5] = "- - - 1     1 - - -";
    	expected[6] = "- - - - 1 1 - - - -";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    }
    
    /**
     * covers middle
     *  dig more than two deep further
     */
    @Test
    public void testDigMiddleWithMoreThanTwoDeepFurther() {
    	String[] expected = makeInitExpectStrings(10, 10);
    	
    	/** 
		construct board and dig x marked square
		----------
		------*---
		----------
		----------
		-----x----
		----------
		----------
		-----*----
		----------
		----------
    	 */
    	Board board = new Board(10, 10, List.of(
    			new Position(6, 1),
    			new Position(5, 7)));
    	assertFalse("expected dig no bomb", board.dig(new Position(5, 4)));
    	expected[0] = "          1 - 1    ";
    	expected[1] = "          1 - 1    ";
    	expected[2] = "          1 1 1    ";
    	expected[3] = "                   ";
    	expected[4] = "                   ";
    	expected[5] = "                   ";
    	expected[6] = "        1 1 1      ";
    	expected[7] = "        1 - 1      ";
    	expected[8] = "        1 1 1      ";
    	expected[9] = "                   ";
    	assertEquals("expected lookup board", expectedBoardString(expected), board.toString());
    }
    
    /**
     * covers untouched, flagged, dug
     */
    @Test
    public void testFlag() {
    	Board board = new Board(5, 5, List.of(new Position(2, 3)));
    	board.flag(new Position(1, 1));
    	assertEquals("expected square flagged", State.Flagged, board.inspect(new Position(1, 1)));
    	board.flag(new Position(1, 1));
    	assertEquals("expected square flagged", State.Flagged, board.inspect(new Position(1, 1)));
    	
    	assertFalse("expected no bomb", board.dig(new Position(2, 2)));
    	assertEquals("expected square dug", State.count(1), board.inspect(new Position(2, 2)));
    	board.flag(new Position(2, 2));
    	assertEquals("expected square flagged", State.count(1), board.inspect(new Position(2, 2)));
    }
    
    /**
     * covers untouched, flagged, dug
     */
    @Test
    public void testDeflag() {
    	Board board = new Board(5, 5, List.of(new Position(2, 3)));
    	board.flag(new Position(2, 2));
    	board.deflag(new Position(2, 2));
    	assertEquals("expected square untouched", State.Untouched, board.inspect(new Position(1, 1)));
    	board.deflag(new Position(2, 2));
    	assertEquals("expected square untouched", State.Untouched, board.inspect(new Position(1, 1)));
    	board.dig(new Position(2, 2));
    	assertEquals("expected square dug", State.count(1), board.inspect(new Position(2, 2)));
    	board.deflag(new Position(2, 2));
    	assertEquals("expected square dug", State.count(1), board.inspect(new Position(2, 2)));
    }
    
    private String expectedBoardString(String[] arr) {
    	return Stream.of(arr).parallel().collect(Collectors.joining("\n", "", ""));
    }
    
    private String[] makeInitExpectStrings(int rows, int cols) {
    	String[] expected = new String[rows];
    	for (int y = 0; y < rows; y++) {
    		String[] squares = new String[cols];
        	for (int x = 0; x < cols; x++) {
        		squares[x] = "-";
        	}
        	expected[y] = String.join(" ", squares);
		}
    	return expected;
    }
}
