import org.infai.seits.sepl.operators.Message;
import org.junit.Test;

import java.util.List;

public class TestPreprocessing {

    public void run() throws Exception {
        PreProcessing pre = new PreProcessing();
        List<Message> messages = TestMessageProvider.getTestMesssagesSet();
        int data_size = messages.size();
        for (int i = 0; i < data_size; i++) {
            Message m = messages.get(i);
            pre.config(m);
            pre.run(m);
        }

        RequestHandler requestHandler = new RequestHandler();
        requestHandler.getSolutions();
    }

    @Test
    public void Test() throws Exception {
        run();
    }
}
