package com.rikerapp.riker.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rikerapp.riker.R;
import com.rikerapp.riker.Utils;

public final class SplashTitleFragment extends Fragment {

    @Override
    public final View onCreateView(final LayoutInflater inflater, final ViewGroup container, final Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_splash_title, container, false);
        Utils.applyRikerFont(getActivity().getAssets(), view.findViewById(R.id.rikerTitle));
        return view;
    }
}
