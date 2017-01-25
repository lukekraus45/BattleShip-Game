package edu.pitt.battleshipgame.client;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Scanner;

import edu.pitt.battleshipgame.common.Serializer;
import edu.pitt.battleshipgame.common.board.*;
import edu.pitt.battleshipgame.common.ships.*;
import edu.pitt.battleshipgame.common.*;

public class ClientWrapper implements GameInterface {
    ServerInterface serverInterface = null;
    int myPlayerID;

    private static ServerInterface getServer() {
        URL url = null;
        try {
            url = new URL("http://localhost:9999/battleship?wsdl");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        QName qname = new QName("http://server.battleshipgame.pitt.edu/", "ServerWrapperService");
        Service service = Service.create(url, qname);
        return service.getPort(ServerInterface.class);
    }
    
    public ClientWrapper() {
        serverInterface = getServer();
    }
    
    public int registerPlayer() {
        return serverInterface.registerPlayer();
    }
    
    public void waitForPlayers(int playerID) {
        serverInterface.waitForPlayers(playerID);
    }
    
    public void registerBoard(int playerID, Board board) {
        serverInterface.registerBoard(playerID, Serializer.toByteArray(board));
    }
    
    /**
     * Client side wrapper around the 
     * @return 
     */
    public ArrayList<Board> getBoards() {
        return (ArrayList<Board>) Serializer.fromByteArray(serverInterface.getBoards());
    }
}