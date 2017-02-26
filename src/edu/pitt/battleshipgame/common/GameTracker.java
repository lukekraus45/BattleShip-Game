package edu.pitt.battleshipgame.common;

import java.util.ArrayList;
import java.util.List;

import edu.pitt.battleshipgame.common.board.Board;

public class GameTracker {
    public static final int MAX_PLAYERS = 2;
    private int registeredPlayers = 0;
    private ArrayList<Board> gameBoards;
    private GameState state = GameState.INIT;
    private int playerTurn = 0;
    private long[] lastHeartBeat;
    private int[] beatCount;
    Object lock;
    
    public GameTracker() {
        // Exists to protect this object from direct instantiation
        lock = new Object();
        gameBoards = new ArrayList<Board>(MAX_PLAYERS);
        this.lastHeartBeat = new long[MAX_PLAYERS];
        this.beatCount = new int[MAX_PLAYERS];
        System.out.println("Server constructed.");
    }

    public int registerPlayer() {
        synchronized(lock) {
            registeredPlayers++;
            gameBoards.add(new Board("Player " + (registeredPlayers - 1) + " board"));
        }
        return registeredPlayers - 1;
    }
    
    public void player_leave(){
        
        registeredPlayers--;
    }
    
    public void wait(int playerID) {
        switch (state) {
            case INIT:
            {
                System.out.println("Player " + playerID + " is waiting for other players");
                while(registeredPlayers < MAX_PLAYERS) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        System.err.println(e + " I can't sleep!");
                    }
                }
                state = GameState.PLAYING;
                break;
            }
            case PLAYING:
            {
                while(playerTurn != playerID) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        System.err.println(e + " I can't sleep!");
                    }
                }
                break;
            }
            default:
                break;
        }
    }
    
    public boolean hasBeatingHeart(int playerID)
    {
        long timeoutAmount = this.beatCount[playerID] == 1 ? ((long)120 * (long)1000000000) : ((long)30 * (long)1000000000);
        return (System.nanoTime() - this.lastHeartBeat[playerID]) < timeoutAmount;
    }
    
    public void beatHeart(int playerID)
    {
        this.lastHeartBeat[playerID] = System.nanoTime();
        this.beatCount[playerID]++;
    }
    
    public List<Board> getBoards() {
        return gameBoards;
    }
    
    public void setBoards(ArrayList<Board> boards) {
        gameBoards = boards;
        playerTurn = (playerTurn + 1) % registeredPlayers;
    }
    
    public boolean bothUsersConnected()
    {
        return (this.registeredPlayers == 2);
    }
    
    public boolean isGameOver() {
        System.out.println("Checking if the game is over...");
        for(Board board : gameBoards) {
            if(board.areAllShipsSunk() && board.getShipList().size() == 5) {
                return true;
            }
        }
        return false;
    }
}