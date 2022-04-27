package byow.Core;

import java.io.Serializable;
import java.util.Random;

public class Pointer implements Serializable {
    private int x;
    private int y;
    private int X_MAX;
    private int Y_MAX;

    public Pointer(){
        this.x = 0;
        this.y = 0;
    }

    public Pointer(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void setRandomPointer(int xMin, int xMax, int yMin, int yMax, Random gen){
        setX(RandomUtils.uniform(gen, xMin, xMax));
        setY(RandomUtils.uniform(gen, yMin, yMax));
    }

    public void printPointer(){
        System.out.println("Pointer is at: (" + x +", " + y + ")");
    }
    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public void setX(int x){
        this.x = x;
    }

    public void setY(int y){
        this.y = y;
    }

    public void up(){
        up(1);
    }

    public void up(int distance){
        y += distance;
    }

    public void down(){
        down(1);
    }

    public void down(int distance){
        y -= distance;
    }

    public void right(){
        right(1);
    }

    public void right(int distance){
        x += distance;
    }

    public void left(){
        left(1);
    }

    public void left(int distance){
        x-= distance;
    }

}
