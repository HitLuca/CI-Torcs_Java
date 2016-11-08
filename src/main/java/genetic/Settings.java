package genetic;

/**
 * Created by luca on 11/8/16.
 */
public class Settings {
    static int classes = 6;
    static int dataPoints = 500;

    static double learning_rate = 0.001;

    static int crossoverPoints = 3;
    static double mutationProbability = 0.02;

    static int individuals = 60;
    static int elites = 1;
    static int stagnation = 30;

    static int minWeight = -10;
    static int maxWeight = 10;
    static int layer1neurons = 30;
    static int layer2neurons = 30;
    static int totalParameters = (2 + 1) * layer1neurons + (layer1neurons + 1) * layer2neurons + (layer2neurons + 1) * classes;
}
