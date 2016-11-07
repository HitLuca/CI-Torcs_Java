package qlearn;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public interface FitablePredictable<S extends State<E>, E extends Enum> extends Serializable {
    void fit(List<S> states, List<double[]> qVals);
    E[] predictActions(S state);
    E predictBestActions(S state);
    E predictRandomAction();
    double[] predict(S state);
    void save(String filename) throws IOException;
}
