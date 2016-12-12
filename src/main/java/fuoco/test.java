package fuoco;

import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.genome.IGenome;
import cicontest.torcs.race.RaceResult;
import race.TorcsConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Created by luca on 12/9/16.
 */
public class test {
    public static void main(String[] args) throws Exception {
//        PairDriving driver = new PairDriving();
        TorcsConfiguration.getInstance().initialize(new File("torcs.properties"));
        DriversUtils.registerMemory(FuocoDriver.class);

        int driversNumber = 4;
        IGenome[] drivers = new IGenome[driversNumber];
        for (int i = 0; i < driversNumber; i++) {
            drivers[i] = new FuocoCoreGenome("memory/nets", 8, 1.5201523);
        }

        FuocoRace race = new FuocoRace();
        race.setTrack("e-track-4", "road");
        race.laps = 5;

        RaceResult[] r = race.runRace(drivers, true);
    }
}
