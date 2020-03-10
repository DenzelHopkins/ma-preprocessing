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

    protected Boolean training;
    protected Boolean manuellSegmentation;
    protected Integer trainingTime;
    protected LocalDateTime start;

    protected List answer;

    public PreProcessing() {
        segment = new Stack<>();
        amountOfMotionSensors = 32;
        amountOfDoorSensors = 4;
        jsonRequest = new JSONObject();
        windowSize = 9;
        extraction = new FeatureExtraction(amountOfMotionSensors, amountOfDoorSensors, windowSize);
        segmentation = new Segmentation(windowSize);

        manuellSegmentation = true;
        training = true;
        trainingTime = 10; // in days
        start = LocalDateTime.parse("2010-11-04 00:03:50.209589", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        startTime = start;
    }

    @Override
    public void run(Message message) {

        label = message.getInput("activity").getString();
        time_to_parse = message.getInput("timestamp").getString();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime time = LocalDateTime.parse(time_to_parse, formatter);

        if (training && manuellSegmentation){
            if (time.minusDays(trainingTime).isAfter(start)){
                training = false;
            }
            answer = segmentation.manuell(segment, message, label, training);
        } else {
            answer = segmentation.sensorEventBased(segment, message);
        }
        segment = (Stack) answer.get(1);

        if ((Boolean)answer.get(0)){
            try {
                extraction.run(segment, label, startTime, training, manuellSegmentation);
                segment.clear();
                segment.add(message);
                startTime = time;
                System.out.println(startTime);
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

