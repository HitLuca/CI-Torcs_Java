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
import java.text.DecimalFormat;
import java.util.*;

public class FuocoDriverAlgorithm implements Serializable {

    public class FuocoResults {
        public RaceResult res;
        public boolean damage;

        FuocoResults(RaceResult res, boolean damage) {
            this.res = res;
            this.damage = damage;
        }
    }

    Map<String, String> trackDict = new HashMap<>();
    Set<String> tracks = new HashSet<>();

    public FuocoDriverAlgorithm() {
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
        TorcsConfiguration.getInstance().initialize(new File("torcs.properties"));
        DriversUtils.registerMemory(FuocoDriver.class);
    }

    private static ArgumentParser configureParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("Train")
                .defaultHelp(true);

        parser.addArgument("-a", "--all")
                .action(Arguments.storeTrue())
                .help("Test on all tracks");

        parser.addArgument("-g", "--gui")
                .action(Arguments.storeTrue())
                .help("Specify wheather run TORCS with GUI");

        parser.addArgument("-l", "--laps")
                .nargs(1)
                .setDefault("[1]")
                .help("Number of laps");

        parser.addArgument("-o", "--opponents")
                .nargs(1)
                .setDefault("[0]")
                .help("Number of opponents");

        parser.addArgument("-t", "--track")
                .nargs(1)
                .setDefault("b-speedway")
                .help("Name of track");

        return parser;
    }

    public static void main(String[] args) throws Exception {
        FuocoDriverAlgorithm algorithm = new FuocoDriverAlgorithm();
        Logger.init();

        ArgumentParser parser = configureParser();
        Namespace res;

        boolean withGUI = false;
        String laps_string = "";
        boolean allTracks = false;
        String track = "";
        String opp_string = "";

        try {
            res = parser.parseArgs(args);
            withGUI = res.getBoolean("gui");
            laps_string = res.getString("laps");
            allTracks = res.getBoolean("all");
            track = res.getString("track");
            opp_string = res.getString("opponents");
        } catch (ArgumentParserException e) {
            e.printStackTrace();
        }

        int laps = Integer.parseInt(laps_string.substring(1).substring(0, laps_string.length() - 2));
        int opp = Integer.parseInt(opp_string.substring(1).substring(0, opp_string.length() - 2));
        track = track.substring(1).substring(0, track.length() - 2);

        if (allTracks) {
            algorithm.testAllTracks(withGUI, laps, opp, 13, 2);
        } else {
            FuocoResults[] result = algorithm.runRace(withGUI, laps, opp, track, 13, 2);
            for (int i = 0; i < opp + 1; i++) {
                Logger.println(track);
                Logger.println(result[i].res.getTime());
                Logger.println(result[i].damage + "\n");
            }
        }
    }

    private void testAllTracks(boolean withGUI, int laps, int opponents, double space_offset, double brake_force) throws Exception {
        SortedSet<String> allTracks = new TreeSet<>(trackDict.keySet());
        DecimalFormat df = new DecimalFormat("#.0000");

        double totalTime = 0D;
        int totalFails = 0;
        double failedTimes = 0D;

        for (String t : allTracks) {
            FuocoResults[] result = runRace(withGUI, laps, opponents, t, space_offset, brake_force);

            for (int i = 0; i < opponents + 1; i++) {
                boolean damage = result[i].damage;
                double time = Double.parseDouble(df.format(result[i].res.getTime()));

                if (t.equals("g-track-3") || t.equals("ole-road-1")) {
                    damage = false;
                }

                Logger.println(t);
                Logger.println(time / laps);
                Logger.println(damage + "\n");

                if (!damage) {
                    totalTime += result[i].res.getTime();
                } else {
                    totalFails += 1;
                    failedTimes += result[i].res.getTime();
                }
            }
            Logger.println("\n\n");
        }
        Logger.println(totalTime / laps);
        Logger.println(totalFails);
        Logger.println(failedTimes);
    }

    private FuocoResults[] runRace (boolean withGUI, int laps, int opponents, String track, double space_offset, double brake_force) throws Exception {

        IGenome[] drivers = new IGenome[opponents + 1];
        for (int i = 0; i < opponents + 1; i++) {
            drivers[i] = new FuocoCoreGenome("memory/nets", space_offset, brake_force);
        }

        FuocoRace race = new FuocoRace();
        race.setTrack(track, trackDict.get(track));
        race.laps = laps;

        RaceResult[] r = race.runRace(drivers, withGUI);
        FuocoResults[] fr = new FuocoResults[opponents + 1];

        for (int i = 0; i < opponents + 1; i++) {
            fr[i] =  new FuocoResults(r[i], ((FuocoDriver) r[i].getDriver()).hasDamage());
        }

        return fr;
    }

    public List<FuocoResults> fitness(double space_offset, double brake_force) throws Exception {
        List<FuocoResults> results = new ArrayList<>();

        for(String t : tracks) {
            FuocoResults result = runRace(false, 1, 0, t, space_offset, brake_force)[0];
            results.add(result);
        }
        return results;
    }

    public void setTracks() {
        tracks.clear();
        tracks.add("alpine-1");
        tracks.add("alpine-2");
        tracks.add("b-speedway");
        tracks.add("corkscrew");
        tracks.add("aalborg");
        tracks.add("e-track-4");
    }

}