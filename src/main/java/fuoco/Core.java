package fuoco;

import cicontest.torcs.genome.IGenome;
import scr.SensorModel;
import scr.Action;

import java.io.*;

public interface Core extends Serializable {

    public Action computeAction(SensorModel a);

    void loadGenome(IGenome genome);

    IGenome getGenome();
}
