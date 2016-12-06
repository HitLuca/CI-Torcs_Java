package fuoco;

import java.io.FileNotFoundException;
import java.io.PrintStream;


public class Logger {
    private static PrintStream sout = System.out;

    public static void println(Object s) {
        System.setOut(sout);
        System.out.println(s);
        try {
            System.setOut(new PrintStream("/dev/null"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

}
