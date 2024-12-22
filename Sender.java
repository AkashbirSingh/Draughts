/**
 * Send messages using a 'Network' connection. Messages are just strings.
 *  
 * @author (Akashbir Singh)
 * @version (SWL 2024)
 */

import java.io.IOException;
import java.io.PrintWriter;

public class Sender implements Runnable {
    // Network connection info
    Network network;

    // Used to send messages over the network
    PrintWriter printWriter;

    // The latest message sent
    String message;

    // If 'message' has already been sent
    boolean messageSent;

    // Constructor
    public Sender(Network network) throws IOException {
        this.network = network;
        this.printWriter = new PrintWriter(network.getClientSocket().getOutputStream());
        this.messageSent = true;
    }
    
    /**
     * Called when the thread is started.
     * Then loops through the same code to handle each new message to send.
     *
     * @param none
     * @return void
    */
    @Override
    public void run() {
        while (network.isOpen()) {
            // Don't repeatedly send the same message
            if (messageSent) continue;
            
            // The message was either empty or invalid
            if (message == null) continue;

            // Send the message
            printWriter.println(message);
            printWriter.flush();

            System.out.println("Outbound: " + message);

            // Set flag so it won't be resent
            messageSent = true;
            
            // Wait 10ms between each iteration
            try { Thread.sleep(10); }
            catch (InterruptedException e) { network.close(); return; }
        }
    }

    /*
     * Send a new message over the network
     *
     * @param String The message to send
     * @return void
    */    
    public void sendMessage(String messageToSend) {
        message = messageToSend;
        messageSent = false;
    }
}
