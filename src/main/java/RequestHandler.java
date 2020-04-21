import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;


public class RequestHandler {

    String uri = "http://127.0.0.1:5000/";
    StringEntity stringEntity;

    public JSONObject analyseDataPoint(JSONObject jsonRequest) throws IOException {
        stringEntity = new StringEntity(jsonRequest.toString());
        HttpPost post = new HttpPost(uri + "analyseDataPoint");
        post.setEntity(stringEntity);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                return new JSONObject(EntityUtils.toString(response.getEntity()));
            }
        }
    }

    public JSONObject initializeServer(JSONObject jsonRequest) throws IOException {
        stringEntity = new StringEntity(jsonRequest.toString());
        HttpPost post = new HttpPost(uri + "initializeServer");
        post.setEntity(stringEntity);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpClient.execute(post)) {
                return new JSONObject(EntityUtils.toString(response.getEntity()));
            }
        }
    }
}
