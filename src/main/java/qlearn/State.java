package qlearn;


public interface State<E extends Enum> {
    double[] getValues();
    State<E> performAction(E action);
    double getReward();
}
