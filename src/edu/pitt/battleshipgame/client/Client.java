package edu.pitt.battleshipgame.client;

import java.util.ArrayList;
import java.util.Scanner;

import edu.pitt.battleshipgame.common.Serializer;
import edu.pitt.battleshipgame.common.board.*;
import edu.pitt.battleshipgame.common.ships.*;
import edu.pitt.battleshipgame.common.GameInterface;

public class Client {
    public static void main(String [] args) {
        GameInterface gi = new ClientWrapper();
        int myPlayerID = gi.registerPlayer();
        System.out.println("You have registered as Player " + myPlayerID);
        System.out.println("Please wait for other players to join");
        gi.waitForPlayers(myPlayerID);
        System.out.println("Both Players have joined, starting the game.");
        Board myBoard = gi.getBoards().get(myPlayerID);
        placeShips(myBoard);
        System.out.println("Your board:");
        System.out.println(myBoard.toString(true));
        gi.registerBoard(myPlayerID, myBoard);        
        // Start Game Loop
    }

    public static void placeShips(Board board) {
        Scanner scan = new Scanner(System.in);
        System.out.println("Your Board:");
        System.out.println(board.toString(true));
        for(Ship.ShipType type : Ship.ShipType.values()) {
            if(type != Ship.ShipType.NONE) {
                System.out.println("Please enter a start coordinate to place your " + ShipFactory.getNameFromType(type));
                Coordinate start = new Coordinate(scan.nextLine());
                System.out.println("Please enter an end coordinate to place your " + ShipFactory.getNameFromType(type));
                Coordinate end = new Coordinate(scan.nextLine());
                // We don't need to track a reference to the ship since it will be
                // on the board.
                ShipFactory.newShipFromType(type, start, end, board);
            }
        }
    }

    public static void gameLoop() {
        // while not game over
            // Make a move
            // wait for other player's move.
    }
}