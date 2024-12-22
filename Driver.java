/**
 * Play the Game of Draughts - A popular variation of checkers where multi-jumps are forced.
 * The game is played on an 8x8 board with diagonally moving tiles.
 * The objective is to capture as many of the opponent's pieces as possible.
 * The game contains a draw feature as well as the ability to reset the playing field.
 *
 * @author (Akashbir Singh)
 * @version (SWL 2024)
*/

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.FlowLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Driver implements ActionListener {
    // Window
    JFrame frame;

    // Game
    Board board;

    // Separate the buttons from the game
    JPanel gamePanel;
    JPanel buttonPanel;

    // The 3 buttons that are shown on the bottom
    JButton drawButton;
    JButton forefeitButton;
    
    // Constructor
    public Driver() {       
        // Create the window and set basic properties
        frame = new JFrame();
        frame.setTitle("Draughts | Akashbir Singh");
        frame.setIconImage(new ImageIcon("icon.png").getImage());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setVisible(true);

        // Create the panels
        gamePanel = new JPanel();
        buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        // Set the background of the panels (Note: must be the same)
        gamePanel.setBackground(new Color(89,57,44));
        buttonPanel.setBackground(new Color(89,57,44));

        // Create the board and add it to the gamePanel
        board = new Board();
        gamePanel.add(board);

        // Create the draw button for the white player
        drawButton = new JButton("DRAW");
        drawButton.setBorderPainted(false);
        drawButton.setFocusPainted(false);
        drawButton.setContentAreaFilled(false);
        drawButton.setBackground(new Color(180, 150, 100));
        drawButton.setForeground(Color.WHITE);
        drawButton.setOpaque(true);
        drawButton.addActionListener(this);
        
        // Create the reset button and set properties and then add it to the window
        forefeitButton = new JButton("FOREFEIT");
        forefeitButton.setBorderPainted(false);
        forefeitButton.setFocusPainted(false);
        forefeitButton.setContentAreaFilled(false);
        forefeitButton.setBackground(new Color(180, 150, 100));
        forefeitButton.setForeground(Color.WHITE);
        forefeitButton.setOpaque(true);
        forefeitButton.addActionListener(this);

        // Add the buttons to the button panel
        buttonPanel.add(drawButton);
        buttonPanel.add(forefeitButton);

        // Add the buttons below the game
        frame.add(gamePanel, BorderLayout.NORTH);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        
        // Show the game only when the networker is ready
        frame.pack();
    }

    @Override
    /**
     * Button click events are handled here.
     *
     * @param ActionEvent Contains information about the button click
     * @return void
     */
    public void actionPerformed(ActionEvent e) {
        if ((JButton)e.getSource() == forefeitButton) {
            board.getNetwork().requestForefeit(true);
        } else if ((JButton)e.getSource() == drawButton) {
            board.getNetwork().requestDraw(true);
        }
    }

    // Entry point
    public static void main(String[] args) {
        Driver driver = new Driver();
    }
}
