package edu.pitt.battleshipgame.common;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;

//Service Endpoint Interface
@WebService
@SOAPBinding(style = Style.RPC)
public interface ServerInterface {
    @WebMethod int registerPlayer() throws TooManyPlayersException;
    @WebMethod void wait(int playerID);
    @WebMethod byte [] getBoards();
    @WebMethod void setBoards(byte [] boards);
    @WebMethod boolean isGameOver();
    @WebMethod boolean bothUsersConnected();
    @WebMethod void player_leave();
    @WebMethod void beatHeart(int id);
    @WebMethod boolean hasBeatingHeart(int id);
}