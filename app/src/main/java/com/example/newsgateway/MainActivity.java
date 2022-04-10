package com.example.newsgateway;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.ListView;
import android.widget.Toast;

import com.example.newsgateway.PageAdapter;
import com.example.newsgateway.SourceAdapter;
import com.example.newsgateway.NewsFragment;
import com.example.newsgateway.Drawer;
import com.example.newsgateway.LayoutManager;
import com.example.newsgateway.NewsArticle;
import com.example.newsgateway.NewsSource;
import com.example.newsgateway.MainActivityReceiver;
import com.example.newsgateway.NewsService;
import com.example.newsgateway.SourceDownloader;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    public static final String ACTION_MSG_TO_SERVICE = "ACTION_MSG_TO_SERVICE";
    public static final String ACTION_NEWS_STORY = "ACTION_NEWS_STORY";
    public static final String ARTICLE_LIST = "ARTICLE_LIST";
    public static final String SOURCE_ID = "SOURCE_ID";
    public static final String LANG_ID="LANG_ID";
    private static final String TAG = "MainActivity";
    private String newsSource;
    private String newsLanguage;
    Menu menu;

    private int currentSourcePointer;
    private boolean appState;
    private boolean serviceStatus = false;

    private List<String> sourceList;
    private List<String> languagesList;
    private List<NewsSource> sources;
    private List<String> categories;
    private List<String> languages;
    private List<String> countries;
    private List<NewsArticle> articles;
    private List<NewsFragment> newsFragments;
    private Map<String, NewsSource> sourceStore;

    private String submenu_category;
    private String submenu_language;
    private String submenu_country;

    private MainActivityReceiver receiver;
    private SourceAdapter adapter;

    private HashMap<String,String> hash_code_lname;
    private HashMap<String,String> hash_lname_code;

    private HashMap<String,String> hash_cname_code;
    private HashMap<String,String> hash_code_cname;

    private Menu categoryMenu;

    private List<Drawer> drawerList;
    private DrawerLayout drawerLayout;
    private ListView drawerListView;
    private ActionBarDrawerToggle drawerToggle;
    private PageAdapter pageAdapter;
    private ViewPager viewPager;

    private int[] topicColors;
    private Map<String, Integer> topicIntMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sourceList = new ArrayList<>();
        sources = new ArrayList<>();
        categories = new ArrayList<>();
        countries = new ArrayList<>();
        languages = new ArrayList<>();
        articles = new ArrayList<>();

        drawerList = new ArrayList<>();
        newsFragments = new ArrayList<>();

        hash_code_lname= new HashMap<>();
        hash_lname_code= new HashMap<>();
        hash_cname_code= new HashMap<>();
        hash_code_cname= new HashMap<>();

        sourceStore = new HashMap<>();

        topicIntMap = new HashMap<>();
        topicColors = getResources().getIntArray(R.array.topicColors);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        receiver = new MainActivityReceiver(this);
        drawerLayout = findViewById(R.id.drawerLayout);
        drawerListView = findViewById(R.id.drawerList);
        adapter = new SourceAdapter(this, drawerList);
        drawerListView.setAdapter(adapter);
        pageAdapter = new PageAdapter(getSupportFragmentManager(), newsFragments);
        viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(pageAdapter);

        // Start service if not started
        if (savedInstanceState == null && !serviceStatus) {
            Log.d(TAG, "onCreate: Starting News Service");
            Intent intent = new Intent(MainActivity.this, NewsService.class);
            startService(intent);
            serviceStatus = true;
        }

        IntentFilter filter = new IntentFilter(MainActivity.ACTION_NEWS_STORY);
        registerReceiver(receiver, filter);

        // if no data is there to restore
        if (sourceStore.isEmpty() && savedInstanceState == null)
            new Thread(new SourceDownloader(this, "", "", "")).start();

        // add click listener to drawer list view
        drawerListView.setOnItemClickListener((parent, view, position, id) -> {
            viewPager.setBackgroundResource(0);
            currentSourcePointer = position;
            selectListItem(position);
        });

        // update the drawer toggle
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open_navigation_drawer, R.string.close_navigation_drawer);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");
//        getMenuInflater().inflate(R.menu.categories, menu);
//        categoryMenu = menu;
//        if (appState) {
//            SubMenu topicsubMenu = categoryMenu.addSubMenu("Topics");
//            SubMenu languagesubMenu = categoryMenu.addSubMenu("Languages");
//            SubMenu countrysubMenu = categoryMenu.addSubMenu("Countries");
//            for (String category : categories)
//                topicsubMenu.add(category);
//            for(String language : languages)
//                languagesubMenu.add(language);
//            for(String country: countries)
//                countrysubMenu.add(country);
//        }
        this.menu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: ");
        if (drawerToggle.onOptionsItemSelected(item)) {  // <== Important!
            Log.d(TAG, "onOptionsItemSelected: drawerToggle " + item);
            return true;
        }

        if(item.hasSubMenu())
            return true;

        int group_id = item.getGroupId();
        int menuitem_id = item.getItemId();

        //itemid  = 0 -> Topics -> categories
        if(item.getGroupId() == 0){
            submenu_category = item.getTitle().toString();
            submenu_language="";
            submenu_country="";
        }

        //itemid = 1 -> Languages
        if(item.getGroupId()==1){
           submenu_language = hash_lname_code.get(item.getTitle().toString());
           submenu_country="";
        }
        //itemid = 2  -> Countries
        if(item.getGroupId()==2){
           submenu_country = hash_cname_code.get(item.getTitle().toString());
           submenu_language="";
        }
        Log.d(TAG, "onOptionsItemSelected: Starting Source Downloader thread");
        new Thread(new SourceDownloader(this, submenu_category, submenu_language.toLowerCase(),submenu_country.toLowerCase())).start();
        drawerLayout.openDrawer(drawerListView);

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onPostCreate: ");
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged: ");
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");
        LayoutManager layoutRestore = new LayoutManager();

        Log.d(TAG, "categories: " + categories);
        layoutRestore.setCategories(categories);

        Log.d(TAG, "sources: " + sources);
        layoutRestore.setSources(sources);

        layoutRestore.setArticle(viewPager.getCurrentItem());

        Log.d(TAG, "currentSourcePointer : " + currentSourcePointer);
        layoutRestore.setSource(currentSourcePointer);

        Log.d(TAG, "articles : " + articles);
        layoutRestore.setArticles(articles);

        Log.d(TAG, "languages: "+languages);
        layoutRestore.setLanguages(languages);

        Log.d(TAG,"countries :"+countries);
        layoutRestore.setCountries(countries);

        outState.putSerializable("state", layoutRestore);

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState: ");
        super.onRestoreInstanceState(savedInstanceState);

        setTitle(R.string.app_name);
        LayoutManager layoutManager = (LayoutManager) savedInstanceState.getSerializable("state");
        appState = true;

        articles = layoutManager.getArticles();
        Log.d(TAG, "articles: " + articles);

        categories = layoutManager.getCategories();
        Log.d(TAG, "categories: " + categories);

        sources = layoutManager.getSources();
        Log.d(TAG, "sources: " + sources);

        languages= layoutManager.getLanguages();
        Log.d(TAG, "languages:"+languages);

        countries= layoutManager.getLanguages();
        Log.d(TAG, "countries:"+countries);

        for (int i = 0; i < sources.size(); i++) {
            sourceList.add(sources.get(i).getName());
            sourceStore.put(sources.get(i).getName(), sources.get(i));
        }

        drawerListView.clearChoices();
        adapter.notifyDataSetChanged();
        drawerListView.setOnItemClickListener((parent, view, position, id) -> {
                    viewPager.setBackgroundResource(0);
                    currentSourcePointer = position;
                    selectListItem(position);
                }
        );
    }

    public void news_source_count()
    {
        int source_count = sourceList.size();
        if (source_count != 0)
            setTitle(getString(R.string.app_name)+ " (" + source_count + ")");
        else
            setTitle(getString(R.string.app_name));
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: Stopping News Service");
        unregisterReceiver(receiver);
        Intent intent = new Intent(MainActivity.this, MainActivityReceiver.class);
        stopService(intent);
        super.onDestroy();
    }

    public void populateSourceAndCategory(List<String> newsCategories, List<NewsSource> newsSources, List<String> newsLanguages, List<String> newsCountries) {
        Log.d(TAG, "populateSourceAndCategory: ");
        Log.d(TAG, "newsSources size: " + newsSources.size() + ", newsCategories size: " + newsCategories.size());
        sourceStore.clear();
        sourceList.clear();
        sources.clear();
        drawerList.clear();
        sources.addAll(newsSources);

        // Sort and update category list in the options menu
        if (!menu.hasVisibleItems()) {
            categories.clear();
            menu.clear();
            SubMenu topicsubMenu = menu.addSubMenu("Topics");
            SubMenu languagesubMenu = menu.addSubMenu("Languages");
            SubMenu countrysubMenu = menu.addSubMenu("Countries");
            categories = newsCategories;
            topicsubMenu.add("all");
            languagesubMenu.add("all");
            countrysubMenu.add("all");
            Collections.sort(newsCategories);
            int i = 0;
            int a=0;
            for (String category : newsCategories) {
                SpannableString categoryString = new SpannableString(category);
                categoryString.setSpan(new ForegroundColorSpan(topicColors[i]), 0, categoryString.length(), 0);
                topicIntMap.put(category, topicColors[i++]);
                topicsubMenu.add(0,a++,a++,categoryString);
            }
            int j=0;
            fn_load_lang();
            for(String language : newsLanguages){
                // language_code -> open languages.json -> find languge_code-> retrieve language_name
                languagesubMenu.add(1,j++,j++,hash_code_lname.get(language.toUpperCase()));

            }
            int k =0;
            fn_load_country();
            for(String country : newsCountries){
                countrysubMenu.add(2,k++,k++,hash_code_cname.get(country.toUpperCase()));
            }
        }
        for (NewsSource source : newsSources) {
            if (topicIntMap.containsKey(source.getCategory())) {
                int color = topicIntMap.get(source.getCategory());
                SpannableString coloredString = new SpannableString(source.getName());
                coloredString.setSpan(new ForegroundColorSpan(color), 0, source.getName().length(), 0);
                source.setColoredName(coloredString);
                sourceList.add(source.getName());
                sourceStore.put(source.getName(), source);
            }
        }
        news_source_count();
        // Update the drawer
        for (NewsSource source : newsSources) {
            Drawer drawerContent = new Drawer();
            drawerContent.setItemName(source.getColoredName());
            drawerList.add(drawerContent);

        }
        adapter.notifyDataSetChanged();
        boolean check=drawerList.isEmpty();
        if (check==true)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            //builder.setIcon(R.drawable.icon1);

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });


            builder.setMessage("No available news for this selection: Topic:"+submenu_category+" Language:"+submenu_language+" Country:"+submenu_country);
            //builder.setTitle("Yes/No Dialog");

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void updateFragments(List<NewsArticle> articles) {
        Log.d(TAG, "updateFragments: ");
        setTitle(newsSource);

        for (int i = 0; i < pageAdapter.getCount(); i++)
            pageAdapter.notifyChangeInPosition(i);

        newsFragments.clear();

        for (int article = 0; article < articles.size(); article++) {
            newsFragments.add(NewsFragment.newInstance(articles.get(article), article, articles.size()));
        }
        pageAdapter.notifyDataSetChanged();
        viewPager.setCurrentItem(0);
        this.articles = articles;
    }

    private void selectListItem(int position) {
        Log.d(TAG, "selectListItem => selected pos: " + position + ", sourceList size: " + sourceList.size());
        newsSource = sourceList.get(position);
        newsLanguage = submenu_language;
        Intent intent = new Intent(MainActivity.ACTION_MSG_TO_SERVICE);
        intent.putExtra(SOURCE_ID, newsSource);
        intent.putExtra(LANG_ID,newsLanguage);
        sendBroadcast(intent);
        drawerLayout.closeDrawer(drawerListView);
    }
    public void  fn_load_lang(){
        String temp=null;
        try {
            InputStream rd = getResources().openRawResource(R.raw.language_codes);
            int size = rd.available();
            byte[] buffer = new byte[size];
            rd.read(buffer);
            rd.close();
            temp = new String(buffer,"UTF-8");

            JSONObject obj = new JSONObject(temp);
            JSONArray lang_array = obj.getJSONArray("languages");
            for (int i = 0; i < lang_array.length(); i++) {
                JSONObject jsonObject = lang_array.getJSONObject(i);
                String lang_code = jsonObject.getString("code");
                String lang_name = jsonObject.getString("name");
                hash_code_lname.put(lang_code,lang_name);
                hash_lname_code.put(lang_name,lang_code);
            }
        }
        catch (FileNotFoundException e){
            Toast.makeText(this, " No JSON file found", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void  fn_load_country(){
        String temp=null;
        try {
            InputStream rd = getResources().openRawResource(R.raw.country_codes);
            int size = rd.available();
            byte[] buffer = new byte[size];
            rd.read(buffer);
            rd.close();
            temp = new String(buffer,"UTF-8");

            JSONObject obj = new JSONObject(temp);
            JSONArray country_array = obj.getJSONArray("countries");
            for (int i = 0; i < country_array.length(); i++) {
                JSONObject jsonObject = country_array.getJSONObject(i);
                String country_code = jsonObject.getString("code");
                String country_name = jsonObject.getString("name");
                hash_code_cname.put(country_code,country_name);
                hash_cname_code.put(country_name,country_code);
            }
        }
        catch (FileNotFoundException e){
            Toast.makeText(this, " No JSON file found", Toast.LENGTH_SHORT).show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}