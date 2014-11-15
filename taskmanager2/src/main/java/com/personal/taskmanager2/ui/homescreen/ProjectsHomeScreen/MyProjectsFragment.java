package com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.ui.widget.FloatingActionButton;

public class MyProjectsFragment extends BaseProjectFragment {

    private static final String TAG = "MyProjectsFragment";

    private FloatingActionButton mCreateButton;

    public static BaseProjectFragment newInstance() {

        Bundle args = new Bundle();
        args.putInt("resourceId", R.layout.fragment_my_projects);
        args.putBoolean("archive", false);
        args.putBoolean("trash", false);

        BaseProjectFragment frag = new MyProjectsFragment();
        frag.setArguments(args);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        mCreateButton = (FloatingActionButton) rootView.findViewById(R.id.create_project);
        mCreateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickFloatingButton();
            }
        });

        return rootView;
    }

    private void onClickFloatingButton() {
        Log.d(TAG, "Floating Button Clicked");
    }
}