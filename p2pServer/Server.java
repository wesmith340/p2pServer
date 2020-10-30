/*--------------------------------------------------------------------------------------------------------------------*/
/* Project 2                                                                                                          */
/* This application is designed to create a server to handle multiple Clients in a Peer-to-Peer file sharing format   */
/* @author SVSU - CS 401 - Weston Smith                                                                               */
/*--------------------------------------------------------------------------------------------------------------------*/

package p2pServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * This class is designed to handle server side operations for a Peer-to-Peer style file-sharer
 */
public class Server extends Thread{
    final Font font1     = new Font("Calibri", Font.PLAIN, 20);
    final Font messageFont = new Font("Courier New", Font.PLAIN, 20);
    private final Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    private final int WIDTH = 1200;
    private final int HEIGHT = 600;

    private JFrame mainFrame;
    private static JTextArea outputArea;
    private JButton listButton, quitButton;

    // Declarations
    private int serverPort;
    private int MAX_CONNECTED_CLIENTS;
    private ServerSocket listener;
    private static ConnectionList connections;

    boolean running;
/*--------------------------------------------------------------------------------------------------------------------*/
    // Constructors
    /**
     * No-Arg Constructor
     */
    public Server() {
        UIManager.put("OptionPane.messageFont", messageFont);
        UIManager.put("OptionPane.buttonFont", messageFont);


        mainFrame = new JFrame();

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        outputArea = new JTextArea(30,30);
        outputArea.setEditable(false);
        outputArea.setFont(messageFont);
        outputArea.setBackground(Color.LIGHT_GRAY);
        outputArea.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        mainPanel.add(outputArea);

        mainFrame.add(mainPanel);

        JPanel buttonPanel = new JPanel();
        listButton = new JButton("List Connections");
        listButton.setFont(font1);
        listButton.addActionListener(new ButtonHandler());
        buttonPanel.add(listButton);
        quitButton = new JButton("Quit");
        quitButton.setFont(font1);
        quitButton.addActionListener(new ButtonHandler());
        buttonPanel.add(quitButton);
        mainFrame.add(buttonPanel, BorderLayout.SOUTH);

        mainFrame.setLocation((dim.width - WIDTH)/2, (dim.height - HEIGHT)/2);
        mainFrame.setVisible(true);
        mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        WindowListener exitListener = new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int confirm = JOptionPane.showOptionDialog(
                        null, "Are you sure to want to close the server?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == 0) {
                    closeServer();
                    System.exit(0);
                }
            }
        };
        mainFrame.addWindowListener(exitListener);
        mainFrame.setMinimumSize(new Dimension(WIDTH, HEIGHT));

        serverPort = 5000;
        MAX_CONNECTED_CLIENTS = 20;
        try {
            outputArea.append("Starting Server\n");
            listener = new ServerSocket(serverPort);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to create server. SHUTTING DOWN");
            System.exit(0);
        }
        connections = new ConnectionList();
        running = true;
    }
/*--------------------------------------------------------------------------------------------------------------------*/
    // Run method for threading
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
                outputArea.append("\nUnable to accept new connection");
                System.out.println("Unable to accept new connection");
            }
        }
    }
/*--------------------------------------------------------------------------------------------------------------------*/
    // Methods for user input
    /**
     * This method closes the Server
     */
    public void closeServer() {
        running = false;
        try {
            connections.closeConnections();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            listener.close();
        } catch (IOException e) {
            e.printStackTrace();
            outputArea.append("\nUnable to close server socket");
            System.out.println("Unable to close server socket");
        }
    }
    public synchronized static void appendOutput(String msg) {
        outputArea.append(msg);
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

/*--------------------------------------------------------------------------------------------------------------------*/
    // Handlers
    private class ButtonHandler implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            if (event.getSource() == listButton) {
                appendOutput(connections.toString());
                System.out.println(connections.toString());
            } else if (event.getSource() == quitButton) {
                int confirm = JOptionPane.showOptionDialog(
                        null, "Are you sure to want to close the server?",
                        "Exit Confirmation", JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE, null, null, null);
                if (confirm == 0) {
                    closeServer();
                    System.exit(0);
                }
            }
        }
    }
/*--------------------------------------------------------------------------------------------------------------------*/
    // Main
    /**
     * Main
     * @param args
     */
    public static void main(String args[]) {
        System.out.println("Starting Server");
        Server server = new Server();
        server.start();
        boolean running = true;

        /*
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
         */
    }
/*--------------------------------------------------------------------------------------------------------------------*/
}

