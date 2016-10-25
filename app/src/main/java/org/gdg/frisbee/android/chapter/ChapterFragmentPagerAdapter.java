package org.gdg.frisbee.android.chapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;

import org.gdg.frisbee.android.R;
import org.gdg.frisbee.android.api.model.Chapter;
import org.gdg.frisbee.android.eventseries.GdgEventListFragment;

class ChapterFragmentPagerAdapter extends FragmentPagerAdapter {
    private static final int[] PAGES = {
        R.string.news, R.string.info, R.string.events
    };
    private static final int[] ORGANIZER_PAGES = {
        R.string.news, R.string.info, R.string.events, R.string.for_leads
    };

    private final Context context;
    private final Chapter selectedChapter;
    private final boolean isOrganizer;
    private final int[] pageTitles;

    ChapterFragmentPagerAdapter(FragmentActivity activity,
                                Chapter selectedChapter,
                                boolean isOrganizer) {
        super(activity.getSupportFragmentManager());
        this.context = activity;
        this.selectedChapter = selectedChapter;
        this.isOrganizer = isOrganizer;
        pageTitles = isOrganizer ? ORGANIZER_PAGES : PAGES;
    }

    @Override
    public int getCount() {
        return pageTitles.length;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return NewsFragment.newInstance(selectedChapter.getGplusId());
            case 1:
                return InfoFragment.newInstance(selectedChapter.getGplusId());
            case 2:
                return GdgEventListFragment.newInstance(selectedChapter.getGplusId());
            case 3:
                return new LeadFragment();
            default:
                throw new IllegalStateException("Unknown page in MainActivity" + position);
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (0 <= position && position < pageTitles.length) {
            return context.getString(pageTitles[position]);
        } else {
            return "";
        }
    }

    @Override
    public long getItemId(int position) {
        if (position == 3) {
            return position;
        }
        return selectedChapter.hashCode() * 10 + position;
    }

    boolean isOrganizerFragmentShown() {
        return isOrganizer;
    }
}
