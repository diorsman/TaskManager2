package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.model.parse.Project;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Omid Ghomeshi on 12/18/14.
 */
public class DetailProjectAdapter extends BaseProjectAdapter<DetailProjectAdapter.ViewHolder> {

    private final static DateFormat DETAIL_DATE_FORMAT =
            new SimpleDateFormat("'Due' 'on' MMM dd, yyyy 'at' hh:mm a");

    public DetailProjectAdapter(Context context, List<Project> projectList, OnItemClickListener listener) {
        super(context, projectList, listener);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View view = initView(parent, R.layout.list_item_project_detail);
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

        holder.projectName.setText(project.getName());
        setTitleAppearance(holder.projectName,
                           project,
                           R.style.completed_detail,
                           R.style.not_completed_detail);

        //set due date
        holder.projectDueDate.setText(DETAIL_DATE_FORMAT.format(project.getDueDate()));

        holder.projectDescription.setText(project.getDescription());

        //Get number of completed tasks
        String completedTasks = "Completed ";
        int numTasks = project.getNumCompletedTasks();
        int totalTasks = project.getNumTotalTask();
        completedTasks += numTasks + " of " + totalTasks + " tasks";
        holder.projectTasks.setText(completedTasks);
    }

    public static class ViewHolder extends BaseProjectAdapter.ViewHolder {

        public TextView projectDescription;
        public TextView projectTasks;

        public ViewHolder(View itemView) {
            super(itemView);

            projectDescription = (TextView) itemView.findViewById(R.id.project_detail_description);
            projectTasks = (TextView) itemView.findViewById(R.id.project_detail_status);
        }
    }

}
