import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.infai.seits.sepl.operators.Message;
import org.json.JSONObject;
import org.junit.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TestPreprocessing {

    public void run() throws Exception {
        preProcessing pre = new preProcessing();
        List<Message> messages = TestMessageProvider.getTestMesssagesSet();
        int data_size = messages.size();
        //int data_size = 500000;
        for (int i = 0; i < data_size; i++) {
            Message m = messages.get(i);
            pre.config(m);
            pre.run(m);
        }

        // get solutions
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            CloseableHttpResponse response = httpClient.execute(new HttpGet("http://127.0.0.1:5000/solution"));
            try {
                JSONObject result = new JSONObject(EntityUtils.toString(response.getEntity()));
                String founded_activities = result.get("founded_activities").toString();
                String accuracy = result.get("accuracy").toString();
                System.out.println("Founded activities are: " + founded_activities);
                System.out.println("Accuracy is: " + accuracy);
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }
    }

    @Test
    public void Test() throws Exception {
        run();
    }
}
