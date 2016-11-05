package fuoco;


import cicontest.algorithm.abstracts.DriversUtils;
import cicontest.torcs.genome.IGenome;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;

public class TrainFuocoCore {
    public static void main(String[] args) throws IOException, ArgumentParserException, InterruptedException {

        ArgumentParser parser = ArgumentParsers.newArgumentParser("Train")
                .defaultHelp(true)
                .description("#TODO");

        parser.addArgument("open")
                .help("Filename for loading genome");

        parser.addArgument("save")
                .help("Filename for saving genome");

        Namespace res = parser.parseArgs(args);

        String load = res.getString("open");
        String save = res.getString("save");

        RecordReader recordReader = new CSVRecordReader(1,",");
        recordReader.initialize(new FileSplit(new File(load)));

        DataSetIterator iterator = new RecordReaderDataSetIterator(recordReader, 1, 0, 2, true);

        DataSet train_set = iterator.next();

        int iterations = 20;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(12165115614545L)
            .iterations(iterations)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .weightInit(WeightInit.XAVIER)
            .list()
            .layer(0, new DenseLayer.Builder()
                .nIn(22)
                .nOut(100)
                .activation("sigmoid")
                .build())
            .layer(1, new DenseLayer.Builder()
                .nIn(100)
                .nOut(100)
                .activation("sigmoid")
                .build())
            .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.SQUARED_LOSS)
                .activation("identity")
                .nIn(100)
                .nOut(3)
                .build())
            .pretrain(false)
            .backprop(true)
            .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();
        net.setListeners(new ScoreIterationListener(1));
        net.fit(train_set);

        ModelSerializer.writeModel(net, "pippo", true);
    }
}
