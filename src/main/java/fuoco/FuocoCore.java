package fuoco;

import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.genome.IGenome;
import com.sun.org.apache.bcel.internal.generic.FADD;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import scr.Action;
import scr.SensorModel;

import java.io.*;
import java.util.ArrayList;

import static org.nd4j.linalg.api.ndarray.INDArray.*;


public class FuocoCore implements Core {

    private MultiLayerNetwork[] nets;
    private PrintWriter file = new PrintWriter("java_gear.dat");
    private boolean recoveryMode = false;
    int count = 0;
    ArrayList<INDArray> lastPredictions;


    public FuocoCore() throws FileNotFoundException {
    }

    private ArrayList<INDArray> retrievePredictions(SensorModel sensors) {
        if(count == 0) {
            ArrayList<INDArray> predictions = new ArrayList<>(2);
            double[] steering = new double[nets.length];
            double[] accelBrake = new double[nets.length];
            for (int i = 0; i < nets.length; i++) {
                INDArray prediction = nets[i].output(sensors2INDArray(sensors));
                steering[i] = prediction.getDouble(0);
                accelBrake[i] = prediction.getDouble(1);
            }

            predictions.add(Nd4j.create(steering));
            predictions.add(Nd4j.create(accelBrake));
            lastPredictions = predictions;
        }
        count++;
        if(count == 5){
            count=0;
        }

        return lastPredictions;
    }

    private INDArray sensors2INDArray(SensorModel sensors) {
        double[] d = new double[29];

        d[0] = sensors.getAngleToTrackAxis()/Math.PI;
        for (int i = 1; i < 20; i++) {
            d[i] = sensors.getTrackEdgeSensors()[i - 1]/200.0;
        }
        d[20] = sensors.getTrackPosition();
        d[21] = sensors.getSpeed()/300.0;
        d[22] = sensors.getLateralSpeed()/300.0;
        d[23] = sensors.getZSpeed()/300.0;
        for (int i = 24; i < 28; i++) {
            d[i] = sensors.getWheelSpinVelocity()[i - 24]/100.0;
        }
        d[28] = sensors.getRPM()/10000.0;



        return Nd4j.create(d);
    }

    public Action computeAction(SensorModel sensors) {
        Action action = new Action();

        ArrayList<INDArray> predictions = retrievePredictions(sensors);



        action.steering = predictions.get(0).meanNumber().doubleValue();
        double predicted = predictions.get(1).minNumber().doubleValue();
        if (predicted >= 0) {
            action.accelerate = predicted;
            action.brake = 0;
        } else {
            action.accelerate = 0;
            action.brake = -predicted;
        }

        if(predictions.get(0).stdNumber().doubleValue()>0.6){
            recoveryMode = true;
        } else if(predictions.get(0).stdNumber().doubleValue()>0.3){
            recoveryMode = false;
        }

        file.write(action.gear+"\n");

        return action;
    }

    public void loadGenome(IGenome genome) {
        nets = ((FuocoCoreGenome) genome).getNets();
    }

    public IGenome getGenome() throws IOException {
        return null;
    }
}
