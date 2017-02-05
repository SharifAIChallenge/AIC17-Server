package Swarm.objects;
import Swarm.map.Cell;

/**
 * Created by yourname on 2/4/17.
 */
public abstract class GameObject {

    private int id;
    private Cell position;
    private int deadTime;

    public GameObject(int id, Cell position){
        this.id = id;
        this.position = position;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Cell getPosition() {
        return position;
    }

    public void setPosition(Cell position) {
        this.position = position;
    }

    public int getDeadTime() {
        return deadTime;
    }

    public void setDeadTime(int deadTime) {
        this.deadTime = deadTime;
    }
}
