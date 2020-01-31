import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;


public class requestHandler {

    String uri = "http://127.0.0.1:5000/";
    HttpPost post = new HttpPost(uri + "discovery");
    HttpGet get = new HttpGet(uri + "solution");
    StringEntity stringEntity;

    public void postSegment(JSONObject jsonRequest) throws IOException {
        stringEntity = new StringEntity(jsonRequest.toString());
        post.setEntity(stringEntity);
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            CloseableHttpResponse response = httpClient.execute(post);
            try {
                JSONObject result = new JSONObject(EntityUtils.toString(response.getEntity()));
                result.get("answer").toString();
            } finally {
                response.close();
            }
        } finally {
            httpClient.close();
        }

    }

    public void getSolutions() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        try {
            CloseableHttpResponse response = httpClient.execute(get);
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
}
