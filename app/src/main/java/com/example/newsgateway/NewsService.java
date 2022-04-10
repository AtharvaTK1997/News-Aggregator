package com.example.newsgateway;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.example.newsgateway.MainActivity;
import com.example.newsgateway.NewsArticle;
import com.example.newsgateway.NewsServiceReceiver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NewsService extends Service {
    private static final String TAG = "NewsService";

    private boolean isRunning = true;
    private NewsServiceReceiver receiver;
    private List<NewsArticle> articles = new ArrayList<>();

    public NewsService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        receiver = new NewsServiceReceiver(this);
        IntentFilter intentFilter = new IntentFilter(MainActivity.ACTION_MSG_TO_SERVICE);
        registerReceiver(receiver, intentFilter);

        new Thread(() -> {
            while (isRunning) {
                while (articles.isEmpty()) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                sendArticles();
            }
        }).start();
        return Service.START_STICKY;
    }

    private void sendArticles() {
        Log.d(TAG, "sendArticles: Broadcasting Article");
        Intent intent = new Intent();
        intent.setAction(MainActivity.ACTION_NEWS_STORY);
        intent.putExtra(MainActivity.ARTICLE_LIST, (Serializable) articles);
        sendBroadcast(intent);
        articles.clear();
    }

    public void populateArticles(List<NewsArticle> articles) {
        this.articles.clear();
        this.articles.addAll(articles);
    }

    @Override
    public void onDestroy() {
        isRunning = false;
        super.onDestroy();
    }
}