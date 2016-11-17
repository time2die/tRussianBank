import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.time2java.tRussianBank.utils.ConvertTools;
import org.time2java.tRussianBank.domain.User;
import org.time2java.tRussianBank.domain.gaAnswer;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

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


    static private  gaAnswer getUsersAnswer(){
        ObjectMapper om = new ObjectMapper();
        try {
            return om.readValue(getFileContent("users.json"), gaAnswer.class);
        } catch (IOException e) {
            Assert.fail();
        }
        return null ;
    }

    @Test
    public void canParseUser(){
        gaAnswer usersAnswer = getUsersAnswer() ;
        List<User> users = ConvertTools.convertAnswerToUsers(usersAnswer) ;
        Assert.assertTrue(users.size() == 3);
        Assert.assertTrue(users.contains(getUser1()));
    }

    private User getUser1() {
        return User
                .builder()
                .name("Кухарев Даниил")
                .city("Новосибирск")
                .build();
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
