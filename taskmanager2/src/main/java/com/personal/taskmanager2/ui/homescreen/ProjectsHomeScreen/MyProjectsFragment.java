package com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.os.Handler;
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

    private Handler mHandler = new Handler();

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

        mCreateButton = (FloatingActionButton) rootView.findViewById(R.id.create_project_fab);
        mAddProjectButton = (FloatingActionButton) rootView.findViewById(R.id.add_project_fab);
        mJoinProjectButton = (FloatingActionButton) rootView.findViewById(R.id.join_project_fab);
        mCreateButton.setOnClickListener(this);
        mAddProjectButton.setOnClickListener(this);
        mJoinProjectButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_project_fab:
                createProjectsButtonClick();
                break;
            case R.id.add_project_fab:
                createProjectsButtonClick();
                CreateProjectFragment.newInstance().show(getFragmentManager(), null);
                break;
            case R.id.join_project_fab:
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
                     .rotation(45)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationStart(Animator animation) {
                             fadeInViews();
                         }

                         @Override
                         public void onAnimationEnd(Animator animation) {
                             mOpenAdd = true;
                         }
                     });
    }

    private void rotateCounterClockwise() {
        mCreateButton.animate()
                     .setDuration(250)
                     .rotation(0)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationStart(Animator animation) {
                             fadeOutViews();
                         }

                         @Override
                         public void onAnimationEnd(Animator animation) {
                             mOpenAdd = false;
                         }
                     });
    }

    private void fadeInViews() {
        mAddProjectButton.animate()
                         .setDuration(125)
                         .scaleX(1)
                         .scaleY(1)
                         .setListener(new AnimatorListenerAdapter() {
                             @Override
                             public void onAnimationStart(Animator animation) {
                                 mAddProjectButton.setVisibility(View.VISIBLE);
                             }
                         });

        mHandler.postDelayed(mJoinProjectFadeIn, 125 / 2);
    }

    Runnable mJoinProjectFadeIn = new Runnable() {
        @Override
        public void run() {
            mJoinProjectButton.animate()
                              .setDuration(125)
                              .scaleX(1)
                              .scaleY(1)
                              .setListener(new AnimatorListenerAdapter() {
                                  @Override
                                  public void onAnimationStart(Animator animation) {
                                      mJoinProjectButton.setVisibility(View.VISIBLE);
                                  }
                              });
        }
    };


    private void fadeOutViews() {
        mJoinProjectButton.animate()
                          .setDuration(125)
                          .scaleX(0)
                          .scaleY(0)
                          .setListener(new AnimatorListenerAdapter() {
                              @Override
                              public void onAnimationEnd(Animator animation) {
                                  mJoinProjectButton.setVisibility(View.INVISIBLE);
                              }
                          });

        mHandler.postDelayed(mAddProjectFadeOut, 125 / 2);
    }

    Runnable mAddProjectFadeOut = new Runnable() {
        @Override
        public void run() {
            mAddProjectButton.animate()
                             .setDuration(125)
                             .scaleX(0)
                             .scaleY(0)
                             .setListener(new AnimatorListenerAdapter() {
                                 @Override
                                 public void onAnimationEnd(Animator animation) {
                                     mAddProjectButton.setVisibility(View.INVISIBLE);
                                 }
                             });
        }
    };
}