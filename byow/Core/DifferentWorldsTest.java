package byow.Core;

import byow.TileEngine.TETile;
import org.junit.Test;
import static org.junit.Assert.*;


public class DifferentWorldsTest {

    @Test
    public void test(){
        Engine eng = new Engine();
        TETile[][] world = eng.interactWithInputString("n5643591630821615871swwaawd");
        String testString = TETile.toString(world);
        System.out.println(testString);

        world = eng.interactWithInputString("n7313251667695476404sasdw");
        String testString2 = TETile.toString(world);
        System.out.println(testString2);


        world = eng.interactWithInputString("n8772076153521736045sawsasdsadwwwwsa");
        String testString3 = TETile.toString(world);
        System.out.println(testString3);

        world = eng.interactWithInputString("n6547766204324870169ssdswa");
        String testString4 = TETile.toString(world);
        System.out.println(testString4);
        System.out.println("poop");

        System.out.println(testString);
        System.out.println(testString2);
        assertNotEquals(testString, testString2);
        assertNotEquals(testString2, testString3);
        assertNotEquals(testString, testString3);
        assertNotEquals(testString, testString4);

    }

}
