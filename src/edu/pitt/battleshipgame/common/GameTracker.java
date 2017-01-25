package edu.pitt.battleshipgame.common;

import java.util.ArrayList;
import java.util.List;

import edu.pitt.battleshipgame.common.board.Board;
import edu.pitt.battleshipgame.common.ships.Ship;
import edu.pitt.battleshipgame.common.Serializer;

public class GameTracker {
    public static final int MAX_PLAYERS = 2;
    private int registeredPlayers = 0;
    private ArrayList<Board> gameBoards;
    Object lock;
    
    public GameTracker() {
        // Exists to protect this object from direct instantiation
        lock = new Object();
        gameBoards = new ArrayList<Board>(MAX_PLAYERS);
        System.out.println("Server constructed.");
    }

    public int registerPlayer() {
        synchronized(lock) {
            registeredPlayers++;
            gameBoards.add(new Board("Player " + (registeredPlayers - 1) + " board"));
        }
        return registeredPlayers - 1;
    }

    public void waitForPlayers(int playerID) {
        System.out.println("Player " + playerID + " is waiting for other players");
        while(registeredPlayers < 2) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                System.err.println("I can't sleep!'");
            }
        }
    }
    
    public List<Board> getBoards() {
        return gameBoards;
    }
    
    public void registerBoard(int playerID, byte [] board) {
        registerBoard(playerID, (Board)Serializer.fromByteArray(board));
    }
    
    public void registerBoard(int playerID, Board board) {
        gameBoards.set(playerID, board);
        System.out.println("GameTracker is registering a board for " + playerID);
        for(Board boardItr : gameBoards) {
            System.out.println(boardItr.getName() + ":");
            System.out.println(boardItr);
            System.out.println("The ShipList looks like:");
            for(Ship ship : boardItr.getShipList()) {
                System.out.println(ship.getName());
            }
        }
    }
}