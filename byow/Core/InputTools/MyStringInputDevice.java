package byow.Core.InputTools;

import byow.InputDemo.InputSource;

/**
 * Created by hug.
 */
public class MyStringInputDevice implements InputType {
    private String input;
    private int index;

    public MyStringInputDevice(String s) {
        index = 0;
        input = s;
    }

    public char getNextKey() {
        char returnChar = input.charAt(index);
        index += 1;
        return returnChar;
    }

    public boolean hasNext(){
        return possibleNextInput();
    }

    public boolean possibleNextInput() {
        return index < input.length();
    }
}
