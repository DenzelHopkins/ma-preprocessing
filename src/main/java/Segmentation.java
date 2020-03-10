import org.infai.seits.sepl.operators.Message;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Segmentation {

    protected int windowSize;

    public Segmentation(int size){
        this.windowSize = size;
    }

    public List<Object> sensorEventBased(Stack<Message> segment, Message message){

            if (segment.size() < windowSize - 1) {
                segment.add(message);
                return Arrays.asList(false, segment);
            } else if (segment.size() == windowSize - 1){
                segment.add(message);
                return Arrays.asList(true, segment);
            }
             return Arrays.asList(false, segment);
    }

    public void timeEventBased(){

    }
}
