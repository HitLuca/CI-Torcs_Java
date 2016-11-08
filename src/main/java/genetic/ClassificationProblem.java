package genetic;

import javafx.util.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by luca on 11/7/16.
 */
public class ClassificationProblem {
    private List<Pair<INDArray, INDArray>> data;
    private int minX = -10;
    private int maxX = 10;
    private int minY = -20;
    private int maxY = 20;

    public ClassificationProblem(int dataPoints, int classes) {
        data = new ArrayList<Pair<INDArray, INDArray>>(dataPoints);
        Random random = new Random();

        double step = (maxX - minX) / (classes * 1.0);

        for (int i = 0; i < dataPoints; i++) {
            double x = minX + (maxX - minX) * random.nextDouble();
            double y = minY + (maxY - minY) * random.nextDouble();
            INDArray t = null;

            for (int j = 1; j <= classes; j++) {
                if (x < minX + step * j) {
                    double[] target = new double[classes];
                    for (int k = 0; k < classes; k++) {
                        target[k] = 0;
                    }
                    target[j - 1] = 1;
                    t = Nd4j.create(target);
                    break;
                }
            }

            INDArray dataPoint = Nd4j.create(new double[]{x, y});

            data.add(new Pair(dataPoint, t));
        }
    }

    public List<Pair<INDArray, INDArray>> getData() {
        return data;
    }
}
