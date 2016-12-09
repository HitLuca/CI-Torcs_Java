package fuoco;

import cicontest.torcs.genome.IGenome;
import java.io.*;


public class FuocoCoreGenome implements IGenome {
    private static final long serialVersionUID = 6534186543165341653L;
    private NeuralNet[] nets;
    private double space_offset;
    private double brake_force;

    FuocoCoreGenome(String path, double space_offset, double brake_force) throws Exception {


        nets = new NeuralNet[5];

        String[] names = {"alpine-1.ffn", "alpine-1_speed245_actor.ffn", "corkscrew.ffn", "e-track-4_speed170_actor.ffn", "street-1.ffn"};

        for (int c = 0; c < names.length; c++) {
            InputStreamReader stream = new InputStreamReader(getClass().getResourceAsStream("/memory/nets/"+names[c]));
            nets[c] = new NeuralNet(stream);

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