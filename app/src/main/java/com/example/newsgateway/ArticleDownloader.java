package com.example.newsgateway;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import com.example.newsgateway.NewsArticle;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class ArticleDownloader implements Runnable {

    private static final String TAG = "ArticleDownloader";
    private static final String API_KEY = "1b9cae502da542a6a5ed5f26816838b3"; //my key ="305e754558a94466a097eac0cbe35939";
    private static final String BASE_URL = "https://newsapi.org/v2/top-headlines?pageSize=10";
    private static final String SOURCE_TOKEN = "&sources=";
    private static final String API_KEY_TOKEN = "&apiKey=";
    private static final String LANG_TOKEN ="&language=";


    private String sourceId;
    private String languageId;
    private List<NewsArticle> newsArticles;
    private NewsService newsService;

    public ArticleDownloader(NewsService newsService, String sourceId, String languageId) {
        this.newsService = newsService;
        this.sourceId = sourceId;
        this.languageId=languageId;

        newsArticles = new ArrayList<>();
    }

    @Override
    public void run() {
        Log.d(TAG, "run: Downloading Article");
        Uri uri = Uri.parse(initURL());

        String line;
        StringBuilder sb = new StringBuilder();

        try {
            URL url = new URL(uri.toString());

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.addRequestProperty("User-Agent", "");
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.setRequestProperty("Accept", "application/json");
            connection.connect();

            InputStream is = connection.getInputStream();
            BufferedReader reader = new BufferedReader((new InputStreamReader(is)));

            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        } catch (Exception e) {
            Log.e(TAG, "run: Exception: ", e);
        }
        handleResults(sb.toString());
    }

    private String initURL() {
        return BASE_URL + SOURCE_TOKEN + sourceId + LANG_TOKEN + languageId + API_KEY_TOKEN + API_KEY;
    }

    public void handleResults(final String jsonString) {
        Log.d(TAG, "handleResults: Updating Article data to News Service");
        parseJSON(jsonString);
        newsService.populateArticles(newsArticles);
    }

    private void parseJSON(String input) {
        Log.d(TAG, "parseJSON: Parsing Article data");
        try {
            JSONObject jsonObject = new JSONObject(input);
            JSONArray jsonArray = jsonObject.getJSONArray("articles");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject articleJsonObject = jsonArray.getJSONObject(i);
                NewsArticle article = new NewsArticle();
                article.setAuthor(articleJsonObject.getString("author"));
                article.setTitle(articleJsonObject.getString("title"));
                article.setDescription(articleJsonObject.getString("description"));
                article.setUrl(articleJsonObject.getString("url"));
                article.setUrlToImage(articleJsonObject.getString("urlToImage"));
                article.setPublishedAt(articleJsonObject.getString("publishedAt"));

                newsArticles.add(article);
            }
        } catch (Exception e) {
            Log.e(TAG, "parseJSON: Failed to parse JSON", e);
        }
    }
}
