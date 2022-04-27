package byow.Core.InputTools;

import edu.princeton.cs.introcs.StdDraw;

public class MyKeyboardInputSource implements InputType {
    private static final boolean PRINT_TYPED_KEYS = false;

    public char getNextKey() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                char c = Character.toUpperCase(StdDraw.nextKeyTyped());
                if (PRINT_TYPED_KEYS) {
                    System.out.print(c);
                }
                return c;
            }
        }
    }

    public boolean hasNext(){
        return StdDraw.hasNextKeyTyped();
    }
    public boolean possibleNextInput() {
        return true;
    }
}
