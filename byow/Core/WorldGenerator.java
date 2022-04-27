package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.Serializable;
import java.util.Random;

public class WorldGenerator implements Serializable {

    private int WIDTH;
    private int HEIGHT;
    private int MAX_ROOMS = 10;
    private int MIN_ROOMS = 6;
    private int roomMaxSize = 10;
    private TETile[][] world;
    //private TETile[][] underlyingWorld;
    private boolean[][] isFloor;
    private boolean[][] isDoor;
    private Pointer pointer;
    private Pointer avPointer; //pointer for avatar position
    private long seed;
    private Random seedVal;
    private Room currRoom;
    private Room firstRoom;

    /*
     public static void main (String[] args){
         //int screenWidth = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth() / 16;
         //int screenHeight = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight() / 16 - 7;
         WorldGenerator mainRunner = new WorldGenerator(908, 80, 30);
         TERenderer ter = new TERenderer();
         ter.initialize(WIDTH, HEIGHT);
         ter.renderFrame(mainRunner.getWorld());
         Pointer newPoint = mainRunner.randomAvatarRoomPosition();
         mainRunner.world[newPoint.getX()][newPoint.getY()] = Tileset.AVATAR;
     } */

    //Constructor, fills tiles in world and initializes renderer.
    public WorldGenerator(long seed, int width, int height){
         WIDTH = width;
         HEIGHT = height;
         world = new TETile[WIDTH][HEIGHT];
         isFloor = new boolean[WIDTH][HEIGHT];
         isDoor = new boolean[WIDTH][HEIGHT];
         pointer = new Pointer();
         avPointer = new Pointer();
         this.seed = seed;
         seedVal = new Random(seed);
         fillTilesNothing();
         createWorld();

    }

    //draws randomly generated rooms and hallways in the world
    private void createWorld(){
         int numRooms = (RandomUtils.uniform(seedVal, MIN_ROOMS, MAX_ROOMS));
         createRooms(numRooms);
         //underlyingWorld = world.clone();
         setAvatarPosition(firstRoom.bottomLeftPoint.getX() + 1, firstRoom.bottomLeftPoint.getY() + 1);
         //randomAvatarRoomPosition();

    }

    public Pointer getAvatarPosition() {
        return avPointer;
    }

    //checks if the position the avatar is trying to move to is a floor
    public boolean canMove(){
        return isFloor();
        //return isFloor() || isDoor();
    }

    public boolean isDoor(){
        return isDoor[avPointer.getX()][avPointer.getY()];
    }

    public boolean isFloor(){
        return world[avPointer.getX()][avPointer.getY()].character() == (Tileset.FLOOR_TWO.character());
        //return isFloor[avPointer.getX()][avPointer.getY()];
    }

    public void moveAvatarUp() {
        boolean isDoor = false;
        avPointer.up();
        if (canMove()){
            isDoor = isDoor();
            avPointer.down();
            replaceAv();
            avPointer.up();
            if (isDoor){
                avPointer.up();
            }
            drawAvatar();
        } else {
            avPointer.down();
        }
    }

    public void moveAvatarDown() {
        boolean isDoor = false;
        avPointer.down();
        if (canMove()){
            isDoor = isDoor();
            avPointer.up();
            replaceAv();
            avPointer.down();
            if (isDoor){
                avPointer.down();
            }
            drawAvatar();
        } else {
            avPointer.up();
        }
    }

    public void moveAvatarLeft() {
        boolean isDoor = false;
        avPointer.left();
        if (canMove()){
            isDoor = isDoor();
            avPointer.right();
            replaceAv();
            avPointer.left();
            if (isDoor){
                avPointer.left();
            }
            drawAvatar();
        } else {
            avPointer.right();
        }
    }

    public void moveAvatarRight() {
        boolean isDoor = false;
        avPointer.right();
        if (canMove()){
            isDoor = isDoor();
            avPointer.left();
            replaceAv();
            avPointer.right();
            if (isDoor){
                avPointer.right();
            }
            drawAvatar();
        } else {
            avPointer.left();
        }
    }

    public void replaceAv(){
        //if (world[avPointer.getX()][avPointer.getY()].equals(Tileset.FLOOR_TWO)){
            drawTile(avPointer.getX(), avPointer.getY(), Tileset.FLOOR_TWO);
        //} else if (world[avPointer.getX()][avPointer.getY()].equals(Tileset.DOOR)) {

        //}

    }

    public void randomAvatarRoomPosition() {
        avPointer.setRandomPointer(0, WIDTH, 0, HEIGHT, seedVal);
        while(!canMove()){
            avPointer.setRandomPointer(0, WIDTH, 0, HEIGHT, seedVal);
        }
        drawAvatar();
    }

    public void setAvatarPosition(int x, int y){
        avPointer.setX(x);
        avPointer.setY(y);
        if (!canMove()){
            randomAvatarRoomPosition();
        }
        drawAvatar();
    }

