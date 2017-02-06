package Swarm.objects;

import Swarm.map.Cell;

import java.io.Serializable;

/**
 * Created by yourname on 2/4/17.
 */
public class Net extends GameObject implements Serializable{

    public Net(int id, Cell position) {
        super(id, position);
    }

}
