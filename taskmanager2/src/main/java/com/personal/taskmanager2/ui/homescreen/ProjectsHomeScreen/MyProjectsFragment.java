package com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.ui.homescreen.AddProjects.CreateProjectFragment;
import com.personal.taskmanager2.ui.homescreen.AddProjects.JoinProjectFragment;
import com.personal.taskmanager2.ui.widget.FloatingActionButton;

public class MyProjectsFragment extends BaseProjectFragment implements View.OnClickListener {

    private static final String TAG = "MyProjectsFragment";

    private FloatingActionButton mCreateButton;
    private FloatingActionButton mAddProjectButton;
    private FloatingActionButton mJoinProjectButton;

    private boolean mOpenAdd = false;

    public static BaseProjectFragment newInstance() {

        Bundle args = new Bundle();
        args.putInt("resourceId", R.layout.fragment_my_projects);
        args.putBoolean("archive", false);
        args.putBoolean("trash", false);
        args.putString("title", "My Projects");

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
        mAddProjectButton = (FloatingActionButton) rootView.findViewById(R.id.add_new_project);
        mJoinProjectButton = (FloatingActionButton) rootView.findViewById(R.id.join_project);
        mCreateButton.setOnClickListener(this);
        mAddProjectButton.setOnClickListener(this);
        mJoinProjectButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_project:
                createProjectsButtonClick();
                break;
            case R.id.add_new_project:
                createProjectsButtonClick();
                CreateProjectFragment.newInstance().show(getFragmentManager(), null);
                break;
            case R.id.join_project:
                createProjectsButtonClick();
                JoinProjectFragment.newInstance().show(getFragmentManager(), null);
                break;
        }
    }


    private void createProjectsButtonClick() {
        Animation openAnim, closeAnim;
        openAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_left_to_right);
        closeAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.rotate_right_to_left);

        if (!mOpenAdd) {

            openAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mAddProjectButton.setVisibility(View.VISIBLE);
                    mJoinProjectButton.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mCreateButton.setIcon(getResources().getDrawable(R.drawable.ic_navigation_close));
                    mOpenAdd = true;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            mCreateButton.startAnimation(openAnim);
        }
        else {
            closeAnim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    mAddProjectButton.setVisibility(View.INVISIBLE);
                    mJoinProjectButton.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    mCreateButton.setIcon(getResources().getDrawable(R.drawable.ic_action_content_add));
                    mOpenAdd = false;
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            mCreateButton.startAnimation(closeAnim);
        }
    }
}