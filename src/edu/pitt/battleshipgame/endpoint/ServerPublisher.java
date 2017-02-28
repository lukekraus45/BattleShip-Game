package edu.pitt.battleshipgame.endpoint;

import javax.xml.ws.Endpoint;

import edu.pitt.battleshipgame.server.ServerWrapper;

//Endpoint publisher
public class ServerPublisher {
    public static void main(String [] args) {
        Endpoint.publish("http://0.0.0.0:9999/battleship", new ServerWrapper());
    }
}