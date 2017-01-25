package edu.pitt.battleshipgame.common;

import edu.pitt.battleshipgame.common.board.*;
import java.util.List;

public interface GameInterface {
    int registerPlayer();
    void waitForPlayers(int playerID);
    List<Board> getBoards();
    void registerBoard(int playerID, Board board);
}