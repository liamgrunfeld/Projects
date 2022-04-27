package byow.Core;

import byow.Core.InputTools.InputType;
import byow.Core.InputTools.MyKeyboardInputSource;
import byow.Core.InputTools.MyStringInputDevice;
import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.*;
import java.io.*;

public class Engine implements Serializable{
    private final boolean DEBUG = true; //set to false when submitting to AG
    TERenderer ter = new TERenderer();
    /* Feel free to change the width and height. */
    public static final int WIDTH = 80;
    public static final int HEIGHT = 40;
    public static final int VIS_DIST = 5;
    public static final File worldFile = new File("world.txt");
    public static final File nameFile = new File("character_name.txt");
    private WorldGenerator worldGen;
    public TETile[][] world;
    private TETile[][] LOSworld;
    public InputType inputSource;
    public int inputType; //0 --> Keyboard, 1 --> String
    private String currHUDText = "Nothing";
    private static String characterName;
    private boolean LOS = false;



    public static void main (String[] args){
        Engine test = new Engine();
        TETile[][] testWorld;
        TETile[][] testWorld2;
        TERenderer testRend = new TERenderer();
        testRend.initialize(WIDTH, HEIGHT);
        //testWorld2 = test.interactWithInputString("n7193300625454684331saaawasdaawdwsd");
        testWorld = test.interactWithInputString("n7193300625454684331saaawasdaawd:q");
        testWorld = test.interactWithInputString("lwsd");
        testRend.renderFrame(testWorld);


        ///test.interactWithKeyboard();
    }

