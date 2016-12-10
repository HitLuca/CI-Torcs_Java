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

        IGenome[] drivers = new IGenome[1];
        for (int i = 0; i < 1; i++) {
            drivers[i] = new FuocoCoreGenome("memory/nets", 8, 1.5201523);
        }

        FuocoRace race = new FuocoRace();
        race.setTrack("e-track-4", "road");
        race.laps = 3;

        RaceResult[] r = race.runRace(drivers, true);
    }
}
