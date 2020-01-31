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
        for (int i = 0; i < data_size; i++) {
            Message m = messages.get(i);
            pre.config(m);
            pre.run(m);
        }

        requestHandler requestHandler = new requestHandler();
        requestHandler.getSolutions();
    }

    @Test
    public void Test() throws Exception {
        run();
    }
}