    public Engine(){
        if (!worldFile.exists()) { //creates file for the world if it doesnt already exist
            Utils.writeContents(worldFile, "");
        }
        if (!nameFile.exists()){
            Utils.writeObject(nameFile, "BOB");
        }
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */

    /** public void Engine(){
     WorldGenerator newWorld = new WorldGenerator();
     world = newWorld.getWorld();
     } */

    //parses through initial menu keyboard input
    public void interactWithKeyboard() {
        inputType = 0;
        inputSource = new MyKeyboardInputSource();
        StdDraw.setCanvasSize(WIDTH * 16, HEIGHT * 16);
        drawIntroGui();
        while (inputSource.possibleNextInput()){
            if (inputSource.hasNext()) {
                char currKey = inputSource.getNextKey();
                if (currKey == 'n' || currKey == 'N') { //creates a new game and runs it
                    createNewGame();
                    loadName();
                    runGame();
                } else if (currKey == 'l' || currKey == 'L') { //loads a new game and runs it
                    if (loadGame()) {
                        runGame();
                    } else {
                        if (DEBUG) {
                            System.exit(0);
                        }
                    }
                } else if(currKey == 'c' || currKey == 'C'){
                    characterName = getCharacterName();
                    saveName();
                    drawIntroGui();
                } else if (currKey == 'q' || currKey == 'Q') {
                    if (DEBUG) {
                        System.exit(0);
                    } else {
                        break;
                    }
                }
            }
        }

    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     *
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     *
     * In other words, both of these calls:
     *   - interactWithInputString("n123sss:q")
     *   - interactWithInputString("lww")
     *
     * should yield the exact same world state as:
     *   - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        // TODO: Fill out this method so that it run the engine using the input
        // passed in as an argument, and return a 2D tile representation of the
        // world that would have been drawn if the same inputs had been given
        // to interactWithKeyboard().
        //
        // See proj3.byow.InputDemo for a demo of how you can make a nice clean interface
        // that works for many different input types.

        inputType = 1;
        inputSource = new MyStringInputDevice(input);
        while (inputSource.possibleNextInput()){
            //if (inputSource.possibleNextInput()) {
            char currKey = inputSource.getNextKey();
            if (currKey == 'n' || currKey == 'N') {
                createNewGame();
                runGame();
            } else if (currKey == 'l' || currKey == 'L') {
                if (loadGame()) {
                    runGame();
                }
            } else if (currKey == 'q' || currKey == 'Q') {
                if (DEBUG) {
                    System.exit(0);
                }
            }
        }
        updateWorld();
        return world;
    }

    public TETile[][] formLOS(){
        TETile[][] newLOS = new TETile[WIDTH][HEIGHT];
        Pointer LOSptr = worldGen.getAvatarPosition();
        int avX = LOSptr.getX();
        int avY = LOSptr.getY();
        int leftLim = avX - VIS_DIST;
        int rightLim = avX + VIS_DIST;
        int upLim = avY + VIS_DIST;
        int lowLim = avY - VIS_DIST;

        newLOS = copyWorld(world);
        for(int x = 0; x < WIDTH; x++){
            for (int y = 0; y < HEIGHT - 1; y++){
                if ((x < leftLim || x > rightLim) || y < lowLim || y > upLim)           {
                    //drawNothingLOS(x, y);
                    newLOS[x][y] = Tileset.NOTHING;
                }
            }
        }
        return newLOS;
    }

    public TETile[][] copyWorld(TETile[][] world){
        TETile[][] tempWorld = new TETile[world.length][world[0].length];
        for (int x = 0; x < world.length; x++){
            for (int y = 0; y < world[0].length; y++){
                tempWorld[x][y] = world[x][y];
            }
        }
        return tempWorld;
    }

    public void drawNothingLOS(int x, int y){
        LOSworld[x][y] = Tileset.NOTHING;
    }

    /** if inputType is keyboard, draws the HUD*/
    public void HUD(){
        if (inputType == 0) {
            if (!getMouseTile().description().equals(currHUDText)) {
                drawHUD(getMouseTile().description());
            }
        }
    }

    /** draws the HUD */
    public void drawHUD(String tileName){
        Font HUD = new Font("Monaco", Font.BOLD, 20);
        StdDraw.setFont(HUD);
        StdDraw.setPenColor(new Color(54, 195, 255));
        StdDraw.filledRectangle(WIDTH/16, HEIGHT, 5, 2);
        StdDraw.filledRectangle(WIDTH - 5, HEIGHT, 5, 2);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(WIDTH/16, HEIGHT - 1, tileName);
        StdDraw.text(WIDTH - 5, HEIGHT - 1, characterName);

        StdDraw.enableDoubleBuffering();
        StdDraw.show();
    }

    /** Gets the tile that the cursor is currently over */
    public TETile getMouseTile(){
        double x = StdDraw.mouseX();
        double y = StdDraw.mouseY();
        if (x < WIDTH - 2 && y < HEIGHT - 2) {
            return world[(int) x][(int) y];
        }
        return Tileset.NOTHING;
    }

    /** Runs the game, parsing through input to decide actions, updates the world, draws the updated world if inputType is keyboard */
    public void runGame(){
        if (inputType == 0){
            //String currMouseTile = getMouseTile().description();
            drawWorld();
            HUD();
        }
        while (inputSource.possibleNextInput()) {
            if (inputType == 0) {
                HUD();
            }
            if (inputSource.hasNext()){
                char currKey = inputSource.getNextKey();
                if (currKey == ':') {
                    //if (inputSource.possibleNextInput()) {
                        currKey = inputSource.getNextKey();
                        if (currKey == 'q' || currKey == 'Q') {
                            updateWorld();
                            quitGame();
                            break;
                        }
                    //}
                }
                if (currKey == 'a' || currKey == 'A') {
                    worldGen.moveAvatarLeft();
                } else if (currKey == 's' || currKey == 'S') {
                    worldGen.moveAvatarDown();
                } else if (currKey == 'd' || currKey == 'D') {
                    worldGen.moveAvatarRight();
                } else if (currKey == 'w' || currKey == 'W') {
                    worldGen.moveAvatarUp();
                    world = worldGen.getWorld();
                } else if (currKey == 'v' || currKey == 'V'){
                    toggleLOS();
                }
                updateWorld();
                if (inputType == 0) {
                    drawWorld();
                    HUD();
                }
            }
            updateWorld();
        }
    }

    public void toggleLOS(){
        if (LOS){
            LOS = false;
        } else {
            LOS = true;
        }
    }

    /** updates the world object to the reflect the world in worldGen */
    public void updateWorld(){
        world = worldGen.getWorld();
        LOSworld = formLOS();
    }


    /** draws the current world */
    public void drawWorld(){
        //ter.initialize(WIDTH, HEIGHT);
        if (LOS){
            ter.renderFrame(LOSworld);
        } else {
            ter.renderFrame(world);
        }
    }

    /** creates new instance of the game, getting the seed from the user, and creating a new world with the seed */
    public void createNewGame(){ //gets a new seed and creates a world with it
        long seed = formSeed();

        createNewWorld(seed);
    }

    /** Gets the seed from the user, stopping at s */
    public long formSeed(){ //forms the seed as the user gives input
        String seed = "";
        if (inputType == 0) {
            drawSeedGui(seed);
        }
            char currKey = 'a';
            while (currKey != 's' && currKey != 'S'){ //adds input numbers to string until user inputs s
                currKey = inputSource.getNextKey();
                if (Character.isDigit(currKey)){
                    seed = seed + currKey;
                    if (inputType == 0) {
                        drawSeedGui(seed);
                    }
                }
            }

        return Long.parseLong(seed);
    }

    public String getCharacterName(){ //forms the seed as the user gives input
        String charName = "";
        if (inputType == 0) {
            drawCharGui(charName);
        }
        char currKey = ' ';
        while (currKey != '*'){ //adds input numbers to string until user inputs s
            currKey = inputSource.getNextKey();
            if (Character.isLetter(currKey)){
                charName = charName + currKey;
                if (inputType == 0) {
                    drawCharGui(charName);
                }
            }
        }
        return charName;
    }

    /**creates the new world with a seed */
    public void createNewWorld(long seed){ //creates a new world generator with the provided string and sets world
        long longSeed = seed;
        worldGen = new WorldGenerator(longSeed, WIDTH, HEIGHT - 1);
        updateWorld();
    }

    /** loads the game from world.txt */
    public boolean loadGame(){
        if (Utils.readContentsAsString(worldFile).equals("")){
            return false;
        } else {
            worldGen = Utils.readObject(worldFile, WorldGenerator.class);
            updateWorld();
            loadName();
            return true;
        }
    }

    public void loadName(){
        characterName = Utils.readObject(nameFile, String.class);
    }

    /** saves the game and quits */
    public void quitGame() {
        saveGame();
        saveName();
        if (DEBUG) {
            System.exit(0);
        }
    }

    /** saves the game to world.txt */
    public void saveGame() {
        Utils.writeObject(worldFile, worldGen);
    }

    public void saveName(){
        Utils.writeObject(nameFile, characterName);
    }

    /** draws the Gui asking for the seed from the user */
    public void drawSeedGui(String seed){
        String header = "Please enter a string";
        Font titleFont = new Font("Monaco", Font.BOLD, WIDTH * 16 / 20);
        Font optionsFont = new Font("Monaco", Font.PLAIN, WIDTH * 16 / 40);
        StdDraw.setFont(titleFont);
        StdDraw.setPenColor(Color.white);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(new Color(0, 100, 100));
        StdDraw.text(WIDTH/2, HEIGHT * 3 / 4, header);
        StdDraw.setFont(optionsFont);
        StdDraw.text(WIDTH/2, HEIGHT * 4 / 10, seed);
        StdDraw.enableDoubleBuffering();
        StdDraw.show();
    }
    /** draws the Gui asking for the seed from the user */
    public void drawCharGui(String characterName){
        String header = "Please enter a name";
        Font titleFont = new Font("Monaco", Font.BOLD, WIDTH * 16 / 20);
        Font optionsFont = new Font("Monaco", Font.PLAIN, WIDTH * 16 / 40);
        StdDraw.setFont(titleFont);
        StdDraw.setPenColor(Color.white);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(new Color(0, 100, 100));
        StdDraw.text(WIDTH/2, HEIGHT * 3 / 4, header);
        StdDraw.setFont(optionsFont);
        StdDraw.text(WIDTH/2, HEIGHT * 4 / 10, characterName);
        StdDraw.enableDoubleBuffering();
        StdDraw.show();
    }
    /** draws the GUI of the initial menu */
    public void drawIntroGui(){
        int guiWidth = WIDTH * 16;
        int guiHeight = HEIGHT * 16;
        String gameName = "A Game";
        String newGame = "New Game (N)";
        String loadGame = "Load Game (L)";
        String characterGUI = "Change Character Name (C)";
        String quit = "Quit (Q)";
        Font titleFont = new Font("Monaco", Font.BOLD, guiWidth / 20);
        Font optionsFont = new Font("Monaco", Font.PLAIN, guiWidth / 40);
        StdDraw.setFont(titleFont);
        StdDraw.setPenColor(Color.white);
        StdDraw.setXscale(0, WIDTH);
        StdDraw.setYscale(0, HEIGHT);
        StdDraw.clear(new Color(0, 100, 100));
        StdDraw.text(WIDTH/2, HEIGHT * 3 / 4, gameName);
        StdDraw.setFont(optionsFont);
        StdDraw.text(WIDTH/2, HEIGHT * 4 / 10, newGame);
        StdDraw.text(WIDTH/2, HEIGHT * 3 / 10, loadGame);
        StdDraw.text(WIDTH/2, HEIGHT * 2 / 10, characterGUI);
        StdDraw.text(WIDTH/2, HEIGHT * 1 / 10, quit);
        StdDraw.enableDoubleBuffering();
        StdDraw.show();
    }
}