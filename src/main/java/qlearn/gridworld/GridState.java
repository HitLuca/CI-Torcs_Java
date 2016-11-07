package qlearn.gridworld;

import qlearn.State;

import java.util.Arrays;
import java.util.Random;

public class GridState implements State<GridState.Move> {

    enum Move {
        UP,
        RIGHT,
        DOWN,
        LEFT
    }

    private enum Obj {
        PIT,
        GOAL,
        PLAYER
    }

    private int[][][] state = new int[4][4][3];

    private int getState(int x, int y, Obj k) {
        return state[x][y][k.ordinal()];
    }

    private void setState(int x, int y, Obj k, int v) {
        state[x][y][k.ordinal()] = v;
    }

    private int[] getLocation(Obj obj) {
        int[] r = {-1, -1};
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (getState(i, j, obj) == 1) {
                    r[0] = i;
                    r[1] = j;
                    return r;
                }
            }
        }
        return r;
    }

    private static GridState nextState(Random rnd) {
        GridState state = new GridState();

        int[] pit_loc = {rnd.nextInt(3), rnd.nextInt(3)};
        state.setState(pit_loc[0], pit_loc[1], Obj.PIT, 1);

        int[] goal_loc = new int[2];
        do {
            goal_loc[0] = rnd.nextInt(3);
            goal_loc[1] = rnd.nextInt(3);
        } while (Arrays.equals(pit_loc, goal_loc));
        state.setState(goal_loc[0], goal_loc[1], Obj.GOAL, 1);

        int[] player_loc = new int[2];
        do {
            player_loc[0] = rnd.nextInt(3);
            player_loc[1] = rnd.nextInt(3);
        } while (Arrays.equals(pit_loc, player_loc) || Arrays.equals(goal_loc, player_loc));
        state.setState(player_loc[0], player_loc[1], Obj.PLAYER, 1);

        return state;
    }

    public static GridState nextState(long seed) {
        return nextState(new Random(seed));
    }

    public static GridState nextState() {
        return nextState(new Random());
    }

    public double[] getValues() {
        double[] r = new double[4 * 4 *3];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 3; k++) {
                    r[12*i + 3*j + k] = state[i][j][k];
                }
            }
        }
        return r;
    }

    public GridState performAction(Move action) {
        GridState new_state = new GridState();
        int[] pit_loc = getLocation(Obj.PIT);
        int[] goal_loc = getLocation(Obj.GOAL);
        int[] player_loc = getLocation(Obj.PLAYER);
        int[] new_loc = new int[2];

        if (action == Move.UP) {
            new_loc[0] = player_loc[0] - 1;
            new_loc[1] = player_loc[1];
        } else if (action == Move.RIGHT) {
            new_loc[0] = player_loc[0];
            new_loc[1] = player_loc[1] + 1;
        } else if (action == Move.DOWN) {
            new_loc[0] = player_loc[0] + 1;
            new_loc[1] = player_loc[1];
        } else if (action == Move.LEFT) {
            new_loc[0] = player_loc[0];
            new_loc[1] = player_loc[1] - 1;
        }

        if (new_loc[0] >= 0 && new_loc[0] <= 3 && new_loc[1] <= 3 && new_loc[1] >= 0) {
            new_state.setState(new_loc[0], new_loc[1], Obj.PLAYER, 1);
        } else {
            new_state.setState(player_loc[0], player_loc[1], Obj.PLAYER, 1);
        }

        new_state.setState(pit_loc[0], pit_loc[1], Obj.PIT, 1);
        new_state.setState(goal_loc[0], goal_loc[1], Obj.GOAL, 1);

        return new_state;
    }

    public double getReward() {
        int[] player_loc = getLocation(Obj.PLAYER);

        if (Arrays.equals(player_loc, getLocation(Obj.PIT))) {
            return -10;
        } else if (Arrays.equals(player_loc, getLocation(Obj.GOAL))) {
            return 10;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        String r = "";
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (getState(i, j, Obj.PIT) == 1) {
                    r += "-";
                } else if (getState(i, j, Obj.GOAL) == 1) {
                    r += "+";
                } else if (getState(i, j, Obj.PLAYER) == 1) {
                    r += "P";
                } else {
                    r += "#";
                }
            }
            r += "\n";
        }
        return r;
    }
}
