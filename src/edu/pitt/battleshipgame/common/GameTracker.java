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
    private Object lock;
    private int playerEverConnected = 0;
    private boolean[] playerInited;
    
    public GameTracker() {
        // Exists to protect this object from direct instantiation
        initialize();
        System.out.println("Server constructed.");
    }
    
    private void initialize()
    {
        lock = new Object();
        gameBoards = new ArrayList<Board>(MAX_PLAYERS);
        this.lastHeartBeat = new long[MAX_PLAYERS];
        this.beatCount = new int[MAX_PLAYERS];
        registeredPlayers = 0;
        state = GameState.INIT;
        playerEverConnected = 0;
        playerInited = new boolean[2];
        playerInited[0] = false;
        playerInited[1] = false;
    }

    public int registerPlayer() throws TooManyPlayersException {
        if (((this.beatCount[0] != 0 || beatCount[1] != 0) && (!hasBeatingHeart(0) && !hasBeatingHeart(1))) ||
                (this.beatCount[0] > 0 && this.beatCount[1] > 0 && this.registeredPlayers < 2))
        {
            initialize();
        }
        synchronized(lock) {
            if (this.playerEverConnected >= 2)
            {
                throw new TooManyPlayersException();
            }
            registeredPlayers++;
            this.playerEverConnected++;
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
                playerInited[playerID] = true;
                if (playerInited[0] && playerInited[1])
                {
                    state = GameState.PLAYING;
                }
                break;
            }
            case PLAYING:
            {
                while(playerTurn != playerID || beatCount[0] + beatCount[1] < 4) {
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
        if ((beatCount[playerID] >= 2 && beatCount[(playerID + 1) % 2] < 2) || beatCount[0] == 0 || beatCount[1] == 0)
        {
            return true;
        }
        long timeoutAmount = this.beatCount[playerID] == 1 ? ((long)120 * (long)1000000000) : ((long)30 * (long)1000000000);
        return (System.nanoTime() - this.lastHeartBeat[playerID]) < timeoutAmount;
    }
    
    public void beatHeart(int playerID)
    {
        this.lastHeartBeat[playerID] = System.nanoTime();
        if (this.beatCount[playerID] == 1 && this.beatCount[(playerID + 1) % 2] == 2)
        {
            this.lastHeartBeat[(playerID + 1) % 2] = System.nanoTime();
        }
        ++this.beatCount[playerID];
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