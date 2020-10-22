package p2pServer;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class manages a list of Connections create by the Server
 */
public class ConnectionList {
    private ConcurrentHashMap<Integer, Connection> connections;

    /**
     * No-Arg Constructor
     */
    public ConnectionList() {
        connections = new ConcurrentHashMap<>();
    }

    /**
     * This method adds a new Connection
     * @param connection
     */
    public void addConnection(Connection connection) {
        connection.start();
        connections.put(connections.size(), connection);
    }

    /**
     * This method removes a Connection from the list
     * @param connection
     */
    public void removeConnection(Connection connection) {
        for (Object i : connections.keySet().toArray()) {
            if (connections.get(i).equals(connection)){
                connections.remove(i);
            }
        }
    }

    /**
     * This method returns the number of Connections in the list
     * @return
     */
    public int numConnections(){
        return connections.size();
    }

    /**
     * This method returns a list of the Connections with a specific file
     * @param fileIndex
     * @return ArrayList of Connections
     */
    public ArrayList<Connection> findFileHolder(int fileIndex) {
        ArrayList<Connection> connectionsWithFile = new ArrayList<>();
        for (Object i : connections.keySet().toArray()){
            if (connections.get(i).hasFile(fileIndex)) {
                connectionsWithFile.add(connections.get(i));
            }
        }
        return connectionsWithFile;
    }

    /**
     * This method closes all Connections in the list
     */
    public void closeConnections() {
        for (Object i : connections.keySet().toArray()) {
            connections.get(i).closeConnection();
        }

    }

    /**
     * This method returns the contents of this Class as a human-readable String
     * @return String
     */
    public String toString() {
        String msg = "\nList of current server connections\n";
        for (Object i : connections.keySet().toArray()) {
            msg += connections.get(i).toString();
        }
        return msg;
    }
}