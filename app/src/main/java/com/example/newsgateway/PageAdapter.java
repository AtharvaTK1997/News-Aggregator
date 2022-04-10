package com.example.newsgateway;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.newsgateway.NewsFragment;

import java.util.List;

public class PageAdapter extends FragmentPagerAdapter {
    private long baseId = 0;
    private List<NewsFragment> newsFragments;

    public PageAdapter(FragmentManager fm, List<NewsFragment> newsFragments) {
        super(fm);
        this.newsFragments = newsFragments;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return newsFragments.get(position);
    }

    @Override
    public int getCount() {
        return newsFragments.size();
    }

    @Override
    public long getItemId(int position) {
        // give an ID different from position when position has been changed
        return baseId + position;
    }

    /**
     * Notify that the position of a fragment has been changed.
     * Create a new ID for each position to force recreation of the fragment
     *
     * @param n number of items which have been changed
     */
    public void notifyChangeInPosition(int n) {
        // shift the ID returned by getItemId outside the range of all previous fragments
        baseId += getCount() + n;
    }
}
