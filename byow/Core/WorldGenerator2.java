package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import javax.swing.event.TreeSelectionEvent;
import java.util.*;

public class WorldGenerator2 {
    private static final int MIN_NODES = 5;
    private static final int MAX_NODES = 15;
    private int WIDTH;
    private int HEIGHT;
    private TETile[][] worldArr;
    private TETile[][] worldArrLOS;
    private Random gen;
    private int numNodes;
    private int numRooms;
    private Pointer ptr;
    private int[][] nodeMap;
    private boolean[][] isVisited;
    private boolean[][] isFloor;
    private boolean[][] isDoor;
    private RoomNode[][] nodeArr;
    private Stack fringe = new Stack();
    private LinkedList<Pointer> visitedNodes;

    public static void main (String[] args){
        TERenderer render = new TERenderer();
        render.initialize(80, 40);
        WorldGenerator2 test = new WorldGenerator2(1234,80, 40);
        render.renderFrame(test.getWorldArr());
    }

    public WorldGenerator2(long seed, int width, int height){
        WIDTH = width;
        HEIGHT = height;
        ptr = new Pointer();
        worldArr = new TETile[WIDTH][HEIGHT];
        worldArrLOS = new TETile[WIDTH][HEIGHT];
        gen = new Random(seed);
        numNodes = RandomUtils.uniform(gen, MIN_NODES, MAX_NODES);
        numRooms = numNodes; //maybe change later
        nodeMap = new int[numNodes][2];
        isVisited = new boolean[WIDTH][HEIGHT];
        isFloor = new boolean[WIDTH][HEIGHT];
        isDoor = new boolean[WIDTH][HEIGHT];
        visitedNodes = new LinkedList<>();
        nodeArr = new RoomNode[WIDTH][HEIGHT];
        populateNodeArr();
        fillTilesNothing();
        createWorld();
    }

    public void populateNodeArr(){
        for (int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT; y++){
                nodeArr[x][y] = new RoomNode(x, y);
            }
        }
    }

    public TETile[][] getWorldArr(){
        return worldArr;
    }

    public void setRandPtr(){
        ptr.setRandomPointer(1, WIDTH - 1, 1, HEIGHT - 1, gen);
    }

    public void createWorld (){
        setRandPtr();
        while (!canPlace()){
            setRandPtr();
        }
        nodeArr[ptr.getX()][ptr.getY()].markRoom();
        fringe.push(nodeArr[ptr.getX()][ptr.getY()]);
        int nodesDrawn = 0;
        while (!fringe.isEmpty() && nodesDrawn < 100){
            RoomNode currNode = (RoomNode) fringe.pop();
            if (!currNode.isVisited) {
                ptr.setX(currNode.x);
                ptr.setY(currNode.y);
                if (canPlace()) {
                    placeFloorNode();
                }
                currNode.visit();

                if (currNode.x != 0) {
                    fringe.add(nodeArr[currNode.x - 1][currNode.y]);
                }
                if (currNode.x < WIDTH - 1) {
                    fringe.add(nodeArr[currNode.x + 1][currNode.y]);
                }

                if (currNode.y != 0) {
                    fringe.add(nodeArr[currNode.x][currNode.y - 1]);
                }

                if (currNode.y < HEIGHT - 1) {
                    fringe.add(nodeArr[currNode.x][currNode.y + 1]);
                }
                nodesDrawn++;

            }
        }
    }


    public LinkedList<Pointer> unvisitedNeighbors () {
        LinkedList<Pointer> unvisited = new LinkedList<>();
        return null;
    }


    public Pointer getNextNode(){
        return (Pointer) fringe.pop();
    }

    public void placeNodes(){
        setRandPtr();
        for (int i = 0; i < numNodes; i++){
            if(canPlace()) {
                placeFloorNode();
                i++;
            }
        }
    }

    /*
    public void getNextNode(){
        setRandPtr();
        while (isConnected())
    } */

    public void expandNode(){
        int targetWidth =RandomUtils.uniform(gen, 5, 10);
        int targetHeight = RandomUtils.uniform(gen, 5, 10);

    }

    /** places a node at ptr */
    public void placeFloorNode(){
        placeNode(ptr.getX(), ptr.getY(), Tileset.FLOOR_TWO);
        isFloor[ptr.getX()][ptr.getY()] = true;
    }

    public void placeDoor(){
        placeNode(ptr.getX(), ptr.getY(), Tileset.DOOR);
        isDoor[ptr.getX()][ptr.getY()] = true;
    }


    /** places a node at x, y and sets isVisted == true; */
    public void placeNode(int x, int y, TETile tile){
        drawTile(x, y, tile);
        isVisited[x][y] = true;
        visitedNodes.add(new Pointer(x, y));
    }

    public void drawTile(TETile tile){
        drawTile(ptr.getX(), ptr.getY(), tile);
    }

    public void drawTile(int x, int y, TETile tile){
        worldArr[x][y] = tile;
    }


    /** checks if a node can be placed at ptr*/
    public boolean canPlace (){
        return canPlace(ptr.getX(), ptr.getY());
    }

    /**checks if a node can be placed at x,y*/
    public boolean canPlace(int x, int y){
        if (!isVisited[x][y]){
            return true;
        }
        return false;
    }
    public boolean adjToVisted(){
        return adjToVisited(ptr.getX(), ptr.getY());
    }
    public boolean adjToVisited(int x, int y){
        return true;
    }
    /** checks if arr[x1][y1] is adjacent to arr[x2][y2] */
    public boolean isConnected(int x1, int y1, int x2, int y2){
        if (x1 == x2 - 1){
            return true;
        } else if (x1 == x2 + 1){
            return true;
        }else if (y1 == y2 - 1){
            return true;
        } else if (y1 == y2 + 1){
            return true;
        }
        return false;
    }

    public LinkedList<Pointer> visitedNodes(){
        LinkedList<Pointer> visitedList = new LinkedList<>();
        for (int x = 0; x < isVisited.length; x ++){
            for (int y = 0; y < isVisited[x].length; y++){
                if (isVisited[x][y] == true){
                    visitedList.add(new Pointer(x, y));
                }
            }

        }
        return visitedList;
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
}
