package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.utilities.DateParser;
import com.personal.taskmanager2.utilities.ListViewAnimationHelper;

import java.util.List;

public class DetailProjectAdapter extends BaseProjectAdapter {

    private static final String TAG = "DetailProjectAdapter";

    private DateParser dateParser = new DateParser(DateParser.DETAIL);

    public DetailProjectAdapter(Activity context,
                                List<Project> projectList,
                                ListViewAnimationHelper<Project> animationHelper) {

        super(context, projectList, animationHelper);
    }

    private static class ViewHolder {

        View        colorSlice;
        TextView    lineOneView;
        TextView    lineTwoView;
        TextView    lineThreeView;
        TextView    lineFourView;
        ImageButton overFlowButton;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View colorSlice;
        TextView lineOneView;
        TextView lineTwoView;
        TextView lineThreeView;
        TextView lineFourView;
        ImageButton overFlowButton;

        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_project_detail, parent, false);

            colorSlice = convertView.findViewById(R.id.project_detail_color);
            lineOneView = (TextView) convertView.findViewById(R.id.project_detail_name);
            lineTwoView = (TextView) convertView.findViewById(R.id.project_detail_due_date);
            lineThreeView = (TextView) convertView.findViewById(R.id.project_detail_description);
            lineFourView = (TextView) convertView.findViewById(R.id.project_detail_status);
            overFlowButton = (ImageButton) convertView.findViewById(R.id.project_detail_overflow);

            ViewHolder viewHolder = new ViewHolder();
            viewHolder.colorSlice = colorSlice;
            viewHolder.lineOneView = lineOneView;
            viewHolder.lineTwoView = lineTwoView;
            viewHolder.lineThreeView = lineThreeView;
            viewHolder.lineFourView = lineFourView;
            viewHolder.overFlowButton = overFlowButton;
            convertView.setTag(viewHolder);
        }
        else {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            colorSlice = viewHolder.colorSlice;
            lineOneView = viewHolder.lineOneView;
            lineTwoView = viewHolder.lineTwoView;
            lineThreeView = viewHolder.lineThreeView;
            lineFourView = viewHolder.lineFourView;
            overFlowButton = viewHolder.overFlowButton;
        }

        Project project = getItem(position);
        initButton(overFlowButton, project);

        //Get number of completed tasks
        String completedTasks = "Completed ";
        int numTasks = project.getNumCompletedTasks();
        int totalTasks = project.getNumTotalTask();
        completedTasks += numTasks + " of " + totalTasks + " tasks";

        // Set color and title
        colorSlice.setBackgroundResource(project.getColorRsrc());

        lineOneView.setText(project.getName());


        //set due date
        dateParser.parse(project.getDueDate(), lineTwoView);

        lineThreeView.setText(project.getDescription());
        lineFourView.setText(completedTasks);

        setTitleAppearance(lineOneView,
                           project,
                           R.style.completed_detail,
                           R.style.not_completed_detail);

        /*if (!project.getStatus()) {
            lineOneView.setTextColor(getContext().getResources()
                                                 .getColor(project.getColorRsrc()));
        }*/

        return convertView;
    }
}