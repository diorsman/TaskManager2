package com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.utilities.Utilities;

public class TrashFragment extends BaseProjectFragment {

    private static final String TAG = "TrashFragment";

    public static BaseProjectFragment newInstance() {

        Bundle args = new Bundle();
        args.putInt("resourceId", R.layout.fragment_trash);
        args.putBoolean("archive", false);
        args.putBoolean("trash", true);

        BaseProjectFragment frag = new TrashFragment();
        frag.setArguments(args);

        return frag;
    }
    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        Toolbar toolbar = Utilities.getToolbar(getActivity());
        toolbar.getMenu().clear();
    }
}