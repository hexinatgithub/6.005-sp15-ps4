/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package minesweeper.server;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.IconifyAction;

import minesweeper.Board;

/**
 * Multiplayer Minesweeper server.
 * MinesweeperServer handle multiple connection concurrent.
 * Concurrent request will be synchronized.
 */
public class MinesweeperServer {

    // System thread safety argument
    //   TODO Problem 5

    /** Default server port. */
    private static final int DEFAULT_PORT = 4444;
    /** Maximum port number as defined by ServerSocket. */
    private static final int MAXIMUM_PORT = 65535;
    /** Default square board size. */
    private static final int DEFAULT_SIZE = 10;

    /** Socket for receiving incoming connections. */
    private final ServerSocket serverSocket;
    /** True if the server should *not* disconnect a client after a BOOM message. */
    private final boolean debug;
    /** minesweeper board to play */
    private final Board board;
    /** how many player online */
    private int players = 0;
    
    // TODO: Abstraction function, rep invariant, rep exposure

    /**
     * Make a MinesweeperServer that listens for connections on port.
     * 
     * @param port port number, requires 0 <= port <= 65535
     * @param debug debug mode flag
     * @throws IOException if an error occurs opening the server socket
     */
    public MinesweeperServer(Board board, int port, boolean debug) throws IOException {
        serverSocket = new ServerSocket(port);
        this.debug = debug;
        this.board = board;
    }

    /**
     * Run the server, listening for client connections and handling them.
     * Never returns unless an exception is thrown.
     * 
     * @throws IOException if the main server socket is broken
     *                     (IOExceptions from individual clients do *not* terminate serve())
     */
    public void serve() throws IOException {
        while (true) {
            // block until a client connects
            Socket socket = serverSocket.accept();

            new Thread(() -> {
	            // handle the client
	            try (socket){
	                handleConnection(socket);
	            } catch (IOException ioe) {
	                ioe.printStackTrace();
	            }
			}).start();
        }
    }

    /**
     * Handle a single client connection. Returns when client disconnects.
     * 
     * @param socket socket where the client is connected
     * @throws IOException if the connection encounters an error or terminates unexpectedly
     */
    private void handleConnection(Socket socket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        
        try {
        	int players = incrementPlayers();
        	out.println(String.format("Welcome to Minesweeper. Board: %d columns by %d rows. Players: %d"
        			+ "including you. Type 'help' for help.", board.sizeX, board.sizeY, players));
        	
            for (String line = in.readLine(); line != null; line = in.readLine()) {
                Optional<String> output = handleRequest(line);
                if (output.isPresent()) {
                    // TODO: Consider improving spec of handleRequest to avoid use of null
                    out.println(output.get());
                    
                    if (!debug && output.get() == BOOM_MSG) {
                    	socket.close();
                    	decrementPlayers();
                    	break;
                    }
                }
            }
        } catch (DisconnectException e) {
        	socket.close();
        	decrementPlayers();
		} finally {
            out.close();
            in.close();
        }
    }
    
    /**
     * decrement online players by one.
     * @return current connect online players.
     */
    private synchronized int decrementPlayers() {
		return --players;
	}
    
    /**
     * increment online players by one.
     * @return current connect online players.
     */
    private synchronized int incrementPlayers() {
		return ++players;
	}

    /**
     * Handler for client input, performing requested operations and returning an output message.
     * 
     * @param input message from client
     * @return message to client, or null if none
     * @throws DisconnectException indicate a user active disconnect.
     */
    private Optional<String> handleRequest(String input) throws DisconnectException {
        String regex = "(look)|(help)|(bye)|"
                     + "(dig -?\\d+ -?\\d+)|(flag -?\\d+ -?\\d+)|(deflag -?\\d+ -?\\d+)";
        if ( ! input.matches(regex)) {
            // invalid input
            // TODO Problem 5
        }
        String[] tokens = input.split(" ");
        if (tokens[0].equals("look")) {
            // 'look' request
            // TODO Problem 5
        	return Optional.of(board.toString());
        } else if (tokens[0].equals("help")) {
            // 'help' request
            // TODO Problem 5
        	return Optional.of(HELP_MSG);
        } else if (tokens[0].equals("bye")) {
            // 'bye' request
            // TODO Problem 5
        	throw new DisconnectException();
        } else {
            int x = Integer.parseInt(tokens[1]);
            int y = Integer.parseInt(tokens[2]);
            if (tokens[0].equals("dig")) {
                // 'dig x y' request
                // TODO Problem 5
            	if (board.dig(new Board.Position(x, y))) {
            		return Optional.of(BOOM_MSG);
            	}
            	return Optional.of(board.toString());
            } else if (tokens[0].equals("flag")) {
                // 'flag x y' request
                // TODO Problem 5
            	board.flag(new Board.Position(x, y));
            	return Optional.of(board.toString());
            } else if (tokens[0].equals("deflag")) {
                // 'deflag x y' request
                // TODO Problem 5
            	board.deflag(new Board.Position(x, y));
            	return Optional.of(board.toString());
            }
        }
        // TODO: Should never get here, make sure to return in each of the cases above
        throw new UnsupportedOperationException();
    }

