/**
 * A tile on a checkerboard.
 * The tile can have a Piece on it, or not.
 * The tile also has it's own dimensions and colour.
 *  
 * @author (Akashbir Singh)
 * @version (SWL 2024)
 */

import java.awt.Color;
import java.awt.Graphics2D;

public class Tile {
    // Coordinates
    int x;
    int y;

    // Dimensions
    int width;
    int height;

    // The color of the tile
    Color tileColor;

    // The piece associated with this tile (if it even exists)
    Piece piece;

    // If this tile is selected and a piece might be removed
    boolean isSelected;

    // If this tile can be moved to by another piece
    boolean isMoveable;

    // Constructor
    public Tile(int x, int y, int width, int height, Color tileColor, Color pieceColor) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.tileColor = tileColor;
        this.isSelected = false;
        this.isMoveable = false;
        this.piece = null;

        // If a non-null pieceColor value was provided, make a piece with that colour for this tile        
        if (pieceColor != null) this.piece = new Piece(pieceColor);
    }

    /**
     * Get the x coordinate of the tile
     *
     * @param none
     * @return int The x coordinate of the tile
     */
    public int getX() {
        return x;
    }
    
    /**
     * Get the y coordinate of the tile
     *
     * @param none
     * @return int The y coordinate of the tile
     */
    public int getY() {
        return y;
    }
    
    /**
     * Get the width of the tile
     *
     * @param none
     * @return int The width of the tile
     */
    public int getWidth() {
        return width;
    }
    
    /**
     * Get the height of the tile
     *
     * @param none
     * @return int The height of the tile
     */
    public int getHeight() {
        return height;
    }

    /**
     * Return whether or not the tile has a Piece on it
     *
     * @param none
     * @return boolean If the tile has a piece on it
     */
    public boolean hasPiece() {
        return (piece != null);
    }

    /**
     * Return the piece associated with the tile
     *
     * @param none
     * @return Piece The piece on the tile
     */
    public Piece getPiece() {
        return piece;
    }

    /**
     * Set a new piece for the tile
     *
     * @param Piece the new piece
     * @return void
     */
    public void setPiece(Piece newPiece) {
        this.piece = newPiece;
    }

    /**
     * Return whether or not the tile is selected by the player
     * 
     * @param none
     * @return boolean Whether or not the tile is selected by the player
     */
    public boolean isSelected() {
        return this.isSelected;
    }

    /**
     * Set a new selected state for the tile
     *
     * @param boolean The new selection state for the tile
     * @return void
     */
    public void setSelected(boolean newIsSelected) {
        this.isSelected = newIsSelected;
    }

    /**
     * Set a new moveable state for the tile
     *
     * @param boolean The new moveable state for the tile
     * @return void
     */
    public void setMoveable(boolean newIsMoveable) {
        this.isMoveable = newIsMoveable;
    }

    /**
     * Return whether or not the tile is marked as moveable
     *
     * @param none
     * @return boolean Whether or not the tile is marked as moveable
     */
    public boolean isMoveable() {
        return this.isMoveable;
    }

    /**
     * Draw the tile to the JPanel
     *
     * @param g2d The graphics2D object used to draw
     * @return void
     */
    public void draw(Graphics2D g2d) {
        // Selected, Moveable, and Default colours are all different
        if (isSelected) g2d.setColor(Color.GRAY);
        else if (isMoveable) g2d.setColor(Color.GREEN);
        else g2d.setColor(tileColor);

        // Draw the background of the tile 
        g2d.fillRect(x, y, width, height);

        // Draw the border of the tile
        g2d.setColor(Color.BLACK);
        g2d.drawRect(x, y, width, height);
        
        // Draw piece if it exists
        if (piece != null) piece.draw(g2d, x, y, width, height);
    }
}
