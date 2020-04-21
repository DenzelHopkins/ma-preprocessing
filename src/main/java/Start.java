import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Start {

    public static void main(String[] args) throws IOException {
        boolean activityDiscovery = true; // Use Activity Discovery or not
        boolean otherClass = false; // Include Other-Class or not
        int windowSize = 6; // WindowsSize Sensor based
        int trainingDuration = 1; // TrainingDuration in days
        int amountOfMotionSensors = 32; // Amount of the Motion Sensors
        int amountOfDoorSensors = 4; // Amount of the Door Sensors

        RequestHandler requestHandler = new RequestHandler();
        JSONObject jsonRequest = new JSONObject();
        JSONObject answer;


        LocalDateTime startTime = LocalDateTime.parse(
                "2010-11-04 00:03:50.209589",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));  // StartTime of the dataSet

        PreProcessing pre = new PreProcessing(
                otherClass, windowSize, trainingDuration,
                amountOfMotionSensors, amountOfDoorSensors,
                startTime);

        /* HTTP request to initialize the server */
        jsonRequest.put("useActivityDiscovery", activityDiscovery);
        answer = requestHandler.initializeServer(jsonRequest);
        System.out.println(answer.getString("answer"));

        /* Iterate over data */
        BufferedReader br = new BufferedReader(new FileReader("src/main/data/arubaTest.json"));
        String line;
        JSONObject message;
        while ((line = br.readLine()) != null) {
            message = new JSONObject(line);
            pre.run(message);
        }
    }
}
