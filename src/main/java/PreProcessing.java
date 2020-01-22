import com.google.gson.Gson;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.infai.seits.sepl.operators.Message;
import org.infai.seits.sepl.operators.OperatorInterface;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class PreProcessing implements OperatorInterface {

    protected Stack<Message> segment;
    protected int windowSize;
    protected LocalDateTime startTime;
    protected int amountOfMotionSensors;
    protected Map<String, Integer> solution;
    protected JSONObject jsonRequest;

    protected String time_to_parse;
    protected String label;

    protected featureExtraction extraction;

    public PreProcessing() {
        segment = new Stack<>();
        windowSize = 120;
        amountOfMotionSensors = 32;
        jsonRequest = new JSONObject();
        extraction = new featureExtraction(amountOfMotionSensors);

        solution = new HashMap<>();
        solution.put("Sleeping", 0);
        solution.put("Meal_Preparation", 0);
        solution.put("Relax", 0);
        solution.put("Eating", 0);
        solution.put("Work", 0);
        solution.put("Wash_Dishes", 0);
        solution.put("Bed_to_Toilet", 0);
        solution.put("Enter_Home", 0);
        solution.put("Leave_Home", 0);
        solution.put("Housekeeping", 0);
        solution.put("Resperate", 0);
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
                    extraction.run(segment, label, startTime, solution);
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

