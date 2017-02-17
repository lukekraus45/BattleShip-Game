package edu.pitt.battleshipgame.common.board;

import java.util.LinkedList;
import java.util.List;
import java.io.Serializable;

import edu.pitt.battleshipgame.common.ships.Ship;

public class Board implements Serializable {
    public static final int BOARD_DIM = 10;
    // We could track a Ship-Bool pair but it is just as easy to have
    // two arrays. The Ships array will keep track of the ships on the
    // game board. The moves array will be initialized to false and will
    // change to true when that move is made.
    //private ArrayList < ArrayList < Ship > > theShips;
    private Ship [][] theShips;
    private boolean [][] moves;
    private boolean [][] occupied;
    // Keep a list of all ships on this board for quick searching.
    LinkedList<Ship> shipList;
    private String name;

    public Board (String _name) {
        theShips = new Ship[BOARD_DIM][BOARD_DIM];
        moves = new boolean[BOARD_DIM][BOARD_DIM];
        occupied = new boolean[BOARD_DIM][BOARD_DIM];
        shipList = new LinkedList<Ship>();
        name = _name;
    }
    
    public String getName() {
        return name;
    }
    public boolean can_addShip(Ship ship){
     if (!canShipFit(ship)) {
            throw new IllegalArgumentException("This board already has the maximum amount of: " + ship.getName());
        }
        for (Coordinate coord : ship.getCoordinates()){
            if(!occupied[coord.getRow()][coord.getCol()]){
            //if not occupied you can add the ship otherwise tell them to reenter the coordinates
            //theShips[coord.getRow()][coord.getCol()] = ship;
            occupied[coord.getRow()][coord.getCol()] = true;
           
            }else{
            
            return false;
            }
        }
        
        return true;
    }
    
    
     public void addShip(Ship ship) {
        boolean fail = false;//checks to see if the add ship fails for some reason. If it does, dont add it and set the values back to null
        int length = 0;
        if (!canShipFit(ship)) {
            throw new IllegalArgumentException("This board already has the maximum amount of: " + ship.getName());
        }
         if(!can_addShip(ship)){
            throw new IllegalArgumentException("Ship already has been placed there");
        }
        
        for (Coordinate coord : ship.getCoordinates()){
            length++;
             if(!occupied[coord.getRow()][coord.getCol()]){
            //if not occupied you can add the ship otherwise tell them to reenter the coordinates
            theShips[coord.getRow()][coord.getCol()] = ship;
            occupied[coord.getRow()][coord.getCol()] = true;
            }
        }
        
        if(length != ship.getLength()){
        fail = true;
        }
        if(fail){
        //if ship adding fails for some reason set them all to null rather than the ship
        for (Coordinate coord : ship.getCoordinates()){
            
            theShips[coord.getRow()][coord.getCol()] = null;
            occupied[coord.getRow()][coord.getCol()] = false;
        
        }
        throw new IllegalArgumentException("Ship is of wrong length. Expected ship length is " + ship.getLength());
        }
        if(!fail){
            shipList.add(ship);
        }
    }
    
    public Ship makeMove(Coordinate move) {
        moves[move.getCol()][move.getRow()] = true;
        Ship ship = theShips[move.getCol()][move.getRow()];
        if(ship != null) {
            ship.registerHit();
        }
        return ship;
    }
    
    public boolean canShipFit(Ship ship) {
        int shipCount = 0;
        for (Ship s : shipList) {
            if (s.getType() == ship.getType()) {
                shipCount++;
            }
        }
        if (shipCount >= ship.maxAllowed()) {
            return false;
        } else {
            return true;
        }
    }
    
    public List<Ship> getShipList() {
        return shipList;
    }
    
    public boolean areAllShipsSunk() {
        for (Ship s : shipList) {
            if (! s.isSunk()) {
                return false;
            }
        }
        return true;
    }
    
    public String toString() {
        return toString(false);
    }
    
    public String toString(boolean showShips) {
        StringBuilder sb = new StringBuilder();
        // Buld an intermediate representation of the board as a character array
        char [][] boardRepresentation = new char[BOARD_DIM+1][BOARD_DIM+1];
        boardRepresentation[0][0] = '+';
        for (int row = 1; row < BOARD_DIM+1; row++) {
            // The first column will be filled with the row labels
            boardRepresentation[row][0] = Integer.toString(row).charAt(0);
        }
        for (int col = 1; col < BOARD_DIM+1; col++) {
            boardRepresentation[0][col] = Coordinate.reverseColumnLookup(col-1);
        }
        for (int row = 0; row < BOARD_DIM; row++) {
            for (int col = 0; col < BOARD_DIM; col++) {
                if (moves[row][col]) {
                    if (theShips[row][col] != null) {
                        boardRepresentation[row+1][col+1] = 'X';
                    } else {
                        boardRepresentation[row+1][col+1] = 'O';
                    }
                }
                if (showShips && theShips[row][col] != null) {
                    boardRepresentation[row+1][col+1] = 'S';
                }
            }
        }
        for (char [] row : boardRepresentation) {
            sb.append(row);
            sb.append('\n');
        }
        return sb.toString();
    }
}