/**
 * Receives messages using a 'Network' connection. Messages are just strings.
 * They are received over the network and then can be parsed appropriately.
 *  
 * @author (Akashbir Singh)
 * @version (SWL 2024)
 */
 
import java.io.IOException;

public class Receiver implements Runnable {
    // The main "control center" for the network connection
    Network network;

    // The last message that was received
    String message;

    // Constructor
    public Receiver(Network network) {
        this.network = network;
    }
    
    /**
     * Called when the thread is started.
     * Then loops through the same code to handle each new received message.
     *
     * @param none
     * @return void
    */
    @Override
    public void run() {
        while (network.isOpen()) {
            // Get a message
            try { message = network.getBufferedReader().readLine(); }
            catch (IOException e) { network.close(); return; }

            // Keep checking until something is received
            if (message == null) continue;

            // Print out what was received
            System.out.println("Inbound: " + message);

            // Handle the message
            unpackMessage(message);

            // Wait 10ms between each iteration
            try { Thread.sleep(10); }
            catch (InterruptedException e) { network.close(); return; }
        }
    }

    /*
     * Used to properly handle any incoming messages from the other party.
     *
     * @param String The message to handle
     * @return void
    */
    private void unpackMessage(String message) {
        // Ignore empty messages
        if (message.length() == 0) return;

        // Get the type of command and strip it out of the message
        char type = message.charAt(0);
        message = message.substring(1);

        // Different commands that can be handled by this instance
        switch (type) {
            case 'I': // The initial command sent once the connection is established
                if (message.charAt(0) == '0') network.setWhite(true);
                else network.setWhite(false);
                break;
            case 'W': // When the game ends (somebody wins)
                network.endGame(false);
                break;
            case 'D': // When a draw request is received
                network.requestDraw(false);
                break;
            case 'F': // When a forefeit request is received
                network.requestForefeit(false);
                break;
            case 'M': // When a move is received by the other player
                String[] coords = message.split(":"); // Split initial and final coordinates
                String[] fromCoord = coords[0].split(","); // Get row col of initial coordinate
                String[] toCoord = coords[1].split(","); // Get row col of final coordinate
                network.makeAMove(new int[] { // Create the coordinate array and make the move
                    Integer.parseInt(fromCoord[0]),
                    Integer.parseInt(fromCoord[1]),
                    Integer.parseInt(toCoord[0]),
                    Integer.parseInt(toCoord[1])
                });
                break;
            case 'A': // Reset the board
                network.resetGame(false);
                break;
        }
    }
}
