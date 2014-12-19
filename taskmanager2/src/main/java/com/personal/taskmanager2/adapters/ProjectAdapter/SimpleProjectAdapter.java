package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    public SimpleProjectAdapter(Context context, List<Project> projectList, OnItemClickListener listener) {
        super(context, projectList, listener);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        View view = initView(parent, R.layout.list_item_project);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Project project = getItem(position);

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

        // set name
        holder.lineOneView.setText(project.getName());

        //set due date
        //dateParser.parse(project.getDueDate(), holder.lineTwoView);
        holder.lineTwoView.setText(SIMPLE_DATE_FORMAT.format(project.getDueDate()));

        //set progress
        holder.status.setVisibility(ProgressBar.VISIBLE);
        holder.status.setProgress((int) progress);
        holder.status.getProgressDrawable()
              .setColorFilter(getContext().getResources()
                                          .getColor(Utilities.getColorRsrcFromColor(project.getColor())),
                              PorterDuff.Mode.SRC_IN);

        setTitleAppearance(holder.lineOneView,
                           project,
                           R.style.completed_default,
                           R.style.not_completed_default);

        holder.itemView.setActivated(isItemSelected(position));
        initAvatar(holder.colorSlice, project, position);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public View        colorSlice;
        public TextView    lineOneView;
        public TextView    lineTwoView;
        public ProgressBar status;

        public ViewHolder(final View itemView) {
            super(itemView);

            colorSlice = itemView.findViewById(R.id.project_list_color_slice);
            lineOneView = (TextView) itemView.findViewById(R.id.project_list_name);
            lineTwoView = (TextView) itemView.findViewById(R.id.project_list_due_date);
            status = (ProgressBar) itemView.findViewById(R.id.project_list_status);
        }
    }

}
