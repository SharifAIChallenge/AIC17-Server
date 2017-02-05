package Swarm.objects;
import Swarm.map.Cell;
/**
 * Created by yourname on 2/4/17.
 */
public class Net extends GameObject {

    private int createTime;

    public Net(int id, Cell position) {
        super(id, position);
    }


    public int getCreateTime() {
        return createTime;
    }

    public void setCreateTime(int createTime) {
        this.createTime = createTime;
    }
}
