package com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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


        if (!mOpenAdd) {
            rotateClockwise();
        }
        else {
            rotateCounterClockwise();
        }
    }

    private void rotateClockwise() {
        mCreateButton.animate()
                     .setDuration(250)
                     .rotation(90)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationStart(Animator animation) {
                             fadeInViews();
                         }

                         @Override
                         public void onAnimationEnd(Animator animation) {
                             mCreateButton.setIcon(getResources().getDrawable(R.drawable.ic_navigation_close));
                             mOpenAdd = true;
                         }
                     });
    }

    private void rotateCounterClockwise() {
        mCreateButton.animate()
                     .setDuration(250)
                     .rotation(-90)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationStart(Animator animation) {
                             fadeOutViews();
                         }

                         @Override
                         public void onAnimationEnd(Animator animation) {
                             mCreateButton.setIcon(getResources().getDrawable(R.drawable.ic_navigation_add));
                             mOpenAdd = false;
                         }
                     });
    }

    private void fadeInViews() {
        mAddProjectButton.animate()
                         .setDuration(250)
                         .alpha(1f)
                         .setListener(new AnimatorListenerAdapter() {
                             @Override
                             public void onAnimationEnd(Animator animation) {
                                 mAddProjectButton.setVisibility(View.VISIBLE);
                             }
                         });

        mJoinProjectButton.animate()
                          .setDuration(250)
                          .alpha(1f)
                          .setListener(new AnimatorListenerAdapter() {
                              @Override
                              public void onAnimationEnd(Animator animation) {
                                  mJoinProjectButton.setVisibility(View.VISIBLE);
                              }
                          });
    }

    private void fadeOutViews() {
        mAddProjectButton.animate()
                         .setDuration(250)
                         .alpha(0f)
                         .setListener(new AnimatorListenerAdapter() {
                             @Override
                             public void onAnimationEnd(Animator animation) {
                                 mAddProjectButton.setVisibility(View.GONE);
                             }
                         });

        mJoinProjectButton.animate()
                          .setDuration(250)
                          .alpha(0f)
                          .setListener(new AnimatorListenerAdapter() {
                              @Override
                              public void onAnimationEnd(Animator animation) {
                                  mJoinProjectButton.setVisibility(View.GONE);
                              }
                          });
    }
}