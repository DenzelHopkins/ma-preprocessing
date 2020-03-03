import org.apache.kafka.common.protocol.types.Field;
import org.infai.seits.sepl.operators.Message;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class Segmentation {

    protected int windowSize;
    protected String currentActivity;

    public Segmentation(int size) {
        this.windowSize = size;
        this.currentActivity = "";
    }

    public List<Object> sensorEventBased(Stack<Message> segment, Message message) {
        if (segment.size() < windowSize) {
            segment.add(message);
            return Arrays.asList(false, segment);
        } else if (segment.size() == windowSize) {
            return Arrays.asList(true, segment);
        }
        return Arrays.asList(false, segment);
    }

    public List<Object> manuell(Stack<Message> segment, Message message, String label, Boolean training) {
        if (currentActivity.equals(label) && training) {
            segment.add(message);
            return Arrays.asList(false, segment);
        } else {
            currentActivity = label;
            if (!segment.isEmpty()){
                return Arrays.asList(true, segment);
            } else {
                return Arrays.asList(false, segment);
            }
        }
    }
}
