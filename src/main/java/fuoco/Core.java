package fuoco;

import cicontest.torcs.genome.IGenome;
import scr.Action;
import scr.SensorModel;

import java.io.IOException;
import java.io.Serializable;

public interface Core extends Serializable {

    Action computeAction(Action action, SensorModel sensors);

    void loadGenome(IGenome genome);

    IGenome getGenome() throws IOException;
}
