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

import java.io.FileNotFoundException;
import java.io.PrintStream;
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
                .defaultHelp(true);

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

            FuocoDriverAlgorithm algorithm = new FuocoDriverAlgorithm();
            DriversUtils.registerMemory(FuocoDriver.class);

            File dir = new File("memory/nets");
            File[] files = dir.listFiles();

            assert files != null;

            double[] s = new double[files.length], a = new double[files.length];
            for (int i = 0; i < files.length; i++) {
                s[i] = 1.0D / files.length;
                a[i] = 1.0D / files.length;
            }

            algorithm.test(withGUI, laps, track, s, a, false, true, false, 15, 1.5);
//            algorithm.testAllTracks(withGUI, laps, s, a, false, true, false, 15, 1.5);

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

    private void testAllTracks(boolean withGUI, int laps, double[] steeringWeights, double[] accelBrakegWeights, boolean ABS, boolean AutomatedGearbox, boolean min, double space_offset, double brake_force) throws Exception {
        SortedSet<String> allTracks = new TreeSet<>(trackDict.keySet());

        for (String t : allTracks) {
            test(withGUI, laps, t, steeringWeights, accelBrakegWeights, ABS, AutomatedGearbox, min, space_offset, brake_force);
        }
    }

    private FuocoResults test (boolean withGUI, int laps, String track, double[] steeringWeights, double[] accelBrakegWeights, boolean ABS, boolean AutomatedGearbox, boolean min, double space_offset, double brake_force) throws Exception {

        Logger.println(track);

        IGenome[] drivers = new IGenome[]{new FuocoCoreGenome("memory/nets", steeringWeights, accelBrakegWeights, ABS, AutomatedGearbox, min, space_offset, brake_force)};

        FuocoRace race = new FuocoRace();

        race.setTrack(track, trackDict.get(track));
        race.laps = laps;
        RaceResult r = race.runRace(drivers, withGUI)[0];

        Logger.println(r.getTime());
        Logger.println(((FuocoDriver) r.getDriver()).hasDamage() + "\n");

        return new FuocoResults(r, ((FuocoDriver) r.getDriver()).hasDamage());
    }


    private List<FuocoResults> fitness(double[] steeringWeights, double[] accelBrakegWeights, boolean ABS, boolean AutomatedGearbox, boolean min, double space_offset, double brake_force) throws Exception {
        List<FuocoResults> results = new ArrayList<>();

        for(String t : tracks) {
            FuocoResults result = test(false, 1, t, steeringWeights, accelBrakegWeights, ABS, AutomatedGearbox, min, space_offset, brake_force);
            results.add(result);
        }
        return results;
    }

}