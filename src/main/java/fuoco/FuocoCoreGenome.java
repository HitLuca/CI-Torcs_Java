package fuoco;

import cicontest.torcs.genome.IGenome;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

public class FuocoCoreGenome implements IGenome {
    private static final long serialVersionUID = 6534186543165341653L;
    private MultiLayerNetwork net;

    FuocoCoreGenome(MultiLayerNetwork net) {
        this.net = net;
    }

    public MultiLayerNetwork getNet() {
        return net;
    }
}

