/**
 * Represents a networked board for the game of Draughts.
 * The board size can be easily adjusted through the modification of a single variable.
 * All tiles are created here through a Tile class.
 * NOTE that the logic behind the game is also attached to this class.
 * 
 * The board also contains a forefeit feature and a draw feature. Draws occur when both parties accept.
 
 * @author (Akashbir Singh)
 * @version (SWL 2024)
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Board extends JPanel implements MouseListener {
    // Board dimensions in px
    static final int WIDTH = 640;
    static final int HEIGHT = 480;

    // Board is a square of BOARD_SIZE x BOARD_SIZE dimensions
    static final int BOARD_SIZE = 8;

    // Dynamically calculate the dimensions of tiles with variables above
    static final int TILE_WIDTH = WIDTH / BOARD_SIZE;
    static final int TILE_HEIGHT = HEIGHT / BOARD_SIZE;

    // Constants that help searching algorithms
    // Navigate through the 2D tiles array
    static final int[][] SEARCH_DIRECTIONS = {
        {-1, -1}, // TOP LEFT
        {-1, 1}, // TOP RIGHT
        {1, -1}, // BOTTOM LEFT
        {1, 1} // BOTTOM RIGHT
    };

    // Contains all the tiles that make up the board
    Tile[][] tiles;

    // The 'blue highlighted' tile by the user
    Tile selectedTile;

    // Draughts forces back-to-back jumps
    // This variable shows if that ever comes up
    boolean isNextMoveForced;

    // The current player turn.
    boolean isWhiteTurn;

    // Flag when a player wins
    boolean gameEnded;

    // If a party has requested a draw
    boolean whiteRequestedDraw;
    boolean blackRequestedDraw;

    // Network communications
    Network network;

    // Constructor
    public Board() {
        try { network = new Network(this); }
        catch (Exception e) {
            System.out.println("Network failed to load! Terminating...");
            System.exit(0);
        }

        // Setup the board properties 
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        addMouseListener(this);

        // Create a new tiles array of dimensions BOARD_SIZE x BOARD_SIZE
        tiles = new Tile[BOARD_SIZE][BOARD_SIZE];       

        // Here this also acts as a setup method
        resetGame();
    }

    /*
     * Get the network associated with this game
     *
     * @param none
     * @return Network The network object
     */
    public Network getNetwork() {
        return network;
    }

    /**
     * Reset class variables to their defaults in order to start a new game of Draughts.
     *
     * @param none
     * @return void
    */
    public void resetGame() {
        // Current x,y coordinates to draw the tile
        int drawX = 0;
        int drawY = 0;

        // Create/Reset the board tiles and pieces        
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // If a tile already exists, delete it
                if (tiles[row][col] != null) tiles[row][col] = null;
                
                Color pieceColor = null;
                Color tileColor = null;

                // Calculate if the current tile should have a piece...
                if ((row % 2 == 0 && col % 2 == 1) || (row % 2 == 1 && col % 2 == 0)) {
                    // Use the row number to decide what color it should be
                    if (row < BOARD_SIZE / 2 - 1) pieceColor = Color.BLACK;
                    else if (row > BOARD_SIZE / 2) pieceColor = Color.WHITE;
                }

                // Tile colors alternate between bright and dark
                if ((row % 2 == 0 && col % 2 == 0) || (row % 2 == 1 && col % 2 == 1)) tileColor = new Color(190, 150, 120);
                else tileColor = new Color(122,83,48);

                // Using the information above, create a new tile and set it at position row, col
                tiles[row][col] = new Tile(drawX, drawY, TILE_WIDTH, TILE_HEIGHT, tileColor, pieceColor);

                // Increment the x coordinate to move to the next tile
                drawX += TILE_WIDTH;
            }
            // Reached end of the current row, reset x and increment y
            drawX = 0;
            drawY += TILE_HEIGHT;
        }

        // Defaults
        selectedTile = null;
        isNextMoveForced = false;
        gameEnded = false;
        isWhiteTurn = true;
        whiteRequestedDraw = false;
        blackRequestedDraw = false;
        
        // Clear any selections and movement highlights
        resetSelections();

        // Show the changes to the JPanel
        repaint();
    }

    /*
     * Forefeit to instantly lose or win depending on who forefeit.
     *
     * @param boolean Whether or not white is forefeitting
     * @return void
     */
    public void forefeit(boolean isWhiteForefeitting) {
        if (network.isWhite() && isWhiteForefeitting) {
            JOptionPane.showMessageDialog(this, "You have forefeit!\nOpponent wins the game!", "You Forefeit", JOptionPane.INFORMATION_MESSAGE);
        } else if (!network.isWhite() && !isWhiteForefeitting) {
            JOptionPane.showMessageDialog(this, "You have forefeit!\nOpponent wins the game!", " Forefeit", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Opponent has forefeit!\nYou win the game!", " Forefeit", JOptionPane.INFORMATION_MESSAGE);
        }

        // Force a game reset regardless of what the outcome is
        resetGame();

        // Tell the other party to reset as well
        if ((network.isWhite() && isWhiteForefeitting) || (!network.isWhite() && !isWhiteForefeitting)) network.resetGame(true);
    }

    /**
     * Called when either Draw button is pressed.
     * Param isWhiteRequesting is how you can distinguish between the buttons.
     * This is used to handle drawing/ties in Draughts.
     * Uses JOptionPanes to communicate draw status to the player.
     *
     * @param boolean If it is white or black that is requesting the draw
     * @return void
    */
    public void requestDraw(boolean isWhiteRequesting) {
        // Cannot draw if game isn't in progress!
        if (gameEnded) {
            JOptionPane.showMessageDialog(this, "The game has already ended!", "Cannot Draw", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // The other party has already asked for a draw, so that means you accept and end the game
        if ((isWhiteRequesting && blackRequestedDraw) || (!isWhiteRequesting && whiteRequestedDraw)) {
            gameEnded = true;
            JOptionPane.showMessageDialog(this, "Both players have agreed to draw.\nThe game is a tie!", "Draw Accepted", JOptionPane.INFORMATION_MESSAGE);
            return;
        } 

        // This player has already asked for a draw, and is asking again
        // The else-if runs when it is the player's first time asking for a draw
        if ((isWhiteRequesting && whiteRequestedDraw) || (!isWhiteRequesting && blackRequestedDraw)) {
            JOptionPane.showMessageDialog(this, "A draw request was made, but it's already been dealt with!", "Request Already Handled", JOptionPane.ERROR_MESSAGE);
        } else if ((isWhiteRequesting && !whiteRequestedDraw) || (!isWhiteRequesting && !blackRequestedDraw)) {
            if (isWhiteRequesting) whiteRequestedDraw = true;
            else blackRequestedDraw = true;

            if (isWhiteRequesting && network.isWhite()) {
                JOptionPane.showMessageDialog(this, "Your draw request is now pending.", "Request Sent", JOptionPane.INFORMATION_MESSAGE);
            } else if (!isWhiteRequesting && network.isWhite()) {
                JOptionPane.showMessageDialog(this, "A draw request has been received.\nClick the DRAW button at any time to accept.", "Request Received", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * Locate a tile in the tiles array given an x,y coordinate.
     * This is useful to find a tile based on where a player clicks their mouse.
     *
     * @param int The x coordinate to compare
     * @param int The y coordinate to compare
     * @return Tile The tile that was found in the tiles array
     */
    private Tile getTileWithinBounds(int x, int y) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                Tile tile = tiles[row][col];                
                // X coordinate is within bounds
                if (x < tile.getX() + tile.getWidth() && x > tile.getX()) {
                    // Y coordinate is within bounds
                    if (y < tile.getY() + tile.getHeight() && y > tile.getY()) {
                        return tile;
                    }
                }
            }
        }

        // No tile was within bounds
        return null;
    }

    /**
     * This method is used frequently.
     * It returns the 'i, and j' positions of a tile in the tiles array.
     *
     * @param tile the tile to find the coordinates of
     * @return int[] the coordinates in an int array as {i, j}
     */
    private int[] getTileIndicesInBoard(Tile tile) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // Match found, and we have row, col so just return those
                if (tiles[row][col] == tile) return new int[] {row, col};
            }
        }

        // The tile does not exist in the tiles array
        return null;
    }


    /**
     * Uses the pythagorean theorem to calculate the distance between any two
     * Tiles in the tiles array that are diagonally separated.
     * Used to determine if a jump was made.
     *
     * @param Tile The first tile
     * @param Tile The second tile
     * @return int The distance between the tiles ('c' in the theorem)
     */
    private int getDistanceBetweenTiles(Tile first, Tile second) {
        int[] firstIndices = getTileIndicesInBoard(first);
        int[] secondIndices = getTileIndicesInBoard(second);

        // second - first is equal to delta (or change)
        // Here, changeX is a, and changeY is b
        int changeX = Math.abs(secondIndices[1] - firstIndices[1]);
        int changeY = Math.abs(secondIndices[0] - firstIndices[0]);

        // c = sqrt(a^2 + b^2)
        return (int)Math.sqrt(changeX*changeX + changeY*changeY);
    }

    /**
     * Averages the position of any two tiles to find the tile in between them (center).
     *
     * @param Tile The first tile
     * @param Tile The second tile
     * @return Tile The tile that was found to be in between the two supplied tiles
     */
    private Tile getTileBetweenTiles(Tile first, Tile second) {
        int[] firstTileIndices = getTileIndicesInBoard(first);
        int[] secondTileIndices = getTileIndicesInBoard(second);

        // Average the x, and then the y
        int centerTileRow = (int)((firstTileIndices[0] + secondTileIndices[0]) / 2);
        int centerTileCol = (int)((firstTileIndices[1] + secondTileIndices[1]) / 2);
        
        return tiles[centerTileRow][centerTileCol];
    }

    /**
     * Calls another helper method, getValidMovesInDirection(), which does the heavy lifting.
     * This method is responsible for determining which directions the piece (if found) should be able to move.
     * It also collects all the results from each direction into a single array and returns it to be used later.
     *
     * @param Tile The tile to calculate the moves for
     * @return Tile[][] The tiles found; first iterate through directions, then tiles
     */
    private Tile[][] getValidMoves(Tile rootTile) {
        // Four directions, each having 2 tiles max
        Tile[][] validMoves = {null, null, null, null};

        // Remember the original state in case nothing changes
        boolean oldForceState = isNextMoveForced;
        
        isNextMoveForced = false;

        // Kings go in all 4 directions, otherwise check colour to see which 2 directions
        // White only goes up, while Black only goes down
        if (!rootTile.getPiece().isKing() && isWhiteTurn) {
            validMoves[0] = getValidMovesInDirection(SEARCH_DIRECTIONS[0], rootTile);
            validMoves[1] = getValidMovesInDirection(SEARCH_DIRECTIONS[1], rootTile);
        } else if (!rootTile.getPiece().isKing && !isWhiteTurn) {
            validMoves[0] = getValidMovesInDirection(SEARCH_DIRECTIONS[2], rootTile);
            validMoves[1] = getValidMovesInDirection(SEARCH_DIRECTIONS[3], rootTile);
        } else {
            validMoves[0] = getValidMovesInDirection(SEARCH_DIRECTIONS[0], rootTile);
            validMoves[1] = getValidMovesInDirection(SEARCH_DIRECTIONS[1], rootTile);
            validMoves[2] = getValidMovesInDirection(SEARCH_DIRECTIONS[2], rootTile);
            validMoves[3] = getValidMovesInDirection(SEARCH_DIRECTIONS[3], rootTile);
        }

        // If a jump was found then don't let the player make one-space moves
        if (isNextMoveForced) {
            for (int direction = 0; direction < 4; direction++) {
                if (validMoves[direction] != null) {
                    if (validMoves[direction][0] != null) {
                        if (validMoves[direction][0].isMoveable()) {
                            // Unset the first adjacent tile to be unmoveable if it's moveable
                            validMoves[direction][0].setMoveable(false);
                        }
                    }
                }
            }
        }

        // Update to true if true otherwise to the remembered state
        isNextMoveForced = isNextMoveForced ? true : oldForceState;

        return validMoves;
    }

    /**
     * Calculate all possible moves for a given tile in a given direction.
     * Tiles that can be moved to are marked as isMoveable = true, and are then highlighted green.
     *
     * @param int[] The direction to search (see SEARCH_DIRECTIONS)
     * @param Tile The tile to calculate the moves for
     * @return Tile[] The tiles that were successfully found (not moveable, just found)
    */
    private Tile[] getValidMovesInDirection(int[] searchDirection, Tile rootTile) {
        // Get position in tiles array for the tile provided
        int[] rootTileIndices = getTileIndicesInBoard(rootTile);

        // Calculations only require first two tiles
        Tile firstTile = null;
        Tile secondTile = null;

        // For each tile above..
        for (int i = 1; i <= 2; i++) {
            // Calculate it's position based on the search direction
            int newRow = rootTileIndices[0] + (searchDirection[0] * i);
            int newCol = rootTileIndices[1] + (searchDirection[1] * i);

            // Skip and leave it null if it's out of bounds (of the board)
            if (newCol < 0 || newRow < 0 || newCol > BOARD_SIZE - 1 || newRow > BOARD_SIZE - 1) continue;

            // If the tile exists, set it accordingly
            if (i == 1) firstTile = tiles[newRow][newCol];
            else secondTile = tiles[newRow][newCol];
        }

        // If both tiles were found
        if (firstTile != null && secondTile != null) {
            // The jump scenario..
            if (firstTile.hasPiece() && !secondTile.hasPiece()) {
                // Ensure that the piece you're jumping over belongs to the other player then mark it as a valid move
                if ((isWhiteTurn && firstTile.getPiece().getColor() != Color.WHITE) || (!isWhiteTurn && firstTile.getPiece().getColor() == Color.WHITE)) {
                    secondTile.setMoveable(true);
                    isNextMoveForced = true;
                }
            }
        }

        // Only the first tile was found and it's empty
        if (firstTile != null && !isNextMoveForced) {
            if (!firstTile.hasPiece()) {
                firstTile.setMoveable(true);
            }
        }

        // Return the found tiles
        return new Tile[] {firstTile, secondTile};
    }

    /**
     * Clear all selections and 'potential move' highlights
     *
     * @param none
     * @return void
    */
    private void resetSelections() {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                tiles[row][col].setSelected(false);
                tiles[row][col].setMoveable(false);
            }
        }
    }

    /**
     * Handle ending the game and deciding a winner.
     * A player wins if the other player is out of moves,
     * Or all their pieces are gone.
     * Another way to end the game is to check draws but that's handled in another method.
     * When a player wins, a JOptionPane message is shown the player.
     *
     * @param none
     * @return void
     */
     public void gameEndCheck() {
        // Don't check if already ended
        if (gameEnded) return;

        // Reset any draw requests
        if (whiteRequestedDraw || blackRequestedDraw) {
            whiteRequestedDraw = false;
            blackRequestedDraw = false;
            JOptionPane.showMessageDialog(this, "The draw request has expired.\nYou have to issue a new one if you still want to draw.", "Draw Request Expired", JOptionPane.INFORMATION_MESSAGE);
        }
        
        // Used to check if any opposing player's pieces remain
        boolean whiteWon = true;
        boolean blackWon = true;

        // Used to check if a player is locked out of moves
        boolean whiteHasMoves = false;
        boolean blackHasMoves = false;

        // Go through each tile..
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // The tile must have a piece
                if (!tiles[row][col].hasPiece()) continue;

                // If an opposing piece is found then the player didn't win
                if (tiles[row][col].getPiece().getColor() == Color.WHITE) {
                    blackWon = false;
                } else if (tiles[row][col].getPiece().getColor() == Color.BLACK) {
                    whiteWon = false;
                }
            }
        }

        // Go through each tile in the board
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                // The tile must have a piece
                if (!tiles[row][col].hasPiece()) continue;
                
                // Get the valid moves for that piece
                Tile[][] moves = getValidMoves(tiles[row][col]);

                // If the current tile has a forceable move then the game hasn't ended
                if (isNextMoveForced) {
                    whiteHasMoves = true;
                    blackHasMoves = true;
                    break;
                }

                // Go through each potentially moveable tile for that piece for that tile
                for (int mRow = 0; mRow < (tiles[row][col].getPiece().isKing() ? 4 : 2); mRow++) {
                    for (int mCol = 0; mCol < 2; mCol++) {                        
                        // If the tile can be moved to by the piece..
                        if (moves[mRow][mCol] != null && moves[mRow][mCol].isMoveable()) {
                            // Check the colour of the piece and then set the flag accordingly
                            if (tiles[row][col].getPiece().getColor() == Color.WHITE) {
                               whiteHasMoves = true; 
                            }
                            else if (tiles[row][col].getPiece().getColor() == Color.BLACK) {
                                blackHasMoves = true;
                            }
                        }
                    }
                }
            }
        }

        // Undo all changes made by the getValidMoves() calls
        resetSelections();
        isNextMoveForced = false;                
        
        // Check scenario 1: out of pieces
        if ((whiteWon || blackWon) && !(whiteWon && blackWon)) {
            gameEnded = true;
            JOptionPane.showMessageDialog(this, (whiteWon ? "White" : "Black") + " has won the game!", "Winner", JOptionPane.INFORMATION_MESSAGE);
            network.endGame(true);
        }
        // Scenario 2: both players are locked out of moves
        else if (!whiteHasMoves && !blackHasMoves) {
            gameEnded = true;
            JOptionPane.showMessageDialog(this, "Both players are out of moves! The game is a draw!", "Draw", JOptionPane.INFORMATION_MESSAGE);
        }
        // Scenario 3: Only one player is locked out of moves
        else if (!whiteHasMoves || !blackHasMoves) {
            gameEnded = true;
            JOptionPane.showMessageDialog(this, (whiteHasMoves ? "White" : "Black") + " has won the game!", "Winner", JOptionPane.INFORMATION_MESSAGE);
            network.endGame(true);
        }
    }

    /*
     * Select a new tile and get the moves that tile can make
     *
     * @param Tile The new tile to select
     * @return void
     */
    private void selectNewTile(Tile newSelectedTile) {
        if (!newSelectedTile.hasPiece()) return; // Must select a tile with a piece

        // Must select a piece that matches whose turn it is
        else if (isWhiteTurn && newSelectedTile.getPiece().getColor() != Color.WHITE) return;
        else if (!isWhiteTurn && newSelectedTile.getPiece().getColor() == Color.WHITE) return;

        // Undo all selections since a new tile was selected
        resetSelections();

        // Select the tiles, and show the moves it can make
        Tile[][] moves = getValidMoves(newSelectedTile);

        // Determine what tiles can be selected based on isNextMoveForced
        if (isNextMoveForced) {
            boolean isValidTile = false;

            // Check the moveable tiles for the newly selected one
            for (int i = 0; i < 4; i++) {
                if (moves[i] == null) continue;
                if (moves[i][1] == null) continue;

                // If it's a jump move...
                if (moves[i][1].isMoveable()) {
                    // Update the tiles
                    newSelectedTile.setSelected(true);
                    selectedTile = newSelectedTile;
                    isValidTile = true;
                }
            }

            // You can't select the tile since it doesn't have a jump move...
            if (!isValidTile) {
                resetSelections();
                getValidMoves(selectedTile);
                selectedTile.setSelected(true);
            }
        } else {
            // Update the tiles
            newSelectedTile.setSelected(true);
            selectedTile = newSelectedTile;
        }
    }

    /*
     * Moves pieces according to the rules for the game of Draughts
     *
     * @param e The mouse event (if move is made locally)
     * @param int[] The coordinates for the move (if move is made remotely)
     * @return void
     */
    public void makeAMove(MouseEvent e, int[] coordinates) {
        Tile newSelectedTile;

        // Setup the tiles based on if the move is being received over the network or performed locally
        if (e != null) {
            if ((!isWhiteTurn && network.isWhite()) || (isWhiteTurn && !network.isWhite())) return;
            // Get the tile that was just clicked on by the user using the event
            newSelectedTile = getTileWithinBounds(e.getX(), e.getY());
        } else {
            if ((isWhiteTurn && network.isWhite()) || (!isWhiteTurn && !network.isWhite())) return;
            selectedTile = tiles[coordinates[0]][coordinates[1]];
            selectNewTile(selectedTile);
            newSelectedTile = tiles[coordinates[2]][coordinates[3]];
        }
        
        // Don't run if the game is not in progress
        if (gameEnded) return;

        // Ensure the clicked tile actually exists
        if (newSelectedTile == null) return;

        // Cycle system. SelectedTile becomes 'previousTile' in the scope
        if (selectedTile == null) selectedTile = newSelectedTile;

        // The tile that was clicked on by the user was marked as a potential valid move for an already selected tile
        if (newSelectedTile.isMoveable() && selectedTile.isSelected()) {
            // In that case move the actual selected tile to the new location
            newSelectedTile.setPiece(selectedTile.getPiece());
            selectedTile.setPiece(null);

            // Now check if it can be promoted to a king if it's reached the other side
            // If a promotion happened then switch turns immediately
            if (newSelectedTile.getPiece().getColor() == Color.WHITE) {
                if (getTileIndicesInBoard(newSelectedTile)[0] == 0) {
                    // Send the promotion over the network - special case
                    if (e != null) {
                        int[] fromCoords = getTileIndicesInBoard(selectedTile);
                        int[] toCoords = getTileIndicesInBoard(newSelectedTile);
                        network.sendAMove(fromCoords, toCoords);
                    }            

                    // Handle promotion             
                    newSelectedTile.getPiece().promote();
                    isNextMoveForced = false;
                    isWhiteTurn = !isWhiteTurn;
                    if (getDistanceBetweenTiles(selectedTile, newSelectedTile) == 2) getTileBetweenTiles(selectedTile, newSelectedTile).setPiece(null);
                    resetSelections();
                    selectedTile = null;
                    repaint();
                    gameEndCheck();
                    return;
                }
            } else if (getTileIndicesInBoard(newSelectedTile)[0] == BOARD_SIZE - 1) {
                // Send the promotion over the network - special case
                if (e != null) {
                    int[] fromCoords = getTileIndicesInBoard(selectedTile);
                    int[] toCoords = getTileIndicesInBoard(newSelectedTile);
                    network.sendAMove(fromCoords, toCoords);
                }            

                // Handle promotion
                newSelectedTile.getPiece().promote();
                isNextMoveForced = false;
                isWhiteTurn = !isWhiteTurn;
                if (getDistanceBetweenTiles(selectedTile, newSelectedTile) == 2) getTileBetweenTiles(selectedTile, newSelectedTile).setPiece(null);
                resetSelections();
                selectedTile = null;
                repaint();
                gameEndCheck();
                return;
            }
            
            // Undo all selections since next round will now be calculated
            resetSelections();

            // Reset for next iteration
            isNextMoveForced = false;

            // The move the player made was actually a jump move
            if (getDistanceBetweenTiles(selectedTile, newSelectedTile) == 2) {                
                // Get the tile that should be 'killed'
                getTileBetweenTiles(selectedTile, newSelectedTile).setPiece(null);
                
                // Send jump move
                if (e != null) {
                    int[] fromCoords = getTileIndicesInBoard(selectedTile);
                    int[] toCoords = getTileIndicesInBoard(newSelectedTile);
                    network.sendAMove(fromCoords, toCoords);
                } 

                // Get the valid moves for the new location to check if there are more moves that must be made
                Tile[][] forcedTiles = getValidMoves(newSelectedTile);

                // Force select or undo
                if (isNextMoveForced) {
                    newSelectedTile.setSelected(true);
                    selectedTile = newSelectedTile;
                } else {
                    resetSelections();
                }
            } else if (e != null) { // Send normal move
                int[] fromCoords = getTileIndicesInBoard(selectedTile);
                int[] toCoords = getTileIndicesInBoard(newSelectedTile);
                network.sendAMove(fromCoords, toCoords);
            }  

            // If there are no forced moves, swap turn and check for game end
            if (!isNextMoveForced) {
                // Switch turns
                isWhiteTurn = !isWhiteTurn;

                // Check automatically if any jumping moves can be made and force if found
                if ((network.isWhite() && isWhiteTurn) || (!network.isWhite() && !isWhiteTurn)) {
                    for (int boardRow = 0; boardRow < BOARD_SIZE; boardRow++) {
                        for (int boardCol = 0; boardCol < BOARD_SIZE; boardCol++) {
                            // Ignore this tile if any of these cases are true
                            if (isNextMoveForced) continue;
                            if (!tiles[boardRow][boardCol].hasPiece()) continue;
                            if (isWhiteTurn && tiles[boardRow][boardCol].getPiece().getColor() != Color.WHITE) continue;
                            if (!isWhiteTurn && tiles[boardRow][boardCol].getPiece().getColor() == Color.WHITE) continue;
    
                            // Handle the possible moves for the piece/tile
                            getValidMoves(tiles[boardRow][boardCol]);
    
                            // If nothing is found, reset otherwise force selection
                            if (!isNextMoveForced) {
                                resetSelections();
                            } else {
                                tiles[boardRow][boardCol].setSelected(true);
                                selectedTile = tiles[boardRow][boardCol];
                            }
                        }
                    }
                }

                // Check if the game is complete
                if (!isNextMoveForced) gameEndCheck();
            }          
        } else { // Selecting different tiles, not moving them
            selectNewTile(newSelectedTile);
        }

        // Show all changes that were made to the game
        repaint();
    }

    @Override
    /**
     * Ran each time a user clicks on the JPanel.
     * Attached is a MouseEvent that has information on the mouse click.
     *
     * @param MouseEvent Contains the information attached to the event
     * @return void
     */
    public void mousePressed(MouseEvent e) {
        makeAMove(e, null);
    }

    // Unimplemented events
    public void mouseClicked(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}

    @Override
    /**
     * Draw to the window
     *
     * @param Graphics The graphics object
     * @return void
     */
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw each tile to the JPanel
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                tiles[row][col].draw((Graphics2D)g);
            }
        }
    }
}
