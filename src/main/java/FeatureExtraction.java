import org.infai.seits.sepl.operators.Message;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class FeatureExtraction {

    ArrayList<Double> feature;
    ArrayList<Double> motionSensors;
    ArrayList<Double> doorSensors;
    Message m;
    String device;
    String value;
    int currentSensorNumber;
    int amountOfMotionSensors;
    int amountOfDoorSensors;
    double triggeredMotionSensors;
    double triggeredDoorSensors;

    double windowSize;

    RequestHandler requestHandler = new RequestHandler();
    JSONObject jsonRequest = new JSONObject();

    public FeatureExtraction(int MotionSensors, int DoorSensors, int WindowSize) {

         /* [Late night, Morning, Noon, Afternoon, Evening, Night,
            Monday, Tuesday, Wednesday, Thursday, Friday, Saturday, Sunday,
            Weekday, Weekend,
            M001ON, M002ON, ... , M031OFF, MO32OFF,D001ON,D002ON,..., D004OFF,
            TotalTriggeredMotionSensors, TotalTriggeredDoorSensors]*/

        feature = new ArrayList<>();
        amountOfMotionSensors = MotionSensors;
        amountOfDoorSensors = DoorSensors;
        windowSize = WindowSize;

    }

    public void run(Stack<Message> segment, String label, LocalDateTime startTime, Boolean training, Boolean activityDiscovery) throws IOException {
        feature.clear();
        motionSensors = new ArrayList<>(Collections.nCopies(amountOfMotionSensors * 2, 0.0)); /*[M001ON, M002ON, ... , M031OFF, MO32OFF]*/
        doorSensors = new ArrayList<>(Collections.nCopies(amountOfDoorSensors * 2, 0.0)); /*[M001ON, M002ON, ... , M031OFF, MO32OFF]*/
        triggeredMotionSensors = 0;
        triggeredDoorSensors = 0;
        windowSize = segment.size();

        // building featureSegment
        {
            for (int i = 0; i < windowSize; i++) {
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
                        default:
                            feature.addAll(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
                    }
//                    /*DayOfWeek*/
//                    switch (time.getDayOfWeek()) {
//                        case MONDAY:
//                            feature.addAll(Arrays.asList(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
//                            break;
//                        case TUESDAY:
//                            feature.addAll(Arrays.asList(0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0));
//                            break;
//                        case WEDNESDAY:
//                            feature.addAll(Arrays.asList(0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0));
//                            break;
//                        case THURSDAY:
//                            feature.addAll(Arrays.asList(0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0));
//                            break;
//                        case FRIDAY:
//                            feature.addAll(Arrays.asList(0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0));
//                            break;
//                        case SATURDAY:
//                            feature.addAll(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0));
//                            break;
//                        case SUNDAY:
//                            feature.addAll(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0));
//                            break;
//                        default:
//                            System.out.println("--------------------NO WEEKDAY SET ");
//                            feature.addAll(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
//                    }
//
//                    /*WeekendOrNot*/
//                    switch (time.getDayOfWeek()) {
//                        case MONDAY:
//                        case TUESDAY:
//                        case WEDNESDAY:
//                        case THURSDAY:
//                            feature.addAll(Arrays.asList(1.0, 0.0));
//                            break;
//                        case FRIDAY:
//                        case SATURDAY:
//                        case SUNDAY:
//                            feature.addAll(Arrays.asList(0.0, 1.0));
//                            break;
//                        default:
//                            System.out.println("--------------------NO WEEKEND SET ");
//                            feature.addAll(Arrays.asList(0.0, 0.0));
//                    }
                }

                currentSensorNumber = Integer.parseInt(device.replaceAll("\\D+", ""));

                /* MotionSensor triggered or not */
                if (device.contains("M")) {
                    if (value.equals("ON") && motionSensors.get(currentSensorNumber - 1) == 0.0) {
                        motionSensors.set(currentSensorNumber - 1, 1.0);
                    } else if (value.equals("OFF") && motionSensors.get(currentSensorNumber + amountOfMotionSensors - 1) == 0.0) {
                        motionSensors.set(currentSensorNumber + amountOfMotionSensors - 1, 1.0);
                    }
                    triggeredMotionSensors++;
                }

                /* DoorSensor triggered or not */
                else if (device.contains("T")) {
                    if (value.equals("OPEN") && doorSensors.get(currentSensorNumber - 1) == 0.0) {
                        doorSensors.set(currentSensorNumber - 1, 1.0);
                    } else if (value.equals("CLOSE") && doorSensors.get(currentSensorNumber + amountOfDoorSensors - 1) == 0.0) {
                        doorSensors.set(currentSensorNumber + amountOfDoorSensors - 1, 1.0);
                    }
                    triggeredDoorSensors++;
                }
            }
            /* Adding door and motions */
            feature.addAll(doorSensors);
            feature.addAll(motionSensors);

            /* Amount of triggered motionSensors and doorSensors */
            double triggeredMotionSensorsPre = triggeredMotionSensors / windowSize;
            double triggeredDoorSensorsPre = triggeredDoorSensors / windowSize;
            feature.add(triggeredMotionSensorsPre);
            feature.add(triggeredDoorSensorsPre);

            /* add time feature */
            Date date = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
            feature.add((double) date.getTime());

        }

        // http request to the server with the featureSegment and set answer to solution
        jsonRequest.put("label", label);
        jsonRequest.put("feature", feature);
        jsonRequest.put("training", training);
        jsonRequest.put("activityDiscovery", activityDiscovery);
        System.out.println(jsonRequest);
        requestHandler.postSegment(jsonRequest);


    }
}


