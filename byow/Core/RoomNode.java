package byow.Core;

public class RoomNode{
    private int Max_X = Engine.WIDTH;
    private int Max_Y = Engine.HEIGHT;
    private RoomNode above;
    private RoomNode below;
    private RoomNode left;
    private RoomNode right;
    private Pointer ptr;
    public int x;
    public int y;
    public boolean isVisited;
    public boolean isRoom;

    public RoomNode(int x, int y){
        this.x = x;
        this.y = y;
        ptr = new Pointer(x, y);
    }

    public void setAbove(RoomNode node){

        above = node;
    }
    public void setBelow(RoomNode node){
        below = node;
    }
    public void setLeft(RoomNode node){
        left = node;
    }
    public void setRight(RoomNode node){
        left = node;
    }

    public void visit(){
        isVisited = true;
    }

    public void markRoom(){
        isRoom = true;
    }

}
