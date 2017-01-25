package edu.pitt.battleshipgame.common.ships;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.pitt.battleshipgame.common.board.*;

public abstract class Ship implements Serializable {
    public enum ShipType {
        CARRIER,
        BATTLESHIP,
        CRUISER,
        SUBMARINE,
        DESTROYER,
        NONE        //This is a special ShipType that cannot be instantiated.
    }
    
    /**
     * Base class should provide
     * public static final int LENGTH
     * public static final int MAX_ALLOWED
     */
    
    private int hitCount;
    // Keep a backreference to the board that this ship is placed on
    private Board myBoard = null;
    private LinkedList<Coordinate> myCoordinates;
    
    public Ship(Coordinate start, Coordinate end, Board board) {
        myCoordinates = new LinkedList<Coordinate>();
        if (start.getRow() == end.getRow()) {
            // This ship is oriented column wise
            for (int i = start.getCol(); i <= end.getCol(); i++) {
                myCoordinates.add(new Coordinate(start.getRow(),i));
            }
        } else {
            // This ship is oriented length wise
            for (int i = start.getRow(); i <= end.getRow(); i++) {
                myCoordinates.add(new Coordinate(i, start.getCol()));
            }
        }
        // Make sure calculated length matches the length from the ship
        if (myCoordinates.size() != getLength()) {
            throw new IllegalArgumentException("The ship spans more squares than allowed.");
        }
        addBoard(board);
    }
    
    public List<Coordinate> getCoordinates() {
        return myCoordinates;
    }
    
    public boolean isSunk() {
        return (hitCount == getLength());
    }
    
    public void addBoard(Board board) {
        if (myBoard == null) {
            myBoard = board;
        } else {
            throw new IllegalArgumentException("This ship is already placed on a board: " + myBoard.getName());
        }
        board.addShip(this);
    }
    /**
     * Get the length of this ship instance.
     * @return 
     */
    public abstract int getLength();
    
    /**
     * Get the maximum amount of ships of this type allowed. This function is
     * only here to "force" the base class to have a
     * public static final int MAX_ALLOWED.
     * @return 
     */
    public abstract int maxAllowed();

    /**
     * Get the name of the Ship.
     */
    public abstract String getName();
    
    public abstract ShipType getType();
}