package com.example.newsgateway;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.newsgateway.MainActivity;
import com.example.newsgateway.ArticleDownloader;
import com.example.newsgateway.NewsService;

public class NewsServiceReceiver extends BroadcastReceiver {

    private static final String TAG = "NewsServiceReceiver";

    private final NewsService newsService;

    public NewsServiceReceiver(NewsService newsService) {
        this.newsService = newsService;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive: ");
        String action = intent.getAction();
        if (action == null)
            return;
        if (MainActivity.ACTION_MSG_TO_SERVICE.equals(action)) {
            String sourceId = null;
            String languageId=null;
            if (intent.hasExtra(MainActivity.SOURCE_ID) && intent.hasExtra(MainActivity.LANG_ID)) {
                sourceId = intent.getStringExtra(MainActivity.SOURCE_ID);
                sourceId = sourceId.replaceAll(" ", "-");
                languageId= intent.getStringExtra(MainActivity.LANG_ID);
                if(languageId==null)
                    languageId="";

            }
            Log.d(TAG, "onReceive: Starting ArticleDownloader thread");
            new Thread(new ArticleDownloader(newsService, sourceId,languageId)).start();
        }
    }
}