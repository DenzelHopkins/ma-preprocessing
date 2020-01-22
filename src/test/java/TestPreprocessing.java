import org.infai.seits.sepl.operators.Message;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestPreprocessing {

    public void run() throws Exception {
        PreProcessing pre = new PreProcessing();
        List<Message> messages = TestMessageProvider.getTestMesssagesSet();
        int data_size = messages.size();
        // int data_size = 200000;
        for (int i = 0; i < data_size; i++) {
            Message m = messages.get(i);
            pre.config(m);
            pre.run(m);
        }
        System.out.println(pre.solution.toString());
    }

    @Test
    public void Test() throws Exception {
        run();
    }
}
