package com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.personal.taskmanager2.Constants;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.adapters.ProjectAdapter.BaseProjectAdapter;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.ui.homescreen.AddProjects.CreateProjectFragment;
import com.personal.taskmanager2.ui.homescreen.AddProjects.JoinProjectFragment;
import com.personal.taskmanager2.ui.projectDetails.ProjectDetailActivity;
import com.personal.taskmanager2.ui.widget.FloatingActionButton;
import com.personal.taskmanager2.utilities.Utilities;

public class MyProjectsFragment extends BaseProjectFragment implements View.OnClickListener {

    private static final String TAG = "MyProjectsFragment";

    private static final int ANIM_TIME = 250;

    private FloatingActionButton mCreateButton;
    private FloatingActionButton mAddProjectButton;
    private FloatingActionButton mJoinProjectButton;

    private Handler mHandler = new Handler();

    private boolean mOpenCreate = false;

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

        /*mAddProjectButton.setScaleX(0);
        mAddProjectButton.setScaleY(0);
        mJoinProjectButton.setScaleX(0);
        mJoinProjectButton.setScaleY(0);*/

        return rootView;
    }

    @Override
    ActionMode.Callback initCab() {
        return new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.setTitleOptionalHint(true);
                actionMode.setTitle(Integer.toString(mProjectAdapter.getNumSelected()));
                menu.clear();
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.project_context_menu_single, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                actionMode.setTitleOptionalHint(true);
                actionMode.setTitle(Integer.toString(mProjectAdapter.getNumSelected()));
                menu.clear();
                MenuInflater inflater = actionMode.getMenuInflater();
                if (mProjectAdapter.getNumSelected() > 1) {
                    inflater.inflate(R.menu.project_context_menu_multiple, menu);
                }
                else {
                    inflater.inflate(R.menu.project_context_menu_single, menu);
                }
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_edit_project:
                        /*mProjectAdapter.forEachSelectedItemModifyInPlace(new BaseProjectAdapter.ApplyAction() {
                            @Override
                            public void modifyProject(Project project) {
                                getActionMode().finish();
                                project.safeEdit(MyProjectsFragment.this,
                                                 MyProjectsFragment.this.getActivity(),
                                                 MyProjectsFragment.this.getActivity(),
                                                 mRecyclerView.get);
                            }
                        });*/
                        mProjectAdapter.forEachSelectItemModifyInPlace2(new BaseProjectAdapter.ApplyAction2() {
                            @Override
                            public void modifyProject2(Project project, int pos) {
                                getActionMode().finish();
                                project.safeEdit(MyProjectsFragment.this,
                                                 MyProjectsFragment.this.getActivity(),
                                                 MyProjectsFragment.this.getActivity(),
                                                 mRecyclerView.findViewHolderForPosition(mSectionedAdapter.positionToSectionedPosition(pos)).itemView);
                            }
                        });
                        return true;
                    case R.id.action_share_project:
                        mProjectAdapter.forEachSelectedItemModifyInPlace(new BaseProjectAdapter.ApplyAction() {
                            @Override
                            public void modifyProject(Project project) {
                                project.share(MyProjectsFragment.this.getActivity());
                            }
                        });
                        return true;
                    case R.id.action_mark_complete:
                        mProjectAdapter.forEachSelectedItemModifyInPlace(new BaseProjectAdapter.ApplyAction() {
                            @Override
                            public void modifyProject(Project project) {
                                project.safeChangeStatus(true,
                                                         MyProjectsFragment.this.getActivity());
                            }
                        });
                        getActionMode().finish();
                        return true;
                    case R.id.action_mark_not_complete:
                        mProjectAdapter.forEachSelectedItemModifyInPlace(new BaseProjectAdapter.ApplyAction() {
                            @Override
                            public void modifyProject(Project project) {
                                project.safeChangeStatus(false,
                                                         MyProjectsFragment.this.getActivity());
                            }
                        });
                        getActionMode().finish();
                        return true;
                    case R.id.action_archive:
                        mProjectAdapter.forEachSelectedItemRemove(new BaseProjectAdapter.ApplyAction() {
                            @Override
                            public void modifyProject(Project project) {
                                project.safeArchive(true, getActivity());
                            }
                        });
                        getActionMode().finish();
                        return true;
                    case R.id.action_trash:
                        mProjectAdapter.forEachSelectedItemRemove(new BaseProjectAdapter.ApplyAction() {
                            @Override
                            public void modifyProject(Project project) {
                                project.safeTrash(true, getActivity());
                            }
                        });
                        getActionMode().finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
                int firstVisPos = llm.findFirstVisibleItemPosition();
                int lastVisPos = llm.findLastVisibleItemPosition();
                mProjectAdapter.clearSelection(firstVisPos, lastVisPos);
                mActionMode = null;
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.EDIT_PROJECT_REQUEST && resultCode == Activity.RESULT_OK) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mLoadProjects.setVisibility(View.VISIBLE);
            queryProjects();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_project_fab:
                createProjectsButtonClick();
                break;
            case R.id.add_project_fab:
                createProjectsButtonClick();
                CreateProjectFragment frag = CreateProjectFragment.newInstance();
                frag.setTargetFragment(this, 1);
                frag.show(getFragmentManager(), null);
                break;
            case R.id.join_project_fab:
                createProjectsButtonClick();
                JoinProjectFragment.newInstance().show(getFragmentManager(), null);
                break;
        }
    }


    private void createProjectsButtonClick() {


        if (!mOpenCreate) {
            rotateClockwise();
        }
        else {
            rotateCounterClockwise();
        }
    }

    private void rotateClockwise() {
        mCreateButton.animate()
                     .setDuration(ANIM_TIME * 2)
                     .rotation(45)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationStart(Animator animation) {
                             mAddProjectButton.setVisibility(View.VISIBLE);
                             mJoinProjectButton.setVisibility(View.VISIBLE);
                             //fadeInViews();
                         }

                         @Override
                         public void onAnimationEnd(Animator animation) {
                             mOpenCreate = true;
                             fadeInViews();
                         }
                     });
    }

    private void rotateCounterClockwise() {
        mCreateButton.animate()
                     .setDuration(ANIM_TIME)
                     .rotation(0)
                     .setListener(new AnimatorListenerAdapter() {
                         @Override
                         public void onAnimationStart(Animator animation) {
                             fadeOutViews();
                         }

                         @Override
                         public void onAnimationEnd(Animator animation) {
                             mOpenCreate = false;
                         }
                     });
    }

    private void fadeInViews() {
        mAddProjectButton.setVisibility(View.VISIBLE);
        mAddProjectButton.animate()
                         .setDuration(ANIM_TIME / 2)
                         .scaleX(1)
                         .scaleY(1)
                         .setListener(new AnimatorListenerAdapter() {
                             @Override
                             public void onAnimationStart(Animator animation) {
                             }
                         });

        mHandler.postDelayed(mJoinProjectFadeIn, ANIM_TIME / 4);
    }

    Runnable mJoinProjectFadeIn = new Runnable() {
        @Override
        public void run() {
            mJoinProjectButton.setVisibility(View.VISIBLE);
            mJoinProjectButton.animate()
                              .setDuration(ANIM_TIME / 2)
                              .scaleX(1)
                              .scaleY(1)
                              .setListener(new AnimatorListenerAdapter() {
                                  @Override
                                  public void onAnimationStart(Animator animation) {
                                  }
                              });
        }
    };


    private void fadeOutViews() {
        mJoinProjectButton.animate()
                          .setDuration(ANIM_TIME / 2)
                          .scaleX(0)
                          .scaleY(0)
                          .setListener(new AnimatorListenerAdapter() {
                              @Override
                              public void onAnimationEnd(Animator animation) {
                                  mJoinProjectButton.setVisibility(View.GONE);
                              }
                          });

        mHandler.postDelayed(mAddProjectFadeOut, ANIM_TIME / 4);
    }

    Runnable mAddProjectFadeOut = new Runnable() {
        @Override
        public void run() {
            mAddProjectButton.animate()
                             .setDuration(ANIM_TIME / 2)
                             .scaleX(0)
                             .scaleY(0)
                             .setListener(new AnimatorListenerAdapter() {
                                 @Override
                                 public void onAnimationEnd(Animator animation) {
                                     mAddProjectButton.setVisibility(View.GONE);
                                 }
                             });
        }
    };

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
                    new Intent(MyProjectsFragment.this.getActivity(),
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