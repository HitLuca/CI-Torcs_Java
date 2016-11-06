package tests;

public class Tuple {
    double[] old_state;
    GridWorld.Action action;
    int reward;
    double[] new_state;

    public Tuple(double[] state, GridWorld.Action action, int reward, double[] state1) {
        this.old_state = state;
        this.action = action;
        this.reward = reward;
        this.new_state = state1;
    }
}
