package template;

import cicontest.torcs.genome.IGenome;
import scr.SensorModel;
import scr.Action;

import java.io.*;

public class Core implements Serializable {

    public Action computeAction(SensorModel a) {
        //TODO run algorithm to get the output action
        Action action = new Action();
        return action;
    }

    //Store the state of this neural network
    public void storeGenome(String fileName) {
        //TODO store genome into fileName
    }

    public void loadGenome(IGenome genome) {

        // TODO read genome and set parameters

    }

}
