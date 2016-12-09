package fuoco;

import cicontest.algorithm.abstracts.AbstractRace;
import cicontest.torcs.controller.Driver;
import cicontest.torcs.genome.IGenome;
import cicontest.torcs.race.Race;
import cicontest.torcs.race.RaceResult;
import cicontest.torcs.race.RaceResults;
import scr.Controller;

public class FuocoRace extends AbstractRace {


    public RaceResult[] runRace(IGenome[] drivers, boolean withGUI) {
        int size = Math.min(10, drivers.length);
        FuocoDriver[] driversList = new FuocoDriver[size];
        for (int i = 0; i < size; i++) {
            driversList[i] = new FuocoDriver();
            driversList[i].loadGenome(drivers[i]);
        }

        RaceResult[] fitness = new RaceResult[drivers.length];
        if(drivers.length > 10) {
            throw new RuntimeException("Only 10 drivers are allowed in a RACE");
        } else {
            Race race = new Race();
            race.setTrack(this.tracktype, this.track);
            race.setTermination(Race.Termination.LAPS, this.laps);
            race.setStage(Controller.Stage.RACE);
            Driver[] results = driversList;
            int i = drivers.length;

            for(int var8 = 0; var8 < i; ++var8) {
                Driver driver = results[var8];
                race.addCompetitor(driver);
            }

            RaceResults var10;
            if(withGUI) {
                var10 = race.runWithGUI();
            } else {
                var10 = race.run();
            }

            for(i = 0; i < drivers.length; ++i) {
                System.out.println(var10.get(results[i]));
                fitness[i] = var10.get(results[i]);
            }

            return fitness;
        }
    }
}