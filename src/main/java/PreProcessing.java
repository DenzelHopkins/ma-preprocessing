import org.infai.seits.sepl.operators.Message;
import org.infai.seits.sepl.operators.OperatorInterface;

import org.json.JSONObject;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PreProcessing implements OperatorInterface {

    protected Stack segment;
    protected int windowSize;
    protected LocalDateTime startTime;
    protected int amountOfMotionSensors;
    protected int amountOfDoorSensors;

    protected JSONObject jsonRequest;

    protected String time_to_parse;
    protected String label;

    protected FeatureExtraction extraction;
    protected Segmentation segmentation;

    protected List answer;

    public PreProcessing() {
        segment = new Stack<>();
        amountOfMotionSensors = 32;
        amountOfDoorSensors = 4;
        jsonRequest = new JSONObject();
        extraction = new FeatureExtraction(amountOfMotionSensors, amountOfDoorSensors);
        windowSize = 8;
        segmentation = new Segmentation(windowSize);
    }

    @Override
    public void run(Message message) {
        time_to_parse = message.getInput("timestamp").getString();
        label = message.getInput("activity").getString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime time = LocalDateTime.parse(time_to_parse, formatter);

        if(segment.size() == 0){
            startTime = time;
            System.out.println(startTime);
        }

        answer = segmentation.sensorEventBased(segment, message);
        segment = (Stack) answer.get(1);

        if ((Boolean)answer.get(0)){
            try {
                extraction.run(segment, label, startTime);
                segment.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void config(Message message) {
        message.addInput("value");
        message.addInput("timestamp");
        message.addInput("device");
        message.addInput("activity");
    }
}

