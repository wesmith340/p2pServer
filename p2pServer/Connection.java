package p2pServer;

import Packet.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * This Class manages a single Connection between the Server and the Client
 */
class Connection extends Thread {
    // Declarations
    Socket socket;
    ObjectInputStream inputStream;
    ObjectOutputStream outputStream;
    int peerPort;
    int peerListenPort;
    int peerID;
    InetAddress peerIP;
    char[] fileList;
    ConnectionList connectionList;
    boolean running;
/*--------------------------------------------------------------------------------------------------------------------*/
    /**
     * Constructor
     * @param socket
     * @param connectionList
     * @throws IOException
     */
    public Connection(Socket socket, ConnectionList connectionList) throws IOException {
        this.socket = socket;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.inputStream = new ObjectInputStream(socket.getInputStream());
        this.peerIP = socket.getInetAddress();
        this.peerPort = socket.getPort();
        this.running = true;
        fileList = new char[64];
        Arrays.fill(fileList, '0');

        this.connectionList = connectionList;
    }
/*--------------------------------------------------------------------------------------------------------------------*/
    // Run method for threading
    /**
     * This method listens for incoming Packets from the Client
     */
    @Override
    public void run() {
        Packet p;
        while (running){
            try {
                p = (Packet)inputStream.readObject();
                eventHandler(p);
            } catch (IOException e) {
                exceptionClose();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("Class not found");
            }

        }

    }
/*--------------------------------------------------------------------------------------------------------------------*/
    // Methods to handle incoming packets
    /**
     * This method determines how a Packet should be handled
     * @param p
     */
    private void eventHandler(Packet p) {
        switch (p.event_type) {
            case INITIALIZE: //Client register
                initializeClient(p.peerID, p.peerListenPort, p.FILE_VECTOR);
                break;
            case REQUEST: // Client is requesting a file
                fileSearch(p.fileIndex);
                break;
            case QUIT_CLIENT: // client wants to quit
                closeConnection();
                break;
            default:
                System.out.println("Incoming Packet did not have a valid Event Type");
        }
    }
    /**
     * This method initializes a Server-Client Connection
     * @param peerID
     * @param peerListenPort
     * @param fileList
     */
    private void initializeClient(int peerID, int peerListenPort, char[] fileList) {
        this.peerID = peerID;
        this.peerListenPort = peerListenPort;
        this.fileList = fileList;
        System.out.println("Client "+this.peerID+" has successfully connected");
    }
    /**
     * This method asks the ConnectionList if a Client has a file. Then it sends a Packet with the Client
     * Id back to the requesting Client.
     * @param fileIndex
     */
    private void fileSearch(int fileIndex) {
        System.out.println(String.format(
                "Client id: %d\nClient IP: %s\nRequested index: %d\n",
                peerID, peerIP, fileIndex
        ));

        ArrayList<Connection> list = connectionList.findFileHolder(fileIndex);
        Packet out = new Packet();

        // If user is found
        if (list.size() > 0) {
            System.out.println("Users with the file index: " + fileIndex);
            for (int i = 0; i<list.size(); i++) {
                System.out.println("User " + list.get(i).peerID);
                out.idReplyPacket(fileIndex+i, list.get(i).peerID);
                try {
                    // Send the Packet back to the Client
                    outputStream.writeObject(out);
                    outputStream.reset();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Unable to send packet");
                }
            }
            try {
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // If no users have the file
        } else {
            System.out.println("No users have the file index: " + fileIndex);
            out.idReplyPacket(fileIndex, -1);
            try {
                // Send the Packet back to the Client
                outputStream.writeObject(out);
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Unable to send packet");
            }
        }


    }
    /**
     * This method closes the Connection on request
     */
    public void closeConnection() {
        System.out.println("Closing Client " +peerID+"'s connection");
        connectionList.removeConnection(this);
        try {
            running = false;
            Packet p = new Packet();
            p.ServerQuit();
            outputStream.writeObject(p);
            outputStream.flush();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Unable to close socket");
        }
    }
/*--------------------------------------------------------------------------------------------------------------------*/
    // Miscellaneous methods
    /**
     * This method determines if this Client has a specific file
     * @param fileIndex
     * @return Boolean
     */
    public boolean hasFile(int fileIndex) {
        return fileList[fileIndex] == '1';
    }
    /**
     * This method closes the Connection if a fatal Error occurs
     */
    private void exceptionClose() {
        try {
            System.out.println("Client "+peerID+" has disconnected");
            running = false;
            inputStream.close();
            outputStream.close();
            socket.close();
            connectionList.removeConnection(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * This method returns the contents of a Connection as a human-readable String
     * @return String
     */
    public String toString() {
        return "Id: " + peerID + ", File Vector: " + String.valueOf(fileList) + "\n";
    }
/*--------------------------------------------------------------------------------------------------------------------*/
}