    public void drawAvatar(){
        if (canMove()){
            drawTile(avPointer.getX(), avPointer.getY(), Tileset.AVATAR_TWO);
        }
    }

    //checks if room would conflict with existing room or the edge of the world
    private boolean checkConflict (int Height, int Width){
        int tempX = pointer.getX();
        int tempY = pointer.getY();
        if ((tempX + Width >= WIDTH) || tempY + Height >= HEIGHT){
            return true;
        }
        TETile currTile;
        for (int w = 0; w < Width; w++){
            for (int h = 0; h < Height; h++){
                currTile = world[tempX][tempY];
                if (currTile.equals(Tileset.FLOOR_TWO)){
                    return true;
                } else if (currTile.equals(Tileset.WALL_TWO)) {
                    return true;
                }
                tempY++;
            }
            tempX++;
            tempY = pointer.getY();
        }
        return false;
    }

    public void createRooms(int numberRooms){
        setRandomPointer();
        currRoom = drawRandomRoom();
        firstRoom = currRoom;

        for (int i = 0; i < numberRooms; i++){
            setRandomPointer();
            Room newRoom = drawRandomRoom();
            //Connect newRoom with currRoom and then make currRoom = newRoom
            createHallway(newRoom);
            currRoom = newRoom;
        }
    }

    //puts the pointer at a random location in the world 2D array;
    private void setRandomPointer(){
        pointer.setRandomPointer(1, WIDTH - roomMaxSize, 1, HEIGHT - roomMaxSize, seedVal);
    }

    //returns world
    public TETile[][] getWorld(){
        return world;
    }


    // Fills all the world with NOTHING tiles
    public void fillTilesNothing() {
        fillTiles(Tileset.NOTHING);
    }

