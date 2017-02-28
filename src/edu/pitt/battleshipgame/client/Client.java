package edu.pitt.battleshipgame.client;

import java.util.ArrayList;
import java.util.Scanner;

import edu.pitt.battleshipgame.common.board.*;
import edu.pitt.battleshipgame.common.ships.*;
import edu.pitt.battleshipgame.common.GameInterface;
import edu.pitt.battleshipgame.common.GameTracker;

public class Client {
    public static GameInterface gi;
    public static int myPlayerID;
    public static ArrayList<Board> gameBoards;
    public static Scanner scan = new Scanner(System.in);
    
    public static void main(String [] args) {
        
       
        //gi = new ClientWrapper();
        //myPlayerID = gi.registerPlayer();
        
        System.out.println("You have registered as Player " + myPlayerID);
        System.out.println("Please wait for other players to join");
        gi.wait(myPlayerID);
        System.out.println("Both Players have joined, starting the game.");
        gameBoards = gi.getBoards();
        placeShips(gameBoards.get(myPlayerID));
        System.out.println("Your board:");
        System.out.println(gameBoards.get(myPlayerID).toString(true));
        gi.setBoards(gameBoards);
        gameLoop();
    }

    public static void placeShips(Board board) {
        System.out.println("Your Board:");
        System.out.println(board.toString(true));
        for(Ship.ShipType type : Ship.ShipType.values()) {
            if(type != Ship.ShipType.NONE) {
                System.out.println("Please enter a start coordinate to place your " + ShipFactory.getNameFromType(type));
                Coordinate start = new Coordinate(scan.nextLine().toLowerCase());
                System.out.println("Please enter an end coordinate to place your " + ShipFactory.getNameFromType(type));
                Coordinate end = new Coordinate(scan.nextLine().toLowerCase());
                // We don't need to track a reference to the ship since it will be
                // on the board.
                //ShipFactory.newShipFromType(type, start, end, board);
            }
        }
    }

    public static void gameLoop() {
        System.out.println("The game is starting!");
        do {
            // Wait for our turn
            gi.wait(myPlayerID);
            // Get the updated boards
            gameBoards = gi.getBoards();
            System.out.println("Where would you like to place your move?");
            Coordinate move = new Coordinate(scan.nextLine().toLowerCase());
            Ship ship = gameBoards.get((myPlayerID + 1) % GameTracker.MAX_PLAYERS).makeMove(move);
            if(ship == null) {
                System.out.println("Miss");
            } else if (ship.isSunk()) {
                System.out.println("You sunk " + ship.getName());
            } else {
                System.out.println("Hit");
            }
            // Send the updated boards.
            gi.setBoards(gameBoards);
        } while(!gi.isGameOver());
        System.out.println("The Game is Over!");
    }
}