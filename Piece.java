/**
 * A Draughts game piece that can be either a pawn or a king.
 * Pawns and Kings are shown/drawn differently.
 * Pieces have the ability to be promoted from pawn to king.
 * 
 *  
 * @author (Akashbir Singh)
 * @version (SWL 2024)
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.geom.Rectangle2D;

public class Piece {
    // Color.WHITE or Color.BLACK
    Color pieceColor;

    // The size of the piece shown (font-size)
    int size = 60;

    // King, or pawn?
    boolean isKing;

    // Constructor
    public Piece(Color pieceColor) {
        this.pieceColor = pieceColor;
        this.isKing = false;
    }

    /**
     * Get the colour of the piece, either Color.WHITE or Color.BLACK
     *
     * @param none
     * @return Color The color of the piece
     */
    public Color getColor() {
        return this.pieceColor;
    }

    /**
     * Promote the piece from a pawn to a king
     *
     * @param none
     * @return void
     */
    public void promote() {
        this.isKing = true;
    }

    /**
     * Is the piece a king?
     *
     * @param none
     * @return boolean If the piece is a king or not (then a pawn)
     */
    public boolean isKing() {
        return this.isKing;
    }

    /**
     * Draw the piece to the window
     *
     * @param g2d The graphics2d object used to draw
     * @param int The x coordinate to draw at
     * @param int The y coordinate to draw at
     * @param int The width of the tile the piece is drawn on
     * @param int The width of the tile the piece is drawn on
     */
    public void draw(Graphics2D g2d, int x, int y, int tileWidth, int tileHeight) {
        g2d.setColor(pieceColor);
        
        g2d.setFont(new Font("Monospaced", Font.PLAIN, size));

        // Change what is being drawn based on if the piece is a king or a pawn        
        String glyph = (isKing ? "\u26c3" : "\u26c2");
        
        /*
         * Our Unicode Symbols are slightly different sizes.
         * So we want to bound them inside and centered to our
         * square space. Code for FontMetrics borrowed and
         * modified sligtly to apply to our current object size
         * -StackOverFlow "Java-Center-Text-In-Rectangle"
         * Accessed 2024/03/20
         */
        FontMetrics fm = g2d.getFontMetrics();
        Rectangle2D r = fm.getStringBounds(glyph, g2d);
        int drawX = (tileWidth - (int)r.getWidth()) / 2;
        int drawY = (tileHeight - (int)r.getHeight()) / 2 + fm.getAscent();

        // Draw the symbol on top of the card's background
        g2d.drawString(glyph, x + drawX, y + drawY);
    }
}
