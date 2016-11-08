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

    private int[][][] state = new int[6][6][3];

    private int getState(int x, int y, Obj k) {
        return state[x][y][k.ordinal()];
    }

    private void setState(int x, int y, Obj k) {
        state[x][y][k.ordinal()] = 1;
    }

    private int[] getLocation(Obj obj) {
        int[] r = new int[]{-1, -1};
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
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

        //int[] pit_loc = { 1 + rnd.nextInt(4), 1 + rnd.nextInt(4)};
        int[] pit_loc = {4, 3};
        state.setState(pit_loc[0], pit_loc[1], Obj.PIT);

        int[] goal_loc = {3, 2};
        //int[] goal_loc = new int[2];
        /*do {
            goal_loc[0] = 1 + rnd.nextInt(4);
            goal_loc[1] = 1 + rnd.nextInt(4);
        } while (Arrays.equals(pit_loc, goal_loc));*/
        state.setState(goal_loc[0], goal_loc[1], Obj.GOAL);

        int[] player_loc = new int[2];
        do {
            player_loc[0] = 1 + rnd.nextInt(4);
            player_loc[1] = 1 + rnd.nextInt(4);
        } while (Arrays.equals(pit_loc, player_loc) || Arrays.equals(goal_loc, player_loc));
        state.setState(player_loc[0], player_loc[1], Obj.PLAYER);

        return state;
    }

    public static GridState nextState() {
        return nextState(new Random());
    }

    public double[] getValues() {
        double[] r = new double[6 * 6 * 3];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                for (int k = 0; k < 3; k++) {
                    r[18*i + 3*j + k] = state[i][j][k];
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
            new_loc[0] = player_loc[0];
            new_loc[1] = player_loc[1] + 1;
        } else if (action == Move.RIGHT) {
            new_loc[0] = player_loc[0] + 1;
            new_loc[1] = player_loc[1];
        } else if (action == Move.DOWN) {
            new_loc[0] = player_loc[0];
            new_loc[1] = player_loc[1] - 1;
        } else if (action == Move.LEFT) {
            new_loc[0] = player_loc[0] - 1;
            new_loc[1] = player_loc[1];
        }

        if (new_loc[0] >= 0 && new_loc[0] <= 5 && new_loc[1] <= 5 && new_loc[1] >= 0) {
            new_state.setState(new_loc[0], new_loc[1], Obj.PLAYER);
        } else {
            new_state.setState(player_loc[0], player_loc[1], Obj.PLAYER);
        }

        new_state.setState(pit_loc[0], pit_loc[1], Obj.PIT);
        new_state.setState(goal_loc[0], goal_loc[1], Obj.GOAL);

        return new_state;
    }

    public double getReward() {
        int[] player_loc = getLocation(Obj.PLAYER);

        if (Arrays.equals(player_loc, getLocation(Obj.PIT))) {
            return -10;
        } else if (Arrays.equals(player_loc, getLocation(Obj.GOAL))) {
            return 10;
        } else if (player_loc[0] == 0 || player_loc[0] == 5 || player_loc[1] == 0 || player_loc[1] == 5) {
            return -10;
        } else {
            return -1;
        }
    }

    @Override
    public String toString() {
        String r = "";
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) {
                if (getState(i, j, Obj.PIT) == 1) {
                    r += "-";
                } else if (getState(i, j, Obj.GOAL) == 1) {
                    r += "+";
                } else if (getState(i, j, Obj.PLAYER) == 1) {
                    r += "P";
                } else if(i == 0 || i == 5 || j == 0 || j == 5) {
                    r += "$";
                } else {
                    r += "#";
                }
            }
            r += "\n";
        }
        return r;
    }
}
