package com.rikerapp.riker.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.rikerapp.riker.fragments.SplashEnterBmlFragment;
import com.rikerapp.riker.fragments.SplashEnterRepsFragment;
import com.rikerapp.riker.fragments.SplashExportingFragment;
import com.rikerapp.riker.fragments.SplashLineChartsFragment;
import com.rikerapp.riker.fragments.SplashPieChartsFragment;
import com.rikerapp.riker.fragments.SplashTitleFragment;

import java.util.EnumSet;

public final class SplashPagerAdapter extends FragmentPagerAdapter {

    public static final int NUM_PAGES = SplashPage.values().length;

    public SplashPagerAdapter(final FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public final Fragment getItem(final int position) {
        final SplashPage page = SplashPage.lookup(position);
        switch (page) {
            case TITLE_PAGE:
                return new SplashTitleFragment();
            case ENTER_REPS_PAGE:
                return new SplashEnterRepsFragment();
            case ENTER_BML_PAGE:
                return new SplashEnterBmlFragment();
            case LINE_CHARTS:
                return new SplashLineChartsFragment();
            case PIE_CHARTS:
                return new SplashPieChartsFragment();
            case EXPORTING:
                return new SplashExportingFragment();
        }
        return null;
    }

    @Override
    public final int getCount() {
        return NUM_PAGES;
    }

    public enum SplashPage {

        TITLE_PAGE,
        ENTER_REPS_PAGE,
        ENTER_BML_PAGE,
        LINE_CHARTS,
        PIE_CHARTS,
        EXPORTING
        ;

        public static final SplashPage lookup(final int position) {
            for (final SplashPage page : EnumSet.allOf(SplashPage.class)) {
                if (page.ordinal() == position) {
                    return page;
                }
            }
            throw new IllegalArgumentException("Can't find page for position: [" + position + "]");
        }
    }
}
