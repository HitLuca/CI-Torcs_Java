package fuoco;

import cicontest.torcs.genome.IGenome;

import java.io.File;

public class FuocoCoreGenome implements IGenome {
    private static final long serialVersionUID = 6534186543165341653L;
    private NeuralNet[] nets;
    private double space_offset;
    private double brake_force;

    FuocoCoreGenome(String path, double space_offset, double brake_force) throws Exception {

        File dir = new File(path);
        File[] files = dir.listFiles();

        assert files != null;

        nets = new NeuralNet[files.length];

        for (int c = 0; c < files.length; c++) {
            nets[c] = new NeuralNet(files[c]);
        }

        this.space_offset = space_offset;
        this.brake_force = brake_force;
    }

    public NeuralNet[] getNets() {
        return nets;
    }

    public double getSpace_offset() {
        return space_offset;
    }

    public double getBrake_force() {
        return brake_force;
    }
}

