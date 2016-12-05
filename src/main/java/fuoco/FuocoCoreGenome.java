package fuoco;

import cicontest.torcs.genome.IGenome;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FuocoCoreGenome implements IGenome {
    private static final long serialVersionUID = 6534186543165341653L;
    private MultiLayerNetwork[] nets;

    private double[] steeringWeights;
    private double[] accelBrakegWeights;

    private boolean ABS;
    private boolean AutomatedGearbox;
    private boolean min;
    private double space_offset;
    private double brake_force;

    private static INDArray read(File file) throws IOException {
        List<Double> l = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            l.add(Double.parseDouble(line));
        }
        double[] r = new double[l.size()];
        for (int i = 0; i < l.size(); i++) {
            r[i] = l.get(i);
        }
        return Nd4j.create(r);
    }

    FuocoCoreGenome(String path, double[] steeringWeights, double[] accelBrakegWeights, boolean ABS, boolean AutomatedGearbox, boolean min, double space_offset, double brake_force) throws Exception {

        File dir = new File(path);
        File[] files = dir.listFiles();

        assert files != null;

        nets = new MultiLayerNetwork[files.length];

        for (int c = 0; c < files.length; c++) {
            nets[c] = new MultiLayerNetwork(new NeuralNetConfiguration.Builder()
                    .list()
                    .layer(0, new DenseLayer.Builder()
                            .nIn(29)
                            .nOut(200)
                            .activation("relu")
                            .build())
                    .layer(1, new DenseLayer.Builder()
                            .nIn(200)
                            .nOut(200)
                            .activation("relu")
                            .build())
                    .layer(2, new OutputLayer.Builder()
                            .activation("tanh")
                            .nIn(200)
                            .nOut(2)
                            .build())
                    .pretrain(false)
                    .build());
            nets[c].init(read(files[c]), true);
        }

        this.steeringWeights = steeringWeights;
        this.accelBrakegWeights = accelBrakegWeights;

        this.ABS = ABS;
        this.AutomatedGearbox = AutomatedGearbox;
        this.min = min;
        this.space_offset = space_offset;
        this.brake_force = brake_force;
    }

    public MultiLayerNetwork[] getNets() {
        return nets;
    }

    public double[] getSteeringWeights() {
        return steeringWeights;
    }

    public double[] getAccelBrakegWeights() {
        return accelBrakegWeights;
    }

    public boolean getABS() {
        return ABS;
    }

    public boolean getAutomatedGearbox() {
        return AutomatedGearbox;
    }

    public boolean getMin() {
        return min;
    }

    public double getSpace_offset() {
        return space_offset;
    }

    public double getBrake_force() {
        return brake_force;
    }
}

