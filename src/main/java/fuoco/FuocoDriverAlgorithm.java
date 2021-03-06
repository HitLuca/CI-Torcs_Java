package fuoco;

import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.genome.IGenome;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import race.TorcsConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class FuocoDriverAlgorithm implements Serializable {

    private static ArgumentParser configureParser() {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("Train")
                .defaultHelp(true)
                .description("#TODO"); //TODO implement description

        parser.addArgument("-g", "--gui")
                .action(Arguments.storeTrue())
                .help("Specify wheather run TORCS with GUI");

        parser.addArgument("-l", "--laps")
                .nargs(1)
                .setDefault(1)
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
            int laps = res.getInt("laps");
            String track = res.getString("track");
            String road = res.getString("road");
            String load = res.getString("open");
            String save = res.getString("save");


            FuocoDriverAlgorithm algorithm = new FuocoDriverAlgorithm();
            DriversUtils.registerMemory(FuocoDriver.class);

            algorithm.run(withGUI, laps, track, road,  load, save);
        } catch (ArgumentParserException e) {
            e.printStackTrace();
        }
    }

    private void run(boolean withGUI, int laps, String track, String road, String load, String save) throws Exception {
        try {
            if (withGUI) {
                Runtime.getRuntime().exec("torcs");
            } else {
                Runtime.getRuntime().exec("torcs -r /home/" + System.getProperty("user.name") + "/.torcs/config/raceman/quickrace.xml");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        IGenome genome;
        if (load == null) {
            genome = new DefaultCoreGenome();
        } else {
            load = "memory/" +load;
            genome = new FuocoCoreGenome(load);
            System.out.println(genome);
            if (genome == null) {
                throw new RuntimeException("Genome is null");
            }
        }

        IGenome[] drivers = new IGenome[1];
        drivers[0] = genome;

        FuocoRace race = new FuocoRace();
        race.setTrack(track, road);
        race.laps = laps;

        int[] results = race.runRace(drivers, withGUI);

        if (save != null) {
            DriversUtils.storeGenome(drivers[0], save);
        }
    }
}