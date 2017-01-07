package org.time2java.tRussianBank;

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

    static Config conf = ConfigFactory.load();

    public static final String getStatus() {
        String docId = conf.getString("docId");
        String key = conf.getString("key");
        String url = "https://sheets.googleapis.com/v4/spreadsheets/" + docId + "/values/A5%3AB7?key=" + key;

        sAnswer ga = getAnswerFromGA(url);

        StringBuilder result = new StringBuilder();
        for (List<String> iter1 : ga.values()) {
            for (String iter2 : iter1) {
                result.append(iter2);
                result.append(" ");
            }
            result.append("\n");
        }

        int indexR = result.indexOf("\u20BD");
        while ((indexR != -1)) {
            result.setCharAt(indexR, 'р');
            indexR = result.indexOf("\u20BD");
        }

        return result.toString();
    }

    static sAnswer getAllUser() {
        String docId = conf.getString("docId");
        String key = conf.getString("key");
        String url = "https://sheets.googleapis.com/v4/spreadsheets/" + docId + "/values/%D0%A3%D1%87%D0%B0%D1%81%D1%82%D0%BD%D0%B8%D0%BA%D0%B8!A2%3AM50?key=" + key;

        return getAnswerFromGA(url);
    }

    static sAnswer getCardsInfo() {
        String docId = conf.getString("docId");
        String key = conf.getString("key");
        String url = "https://sheets.googleapis.com/v4/spreadsheets/" + docId + "/values/%D0%94%D0%B5%D1%80%D0%B6%D0%B0%D1%82%D0%B5%D0%BB%D0%B8!A2%3AB23?key=" + key;

        return getAnswerFromGA(url);
    }

    private static sAnswer getAnswerFromGA(String uri) {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(uri);

        HttpResponse response = null;
        try {
            response = client.execute(request);
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader rd = null;
        try {
            rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        StringBuilder result = new StringBuilder();
        String line = "";
        try {
            while ((line = rd.readLine()) != null) {
                result.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        {
            ObjectMapper om = new ObjectMapper();
            gaAnswer ga = null;
            try {
                ga = om.readValue(result.toString(), gaAnswer.class);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new sAnswer(ga.getValues());
        }
    }
}
