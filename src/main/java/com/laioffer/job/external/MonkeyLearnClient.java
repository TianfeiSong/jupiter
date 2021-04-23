package com.laioffer.job.external;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.laioffer.job.entity.ExtractRequestBody;
import com.laioffer.job.entity.ExtractResponseBody;
import com.laioffer.job.entity.Extraction;
import org.apache.http.HttpEntity;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

public class MonkeyLearnClient {

// Test
//    public static void main(String[] args) {
//        List<String> articles = Arrays.asList(
////                "Elon Musk has shared a photo of the spacesuit designed by SpaceX. This is the second image shared of the new design and the first to feature the spacesuit’s full-body look.",
////                "Former Auburn University football coach Tommy Tuberville defeated ex-US Attorney General Jeff Sessions in Tuesday nights runoff for the Republican nomination for the U.S. Senate. ",
////                "The NEOWISE comet has been delighting skygazers around the world this month – with photographers turning their lenses upward and capturing it above landmarks across the Northern Hemisphere."
////                "ResoluteAI is a fast-growth data aggregation and intelligent search startup, with a mission to enable scientifically driven organizations to make their next big discovery. Our tools allow our clients to find relevant results in an efficient manner, across patents, clinical trials, publications, drug and medical device datasets, and within their own internal corporate documents (enterprise search). We accomplish this through our proprietary machine learning algorithms. We are proud to count amongst our clients two of the top four (and five of the top twelve) global pharmaceutical firms by market cap.We are looking for full-stack developers to help build out our capabilities as we continue to grow.Duties and Responsibilities:Integrate public domain datasets into the search platform using our ETL framework.Design and implement new features that integrate and highlight the various public datasets.Work with various external enterprise search APIs to ingest and enrich clients documents for search.Assist machine learning engineers with deploying their models into productionCommunicate directly with clients on requirements, progress, and setting expectations.Assist with bug fixes and writing of regression and unit tests.Education and Previous Experience:Bachelors degree in Computer Science, or equivalent experience in software development5+ years in related experience and trainingKnowledge and Skills:Strongly skilled in Python. Experience with SQL and an ORM (e.g. sqlalchemy).Can hold your own on the frontend UI in ReactJSComfortable working with Docker and in a Linux environmentExperience with ElasticSearch  very strong plusExperience with Elastic Map Reduce or Spark is a plusKnowledge of networking, especially AWS services and infrastructure is a plusMust have a self-starter mentality and willingness to dig deep into the code.Ability to own your work and adopt an outcomes-based approach, in a fast paced environment of quickly evolving requirements.What we offer:Competitive salaryFull benefits (Medical, Dental, Vision, FSA, Commuter benefit)401(k) and matchingEquity in the companyUnlimited Paid Time OffFlexible Work Environment",
////                "Members Exchange (MEMX) is a growing FinTech firm founded by a group of leaders in the worlds financial markets and has recently received approval as a new stock exchange. Our people are the foundation of our business, and we are off to a great start assembling a talented team who are ready to collaborate, innovate, and challenge the status quo. We are committed to maintaining the culture we have set in motion  we take great pride in our selection process  and that starts with finding the right people.Description:MEMX is searching for a Full Stack Developer to work on an assortment of internal and external facing web applications. This person will play a key role in serving our customers and our business by developing applications that administer and support our core trading system. This person will be the go-to resource for HTML5 development.Responsibilities:Meet with Product Managers and Stakeholders to obtain a deep understanding of our markets and the applications we build to serve them.Work with a team of experienced, high-performance developers and mentors to design best-in-class systems.Develop and maintain innovative products that deliver exceptional features and service to our customers and our internal teams.Requirements:5 or more years of web development experience.Bachelors degree or higher in Computer Science or a comparable discipline.Proficient in HTML5, ES6, CSS, Java;Experienced in REST APIs, NodeJS, React, Angular, or an equivalent web framework, git;Familiar with Gradle, CI/CD, GraphQL, web development tools such as Babel and Webpack.-Strong professional and interpersonal skills including exceptional customer service, creative problem solving, and effective written and verbal communications.Broad systems thinking - understands the connections and relationships across functions and entities for both internal and external constituencies.Experience with financial markets, distributed systems and/or high-performance web applications is a plus.Ideal candidate will have demonstrated ability to succeed as a self-starter in a dynamic environmentBenefits:At MEMX you will have the ability to work with a talented team of professionals who bring diversity of thought and background. You will have the opportunity to shape the future of our company and the impact MEMX will have on our clients and the broader markets. We offer competitive employee benefits and perks and will continue to make this a priority to attract the best."
//            "senior security engineer threat detection data center equipment gemini year experience elastic map reduce enterprise search apis global pharmaceutical firm"
//        );
//
//        MonkeyLearnClient client = new MonkeyLearnClient();
//
//        Map<String, Set<String>> keywordMap = client.extract(articles);
//
//        System.out.println(keywordMap);
//    }

    private static final String EXTRACT_URL = "https://api.monkeylearn.com/v3/extractors/ex_YCya9nrn/extract/";
    private static final String AUTH_TOKEN = "05bc643b035a51848b3193f30219439d3d26a3b5";
//    private static final String AUTH_TOKEN = "6d33902a0a8a3e4a75304c002ffc08588b6435fe";

    public Map<String, Set<String>> extract(List<String> articles) {
        ObjectMapper mapper = new ObjectMapper();
        CloseableHttpClient httpclient = HttpClients.createDefault();

        HttpPost request = new HttpPost(EXTRACT_URL);
        request.setHeader("Content-type", "application/json");
        request.setHeader("Authorization", "Token " + AUTH_TOKEN);
        ExtractRequestBody body = new ExtractRequestBody(articles, 3);

        Map<String, Set<String>> keywordMap = new HashMap<>();
        for (String s : articles) {
            keywordMap.put(s, new HashSet<>());
        }

        String jsonBody;
        try {
            jsonBody = mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            return keywordMap;
        }

        try {
            request.setEntity(new StringEntity(jsonBody));
        } catch (UnsupportedEncodingException e) {
            return keywordMap;
        }

        ResponseHandler<Map<String, Set<String>>> responseHandler = response -> {
            if (response.getStatusLine().getStatusCode() != 200) {
                if (response.getStatusLine().getStatusCode() == 429) {
                    System.out.println(response.getStatusLine().getReasonPhrase());
                }
                return keywordMap;
            }
            HttpEntity entity = response.getEntity();
            if (entity == null) {
                return keywordMap;
            }
            ExtractResponseBody[] results = mapper.readValue(entity.getContent(), ExtractResponseBody[].class);
            for (ExtractResponseBody result : results) {
                Set<String> keywords = new HashSet<>();
                for (Extraction extraction : result.extractions) {
                    keywords.add(extraction.parsedValue);
                }
                keywordMap.replace(result.text, keywords);
            }
            return keywordMap;
        };

        try {
            return httpclient.execute(request, responseHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keywordMap;
    }
}
