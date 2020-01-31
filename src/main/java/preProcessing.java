import org.infai.seits.sepl.operators.Message;
import org.infai.seits.sepl.operators.OperatorInterface;

import org.json.JSONObject;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class preProcessing implements OperatorInterface {

    protected Stack<Message> segment;
    protected int windowSize;
    protected LocalDateTime startTime;
    protected int amountOfMotionSensors;
    protected Map<String, Integer> solution;
    protected JSONObject jsonRequest;

    protected String time_to_parse;
    protected String label;

    protected featureExtraction extraction;

    public preProcessing() {
        segment = new Stack<>();
        windowSize = 120;
        amountOfMotionSensors = 32;
        jsonRequest = new JSONObject();
        extraction = new featureExtraction(amountOfMotionSensors);
    }

    @Override
    public void run(Message message) {
        time_to_parse = message.getInput("timestamp").getString();
        label = message.getInput("activity").getString();


        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime time = LocalDateTime.parse(time_to_parse, formatter);

        // Building Segments
        {
            if (segment.size() == 0) {
                segment.add(message);
                startTime = time;
            } else if (Duration.between(startTime, time).getSeconds() > windowSize) {
                try {
                    extraction.run(segment, label, startTime);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println(startTime);
                segment.clear();
                segment.add(message);
                startTime = time;
            } else {
                segment.add(message);
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

