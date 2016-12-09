package fuoco;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class NeuralNet {
    
    private List<Matrix> net;

    private static void relu(Matrix matrix) {
        for(int i = 0; i < matrix.row; i++) {
            for(int j = 0; j < matrix.col; j++) {
                matrix.values[i][j] = Math.max(0,  matrix.values[i][j]);
            }
        }
    }

    private static void tanh(Matrix matrix) {
        for(int i = 0; i < matrix.row; i++) {
            for(int j = 0; j < matrix.col; j++) {
                matrix.values[i][j] = Math.tanh(matrix.values[i][j]);
            }
        }
    }

    private static List<Matrix> readM(File file) throws IOException {
        List<Double> l = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            l.add(Double.parseDouble(line));
        }

        List<Matrix> matrices = new LinkedList<>();

        int c = 0;

        double[][] w1d = new double[200][29];
        for (int i = 0; i < 200; i++) {
            for (int j = 0; j < 29; j++) {
                w1d[i][j] = l.get(c);
                c++;
            }
        }
        matrices.add(new Matrix(w1d));

        double[][] b1d = new double[200][1];
        for (int i = 0; i < 200; i++) {
            b1d[i][0] = l.get(c);
            c++;
        }
        matrices.add(new Matrix(b1d));

        double[][] w2d = new double[200][200];
        for (int i = 0; i < 200; i++) {
            for (int j = 0; j < 200; j++) {
                w2d[i][j] = l.get(c);
                c++;
            }
        }
        matrices.add(new Matrix(w2d));

        double[][] b2d = new double[200][1];
        for (int i = 0; i < 200; i++) {
            b2d[i][0] = l.get(c);
            c++;
        }
        matrices.add(new Matrix(b2d));

        double[][] w3d = new double[2][200];
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 200; j++) {
                w3d[i][j] = l.get(c);
                c++;
            }
        }
        matrices.add(new Matrix(w3d));

        double[][] b3d = new double[2][1];
        for (int i = 0; i < 2; i++) {
            b3d[i][0] = l.get(c);
            c++;
        }
        matrices.add(new Matrix(b3d));

        return matrices;
    }
    
    public NeuralNet(File path) throws IOException {
        net = readM(path);
    }
    
    public Matrix predict(Matrix in) {
        Matrix w1 = net.get(0);
        Matrix b1 = net.get(1);
        Matrix w2 = net.get(2);
        Matrix b2 = net.get(3);
        Matrix w3 = net.get(4);
        Matrix b3 = net.get(5);

        Matrix a1 = w1.Multiply(in).Add(b1);
        relu(a1);
        Matrix a2 = w2.Multiply(a1).Add(b2);
        relu(a2);
        Matrix a3 = w3.Multiply(a2).Add(b3);
        tanh(a3);

        return a3;
    }

}
