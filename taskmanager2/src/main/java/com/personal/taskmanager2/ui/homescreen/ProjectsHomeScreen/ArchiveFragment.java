package com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen;

import android.os.Bundle;
import android.view.ActionMode;

import com.personal.taskmanager2.R;

public class ArchiveFragment extends BaseProjectFragment {

    private static final String TAG = "ArchiveFragment";

    public static BaseProjectFragment newInstance() {

        Bundle args = new Bundle();
        args.putInt("resourceId", R.layout.fragment_archived_projects);
        args.putBoolean("archive", true);
        args.putBoolean("trash", false);
        args.putString("title", "Archive");

        BaseProjectFragment frag = new ArchiveFragment();
        frag.setArguments(args);

        return frag;
    }

    @Override
    ActionMode.Callback initCab() {
        return null;
    }
}