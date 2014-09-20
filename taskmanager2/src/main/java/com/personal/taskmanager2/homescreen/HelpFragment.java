package com.personal.taskmanager2.homescreen;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import com.personal.taskmanager2.R;

public class HelpFragment extends Fragment {

    private static final String TAG = "HelpFragment";

    public static HelpFragment newInstance() {

        return new HelpFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Set title of action bar
        //(getActivity()).getActionBar().setTitle(getString(R.string.help));
        View rootView =
                inflater.inflate(R.layout.fragment_help, container, false);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.home_screen, menu);
        ActionBar actionBar = (getActivity()).getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(getString(R.string.help_title));
    }
}