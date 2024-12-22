/**
 * This class is responsible for networking the Game of Draughts.
 * It establishes a network connection and holds the neccessary informaiton about it
 * And also allows various operations to be performed, like closing the network.
 * It relies on Receiver.java and Sender.java to communicate over the network using Strings/messages.
 *  
 * @author (Akashbir Singh)
 * @version (SWL 2024)
 */
 
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.Random;
import javax.swing.JOptionPane;

public class Network {
    // Connection information
    private final static int PORT = 727;
    private String ipAddress;
    private boolean isHosting;
    
    // Buffers
    private InputStreamReader inputStreamReader;
    private BufferedReader bufferedReader;
    
    // Sockets
    private ServerSocket serverSocket;
    private Socket clientSocket;
    
    // Message passing
    private Sender sender;
    private Receiver receiver;

    // Determine who starts
    private Random random;
    
    // The game the network is associated with
    private Board board;

    // Game info/stats
    private Boolean isWhite;

    // Constructor
    public Network(Board board) throws IOException {
        this.board = board;
        this.random = new Random();
        
        openNewConnection();
        
        sender = new Sender(this);
        receiver = new Receiver(this);

        // Listen and send in seperate threads to keep GUI working
        Thread senderThread = new Thread(sender);
        Thread receiverThread = new Thread(receiver);
        
        senderThread.start();
        receiverThread.start();

        // Randomly determine who is white and black
        if (isHosting) {
            isWhite = random.nextInt(2) == 1 ? true : false;
            sender.sendMessage("I" + (isWhite ? "1" : "0"));
        } else {
            System.out.println("Waiting for game data...");
        }
        
        // Wait for initial message to be received if not set
        while (isWhite == null) {
            try { Thread.sleep(50); }
            catch (InterruptedException e) {}
        }

        System.out.println("Retrieval was successful!");
        System.out.println("You will be playing as: " + (isWhite ? "White" : "Black"));
    }

    /*
     * Create a new connection that will ask if you want to be a client or server
     * and will setup the connection accordingly. If server, the connection info is in the console.
     *
     * @param none
     * @return void
    */
    private void openNewConnection() throws IOException {
        int instanceType = JOptionPane.showConfirmDialog(null, "Is This Instance Running As A Server?", "Select The Instance Type", JOptionPane.YES_NO_OPTION);
            
        if (instanceType != JOptionPane.YES_OPTION && instanceType != JOptionPane.NO_OPTION) System.exit(0);
        
        isHosting = instanceType == JOptionPane.YES_OPTION;
        
        if (!isHosting) {
            // Run forever until a valid IP is supplied or until the dialog box is closed
            while (true) {
                ipAddress = JOptionPane.showInputDialog(null, "Enter The Server's IP Address", "Enter IP Address", JOptionPane.INFORMATION_MESSAGE);
                
                if (ipAddress == null) System.exit(0);

                // Create the socket         
                try {
                    clientSocket = new Socket(ipAddress, PORT);
                    break;
                } catch (IOException e) {
                    System.out.println("Could not connect to that IP address! Try another...");
                }
            }
        } else {
            ipAddress = getIP();
            
            if (ipAddress == null) {
                System.out.println("Connection could not be opened!");
                return;
            }
            
            System.out.println("Server IP: " + ipAddress);
            
            // Create a new server socket using the port
            serverSocket = new ServerSocket(PORT);
            
            System.out.println("Waiting for a client to connect...");
            
            // Wait for a client to try to connect
            clientSocket = serverSocket.accept();
        }
        
        // Setup the buffers so communication can happen
        inputStreamReader = new InputStreamReader(clientSocket.getInputStream());
        bufferedReader = new BufferedReader(inputStreamReader);
        
        System.out.println("Connection Established!");
    }

