package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.utilities.ListViewAnimationHelper;

import java.util.List;

public abstract class BaseProjectAdapter extends BaseAdapter implements View.OnClickListener {

    private static final String TAG = "BaseProjectAdapter";

    private final Activity                         mContext;
    private       List<Project>                    mProjectList;
    private       ListViewAnimationHelper<Project> mAnimHelper;

    public BaseProjectAdapter(Activity context,
                              List<Project> projectList,
                              ListViewAnimationHelper<Project> animationHelper) {

        mContext = context;
        mProjectList = projectList;
        mAnimHelper = animationHelper;
    }

    public void addItems(List<Project> items) {

        mProjectList.addAll(items);
    }

    public void remove(int pos) {

        mProjectList.remove(pos);
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

    private class ProjectMenuItemClick implements PopupMenu.OnMenuItemClickListener {

        private Project mProject;

        public ProjectMenuItemClick(Project project) {

            mProject = project;
        }

        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {

            switch (menuItem.getItemId()) {
                case R.id.action_edit_project:
                    mProject.safeEdit(mContext.getFragmentManager(), mContext);
                    return true;

                case R.id.action_share_project:
                    mProject.share(mContext);
                    return true;

                case R.id.action_mark_complete:
                    if (mProject.safeChangeStatus(true, mContext)) {
                        notifyDataSetChanged();
                    }
                    return true;

                case R.id.action_mark_not_complete:
                    if (mProject.safeChangeStatus(false, mContext)) {
                        notifyDataSetChanged();
                    }
                    return true;

                case R.id.action_archive:
                    if (mProject.safeArchive(true, mContext)) {
                        mAnimHelper.showRemoveAnimation(mProject);
                    }
                    return true;

                case R.id.remove_from_archive:
                    if (mProject.safeArchive(false, mContext)) {
                        mAnimHelper.showRemoveAnimation(mProject);
                    }
                    return true;

                case R.id.remove_from_trash:
                    if (mProject.safeTrash(false, mContext)) {
                        mAnimHelper.showRemoveAnimation(mProject);
                    }
                    return true;

                case R.id.delete:
                    if (mProject.safeTrash(true, mContext)) {
                        mAnimHelper.showRemoveAnimation(mProject);
                    }
                default:
                    return false;
            }
        }
    }

    protected void setTitleAppearance(TextView textView,
                                      Project project,
                                      int styleComplete,
                                      int styleNotComplete) {

        if (project.getStatus()) {
            textView.setTextAppearance(mContext, styleComplete);
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }
        else {
            textView.setTextAppearance(mContext, styleNotComplete);
            textView.setPaintFlags(textView.getPaintFlags() & ~(Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    protected void initButton(ImageButton button, Project project) {

        button.setOnClickListener(this);
        button.setTag(project);
    }

}