package com.personal.taskmanager2.homescreen.ProjectsHomeScreen;

import android.os.Bundle;
import android.view.Menu;

import com.personal.taskmanager2.R;

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

        for (int i = 0; i < 1; i++) {
            menu.getItem(i).setVisible(false);
        }
    }
}