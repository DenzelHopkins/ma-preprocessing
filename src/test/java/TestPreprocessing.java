import org.infai.seits.sepl.operators.Message;
import org.junit.Test;

import java.util.List;

public class TestPreprocessing {

    public void run() throws Exception {
        preProcessing pre = new preProcessing();
        List<Message> messages = TestMessageProvider.getTestMesssagesSet();
        // int data_size = messages.size();
        int data_size = 500000;
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
