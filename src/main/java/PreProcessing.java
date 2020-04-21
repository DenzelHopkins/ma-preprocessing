import org.json.JSONObject;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PreProcessing {

    protected Stack segment;
    protected JSONObject jsonRequest;
    protected RequestHandler requestHandler;
    protected String timeToParse;
    protected String label;
    protected FeatureExtraction extraction;
    protected Segmentation segmentation;
    protected Boolean training;
    protected Integer trainingDuration;
    protected Boolean otherClass;
    protected LocalDateTime startTime;
    protected LocalDateTime segmentTime;
    protected List answerSegmentation;
    protected JSONObject activities;

    public PreProcessing(boolean otherClass,
                         int windowSize,
                         int trainingDuration,
                         int amountOfMotionSensors,
                         int amountOfDoorSensors,
                         LocalDateTime startTime) {

        this.otherClass = otherClass;
        this.trainingDuration = trainingDuration;
        this.startTime = startTime;

        requestHandler = new RequestHandler();
        segment = new Stack<>();
        jsonRequest = new JSONObject();
        extraction = new FeatureExtraction(amountOfMotionSensors, amountOfDoorSensors, windowSize);
        segmentation = new Segmentation(windowSize);
        training = true;
        segmentTime = startTime; // time of the first segment
    }

    public void run(JSONObject message) {
        label = message.getString("activity");
        /* Skip message from temperature sensor */
        if (label.contains("T")) {
            System.out.println("Is Temperature, this message will be skipped!");
        } else {
            /* Get time of the message */
            timeToParse = message.getString("timestamp");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
            LocalDateTime time = LocalDateTime.parse(timeToParse, formatter);

            /* Training or not */
            if (time.minusDays(trainingDuration).isAfter(startTime)) {
                training = false;
            }

            /* Exclude OtherClass Messages */
            if (!otherClass && label.equals("Other")) {
                System.out.println("OtherClass is excluded, this message will be skipped!");
            } else {
                answerSegmentation = segmentation.sensorEventBased(segment, message);
                segment = (Stack) answerSegmentation.get(1);
                if ((Boolean) answerSegmentation.get(0)) {
                    try {
                        jsonRequest = extraction.run(segment,
                                label, startTime, training);

                        activities = requestHandler.analyseDataPoint(jsonRequest);

                        /* Recognized and discovered activity */
                        System.out.println("This is the recognized activity: " +
                                activities.getString("recognizedActivity") +
                                (" and this is the discoveredActivity: ") +
                                activities.getString("discoveredActivity")
                        );
                        segment.clear();
                        segment.add(message);
                        segmentTime = time;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}

