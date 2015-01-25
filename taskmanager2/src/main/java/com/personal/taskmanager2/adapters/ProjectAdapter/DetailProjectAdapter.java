package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.model.parse.Project;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Omid Ghomeshi on 12/18/14.
 */
public class DetailProjectAdapter extends BaseProjectAdapter<DetailProjectAdapter.ViewHolder> {

    public DetailProjectAdapter(Context context,
                                List<Project> projectList,
                                OnItemClickListener listener) {
        super(context,
              R.style.completed_detail,
              R.style.not_completed_detail,
              projectList,
              listener,
              new SimpleDateFormat("'Due' 'on' MMM dd, yyyy 'at' hh:mm a"));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        //View view = initView(parent, R.layout.list_item_project_detail);
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_project_detail,
                                                                 parent,
                                                                 false);
        final ViewHolder viewHolder = new ViewHolder(view);
        initClick(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Project project = getItem(position);

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
