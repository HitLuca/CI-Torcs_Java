package tests;


import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import qlearn.gridworld.GridState;
import qlearn.gridworld.GridWorldLearn;
import qlearn.gridworld.GridWorldNet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Random;

public class GridWorld {
    enum Obj {
        PIT,
        GOAL,
        PLAYER
    }

    enum Action {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

    private static int epochs = 100;
    private static double gamma = 0.975;
    private static double epsilon = 1;
    private static int miniBatchSize = 40;
    private static int buffer = 80;
    private static int iterations = 100;
    private static int testNumber = 500;
    private static String networkConfiguration = "output/test_network";
    private static String networkResults = "output/network_results.csv";

    private static FileWriter writer;

    public static void main(String[] args) throws IOException {
        writer = new FileWriter(networkResults, true);
        File f = new File(networkConfiguration);

        for(int i=0; i<iterations; i++) {
            GridWorldNet gwn;
            if(f.exists()) {
                gwn = new GridWorldNet(networkConfiguration);
            }
            else {
                gwn = new GridWorldNet(10);
            }

            GridWorldLearn gwl = new GridWorldLearn(gwn);
            gwl.train(epochs, gamma, epsilon, miniBatchSize, buffer);
            gwn = gwl.getClassifier();
            gwn.save(networkConfiguration);

            printVal(gwn, testNumber, writer);
        }
        writer.close();
    }

    private static void printVal (GridWorldNet net, int ty, FileWriter writer) throws IOException {
        double[] f = new double[ty];
        for (int e = 0; e < ty; e++) {
            GridState state = GridState.nextState();

            int lim = 10;
            while (state.getReward() == -1 && lim > 0) {
                state = state.performAction(net.predictBestAction(state));
                lim--;
            }
            f[e] = state.getReward();
        }

        double goal = 0;
        double stall = 0;
        double pit = 0;

        for (double e : f) {
            if (e == 10) {
                goal++;
            } else if (e == -10) {
                pit++;
            } else {
                stall++;
            }
        }

        writer.append("" + goal*100.0/ty + ", ");
        writer.append("" + stall*100.0/ty + ", ");
        writer.append("" + pit*100.0/ty + "\n");
        writer.flush();

        System.out.println("---------------");
        System.out.println("Goal reached: " + goal*100.0/ty);
        System.out.println("Stall: " + stall*100.0/ty);
        System.out.println("Pit: " + pit*100.0/ty);
        System.out.println("---------------");
    }
}

