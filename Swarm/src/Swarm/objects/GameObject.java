package Swarm.objects;
import Swarm.map.Cell;
import util.Log;

import java.io.Serializable;

/**
 * Created by yourname on 2/4/17.
 */
public abstract class GameObject implements Serializable {

    private int id;
    private Cell position;
    private int deadTime = -1;
    public static final String TAG = "GameObject";

    public GameObject(int id, Cell position){
        this.id = id;
        this.position = position;
        Log.i(TAG, "Created " + this);
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

    @Override
    public String toString() {
        return String.format("[%s=%d]", getClass().getSimpleName(), id);
    }
}
