package Swarm.objects;
import Swarm.map.Cell;
/**
 * Created by yourname on 2/4/17.
 */
public class Teleport extends GameObject {

    private Cell pair;

    public Teleport(int id, Cell position) {
        super(id, position);
    }

    public Teleport(int id, Cell position, Cell pair) {
        super(id, position);
        this.pair = pair;
    }


    public Cell getPair() {
        return pair;
    }

    public void setPair(Cell pair) {
        this.pair = pair;
    }
}
