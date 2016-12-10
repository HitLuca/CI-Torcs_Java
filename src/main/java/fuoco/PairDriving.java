package fuoco;

import java.io.*;

/**
 * Created by luca on 12/9/16.
 */
public class PairDriving {
    private String rootDir = "pairDriving/";
    private String filename1 = "0.txt";
    private String filename2 = "1.txt";

    private BufferedReader alliedReader;
    private BufferedWriter writer;

    private double myDistance = 0;
    private double alliedDistance;

    private boolean first = false;

    public PairDriving() throws IOException, InterruptedException {
        new File(rootDir).mkdirs();

        File myFile = new File(rootDir + filename1);

        if (!myFile.exists()) {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rootDir + filename1)));
            System.out.println(rootDir + filename1 + " created!");
            myDistance += 3;
            writer.write(Double.toString(myDistance));
            writer.flush();

            File alliedFile = new File(rootDir + filename2);
            while(!alliedFile.exists()) {
                System.out.println(rootDir + filename2 + " doesn't exist...");
                Thread.sleep(1000);
            }
            System.out.println(rootDir + filename2 + " has been found!");
            alliedReader = new BufferedReader(new InputStreamReader(new FileInputStream(rootDir + filename2)));
        } else {
            alliedReader = new BufferedReader(new InputStreamReader(new FileInputStream(rootDir + filename1)));
            System.out.println(rootDir + filename1 + " has been found!");
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(rootDir + filename2)));
            System.out.println(rootDir + filename2 + " created!");
            writer.write(Double.toString(myDistance));
            writer.flush();
        }

        checkFirst();
    }

    public void checkFirst() throws IOException {
        alliedDistance = Double.parseDouble(alliedReader.readLine());
        System.out.println("Allied distance is " + alliedDistance);
        System.out.println("My distance is " + myDistance);

        if (myDistance > alliedDistance) {
            first = true;
        } else {
            first = false;
        }
        System.out.println("I'm first? " + first);
    }
}
