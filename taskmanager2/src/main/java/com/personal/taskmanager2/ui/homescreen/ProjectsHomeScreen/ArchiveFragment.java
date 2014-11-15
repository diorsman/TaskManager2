package com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.personal.taskmanager2.R;

public class ArchiveFragment extends BaseProjectFragment {

    private static final String TAG = "ArchiveFragment";

    public static BaseProjectFragment newInstance() {

        Bundle args = new Bundle();
        args.putInt("resourceId", R.layout.fragment_archived_projects);
        args.putBoolean("archive", true);
        args.putBoolean("trash", false);

        BaseProjectFragment frag = new ArchiveFragment();
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.getMenu().clear();
    }
}