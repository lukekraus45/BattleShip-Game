package edu.pitt.battleshipgame.common;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

import edu.pitt.battleshipgame.common.board.*;
import java.util.ArrayList;

//Service Endpoint Interface
@WebService
@SOAPBinding(style = Style.RPC)
public interface ServerInterface {
    @WebMethod int registerPlayer();
    @WebMethod void waitForPlayers(int playerID);
    @WebMethod byte [] getBoards();
    @WebMethod void setBoards(byte [] boards);
    @WebMethod boolean isGameOver();
}