import com.google.gson.Gson;
import kafka.utils.Json;
import kafka.utils.json.JsonArray;
import kafka.utils.json.JsonObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.infai.seits.sepl.operators.Message;
import org.infai.seits.sepl.operators.OperatorInterface;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.util.*;

public class PreProcessing implements OperatorInterface {

    protected Stack<Message> segment;
    protected int windowSize;
    protected LocalDateTime startTime;
    protected int amountOfMotionSensors;

    public PreProcessing() {
        segment = new Stack<>();
        windowSize = 300;
        amountOfMotionSensors = 32;

    }

    @Override
    public void run(Message message) {

        final String value = message.getInput("value").getString();
        final String device = message.getInput("device").getString();
        final String time_to_parse = message.getInput("timestamp").getString();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime time = LocalDateTime.parse(time_to_parse, formatter);

        // Building Segments
        {
            if (segment.size() == 0) {
                segment.add(message);
                startTime = time;
            } else if (Duration.between(startTime, time).getSeconds() > windowSize) {
                try {
                    featureExtraction(segment);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
    }

    public ArrayList featureExtraction(Stack<Message> segment) throws IOException {
        System.out.println("New Segment");

        /* [Late night, Morning, Noon, Afternoon, Evening, Night,
            Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday,
            Weekday, Weekend,
            M001ON, M002ON, ... , M031OFF, MO32OFF,
            TotalTriggeredMotionSensors]*/

        ArrayList<Double> feature = new ArrayList<>();


        double numberMotionSensors;
        double triggeredMotionSensors = 0;
        String device;
        String value;

        ArrayList<Double> motionSensors = new ArrayList<>(Collections.nCopies(amountOfMotionSensors * 2, 0.0)); /*[M001ON, M002ON, ... , M031OFF, MO32OFF]*/
        Integer currentSensorNumber;

        Message m;

        for (int i = 0; i < segment.size(); i++) {
            m = segment.pop();
            device = m.getInput("device").getString();
            value = m.getInput("value").getString();

            if (i == 0) {
                final String time_to_parse = m.getInput("timestamp").getString();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
                LocalDateTime time = LocalDateTime.parse(time_to_parse, formatter);

                /*DayTimeFeature*/
                switch (time.getHour()) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                        feature.addAll(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 1.0));
                        break;
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                    case 10:
                    case 11:
                        feature.addAll(Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0));
                        break;
                    case 12:
                    case 13:
                    case 14:
                        feature.addAll(Arrays.asList(0.0, 1.0, 0.0, 0.0, 0.0, 0.0));
                        break;
                    case 15:
                    case 16:
                    case 17:
                        feature.addAll(Arrays.asList(0.0, 0.0, 1.0, 0.0, 0.0, 0.0));
                        break;
                    case 18:
                    case 19:
                    case 20:
                    case 21:
                        feature.addAll(Arrays.asList(0.0, 0.0, 0.0, 1.0, 0.0, 0.0));
                        break;
                    case 22:
                    case 23:
                    case 0:
                        feature.addAll(Arrays.asList(0.0, 0.0, 0.0, 0.0, 1.0, 0.0));
                        break;
                }

                /*DayOfWeek*/
                switch (time.getDayOfWeek()) {
                    case MONDAY:
                        feature.addAll(Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
                        break;
                    case TUESDAY:
                        feature.addAll(Arrays.asList(0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0));
                        break;
                    case WEDNESDAY:
                        feature.addAll(Arrays.asList(0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0));
                        break;
                    case THURSDAY:
                        feature.addAll(Arrays.asList(0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0));
                        break;
                    case FRIDAY:
                        feature.addAll(Arrays.asList(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0));
                        break;
                    case SATURDAY:
                        feature.addAll(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0));
                        break;
                    case SUNDAY:
                        feature.addAll(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0));
                        break;
                }

                /*WeekendOrNot*/
                switch (time.getDayOfWeek()) {
                    case MONDAY:
                    case TUESDAY:
                    case WEDNESDAY:
                    case THURSDAY:
                        feature.addAll(Arrays.asList(1.0, 0.0));
                        break;
                    case SATURDAY:
                    case SUNDAY:
                        feature.addAll(Arrays.asList(0.0, 1.0));
                        break;
                }
            }

            if (device.contains("M")) {
                currentSensorNumber = Integer.parseInt(device.replaceAll("\\D+", ""));
                if (value.equals("ON") && motionSensors.get(currentSensorNumber - 1) == 0.0) {
                    motionSensors.set(currentSensorNumber - 1, 1.0);
                } else if (value.equals("OFF") && motionSensors.get(currentSensorNumber + amountOfMotionSensors - 1) == 0.0) {
                    motionSensors.set(currentSensorNumber + amountOfMotionSensors - 1, 1.0);
                }
                triggeredMotionSensors++;
            }
        }

        feature.addAll(motionSensors);
        //feature.add(triggeredMotionSensors); WIE normalisieren?


        Date date = Date.from( startTime.atZone( ZoneId.systemDefault()).toInstant());
        feature.add((double) date.getTime());

        HttpPost post = new HttpPost("http://127.0.0.1:5000/discovery");

        String json = new Gson().toJson(feature);

        StringEntity stringEntity = new StringEntity(json);
        post.setEntity(stringEntity);

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            System.out.println(EntityUtils.toString(response.getEntity()));
        }

        return null;
    }
}

