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
    JSONObject m;
    String device;
    String value;
    int currentSensorNumber;
    int numberOfMotionSensors;
    int numberOfDoorSensors;
    double triggeredMotionSensors;
    double triggeredDoorSensors;

    double windowSize;
    JSONObject jsonRequest = new JSONObject();

    public FeatureExtraction(int MotionSensors, int DoorSensors, int WindowSize) {

         /* [Late night, Morning, Noon, Afternoon, Evening, Night,
            M001ON, M002ON, ... , M031OFF, MO32OFF,D001ON,D002ON,..., D004OFF,
            TotalTriggeredMotionSensors, TotalTriggeredDoorSensors]*/

        feature = new ArrayList<>();
        numberOfMotionSensors = MotionSensors;
        numberOfDoorSensors = DoorSensors;
        windowSize = WindowSize;

    }

    public JSONObject run(Stack<JSONObject> segment, String label, LocalDateTime startTime, Boolean training) throws IOException {

        /* Clear the last feature vector */
        feature.clear();

        /* Array with triggered motionSensors [M001ON, M002ON, ... , M031OFF, MO32OFF]*/
        motionSensors = new ArrayList<>(Collections.nCopies(numberOfMotionSensors * 2, 0.0));

        /* Array with triggered doorSensors [M001ON, M002ON, ... , M031OFF, MO32OFF]*/
        doorSensors = new ArrayList<>(Collections.nCopies(numberOfDoorSensors * 2, 0.0));

        /* Number of triggered motion- and doorSensors */
        triggeredMotionSensors = 0;
        triggeredDoorSensors = 0;

        /* Extract featureVector */
        {
            for (int i = 0; i < windowSize; i++) {
                m = segment.pop();
                device = m.getString("device");
                value = m.getString("value");

                if (i == 0) {
                    final String time_to_parse = m.getString("timestamp");
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
                    LocalDateTime time = LocalDateTime.parse(time_to_parse, formatter);

                    /* Set dayTime feature */
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
                }

                /* Prepare string of the device */
                currentSensorNumber = Integer.parseInt(device.replaceAll("\\D+", ""));

                /* MotionSensor triggered or not */
                if (device.contains("M")) {
                    if (value.equals("ON") && motionSensors.get(currentSensorNumber - 1) == 0.0) {
                        motionSensors.set(currentSensorNumber - 1, 1.0);
                    } else if (value.equals("OFF") && motionSensors.get(currentSensorNumber + numberOfMotionSensors - 1) == 0.0) {
                        motionSensors.set(currentSensorNumber + numberOfMotionSensors - 1, 1.0);
                    }
                    triggeredMotionSensors++;
                }

                /* DoorSensor triggered or not */
                else if (device.contains("T")) {
                    if (value.equals("OPEN") && doorSensors.get(currentSensorNumber - 1) == 0.0) {
                        doorSensors.set(currentSensorNumber - 1, 1.0);
                    } else if (value.equals("CLOSE") && doorSensors.get(currentSensorNumber + numberOfDoorSensors - 1) == 0.0) {
                        doorSensors.set(currentSensorNumber + numberOfDoorSensors - 1, 1.0);
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

            /* Add time feature */
            Date date = Date.from(startTime.atZone(ZoneId.systemDefault()).toInstant());
            feature.add((double) date.getTime());

        }

        /* HTTP request to the server with the featureSegment */
        jsonRequest.put("label", label);
        jsonRequest.put("feature", feature);
        jsonRequest.put("training", training);

        return jsonRequest;

    }
}