    //fills the entire world with a certain TETile type
    public void fillTiles(TETile tile) {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < HEIGHT; j++) {
                drawTile(i, j, tile);
            }
        }
    }

    public void fillWithRandomTiles() {

    }
    public void checkFloor(TETile tile){
        if (tile.equals(Tileset.FLOOR_TWO)){
            isFloor[pointer.getX()][pointer.getY()] = true;
        } else {
            isFloor[pointer.getX()][pointer.getY()] = false;
        }
    }
    //draws a tile at the current pointer location
    public void drawTile(TETile tile){
        drawTile(pointer.getX(), pointer.getY(), tile);
    }

    //draws a tile at point x,y; does not change pointer
    public void drawTile(int x, int y, TETile tile){
        world[x][y] = tile;
        checkFloor(tile);
    }

    //draws a vertical line upwards from pointer for length length, moves pointer to the final tile drawn
    public void drawLineVert(int length, TETile tile){
        for(int i = 0; i < length; i++){
            drawTile(tile);
            pointer.up();
        }
        pointer.down();
    }

    //draws a horizontal line from pointer to the right for length length, moves pointer to the final tile drawn
    public void drawLineHoriz(int length, TETile tile){
        for(int i = 0; i < length; i++){
            drawTile(tile);
            pointer.right();
        }
        pointer.left();
    }

    //draws a line upwards from pointer for length length, moves pointer with it
    public void drawLineUp(int length, TETile tile){
        drawLineVert(length, tile);
    }

    //draws a line downwards from pointer, moves pointer with it
    public void drawLineDown(int length, TETile tile){
        pointer.down(length);
        pointer.up();
        drawLineVert(length, tile);
        pointer.down(length);
        pointer.up();
    }

    //draws a line to the right of pointer, moves pointer with it
    public void drawLineRight(int length, TETile tile){
        drawLineHoriz(length, tile);
    }

    //draws a line left of pointer, moves pointer with it
    public void drawLineLeft(int length, TETile tile){
        pointer.left(length);
        pointer.right();
        drawLineHoriz(length, tile);
        pointer.left(length);
        pointer.right();
    }

    //draws a rectangular box of tile type tile with pointer being the bottom left corner, with width width and height height; pointer returns where it started
    public void drawRectangleBorder(int width, int height, TETile tile){
        drawLineUp(height, tile);
        drawLineRight(width, tile);
        drawLineDown(height, tile);
        drawLineLeft(width, tile);
    }

    //draws a filled in rectangle of tile type tile with width width and height height, pointer ends at the top right corner
    public void drawRectangle(int width, int height, TETile tile){
        for (int h = 0; h < height; h++){
            for (int w = 0; w < width; w++){
                drawTile(tile);
                pointer.right();
            }
            pointer.left(width);
            pointer.up();
        }
        pointer.right(width);
    }

    //draws the border of a room with width and height
    public void drawRoomBorder(int width, int height){
        drawRectangleBorder(width, height, Tileset.WALL_TWO);
    }

    //draws the floor of a room with appropriate width and height
    public void drawRoomFloor(int floorWidth, int floorHeight){
        drawRectangle(floorWidth, floorHeight, Tileset.FLOOR_TWO);
    }


    // Draws a randomly wwd room
    public Room drawRandomRoom() {
        int roomMin = 5;
        int roomMax = roomMaxSize;
        int roomWidth = RandomUtils.uniform(seedVal,roomMin, roomMax);
        int roomHeight = RandomUtils.uniform(seedVal, roomMin, roomMax);
        while (checkConflict(roomHeight, roomWidth)){
            setRandomPointer();
            roomWidth = RandomUtils.uniform(seedVal,roomMin, roomMax);
            roomHeight = RandomUtils.uniform(seedVal, roomMin, roomMax);
        }
        Room newRoom = new Room(roomWidth, roomHeight, pointer, seedVal);
        drawRoomBorder(roomWidth, roomHeight);
        pointer.up();
        pointer.right();
        drawRoomFloor(roomWidth - 2, roomHeight - 2);
        //drawRoomDoor(newRoom);
        return newRoom;
    }

    public void drawRoomDoor(Room room){
        Pointer edgePointer = room.getRandomEdgePointer();
        room.setDoorPtr(edgePointer);
        drawDoor(room.doorPtr.getX(), room.doorPtr.getY());

    }
    public void drawDoor(int x, int y){
        drawTile(x, y, Tileset.DOOR);
        isDoor[x][y] = true;
    }

    //Creates a hallway between the last room and a new room
    public void createHallway(Room newRoom){
        Pointer currRoomPointer = currRoom.getRandomEdgePointer();
        Pointer newRoomPointer = newRoom.getRandomEdgePointer();
        //Pointer currRoomPointer = currRoom.doorPtr;
        //Pointer newRoomPointer = newRoom.doorPtr;
        int hDiff = currRoomPointer.getX() - newRoomPointer.getX();
        int vDiff = currRoomPointer.getY() - newRoomPointer.getY();
        pointer.setX(currRoomPointer.getX());
        pointer.setY(currRoomPointer.getY());
        if (hDiff > 0){
            //drawLineLeft(hDiff, Tileset.FLOOR_TWO);
            //pointer.left();
            drawHallLeft(hDiff);
        } else if (hDiff < 0){
            //pointer.right();
            drawHallRight((-hDiff) );
        }
        if (vDiff > 0){
            //drawLineDown(vDiff, Tileset.FLOOR_TWO);
            //pointer.down();
            drawHallDown(vDiff );
        } else if (vDiff < 0){
            //drawLineUp(vDiff, Tileset.FLOOR_TWO);
            //pointer.up();
            drawHallUp((-vDiff) );
        }
    }

    public boolean isNothing (TETile tile){
        if (tile.equals(Tileset.NOTHING)){
            return true;
        }
        return false;
    }


    //draws a vertical line upwards from pointer for length length, moves pointer to the final tile drawn
    public void drawHallUp(int length) {
        for (int i = 0; i < length; i++) {
            drawTile(Tileset.FLOOR_TWO);
            addHallwayWalls();
            pointer.up();
        }
        pointer.down();
    }

    public void drawHallDown(int length) {
        for (int i = 0; i < length; i++) {
            drawTile(Tileset.FLOOR_TWO);
            addHallwayWalls();
            pointer.down();
        }
        pointer.up();
    }

    //draws a horizontal line from pointer to the right for length length, moves pointer to the final tile drawn
    public void drawHallRight(int length){
        for(int i = 0; i < length; i++) {
            drawTile(Tileset.FLOOR_TWO);
            addHallwayWalls();
            pointer.right();
        }
        pointer.left();
    }

    public void drawHallLeft(int length){
        for(int i = 0; i < length; i++) {
            drawTile(Tileset.FLOOR_TWO);
            addHallwayWalls();
            pointer.left();
        }
        pointer.right();
    }

    public void addHallwayWalls(){
        pointer.up();
        if(isNothing(world[pointer.getX()][pointer.getY()])){
            drawTile(Tileset.WALL_TWO);
        }
        pointer.down();
        pointer.right();
        if(isNothing(world[pointer.getX()][pointer.getY()])){
            drawTile(Tileset.WALL_TWO);
        }
        pointer.left();
        pointer.down();
        if(isNothing(world[pointer.getX()][pointer.getY()])){
            drawTile(Tileset.WALL_TWO);
        }
        pointer.up();
        pointer.left();
        if(isNothing(world[pointer.getX()][pointer.getY()])){
            drawTile(Tileset.WALL_TWO);
        }
        pointer.right();
    }


    public TETile getTileAtPointer(){
        return getTile(pointer.getX(), pointer.getY());
    }

    public TETile getTile(int x, int y){
        return world[x][y];
    }

}