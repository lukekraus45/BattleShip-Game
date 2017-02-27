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
    Coordinate start, end;
    
    public Ship(Coordinate start, Coordinate end) {
        this.start = start;
        this.end = end;
    }
    
    public List<Coordinate> getCoordinates() {
        LinkedList coordinates = new LinkedList<Coordinate>();
        
        if (start.getRow() == end.getRow()) {
            // This ship is oriented column wise
            if(start.getCol() > end.getCol()){
                Coordinate temp = start;
                start = end;
                end = temp;
            }
            for (int i = start.getCol(); i <= end.getCol(); i++) {
                coordinates.add(new Coordinate(start.getRow(),i));
            }
        } else {
            // This ship is oriented length wise
            if(start.getRow() > end.getRow()){
                Coordinate temp = start;
                start = end;
                end = temp;
            }
            for (int i = start.getRow(); i <= end.getRow(); i++) {
                coordinates.add(new Coordinate(i, start.getCol()));
            }
        }

        return coordinates;
    }
    
    public Coordinate getStart()
    {
        return this.start;
    }
    
    public Coordinate getEnd()
    {
        return this.end;
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
    
    public void registerHit() {
        hitCount++;
    }
    
    public boolean isValid()
    {
        int x1 = this.start.getCol();
        int x2 = this.end.getCol();
        int y1 = this.start.getRow();
        int y2 = this.end.getRow();
        boolean valid;
        if (x1 == x2)
        {
            valid = (Math.abs(y1 - y2) + 1) == getLength();
        }
        else if (y1 == y2)
        {
            valid = (Math.abs(x1 - x2) + 1) == getLength();
        }
        else
        {
            valid = false;
        }
        return valid;
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