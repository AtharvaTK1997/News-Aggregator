package com.example.newsgateway;

import android.net.Uri;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.example.newsgateway.MainActivity;
import com.example.newsgateway.NewsSource;

import javax.net.ssl.HttpsURLConnection;


public class SourceDownloader implements Runnable {
    private static final String TAG = "SourceDownloader";

    private static final String API_KEY = "1b9cae502da542a6a5ed5f26816838b3"; //my key = "305e754558a94466a097eac0cbe35939";
//    private static final String BASE_URL = "https://newsapi.org/v2/sources?language=en&country=us";
    private static final String BASE_URL = "https://newsapi.org/v2/sources?";
    private static final String CATEGORY_TOKEN = "&category=";
    private static final String API_KEY_TOKEN = "&apiKey=";
    private static final String LANG_TOKEN ="&language=";
    private static final String COUNTRY_TOKEN="&country=";

    private String newsCategory;
    private String newsLanguage;
    private String newsCountry;
    private List<String> newsCategories;
    private List<String> newsLanguages;
    private List<String> newsCountries;
    private List<NewsSource> newsSources;

    private MainActivity mainActivity;

    public SourceDownloader(MainActivity mainActivity, String category, String language, String country) {
        this.mainActivity = mainActivity;
        newsCategory = category;
        newsLanguage = language;
        newsCountry =  country;

        newsSources = new ArrayList<>();
        newsCategories = new ArrayList<>();
        newsLanguages = new ArrayList<>();
        newsCountries = new ArrayList<>();
    }

    @Override
    public void run() {
        Log.d(TAG, "run: Downloading Sources");
        String DOWNLOAD_LINK = initURL();

        Uri uri = Uri.parse(DOWNLOAD_LINK);
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
        String DOWNLOAD_LINK = BASE_URL + CATEGORY_TOKEN;

        if (!TextUtils.isEmpty(newsCategory) && !"all".equalsIgnoreCase(newsCategory))
            DOWNLOAD_LINK += newsCategory;

        DOWNLOAD_LINK+=LANG_TOKEN;

        if (!TextUtils.isEmpty(newsLanguage) && !"all".equalsIgnoreCase(newsLanguage))
            DOWNLOAD_LINK += newsLanguage;

        DOWNLOAD_LINK+=COUNTRY_TOKEN;

        if (!TextUtils.isEmpty(newsCountry) && !"all".equalsIgnoreCase(newsCountry))
            DOWNLOAD_LINK += newsCountry;

        DOWNLOAD_LINK += API_KEY_TOKEN + API_KEY;
        return DOWNLOAD_LINK;
    }

    public void handleResults(final String jsonString) {
        Log.d(TAG, "handleResults: Populating Sources and Categories in MainActivity");
        parseJSON(jsonString);
        mainActivity.runOnUiThread(() -> mainActivity.populateSourceAndCategory(newsCategories, newsSources, newsLanguages, newsCountries));
    }

    private void parseJSON(String input) {
        Log.d(TAG, "parseJSON: Parsing Source JSON data");
        try {
            JSONObject jsonObject = new JSONObject(input);
            JSONArray jsonArray = jsonObject.getJSONArray("sources");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject sourceJsonObject = jsonArray.getJSONObject(i);
                NewsSource source = new NewsSource();
                source.setId(sourceJsonObject.getString("id"));
                source.setName(sourceJsonObject.getString("name"));
                source.setCategory(sourceJsonObject.getString("category"));
                source.setUrl(sourceJsonObject.getString("url"));
                source.setLanguage(sourceJsonObject.getString("language"));
                source.setCountry(sourceJsonObject.getString("country"));
                source.setColoredName(new SpannableString(source.getName()));

                newsSources.add(source);
                if (!newsCategories.contains(source.getCategory()))
                    newsCategories.add(source.getCategory());
                if(!newsLanguages.contains(source.getLanguage()))
                    newsLanguages.add(source.getLanguage());
                if(!newsCountries.contains(source.getCountry()))
                    newsCountries.add(source.getCountry());
            }
        } catch (Exception e) {
            Log.e(TAG, "parseJSON: Failed to parse JSON", e);
        }
    }
}

