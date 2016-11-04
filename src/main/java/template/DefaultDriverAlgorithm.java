package template;

import java.io.File;
import java.io.Serializable;

import cicontest.algorithm.abstracts.AbstractAlgorithm;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.controller.Driver;
import race.TorcsConfiguration;

public class DefaultDriverAlgorithm implements Serializable {

    FuocoDriverGenome[] drivers = new FuocoDriverGenome[1];
    int[] results = new int[1];

    public Class<? extends Driver> getDriverClass() {
        return FuocoDriver.class;
    }

    //TODO pass useful parameters
    public void run() {

        //init NN
        FuocoDriverGenome genome = new FuocoDriverGenome();
        drivers[0] = genome;

        //Start a race
        FuocoRace race = new FuocoRace();
        race.setTrack("aalborg", "road");
        race.laps = 1;

        //for speedup set withGUI to false
        results = race.runRace(drivers, true);

        // Save genome/nn
        DriversUtils.storeGenome(drivers[0]);

    }

    public static void main(String[] args) {

        //Set path to torcs.properties
        TorcsConfiguration.getInstance().initialize(new File("torcs.properties"));

        DefaultDriverAlgorithm algorithm = new DefaultDriverAlgorithm();
        DriversUtils.registerMemory(algorithm.getDriverClass());

        algorithm.run();

    }

}