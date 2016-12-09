package fuoco;

import java.io.FileNotFoundException;
import java.io.PrintStream;


public class Logger {
    private static PrintStream sout = System.out;
    private static PrintStream fakeOut;

    public static void init() {
        try {
            fakeOut = new PrintStream("/dev/null");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        System.setOut(fakeOut);
    }

    public static void println(Object s) {
        System.setOut(sout);
        System.out.println(s);
        System.setOut(fakeOut);
    }

}
