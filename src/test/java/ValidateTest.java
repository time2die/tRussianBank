import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.time2java.tRussianBanr.domain.gaAnswer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by time2die on 06.11.16.
 */

public class ValidateTest {

    @Test
    public void hellTest() {
        System.out.println("hell");
    }

    @Test
    public void parseGA() throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.readValue(getFileContent("active_debt.json"), gaAnswer.class);
        om.readValue(getFileContent("money_statistic.json"), gaAnswer.class);
        om.readValue(getFileContent("statistic.json"), gaAnswer.class);
        om.readValue(getFileContent("users.json"), gaAnswer.class);
    }

    @Test
    public void canReadResources() throws URISyntaxException, IOException {
        Assert.assertNotNull(getFileContent("users.json"));
    }

    public static String getFileContent(String fileName) {
        try {
            String myString = new String(Files.readAllBytes(Paths.get(ValidateTest.class.getClassLoader().getResource(fileName).toURI())));
            return myString;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        Assert.fail();
        return null;
    }

}
