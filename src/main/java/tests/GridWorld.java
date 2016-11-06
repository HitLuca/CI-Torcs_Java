package tests;


import org.apache.commons.lang3.ArrayUtils;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.util.*;

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

    private static MultiLayerNetwork initNet(int iterations) {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(12165115614545L)
            .iterations(iterations)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .weightInit(WeightInit.RELU)
            .list()
            .layer(0, new DenseLayer.Builder()
                .nIn(4*4*3)
                .nOut(150)
                .activation("relu")
                .build())
            .layer(1, new DenseLayer.Builder()
                .nIn(150)
                .nOut(150)
                .activation("relu")
                .build())
            .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
                .activation("identity")
                .nIn(150)
                .nOut(4)
                .build())
            .pretrain(false)
            .backprop(true)
            .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        //net.setListeners(new ScoreIterationListener(100));

        return net;
    }

    private static double[] initGrid() {
        int[][][] state = new int[4][4][3];

        Random rnd = new Random();

        int[] pit_loc = new int[2];
        int[] goal_loc = new int[2];
        int[] player_loc = new int[2];

        pit_loc[0] = rnd.nextInt(3);
        pit_loc[1] = rnd.nextInt(3);

        do {
            goal_loc[0] = rnd.nextInt(3);
            goal_loc[1] = rnd.nextInt(3);
        } while (Arrays.equals(pit_loc, goal_loc));

        do {
            player_loc[0] = rnd.nextInt(3);
            player_loc[1] = rnd.nextInt(3);
        } while (Arrays.equals(pit_loc, player_loc) || Arrays.equals(goal_loc, player_loc));

        state[pit_loc[0]][pit_loc[1]][Obj.PIT.ordinal()] = 1;
        state[goal_loc[0]][goal_loc[1]][Obj.GOAL.ordinal()] = 1;
        state[player_loc[0]][player_loc[1]][Obj.PLAYER.ordinal()] = 1;

        double[] r = new double[4*4*3];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 3; k++) {
                    r[12*i + 3*j + k] = state[i][j][k];
                }
            }
        }
        return r;
    }

    private static double[] predict(MultiLayerNetwork net, double[] state) {
        INDArray o = net.output(Nd4j.create(state));
        double[] r = new double[4];
        for (int i = 0; i < o.length(); i++) {
            r[i] = o.getDouble(i);
        }
        return r;
    }

    private static int argmax (double [] elems) {
        int bestIdx = -1;
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < elems.length; i++) {
            double elem = elems[i];
            if (elem > max) {
                max = elem;
                bestIdx = i;
            }
        }
        return bestIdx;
    }

    private static int[] getLocation(double[] state, Obj obj) {
        int[] r = {-1, -1};
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (state[12 * i + 3 * j + obj.ordinal()] == 1) {
                    r[0] = i;
                    r[1] = j;
                    return r;
                }
            }
        }
        return r;
    }

    private static double[] makeMove(double[] state, Action action) {
        double[] new_state = new double[4*4*3];
        int[] pit_loc = getLocation(state, Obj.PIT);
        int[] goal_loc = getLocation(state, Obj.GOAL);
        int[] player_loc = getLocation(state, Obj.PLAYER);
        int[] new_loc = new int[2];

        switch (action) {
            case UP:
                new_loc[0] = player_loc[0] - 1;
                new_loc[1] = player_loc[1];
                break;
            case RIGHT:
                new_loc[0] = player_loc[0];
                new_loc[1] = player_loc[1] + 1;
                break;
            case DOWN:
                new_loc[0] = player_loc[0] + 1;
                new_loc[1] = player_loc[1];
                break;
            case LEFT:
                new_loc[0] = player_loc[0];
                new_loc[1] = player_loc[1] - 1;
                break;
        }

        if (new_loc[0] >= 0 && new_loc[0] <= 3 && new_loc[1] <= 3 && new_loc[1] >= 0) {
            new_state[12*new_loc[0] + 3*new_loc[1] + Obj.PLAYER.ordinal()] = 1;
        } else {
            new_state[12*player_loc[0] + 3*player_loc[1] + Obj.PLAYER.ordinal()] = 1;
        }

        new_state[12*pit_loc[0] + 3*pit_loc[1] + Obj.PIT.ordinal()] = 1;
        new_state[12*goal_loc[0] + 3*goal_loc[1] + Obj.GOAL.ordinal()] = 1;

        return new_state;
    }

    private static int getReward(double[] state) {
        int[] pit_loc = getLocation(state, Obj.PIT);
        int[] goal_loc = getLocation(state, Obj.GOAL);
        int[] player_loc = getLocation(state, Obj.PLAYER);
        if (Arrays.equals(player_loc, pit_loc)) {
            return -10;
        } else if (Arrays.equals(player_loc, goal_loc)) {
            return 10;
        } else {
            return -1;
        }
    }

    private static void printState(double[] state) {
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                String v = "#";
                for (int k = 0; k < 3; k++) {
                    if (state[12 * i + 3 * j + k] == 1) {
                        switch (Obj.values()[k]) {
                            case PIT:
                                v = "-";
                                break;
                            case GOAL:
                                v = "+";
                                break;
                            case PLAYER:
                                v = "P";
                                break;
                        }
                    }
                }
                System.out.print(v);
            }
            System.out.print("\n");
        }
    }



    public static void main(String[] args) throws IOException {

        boolean load = true;
        if (load) {
            MultiLayerNetwork net =  ModelSerializer.restoreMultiLayerNetwork("pippo");

            for (int e = 0; e < 20; e++) {
                double[] state = initGrid();

                int lim = 100;
                while (getReward(state) == -1 && lim > 0) {
                    double[] p = predict(net, state);
                    double[] new_state;
                    Action action;

                    do {
                        int k = argmax(p);
                        action = Action.values()[k];
                        new_state = makeMove(state, action);
                        p[k] = Double.MIN_VALUE;
                    } while (Arrays.equals(state, new_state));

                    state = new_state;
                    lim--;
                }
                System.out.println(getReward(state));
            }
        } else {

            MultiLayerNetwork net = initNet(100);
            Random rnd = new Random();
            int epochs = 200;
            double gamma = 0.975;
            double epsilon = 1;
            int batchSize = 40;
            int buffer = 80;
            List<Tuple> replay = new LinkedList<Tuple>();
            int h = 0;

            for (int e = 0; e < epochs; e++) {
                System.out.println("Epoch: " + e);
                double[] state = initGrid();

                boolean status = true;
                while (status) {
                    double[] qval = predict(net, state);
                    Action action;
                    if (rnd.nextDouble() < epsilon) {
                        action = Action.values()[rnd.nextInt(4)];
                    } else {
                        action = Action.values()[argmax(qval)];
                    }
                    double[] new_state = makeMove(state, action);
                    int reward = getReward(new_state);

                    if (replay.size() < buffer) {
                        replay.add(new Tuple(state, action, reward, new_state));
                    } else {
                        if (h < buffer - 1) {
                            h++;
                        } else {
                            h = 0;
                        }
                        replay.set(h, new Tuple(state, action, reward, new_state));

                        List<Tuple> minibatch = new LinkedList<Tuple>();
                        do {
                            int i = rnd.nextInt(replay.size());
                            Tuple t = replay.remove(i);
                            minibatch.add(t);
                        } while (minibatch.size() < batchSize);

                        for (Tuple t : minibatch) {
                            replay.add(new Tuple(t.old_state, t.action, t.reward, t.new_state));
                        }

                        double[][] X_train = new double[minibatch.size()][4 * 4 * 3];
                        double[][] y_train = new double[minibatch.size()][3];

                        for (int i = 0; i < minibatch.size(); i++) {
                            Tuple memory = minibatch.get(i);
                            double[] old_state = memory.old_state;
                            action = memory.action;
                            reward = memory.reward;
                            new_state = memory.new_state;
                            double[] old_qval = predict(net, old_state);
                            double[] new_qval = predict(net, new_state);
                            double max_qval = Collections.max(Arrays.asList(ArrayUtils.toObject(new_qval)));
                            double[] y = {old_qval[0], old_qval[1], old_qval[2], old_qval[3]};

                            double update;
                            if (reward == -1) {
                                update = reward + (gamma * max_qval);
                            } else {
                                update = reward;
                            }

                            y[action.ordinal()] = update;
                            X_train[i] = old_state;
                            y_train[i] = y;
                        }

                        net.fit(new DataSet(Nd4j.create(X_train), Nd4j.create(y_train)));
                        state = new_state;
                    }
                    if (reward != -1) {
                        status = false;
                    }
                }
                if (epsilon > 0.1) {
                    epsilon -= 1 / epochs;
                }
            }
            ModelSerializer.writeModel(net, "pippo", true);
        }
    }
}
