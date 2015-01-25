package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.utilities.Utilities;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Omid Ghomeshi on 12/18/14.
 */
public class SimpleProjectAdapter extends BaseProjectAdapter<SimpleProjectAdapter.ViewHolder> {

    private static final String TAG = "SimpleProjectAdapter";

    public SimpleProjectAdapter(Context context,
                                List<Project> projectList,
                                OnItemClickListener listener) {
        super(context,
              R.style.completed_default,
              R.style.not_completed_default,
              projectList,
              listener,
              new SimpleDateFormat("MM/dd/yyyy 'at' hh:mm a"));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //View view = initView(parent, R.layout.list_item_project);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.list_item_project, parent, false);
        final ViewHolder viewHolder = new ViewHolder(view);
        initClick(viewHolder);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        Project project = getItem(position);

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
