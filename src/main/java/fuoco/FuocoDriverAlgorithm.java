package fuoco;

import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.genome.IGenome;
import cicontest.torcs.race.RaceResult;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import race.TorcsConfiguration;

import java.io.File;

import java.io.Serializable;
import java.util.*;

public class FuocoDriverAlgorithm implements Serializable {

    class FuocoResults {
        public RaceResult res;
        public boolean damage;

        FuocoResults(RaceResult res, boolean damage) {
            this.res = res;
            this.damage = damage;
        }
    }

    Map<String, String> trackDict = new HashMap<>();
    Set<String> tracks = new HashSet<>();

    private FuocoDriverAlgorithm() {
        trackDict.put("aalborg", "road");
        trackDict.put("alpine-1", "road");
        trackDict.put("alpine-2", "road");
        trackDict.put("brondehach", "road");
        trackDict.put("corkscrew", "road");
        trackDict.put("eroad", "road");
        trackDict.put("e-track-1", "road");
        trackDict.put("e-track-2", "road");
        trackDict.put("e-track-3", "road");
        trackDict.put("e-track-4", "road");
        trackDict.put("e-track-6", "road");
        trackDict.put("forza", "road");
        trackDict.put("g-track-1", "road");
        trackDict.put("g-track-2", "road");
        trackDict.put("g-track-3", "road");
        trackDict.put("ole-road-1", "road");
        trackDict.put("ruudskogen", "road");
        trackDict.put("spring", "road");
        trackDict.put("street-1", "road");
        trackDict.put("wheel-1", "road");
        trackDict.put("wheel-2", "road");
        trackDict.put("a-speedway", "oval");
        trackDict.put("b-speedway", "oval");
        trackDict.put("c-speedway", "oval");
        trackDict.put("d-speedway", "oval");
        trackDict.put("e-speedway", "oval");
        trackDict.put("e-track-5", "oval");
        trackDict.put("f-speedway", "oval");
        trackDict.put("g-speedway", "oval");
        trackDict.put("michigan", "oval");
        trackDict.put("dirt-1", "dirt");
        trackDict.put("dirt-2", "dirt");
        trackDict.put("dirt-3", "dirt");
        trackDict.put("dirt-4", "dirt");
        trackDict.put("dirt-5", "dirt");
        trackDict.put("dirt-6", "dirt");
        trackDict.put("mixed-1", "dirt");
        trackDict.put("mixed-2", "dirt");
    }

    private static ArgumentParser configureParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("Train")
                .defaultHelp(true)
                .description("#TODO"); //TODO implement description

        parser.addArgument("-g", "--gui")
                .action(Arguments.storeTrue())
                .help("Specify wheather run TORCS with GUI");

        parser.addArgument("-l", "--laps")
                .nargs(1)
                .setDefault("[1]")
                .help("Number of laps");

        parser.addArgument("-t", "--track")
                .nargs(1)
                .setDefault("b-speedway")
                .help("Name of track");

        parser.addArgument("-r", "--road")
                .nargs(1)
                .setDefault("oval")
                .help("Type of road");

        parser.addArgument("-o", "--open")
                .help("Filename for loading genome");

        parser.addArgument("-s", "--save")
                .help("Filename for saving genome");
        return parser;
    }

    public static void main(String[] args) throws Exception {

        TorcsConfiguration.getInstance().initialize(new File("torcs.properties"));

        ArgumentParser parser = configureParser();

        try {
            Namespace res = parser.parseArgs(args);
            boolean withGUI = res.getBoolean("gui");

            String laps_string = res.getString("laps");
            int laps = Integer.parseInt(laps_string.substring(1).substring(0, laps_string.length() - 2));

            String track = res.getString("track");
            track = track.substring(1).substring(0, track.length() - 2);

            String road = res.getString("road");
            road = road.substring(1).substring(0, road.length() - 2);

            String load = res.getString("open");
            String save = res.getString("save");


            FuocoDriverAlgorithm algorithm = new FuocoDriverAlgorithm();
            DriversUtils.registerMemory(FuocoDriver.class);

            algorithm.sampleTracks(5);
            algorithm.fitness(new double[]{1, 1, 1, 1}, new double[]{1, 1, 1, 1}, false, false, false, 15, 1.5);

//            algorithm.run(withGUI, laps, track, road,  load, save);
        } catch (ArgumentParserException e) {
            e.printStackTrace();
        }
    }

    private void sampleTracks(int n) {
        List<String> allTracks = new ArrayList<>(trackDict.keySet());
        Collections.shuffle(allTracks);
        tracks.clear();
        for (int i = 0; i < n; i++) {
            tracks.add(allTracks.get(i));
        }
    }

    private List<FuocoResults> fitness(double[] steeringWeights, double[] accelBrakegWeights, boolean ABS, boolean AutomatedGearbox, boolean min, double space_offset, double brake_force) throws Exception {
        List<FuocoResults> results = new ArrayList<>();

        for(String t : tracks) {
//            System.out.println(t);
            IGenome[] drivers = new IGenome[]{new FuocoCoreGenome("memory/nets", steeringWeights, accelBrakegWeights, ABS, AutomatedGearbox, min, space_offset, brake_force)};

            FuocoRace race = new FuocoRace();

            race.setTrack(t, trackDict.get(t));
            RaceResult r = race.runRace(drivers, false)[0];

//            System.out.println(r.getTime());
//            System.out.println(((FuocoDriver) r.getDriver()).hasDamage());

            results.add(new FuocoResults(r, ((FuocoDriver) r.getDriver()).hasDamage()));
        }
        return results;
    }

    private void run(boolean withGUI, int laps, String track, String road, String load, String save) throws Exception {

        IGenome genome;
        if (load == null) {
            genome = new DefaultCoreGenome();
        } else {
            genome = new FuocoCoreGenome("memory/" + load, new double[]{1, 1, 1, 1}, new double[]{1, 1, 1, 1}, false, false, false, 15, 1.5);
        }

        IGenome[] drivers = new IGenome[1];
        drivers[0] = genome;

        FuocoRace race = new FuocoRace();

        race.setTrack(track, road);
        race.laps = laps;

        RaceResult[] results = race.runRace(drivers, withGUI);

        // RaceResult has many gets, getTime(), but also isFinished() and getDistance() ;)
        System.out.println(results[0].getTime());

        if (save != null) {
            DriversUtils.storeGenome(drivers[0], save);
        }
    }
}