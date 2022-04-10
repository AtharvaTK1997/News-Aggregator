package com.example.newsgateway;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewsFragment extends Fragment {

    private static final String TAG = "NewsFragment";

    private static final String ARTICLE = "ARTICLE";
    private static final String INDEX = "INDEX";
    private static final String TOTAL = "TOTAL";
    private static final String DATE_FORMAT = "MMM dd, yyyy HH:mm";
    private static final String DATE_FORMAT_PARSE = "yyyy-MM-dd'T'HH:mm:ss";

    private static final SimpleDateFormat sdfFormat = new SimpleDateFormat(DATE_FORMAT);
    private static final SimpleDateFormat sdfParse = new SimpleDateFormat(DATE_FORMAT_PARSE);

    private TextView tvArticleHeadLine;
    private TextView tvArticleDate;
    private TextView tvArticleAuthor;
    private TextView tvArticleText;
    private ImageView ivArticleImage;
    private TextView tvArticleCount;

    private NewsArticle article;

    private View view;

    public NewsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param article Parameter 1.
     * @param index   Parameter 2.
     * @param total   Parameter 3.
     * @return A new instance of fragment NewsFragment.
     */
    public static NewsFragment newInstance(NewsArticle article, int index, int total) {
        Log.d(TAG, "newInstance: Creating News Fragment instance");
        NewsFragment fragment = new NewsFragment();
        Bundle args = new Bundle(1);
        args.putSerializable(ARTICLE, article);
        args.putInt(INDEX, index);
        args.putInt(TOTAL, total);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_news, container, false);

        tvArticleHeadLine = view.findViewById(R.id.head_article);
        tvArticleDate = view.findViewById(R.id.date_article);
        tvArticleAuthor = view.findViewById(R.id.author_article);
        tvArticleText = view.findViewById(R.id.text_article);
        ivArticleImage = view.findViewById(R.id.ivArticleImage);
        tvArticleCount = view.findViewById(R.id.count_article);

        article = (NewsArticle) getArguments().getSerializable(ARTICLE);

        // Show article title only if it is not null and not equal to "null"
        if (isNull(article.getTitle()))
            tvArticleHeadLine.setVisibility(View.GONE);
        else
            tvArticleHeadLine.setText(article.getTitle());

        // Show article author only if it is not null and not equal to "null"
        if (isNull(article.getAuthor()))
            tvArticleAuthor.setVisibility(View.GONE);
        else
            tvArticleAuthor.setText(article.getAuthor());

        // Show article text only if it is not null and not equal to "null"
        if (isNull(article.getDescription()))
            tvArticleText.setVisibility(View.GONE);
        else
            tvArticleText.setText(article.getDescription());

        // Show article published date only if it is not null and not equal to "null"
        if (!isNull(article.getPublishedAt())) {
            try {
                Date parsedDate = sdfParse.parse(article.getPublishedAt());
                if (parsedDate != null) {
                    tvArticleDate.setText(sdfFormat.format(parsedDate));
                }
            } catch (ParseException e) {
                Log.e(TAG, "onCreateView: Failed to parse date", e);
            }
        }

        // Show article image only if URL is not null and not equal to "null"
        if (isNull(article.getUrlToImage()))
            ivArticleImage.setImageResource(R.drawable.noimage);
        else
            showImage(article.getUrlToImage());

        tvArticleCount.setText(String.format("%d of %d", getArguments().getInt(INDEX) + 1, getArguments().getInt(TOTAL)));

        // Article headline clickable and navigate to article in browser
        tvArticleHeadLine.setOnClickListener(v -> startIntent());

        // Article photo clickable and navigate to article in browser
        ivArticleImage.setOnClickListener(v -> startIntent());

        // Article text clickable and navigate to article in browser
        tvArticleText.setOnClickListener(v -> startIntent());

        return view;
    }

    private void startIntent() {
        Log.d(TAG, "startIntent: Opening news in browser");
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_BROWSABLE);
        intent.setData(Uri.parse(article.getUrl()));
        startActivity(intent);
    }

    // Show the image in the image view
    private void showImage(final String imageURL) {
        Log.d(TAG, "showImage: Displaying News image. Image URL : " + imageURL);
        Picasso picasso = new Picasso.Builder(getActivity()).listener((picasso1, uri, exception) -> exception.printStackTrace()).build();
        // Enable logging to check for errors
        picasso.setLoggingEnabled(true);
        // Load the image, if any error then broken image is loaded.
        picasso.load(imageURL)
                .error(R.drawable.brokenimage)
                .placeholder(R.drawable.loading)
                .into(ivArticleImage);
    }

    private boolean isNull(String input) {
        return TextUtils.isEmpty(input) || input.trim().equals("null");
    }
}
