import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.time2java.tRussianBank.domain.gaAnswer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;


/**
 * Created by time2die on 20.11.16.
 */
public class GoogleApiClient {
    public static final String getStatus(){

        Config conf = ConfigFactory.load();

        String docId = conf.getString("docId");
        String key = conf.getString("key") ;
        String url = "https://sheets.googleapis.com/v4/spreadsheets/"+docId+"/values/A5%3AB7?key="+key;

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (IOException e) { e.printStackTrace();}

        System.out.println("-------------------------------------------: "+url);

        BufferedReader rd = null;
        try {
            rd = new BufferedReader( new InputStreamReader(response.getEntity().getContent()));
        } catch (IOException e) { e.printStackTrace(); }

        StringBuffer result = new StringBuffer();
        String line = "";
        try {
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) { e.printStackTrace(); }

        ObjectMapper om = new ObjectMapper();
        gaAnswer ga = null ;
        try {
            ga = om.readValue(result.toString() , gaAnswer.class);
        } catch (IOException e) { e.printStackTrace(); }

        result = new StringBuffer() ;
        for (List<String> iter1 : ga.getValues()) {
            for (String iter2 : iter1) {
                result.append(iter2) ;
                result.append(" ");
            }
            result.append("\n");
        }

        int indexR = result.indexOf("\u20BD") ;
        while ((indexR != -1)){
            result.setCharAt(indexR,'р');
            indexR = result.indexOf("\u20BD") ;
        }

        return  result.toString() ;
    }
}
