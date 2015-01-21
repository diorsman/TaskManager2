package com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen;

import android.os.Bundle;
import android.view.ActionMode;

import com.personal.taskmanager2.R;

public class TrashFragment extends BaseProjectFragment {

    private static final String TAG = "TrashFragment";

    public static BaseProjectFragment newInstance() {

        Bundle args = new Bundle();
        args.putInt("resourceId", R.layout.fragment_trash);
        args.putBoolean("archive", false);
        args.putBoolean("trash", true);
        args.putString("title", "Trash");

        BaseProjectFragment frag = new TrashFragment();
        frag.setArguments(args);

        return frag;
    }

    @Override
    ActionMode.Callback initCab() {
        return null;
    }
}