package fuoco;

import cicontest.torcs.genome.IGenome;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;

import java.io.IOException;

public class FuocoCoreGenome implements IGenome {
    private static final long serialVersionUID = 6534186543165341653L;
    private MultiLayerNetwork net;

    FuocoCoreGenome(String file) throws Exception {
        String ext = file.split("\\.")[1];
        if (ext.equals("ffn")) {
            this.net = ModelSerializer.restoreMultiLayerNetwork(file);
        } else {
            throw new Exception("Unkwnown extension");
        }
    }

    public MultiLayerNetwork getNet() {
        return net;
    }
}

