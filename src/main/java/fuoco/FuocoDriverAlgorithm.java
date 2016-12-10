package fuoco;

import cicontest.algorithm.abstracts.AbstractAlgorithm;
import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.genome.IGenome;
import cicontest.torcs.race.RaceResult;

import race.TorcsConfiguration;

import java.io.File;

import java.util.*;

public class FuocoDriverAlgorithm extends AbstractAlgorithm {

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

    @Override
    public void run(boolean b) {

        IGenome[] drivers = new IGenome[0];
        try {
            drivers = new IGenome[]{new FuocoCoreGenome("memory/nets", 15, 2)};
        } catch (Exception e) {
            e.printStackTrace();
        }


        FuocoRace race = new FuocoRace();
        race.setTrack("dirt-1" , "dirt");
        race.laps = 1;

        RaceResult r = race.runRace(drivers, true)[0];

        Logger.println(r.getTime());
        Logger.println(((FuocoDriver) r.getDriver()).hasDamage() + "\n");
    }


    public static void main(String[] args) throws Exception {
		/*
		 *
		 * Start without arguments to run the algorithm
		 * Start with -continue to continue a previous run
		 * Start with -show to show the best found
		 * Start with -show-race to show a race with 10 copies of the best found
		 * Start with -human to race against the best found
		 *
		 */
;
        int laps = 3;

        String track = "alpine-1";

        FuocoDriverAlgorithm algorithm = new FuocoDriverAlgorithm();



//            s[0] = 0.22D;
//            s[1] = 0.22D;
//            s[2] = 0.33D;
//            s[3] = 0.22D;
         algorithm.run();
        //algorithm.testAllTracks(withGUI, laps, s, a, false, true, false, 15, 1.5);

    }

    public void sampleTracks(int n) {
        List<String> allTracks = new ArrayList<>(trackDict.keySet());
        Collections.shuffle(allTracks);
        tracks.clear();
        for (int i = 0; i < n; i++) {
            tracks.add(allTracks.get(i));
        }
    }

    private void testAllTracks(boolean withGUI, int laps, double[] steeringWeights, double[] accelBrakegWeights, boolean ABS, boolean AutomatedGearbox, boolean min, double space_offset, double brake_force) throws Exception {
        SortedSet<String> allTracks = new TreeSet<>(trackDict.keySet());
        double totalTime = 0D;
        int totalFails = 0;
        double failedTimes = 0D;

        for (String t : allTracks) {
            FuocoResults result = test(withGUI, laps, t, steeringWeights, accelBrakegWeights, ABS, AutomatedGearbox, min, space_offset, brake_force);
            if (!result.damage || t == "g-track-3" || t=="ole-road-1") {
                totalTime += result.res.getTime();
            } else {
                totalFails += 1;
                failedTimes += result.res.getTime();
            }
        }
        Logger.println(totalTime);
        Logger.println(totalFails);
        Logger.println(failedTimes);

    }

    private FuocoResults test (boolean withGUI, int laps, String track, double[] steeringWeights, double[] accelBrakegWeights, boolean ABS, boolean AutomatedGearbox, boolean min, double space_offset, double brake_force) throws Exception {

        Logger.println(track);

        IGenome[] drivers = new IGenome[]{new FuocoCoreGenome("memory/nets", space_offset, brake_force)};

        FuocoRace race = new FuocoRace();

        race.setTrack(track, trackDict.get(track));
        race.laps = laps;
        RaceResult r = race.runRace(drivers, withGUI)[0];

        Logger.println(r.getTime());
        Logger.println(((FuocoDriver) r.getDriver()).hasDamage() + "\n");

        return new FuocoResults(r, ((FuocoDriver) r.getDriver()).hasDamage());
    }


    public List<FuocoResults> fitness(double[] steeringWeights, double[] accelBrakegWeights, boolean ABS, boolean AutomatedGearbox, boolean min, double space_offset, double brake_force) throws Exception {
        List<FuocoResults> results = new ArrayList<>();

        for(String t : tracks) {
            FuocoResults result = test(false, 1, t, steeringWeights, accelBrakegWeights, ABS, AutomatedGearbox, min, space_offset, brake_force);
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