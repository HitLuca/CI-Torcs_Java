package qlearn.gridworld;

import qlearn.QLearn;

public class GridWorldLearn extends QLearn<GridWorldNet, GridState, GridState.Move> {

    public GridWorldLearn(GridWorldNet net) {
        super(net);
    }

    protected GridState nextState() {
        return GridState.nextState();
    }

    protected boolean rewardTerminal(double reward) {
        return reward != -1;
    }
}
