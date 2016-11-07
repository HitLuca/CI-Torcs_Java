package qlearn;

public class Transition<S extends State<E>, E extends Enum> {
    private S oldState;
    private E action;
    private double reward;
    private S newState;

    public Transition(S oldState, E action, double reward, S newState) {
        this.oldState = oldState;
        this.action = action;
        this.reward = reward;
        this.newState = newState;
    }

    public S getOldState() {
        return oldState;
    }

    public E getAction() {
        return action;
    }

    public double getReward() {
        return reward;
    }

    public S getNewState() {
        return newState;
    }
}
