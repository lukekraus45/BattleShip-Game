package edu.pitt.battleshipgame.common;

import edu.pitt.battleshipgame.common.board.*;
import java.util.ArrayList;

public interface GameInterface {
    int registerPlayer() throws TooManyPlayersException;
    void wait(int playerID);
    ArrayList<Board> getBoards();
    void setBoards(ArrayList<Board> boards);
    boolean isGameOver();
    boolean bothUsersConnected();
    void player_leave();
    void beatHeart(int playerID);
    boolean hasBeatingHeart(int id);
}