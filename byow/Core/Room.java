package byow.Core;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

public class Room implements Serializable {
    private static final int numSides = 4;
    private static final int maxHallways = 4;
    int width;
    int height;
    int numHallways;
    Pointer bottomLeftPoint;
    Random gen;
    Pointer doorPtr;

    //Map containing an int refering to the side of the room and a boolean value stating if there is a hallway on that side
    HashMap<Integer, Boolean> sideMap = new HashMap<>();

    public Room (int Width, int Height, Pointer pointer, Random gen){
        this.width = Width;
        this.height = Height;
        this.gen = gen;
        for (int i = 1; i <= numSides; i++){
            sideMap.put(i, false);
        }
        bottomLeftPoint = new Pointer(pointer.getX(), pointer.getY());
        doorPtr = new Pointer();
    }

    // Gets a random tile 1 tile in from the edge
    public Pointer getRandomEdgePointer (){
        int newX = bottomLeftPoint.getX();
        int newY = bottomLeftPoint.getY();
        int randTileNumX = RandomUtils.uniform(gen,1, width - 1);
        int randTileNumY = RandomUtils.uniform(gen,1, height - 1);
        newX += randTileNumX;
        newY += randTileNumY;
        Pointer tempPointer = new Pointer(newX, newY);
        return tempPointer;
    }

    public void setDoorPtr(Pointer ptr){
        doorPtr = ptr;
    }

    /** chooses a random side, with 0 = left, 1 = top, 2 = right, 3 = bottom */
    public int randomSide (){
        int side = RandomUtils.uniform(gen, numSides);
        return side;
    }
}
