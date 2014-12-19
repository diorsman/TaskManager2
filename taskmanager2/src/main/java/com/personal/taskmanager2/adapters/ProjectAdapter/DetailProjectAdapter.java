package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
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
        View view = initView(parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Project project = getItem(position);

        //Get number of completed tasks
        String completedTasks = "Completed ";
        int numTasks = project.getNumCompletedTasks();
        int totalTasks = project.getNumTotalTask();
        completedTasks += numTasks + " of " + totalTasks + " tasks";

        initAvatar(holder.colorSlice, project, position);

        holder.lineOneView.setText(project.getName());

        //set due date
        //dateParser.parse(project.getDueDate(), lineTwoView);
        holder.lineTwoView.setText(DETAIL_DATE_FORMAT.format(project.getDueDate()));

        holder.lineThreeView.setText(project.getDescription());
        holder.lineFourView.setText(completedTasks);

        setTitleAppearance(holder.lineOneView,
                           project,
                           R.style.completed_detail,
                           R.style.not_completed_detail);

        holder.itemView.setActivated(isItemSelected(position));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View        colorSlice;
        TextView    lineOneView;
        TextView    lineTwoView;
        TextView    lineThreeView;
        TextView    lineFourView;

        public ViewHolder(View itemView) {
            super(itemView);

            colorSlice = itemView.findViewById(R.id.project_list_color_slice);
            lineOneView = (TextView) itemView.findViewById(R.id.project_detail_name);
            lineTwoView = (TextView) itemView.findViewById(R.id.project_detail_due_date);
            lineThreeView = (TextView) itemView.findViewById(R.id.project_detail_description);
            lineFourView = (TextView) itemView.findViewById(R.id.project_detail_status);
        }
    }

}
