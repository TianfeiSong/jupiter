package com.laioffer.job.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.job.db.MySQLConnection;
import com.laioffer.job.entity.Item;
import org.apache.http.HttpEntity;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class GitHubClient {

    private static final String URL_TEMPLATE = "http://jobs.github.com/positions.json?description=%s&lat=%s&long=%s";

    private static final String DEFAULT_KEYWORD = "developer";

    public List<Item> search(double lat, double lon, String keyword) {
        if (keyword == null) {
            keyword = DEFAULT_KEYWORD;
        }

        //"hello world"=>"hello%20world"
        try {
            keyword = URLEncoder.encode(keyword, "UTF-8");
        } catch(UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = String.format(URL_TEMPLATE, keyword, lat, lon);

        CloseableHttpClient httpclient = HttpClients.createDefault();

        //Create a custom response handler
        ResponseHandler<List<Item>> responseHandler = response -> {
            if (response.getStatusLine().getStatusCode() != 200) {
                return Collections.emptyList();
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return Collections.emptyList();
            }
            ObjectMapper mapper = new ObjectMapper();

            List<Item> items = Arrays.asList(mapper.readValue(entity.getContent(), Item[].class));
            // remove special characters and <...> from descriptions
            for (Item item : items) {
                item.setDescription(item.getDescription().replaceAll("[^\\x20-\\x7e]", "").replaceAll("<.+?>", ""));
            }

            extractKeywords(items);
            return items;
        };

        try {
            return httpclient.execute(new HttpGet(url), responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();
    }

    private void extractKeywords(List<Item> items) {
        MonkeyLearnClient monkeyLearnClient = new MonkeyLearnClient();
        MySQLConnection connection = new MySQLConnection();
        List<String> descriptions = new ArrayList<>();
        for (Item item : items) {
            Set<String> keywords = connection.getKeywords(item.getId());
            if (keywords.size() > 0) {
                item.setKeywords(keywords);
            } else {
                descriptions.add(item.getDescription());
            }
        }

//        // Java 8 Stream API
//        List<String> descriptions = items.stream()
//            .map(Item::getDescription)
//            .collect(Collectors.toList());

        Map<String, Set<String>> keywordMap = monkeyLearnClient.extract(descriptions);
        for (Item item : items) {
            if (item.getKeywords() == null) {
                item.setKeywords(keywordMap.get(item.getDescription()));
            }
        }
    }
}
