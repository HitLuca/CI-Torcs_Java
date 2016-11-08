package qlearn;

import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

public abstract class QLearn<C extends FitablePredictable<S, E>, S extends State<E>, E extends Enum> {

    private C classifier;
    private Random rnd = new Random();

    public QLearn(C classifier) {
        this.classifier = classifier;
    }

    protected abstract S nextState();

    protected abstract boolean rewardTerminal(double reward);

    public C getClassifier() {
        return classifier;
    }

    public C train(int epochs, double gamma, double epsilon, int batchSize, int buffer) {
        List<Transition<S, E>> replay = new LinkedList<Transition<S, E>>();
        int h = 0;

        for (int e = 0; e < epochs; e++) {
            System.out.println("Epoch: " + e);
            double reward;
            S state = nextState();

            do {
                E action;

                if (rnd.nextDouble() < epsilon) {
                    action = classifier.predictRandomAction();
                } else {
                    action = classifier.predictBestAction(state);
                }

                S newState = (S) state.performAction(action);
                reward = state.getReward();

                if (replay.size() < buffer) {
                    replay.add(new Transition<S, E>(state, action, reward, newState));
                } else {
                    if (h < buffer - 1) {
                        h++;
                    } else {
                        h = 0;
                    }
                    replay.set(h, new Transition<S, E>(state, action, reward, newState));

                    List<Transition<S, E>> minibatch = new LinkedList<Transition<S, E>>();
                    sample(replay, minibatch, batchSize);

                    ArrayList<S> x_train = new ArrayList<S>(minibatch.size());
                    ArrayList<double[]> y_train = new ArrayList<double[]>(minibatch.size());

                    getTrainXYSets(minibatch, gamma, x_train, y_train);

                    classifier.fit(x_train, y_train);
                    state = newState;
                }
            } while (!rewardTerminal(reward));

            epsilon = epsilonDecay(epsilon, e, epochs);
        }
        return classifier;
    }

    private void getTrainXYSets(List<Transition<S, E>> from, double gamma, List<S> x_train, List<double[]> y_train) {
        for (int i = 0; i < from.size(); i++) {
            Transition<S, E> memory = from.get(i);

            S oldState = memory.getOldState();
            E action = memory.getAction();
            double reward = memory.getReward();
            S newState = memory.getNewState();

            double[] oldQVal = classifier.predict(oldState);
            double[] newQVal = classifier.predict(newState);

            double maxQVal = Collections.max(Arrays.asList(ArrayUtils.toObject(newQVal)));

            double[] y = oldQVal.clone();

            double update = reward;
            if (!rewardTerminal(reward)) {
                update += gamma * maxQVal;
            }

            y[action.ordinal()] = update;
            x_train.add(i, oldState);
            y_train.add(i, y);
        }
    }

    private void sample(List<Transition<S, E>> from, List<Transition<S, E>> to, int size) {
        do {
            to.add(from.remove(rnd.nextInt(from.size())));
        } while (to.size() < size);

        for (Transition<S, E> t : to) {
            from.add(new Transition<S, E>(t.getOldState(), t.getAction(), t.getReward(), t.getNewState()));
        }
    }

    double epsilonDecay(double epsilon, int e, int epochs) {
        if (epsilon > 0.1) {
            return  epsilon - 1.0/epochs;
        } else {
            return epsilon;
        }
    }
}
