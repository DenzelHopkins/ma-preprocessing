import org.infai.seits.sepl.operators.Message;
import org.junit.Test;

import java.util.List;

public class TestPreprocessing {

    public void run() throws Exception {
        PreProcessing pre = new PreProcessing();
        List<Message> messages = TestMessageProvider.getTestMesssagesSet();
        int data_size = 300;
        for (int i = 0; i < data_size; i++) {
            Message m = messages.get(i);

            pre.config(m);
            pre.run(m);
        }

    }

    @Test
    public void Test() throws Exception {
        run();
    }
}
