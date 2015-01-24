package com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.view.ActionMode;
import android.view.View;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.ui.projectDetails.ProjectDetailActivity;
import com.personal.taskmanager2.utilities.Utilities;

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

    @Override
    public void onItemClick(View v) {
        int position =
                mSectionedAdapter.sectionedPositionToPosition(mRecyclerView.getChildPosition(v));

        if (mProjectAdapter.isItemSelected(position)) {
            unSelectItem(position);
        }
        else {
            ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
            Intent intent =
                    new Intent(ArchiveFragment.this.getActivity(),
                               ProjectDetailActivity.class);
            intent.putExtra("project",
                            mProjectAdapter.getItem(position));
            startActivity(intent, options.toBundle());
        }
    }

    @Override
    public void onItemLongClick(View v) {
        int position = mRecyclerView.getChildPosition(v);
        toggleSelection(position);
    }

    @Override
    public void onAvatarClick(View avatar, int position) {
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        position = mSectionedAdapter.sectionedPositionToPosition(position);
        if (mProjectAdapter.isItemSelected(position)) {
            unSelectItem(position);
        }
        else {
            mProjectAdapter.selectItem(position);
            if (mActionMode == null) {
                mActionMode =
                        Utilities.getToolbar(getActivity()).startActionMode(mActionModeCallback);
            }
            else {
                mActionMode.invalidate();
            }
        }
    }

    private void unSelectItem(int position) {
        mProjectAdapter.unselectedItem(position);
        if (mProjectAdapter.getNumSelected() == 0) {
            mActionMode.finish();
        }
        else {
            mActionMode.invalidate();
        }
    }
}