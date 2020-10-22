package p2pServer;
// The server class will implement the functions listed in the project description.

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class is designed to handle server side operations for a Peer-to-Peer style file-sharer
 */
public class Server extends Thread{

    private int serverPort;
    private int MAX_CONNECTED_CLIENTS;
    private ServerSocket listener;
    private static ConnectionList connections;

    boolean running;

    /**
     * No-Arg Constructor
     */
    public Server(){
        serverPort = 5000;
        MAX_CONNECTED_CLIENTS = 20;
        try {
            listener = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to create server. SHUTTING DOWN");
            System.exit(0);
        }
        //connections = new ArrayList<>();
        connections = new ConnectionList();
        running = true;
    }

    /**
     * This method closes the Server
     */
    public void closeServer() {
        running = false;
        connections.closeConnections();
        try {
            this.interrupt();
            listener.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to close server socket");
        }
    }

    /**
     * This method returns the contents of the Server as a human-readable String
     * @return
     */
    public String toString() {
        /*for (int i=0;i<connections.size();i++) {
            msg += connections.get(i).toString();
        }*/
        return connections.toString();
    }

    /**
     * This method listens for Clients who want to connect, and then creates a new thread to handle that client
     */
    @Override
    public void run() {
        while (running) {
            Socket clientSocket = null;
            try {
                clientSocket = listener.accept();
                DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());

                // Checks if the server is handling the maximum number of Clients
                if (connections.numConnections() <= MAX_CONNECTED_CLIENTS) {
                    out.writeBoolean(true);
                    out.flush();
                    Connection c = new Connection(clientSocket, connections);
                    connections.addConnection(c);
                } else {
                    out.writeBoolean(false);
                    out.flush();
                    clientSocket.close();
                }
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Unable to accept new connection");
            }
        }
    }

    /**
     * Main
     * @param args
     */
    public static void main(String args[])
    {
        System.out.println("Starting Server");
        Server server = new Server();
        server.start();
        boolean running = true;

        // User input
        Scanner scan = new Scanner(System.in);
        while (running) {
            char input = scan.nextLine().toLowerCase().charAt(0);

            if (input == 'p') { // Print out the Server contents
                System.out.println(server.toString());
            } else if (input == 'q') { // Shutdown the Server
                System.out.println("Server Shutdown");
                running = false;
                server.closeServer();
            }
        }
    }
}

