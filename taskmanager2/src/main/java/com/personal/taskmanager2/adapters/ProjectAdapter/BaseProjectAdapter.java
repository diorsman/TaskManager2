package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Paint;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.parseObjects.Project;
import com.personal.taskmanager2.utilities.Utilities;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class BaseProjectAdapter extends BaseAdapter
        implements View.OnClickListener {

    private static final String TAG = "BaseProjectAdapter";

    private final Context         mContext;
    private       List<Project>   mProjectList;
    private       FragmentManager mFragmentManager;
    private       ParseUser       mCurrentUser;
    private       ListView        mListView;

    private Animation removeAnimation;


    public BaseProjectAdapter(Context context,
                              List<Project> projectList,
                              FragmentManager fm,
                              ListView listView) {

        mContext = context;
        mProjectList = projectList;
        mFragmentManager = fm;
        mCurrentUser = ParseUser.getCurrentUser();
        mListView = listView;

        removeAnimation = AnimationUtils.loadAnimation(mContext,
                                                       android.R.anim.slide_out_right);
        removeAnimation.setDuration(350);
        removeAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                notifyDataSetChanged();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    public void sort(Comparator<Project> comparator) {

        Collections.sort(mProjectList, comparator);
        notifyDataSetChanged();
    }

    public void addItems(List<Project> items) {

        mProjectList.addAll(items);
    }

    public void remove(Project project) {

        mProjectList.remove(project);
    }

    public int getPosition(Project project) {

        return mProjectList.indexOf(project);
    }

    public Context getContext() {

        return mContext;
    }

    @Override
    public int getCount() {

        return mProjectList.size();
    }

    @Override
    public Project getItem(int position) {

        return mProjectList.get(position);
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    @Override
    public void onClick(View v) {

        Project project = (Project) v.getTag();
        switch (v.getId()) {
            case R.id.project_list_overflow:
            case R.id.project_detail_overflow:
                startPopUpMenu(project, v);
                break;
        }
    }

    private void startPopUpMenu(Project project, View view) {

        PopupMenu overflowMenu = new PopupMenu(mContext, view);

        // inflate appropriate menu depending on status
        if (project.getArchive()) {
            overflowMenu.inflate(R.menu.archive_overflow);
        }
        else if (project.getTrash()) {
            overflowMenu.inflate(R.menu.trash_overflow);
        }
        else {
            if (project.getStatus()) {
                overflowMenu.inflate(R.menu.project_overflow_not_complete);
            }
            else {
                overflowMenu.inflate(R.menu.project_overflow_complete);
            }
        }

        overflowMenu.show();
        overflowMenu.setOnMenuItemClickListener(new ProjectMenuItemClick(project));
    }

    private class ProjectMenuItemClick
            implements PopupMenu.OnMenuItemClickListener {

        private Project mProject;

        public ProjectMenuItemClick(Project project) {

            mProject = project;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            switch (menuItem.getItemId()) {
                case R.id.action_edit_project:
                    Utilities.safeEditProject(mProject,
                                              mFragmentManager,
                                              mContext);
                    return true;

                case R.id.action_share_project:
                    shareProject(mProject);
                    return true;

                case R.id.action_mark_complete:
                    changeStatus(true, mProject);
                    return true;

                case R.id.action_mark_not_complete:
                    changeStatus(false, mProject);
                    return true;

                case R.id.action_archive:
                    archive(true, mProject);
                    return true;

                case R.id.remove_from_archive:
                    archive(false, mProject);
                    return true;

                case R.id.remove_from_trash:
                    removeFromTrash(mProject);
                    return true;

                case R.id.delete:
                    moveToTrash(mProject);

                default:
                    return false;
            }
        }
    }

    private void shareProject(Project project) {

        Utilities.shareProject(project, mContext);
    }

    private void changeStatus(boolean status,
                              Project project) {

        if (ParseUser.getCurrentUser()
                     .getObjectId()
                     .equals(project.getAdmin().getObjectId())) {
            project.setStatus(status);
            project.saveInBackground();

            notifyDataSetChanged();
        }
        else {
            Toast.makeText(mContext,
                           "Only the administrator can make changes to the project.",
                           Toast.LENGTH_LONG).show();
        }
    }

    private void archive(boolean archive,
                         Project project) {
        if (ParseUser.getCurrentUser()
                     .getObjectId()
                     .equals(project.getAdmin().getObjectId())) {
            project.setArchive(archive);
            project.saveInBackground();

            removeProjectAnim(project);
        }
        else {
            Toast.makeText(mContext,
                           "Only the administrator can make changes to the project.",
                           Toast.LENGTH_LONG).show();
        }
    }

    private void removeFromTrash(Project project) {

        if (ParseUser.getCurrentUser()
                     .getObjectId()
                     .equals(project.getAdmin().getObjectId())) {
            project.setTrash(false);
            project.saveInBackground();

            removeProjectAnim(project);
        }
        else {
            Toast.makeText(mContext,
                           "Only the administrator can make changes to the project.",
                           Toast.LENGTH_LONG).show();
        }
    }

    private void moveToTrash(Project project) {
        if (ParseUser.getCurrentUser()
                     .getObjectId()
                     .equals(project.getAdmin().getObjectId())) {
            project.setTrash(true);
            project.setArchive(false);
            project.saveInBackground();

            removeProjectAnim(project);
        }
        else {
            Toast.makeText(mContext,
                           "Only the administrator can make changes to the project.",
                           Toast.LENGTH_LONG).show();
        }
    }

    private void removeProjectAnim(Project project) {

        int pos = getPosition(project);
        remove(project);
        int visiblePos = mListView.getFirstVisiblePosition();
        mListView.getChildAt(pos - visiblePos).startAnimation(removeAnimation);
    }

    protected void setTitleAppearance(TextView textView,
                                      Project project,
                                      int styleComplete,
                                      int styleNotComplete) {

        if (project.getStatus()) {
            textView.setTextAppearance(mContext, styleComplete);
            textView.setPaintFlags(
                    textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            textView.setTextAppearance(mContext, styleNotComplete);
            textView.setPaintFlags(
                    textView.getPaintFlags() & ~(Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    protected void initButton(ImageButton button, Project project) {

        button.setOnClickListener(this);
        button.setTag(project);
    }

}