    /*
     * Parse the InetAddress of this machine into a String, removing anything extra.
     * Returns null if it fails to get an IP Address.
     *
     * @param none
     * @return String The IP Address
     */
    private static String getIP() {
        InetAddress ip;
        
        // Use a try catch to get the IP address
        try {
            ip = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
        
        // Convert it to a string
        String parsedIP = ip.toString();
        
        // Remove any extra information that's in the string
        // So that only 'xxx.xxx.xxx.xxx' is left over
        int seperatorIndex = parsedIP.indexOf("/");
        
        // Return that string
        return parsedIP.substring(seperatorIndex + 1);
    }
    
    /*
     * Get whether or not this instance is running as a server
     *
     * @param none
     * @return boolean Whether or not this instance is a server
    */
    public boolean isHosting() {
        return isHosting;
    }
    
    /*
     * Determine if the client-server connection is still open
     *
     * @param none
     * @return boolean If the connection is currently open & valid
    */
    public boolean isOpen() {
        return !clientSocket.isClosed() && clientSocket.isConnected();
    }
    
    /*
     * Get the buffered reader
     *
     * @param none
     * @return BufferedReader The buffered reader
    */
    public BufferedReader getBufferedReader() {
        return bufferedReader;
    }
    
    /*
     * Get the input stream reader
     *
     * @param none
     * @return InputStreamReader The stream reader
    */
    public InputStreamReader getInputStreamReader() {
        return inputStreamReader;
    }
    
    /*
     * Get the server socket. Only valid if you are the server.
     *
     * @param none
     * @return ServerSocket The socket
    */
    public ServerSocket getServerSocket() {
        return serverSocket;
    }
    
    /*
     * Get the socket to the other party
     *
     * @param none
     * @return Socket The socket
    */
    public Socket getClientSocket() {
        return clientSocket;
    }
    
    /*
     * Close the network connection
     *
     * @param none
     * @return void
    */
    public void close() {
        JOptionPane.showMessageDialog(null, "Network Communication Lost!\nThe Game Will Close.", "Connection Error", JOptionPane.ERROR_MESSAGE); 
        try {
            clientSocket.close();
            if (isHosting) serverSocket.close();
        } catch (IOException e) { System.exit(0); }
        System.exit(0);
    }

    /*
     * Check the color of this instance
     *
     * @param none
     * @return boolean If this instance is playing as white
    */
    public boolean isWhite() {
        return isWhite;
    }

    /*
     * Set the color of this instance (white or black = true or false)
     *
     * @param boolean If the instance is playing as white
     * @return void
    */
    public void setWhite(boolean newWhite) {
        isWhite = newWhite;
    }

    /*
     * Used to handle and make moves received over the network by the other party.
     *
     * @param int[] the coordinates (initial and final) in an array.
     * @return void
    */
    public void makeAMove(int[] coordinates) {
        board.makeAMove(null, coordinates);
    }

    /*
     * Used to send moves made locally over the network to the other party.
     *
     * @param int the coordinates of the initial position of the tile
     * @param int the coordinates of the final position of the tile
     * @return void
    */
    public void sendAMove(int[] fromCoord, int[] toCoord) {
        sender.sendMessage("M" + fromCoord[0] + "," + fromCoord[1] + ":" + toCoord[0] + "," + toCoord[1]);
    }

    /*
     * Used to handle inbound/outbound requests for drawing the game.
     *
     * @param boolean Is this command issued locally?
     * @return void
    */
    public void requestDraw(boolean isOutbound) {
        if (isOutbound) sender.sendMessage("D");
        board.requestDraw(isOutbound ? isWhite : !isWhite);
    }

    /*
     * Used to handle inbound/outbound requests for handling forefeits.
     *
     * @param boolean Is this command issued locally?
     * @return void
    */
    public void requestForefeit(boolean isOutbound) {
        if (isOutbound) sender.sendMessage("F");
        board.forefeit(isOutbound ? isWhite : !isWhite);
    }

    /*
     * Used to handle inbound/outbound requests for winning/ending the game.
     *
     * @param boolean Is this command issued locally?
     * @return void
    */
    public void endGame(boolean isOutbound) {
        if (isOutbound) {
            char w;
            if (isHosting) {
                w = isWhite ? '1' : '0';
            } else {
                w = isWhite ? '0' : '1';
            }
            sender.sendMessage("W" + w);
            return;
        }

        // Run all checks instead of setting the value directly
        board.gameEndCheck();
    }

    /*
     * Reset the board entirely to default
     *
     * @param none
     * @return void
    */
    public void resetGame(boolean isOutbound) {
        if (isOutbound) {
            sender.sendMessage("A");
            return;
        }
        board.resetGame();
    }
}
