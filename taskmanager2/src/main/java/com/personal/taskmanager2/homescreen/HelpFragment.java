package com.personal.taskmanager2.homescreen;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
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
        View rootView = inflater.inflate(R.layout.fragment_help, container, false);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.findViewById(R.id.actionbar_spinner).setVisibility(View.GONE);
        toolbar.getMenu().clear();
        ((ActionBarActivity) getActivity()).getSupportActionBar()
                                           .setDisplayShowTitleEnabled(true);
        toolbar.setTitle(getString(R.string.help_title));
    }
}