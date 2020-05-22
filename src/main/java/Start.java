import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Start {

    public static void main(String[] args) throws IOException {

        /* Parameter */
        String system = args[0];
        boolean otherClass = Boolean.parseBoolean(args[1]); // Include Other-Class or not
        int windowSize = Integer.parseInt(args[2]); // WindowsSize Sensor based
        int trainingDuration = 30; // TrainingDuration in days
        int amountOfMotionSensors = 32; // Amount of the Motion Sensors
        int amountOfDoorSensors = 4; // Amount of the Door Sensors

        /* Get startTime of the first dataSegment */
        LocalDateTime startTime = LocalDateTime.parse(
                "2010-11-04 00:03:50.209589",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));

        /* Build processing class */
        Processing pre = new Processing(
                otherClass, windowSize, trainingDuration,
                amountOfMotionSensors, amountOfDoorSensors,
                startTime);

        /* HTTP request to initialize the server */
        RequestHandler requestHandler = new RequestHandler();
        JSONObject jsonRequest = new JSONObject();
        JSONObject answer;
        jsonRequest.put("system", system);
        answer = requestHandler.initializeServer(jsonRequest);
        System.out.println(answer.getString("answer"));

        /* Iterate over data */
        BufferedReader br = new BufferedReader(new FileReader("datenerfassung/src/main/data/arubaTest.json"));
        String line;
        JSONObject message;
        while ((line = br.readLine()) != null) {
            message = new JSONObject(line);
            pre.run(message);
        }
        requestHandler.getSolutions();
    }
}
