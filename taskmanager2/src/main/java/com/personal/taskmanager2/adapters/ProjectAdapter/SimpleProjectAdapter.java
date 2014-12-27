package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.utilities.Utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Omid Ghomeshi on 12/18/14.
 */
public class SimpleProjectAdapter extends BaseProjectAdapter<SimpleProjectAdapter.ViewHolder> {

    private static final String TAG = "SimpleProjectAdapter";

    private final static DateFormat SIMPLE_DATE_FORMAT =
            new SimpleDateFormat("MM/dd/yyyy 'at' hh:mm a");

    public SimpleProjectAdapter(Context context,
                                List<Project> projectList,
                                OnItemClickListener listener) {
        super(context, projectList, listener);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = initView(parent, R.layout.list_item_project);
        final ViewHolder viewHolder = new ViewHolder(view);
        viewHolder.projectAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onAvatarClick(v, viewHolder.getPosition());
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Project project = getItem(position);

        holder.itemView.setActivated(isItemSelected(position));
        initAvatar(holder.projectAvatar, project, position);

        // set name
        holder.projectName.setText(project.getName());
        setTitleAppearance(holder.projectName,
                           project,
                           R.style.completed_default,
                           R.style.not_completed_default);

        //set due date
        holder.projectDueDate.setText(SIMPLE_DATE_FORMAT.format(project.getDueDate()));

        //set progress
        double progress;
        if (project.getStatus()) {
            progress = 100;
        }
        else {
            //get task info
            int numTasks = project.getNumCompletedTasks();
            int totalTasks = project.getNumTotalTask();
            progress = (double) numTasks / totalTasks;
            progress *= 100;
        }

        holder.projectStatus.setVisibility(ProgressBar.VISIBLE);
        holder.projectStatus.setProgress((int) progress);
        holder.projectStatus.getProgressDrawable()
                            .setColorFilter(getContext().getResources()
                                                        .getColor(Utilities.getColorRsrcFromColor(
                                                                project.getColor())),
                                            PorterDuff.Mode.SRC_IN);
    }

    public static class ViewHolder extends BaseProjectAdapter.ViewHolder {

        public ProgressBar projectStatus;

        public ViewHolder(final View itemView) {
            super(itemView);

            projectStatus = (ProgressBar) itemView.findViewById(R.id.project_list_status);
        }
    }

}