    /**
     * Start a MinesweeperServer using the given arguments.
     * 
     * <br> Usage:
     *      MinesweeperServer [--debug | --no-debug] [--port PORT] [--size SIZE_X,SIZE_Y | --file FILE]
     * 
     * <br> The --debug argument means the server should run in debug mode. The server should disconnect a
     *      client after a BOOM message if and only if the --debug flag was NOT given.
     *      Using --no-debug is the same as using no flag at all.
     * <br> E.g. "MinesweeperServer --debug" starts the server in debug mode.
     * 
     * <br> PORT is an optional integer in the range 0 to 65535 inclusive, specifying the port the server
     *      should be listening on for incoming connections.
     * <br> E.g. "MinesweeperServer --port 1234" starts the server listening on port 1234.
     * 
     * <br> SIZE_X and SIZE_Y are optional positive integer arguments, specifying that a random board of size
     *      SIZE_X*SIZE_Y should be generated.
     * <br> E.g. "MinesweeperServer --size 42,58" starts the server initialized with a random board of size
     *      42*58.
     * 
     * <br> FILE is an optional argument specifying a file pathname where a board has been stored. If this
     *      argument is given, the stored board should be loaded as the starting board.
     * <br> E.g. "MinesweeperServer --file boardfile.txt" starts the server initialized with the board stored
     *      in boardfile.txt.
     * 
     * <br> The board file format, for use with the "--file" option, is specified by the following grammar:
     * <pre>
     *   FILE ::= BOARD LINE+
     *   BOARD ::= X SPACE Y NEWLINE
     *   LINE ::= (VAL SPACE)* VAL NEWLINE
     *   VAL ::= 0 | 1
     *   X ::= INT
     *   Y ::= INT
     *   SPACE ::= " "
     *   NEWLINE ::= "\n" | "\r" "\n"?
     *   INT ::= [0-9]+
     * </pre>
     * 
     * <br> If neither --file nor --size is given, generate a random board of size 10x10.
     * 
     * <br> Note that --file and --size may not be specified simultaneously.
     * 
     * @param args arguments as described
     */
    public static void main(String[] args) {
        // Command-line argument parsing is provided. Do not change this method.
        boolean debug = false;
        int port = DEFAULT_PORT;
        int sizeX = DEFAULT_SIZE;
        int sizeY = DEFAULT_SIZE;
        Optional<File> file = Optional.empty();

        Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));
        try {
            while ( ! arguments.isEmpty()) {
                String flag = arguments.remove();
                try {
                    if (flag.equals("--debug")) {
                        debug = true;
                    } else if (flag.equals("--no-debug")) {
                        debug = false;
                    } else if (flag.equals("--port")) {
                        port = Integer.parseInt(arguments.remove());
                        if (port < 0 || port > MAXIMUM_PORT) {
                            throw new IllegalArgumentException("port " + port + " out of range");
                        }
                    } else if (flag.equals("--size")) {
                        String[] sizes = arguments.remove().split(",");
                        sizeX = Integer.parseInt(sizes[0]);
                        sizeY = Integer.parseInt(sizes[1]);
                        file = Optional.empty();
                    } else if (flag.equals("--file")) {
                        sizeX = -1;
                        sizeY = -1;
                        file = Optional.of(new File(arguments.remove()));
                        if ( ! file.get().isFile()) {
                            throw new IllegalArgumentException("file not found: \"" + file.get() + "\"");
                        }
                    } else {
                        throw new IllegalArgumentException("unknown option: \"" + flag + "\"");
                    }
                } catch (NoSuchElementException nsee) {
                    throw new IllegalArgumentException("missing argument for " + flag);
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("unable to parse number for " + flag);
                }
            }
        } catch (IllegalArgumentException iae) {
            System.err.println(iae.getMessage());
            System.err.println("usage: MinesweeperServer [--debug | --no-debug] [--port PORT] [--size SIZE_X,SIZE_Y | --file FILE]");
            return;
        }

        try {
            runMinesweeperServer(debug, file, sizeX, sizeY, port);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    /**
     * Start a MinesweeperServer running on the specified port, with either a random new board or a
     * board loaded from a file.
     * 
     * @param debug The server will disconnect a client after a BOOM message if and only if debug is false.
     * @param file If file.isPresent(), start with a board loaded from the specified file,
     *             according to the input file format defined in the documentation for main(..).
     * @param sizeX If (!file.isPresent()), start with a random board with width sizeX
     *              (and require sizeX > 0).
     * @param sizeY If (!file.isPresent()), start with a random board with height sizeY
     *              (and require sizeY > 0).
     * @param port The network port on which the server should listen, requires 0 <= port <= 65535.
     * @throws IOException if a network error occurs
     */
    public static void runMinesweeperServer(boolean debug, Optional<File> file, int sizeX, int sizeY, int port) throws IOException {
        
        // TODO: Continue implementation here in problem 4
    	Board board = file.isEmpty() ? Board.random(sizeX, sizeY) : Board.load(file.get());
        MinesweeperServer server = new MinesweeperServer(board, port, debug);
        server.serve();
    }
    
    /**
     * DisconnectException indicate a user or server active disconnection.
     */
    private class DisconnectException extends Exception {
		private static final long serialVersionUID = 3115812493847404535L;
    }
    
    static public final String HELP_MSG = "LOOK message\n"
    		+ "The message type is the word “look” and there are no arguments.\n"
    		+ "Example:\n"
    		+ "look\n"
    		+ "Returns a BOARD message, a string representation of the board’s state. Does not mutate anything on the server. See the section below on messages from the server to the user for the exact required format of the BOARD message.\n"
    		+ "DIG message\n"
    		+ "The message is the word “dig” followed by two arguments, the X and Y coordinates. The type and the two arguments are seperated by a single SPACE.\n"
    		+ "Example:\n"
    		+ "dig 3 10\n"
    		+ "The dig message has the following properties:\n"
    		+ "If either x or y is less than 0, or either x or y is equal to or greater than the board size, or square x,y is not in the untouched state, do nothing and return a BOARD message.\n"
    		+ "If square x,y’s state is untouched , change square x,y’s state to dug.\n"
    		+ "If square x,y contains a bomb, change it so that it contains no bomb and send a BOOM message to the user. Then, if the debug flag is missing (see Question 4), terminate the user’s connection. See again the section below for the exact required format of the BOOM message. Note: When modifying a square from containing a bomb to no longer containing a bomb, make sure that subsequent BOARD messages show updated bomb counts in the adjacent squares. After removing the bomb continue to the next step.\n"
    		+ "If the square x,y has no neighbor squares with bombs, then for each of x,y’s untouched neighbor squares, change said square to dug and repeat this step (not the entire DIG procedure) recursively for said neighbor square unless said neighbor square was already dug before said change.\n"
    		+ "For any DIG message where a BOOM message is not returned, return a BOARD message.\n"
    		+ "FLAG message\n"
    		+ "The message type is the word “flag” followed by two arguments the X and Y coordinates. The type and the two arguments are seperated by a single SPACE.\n"
    		+ "Example:\n"
    		+ "flag	11 8\n"
    		+ "The flag message has the following properties:\n"
    		+ "If x and y are both greater than or equal to 0, and less than the board size, and square x,y is in the untouched state, change it to be in the flagged state.\n"
    		+ "Otherwise, do not mutate any state on the server.\n"
    		+ "For any FLAG message, return a BOARD message.\n"
    		+ "DEFLAG message\n"
    		+ "The message type is the word “deflag” followed by two arguments the X and Y coordinates. The type and the two arguments are seperated by a single SPACE.\n"
    		+ "Example:\n"
    		+ "deflag 9	9\n"
    		+ "The flag message has the following properties:\n"
    		+ "If x and y are both greater than or equal to 0, and less than the board size, and square x,y is in the flagged state, change it to be in the untouched state.\n"
    		+ "Otherwise, do not mutate any state on the server.\n"
    		+ "For any DEFLAG message, return a BOARD message.\n"
    		+ "HELP_REQ message\n"
    		+ "The message type is the word “help” and there are no arguments.\n"
    		+ "Example:\n"
    		+ "help\n"
    		+ "Returns a HELP message (see below). Does not mutate anything on the server.\n"
    		+ "BYE message\n"
    		+ "The message type is the word “bye” and there are no arguments.\n"
    		+ "Example:\n"
    		+ "bye\n"
    		+ "Terminates the connection with this client.\r\n";
    
    static public final String BOOM_MSG = "BOOM!";
    
}
