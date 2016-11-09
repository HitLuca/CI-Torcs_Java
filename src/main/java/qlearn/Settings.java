package qlearn;

import org.deeplearning4j.nn.conf.LearningRatePolicy;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * Created by luca on 11/9/16.
 */
public class Settings {
    //GridWorld
    public static int epochs = 100;
    public static double gamma = 0.975;
    public static double epsilon = 1;
    public static int miniBatchSize = 40;
    public static int buffer = 80;
    public static int iterations = 100;
    public static int testNumber = 500;
    public static String networkConfiguration = "output/test_network";
    public static String networkResults = "output/network_results.csv";

    //GridState
    public static int gridX = 7;
    public static int gridY = 7;
    public static int elements = 3;
    public static boolean keepSamePositioning = true;

    public static boolean racingTrack = true;

    //GridWorldNet
    public static double learningRate = 0.05;
    public static int hiddenNeurons = 500;
}
