package com.personal.taskmanager2.homescreen.ProjectsHomeScreen;


import android.os.Bundle;

import com.personal.taskmanager2.R;

public class MyProjectsFragment extends BaseProjectFragment {

    private static final String TAG = "MyProjectsFragment";

    public static BaseProjectFragment newInstance() {

        Bundle args = new Bundle();
        args.putInt("resourceId", R.layout.fragment_my_projects);
        args.putBoolean("archive", false);
        args.putBoolean("trash", false);

        BaseProjectFragment frag = new MyProjectsFragment();
        frag.setArguments(args);

        return frag;
    }
}