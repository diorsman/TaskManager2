package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.ui.CharCircleIcon;
import com.personal.taskmanager2.utilities.DateParser;
import com.personal.taskmanager2.utilities.IconKey;
import com.personal.taskmanager2.utilities.ListViewAnimationHelper;
import com.personal.taskmanager2.utilities.Utilities;

import java.util.HashMap;
import java.util.List;

public class SimpleProjectAdapter extends BaseProjectAdapter {

    private static final String TAG = "SimpleProjectAdapter";

    private static HashMap<IconKey, CharCircleIcon> sIconMap = new HashMap<>();

    Typeface typeface = Typeface.create("sans-serif-light", Typeface.NORMAL);

    private DateParser dateParser = new DateParser(DateParser.DEFAULT);

    public SimpleProjectAdapter(Activity context,
                                List<Project> projectList,
                                ListViewAnimationHelper<Project> animationHelper) {

        super(context, projectList, animationHelper);
    }

    private static class ViewHolder {

        public View        colorSlice;
        public TextView    lineOneView;
        public TextView    lineTwoView;
        public ProgressBar status;
        public ImageButton overFlowButton;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View colorSlice;
        TextView lineOneView;
        TextView lineTwoView;
        ProgressBar status;
        ImageButton overFlowButton;

        if (convertView == null) {
            // inflate row
            LayoutInflater inflater =
                    (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_project, parent, false);

            colorSlice = convertView.findViewById(R.id.project_list_color_slice);
            lineOneView = (TextView) convertView.findViewById(R.id.project_list_name);
            lineTwoView = (TextView) convertView.findViewById(R.id.project_list_due_date);
            status = (ProgressBar) convertView.findViewById(R.id.project_list_status);
            overFlowButton = (ImageButton) convertView.findViewById(R.id.project_list_overflow);

            //set up view holder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.colorSlice = colorSlice;
            viewHolder.lineOneView = lineOneView;
            viewHolder.lineTwoView = lineTwoView;
            viewHolder.status = status;
            viewHolder.overFlowButton = overFlowButton;
            convertView.setTag(viewHolder);
        }
        else {
            ViewHolder viewHolder = (ViewHolder) convertView.getTag();
            colorSlice = viewHolder.colorSlice;
            lineOneView = viewHolder.lineOneView;
            lineTwoView = viewHolder.lineTwoView;
            status = viewHolder.status;
            overFlowButton = viewHolder.overFlowButton;
        }

        Project project = getItem(position);
        initButton(overFlowButton, project);

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


        char initLet = project.getAdminName().charAt(0);
        int colorRsrc = Utilities.getColorRsrcFromColor(project.getColor());

        //check if icon already exists
        IconKey key = new IconKey(initLet, colorRsrc);
        CharCircleIcon icon = sIconMap.get(key);

        //create new icon if it does not exist
        if (icon == null) {
            icon = new CharCircleIcon(initLet,
                                      getContext().getResources().getColor(colorRsrc),
                                      typeface);
            sIconMap.put(key, icon);
        }
        colorSlice.setBackground(icon);

        // set name
        lineOneView.setText(project.getName());

        //set due date
        dateParser.parse(project.getDueDate(), lineTwoView);

        //set progress
        status.setVisibility(ProgressBar.VISIBLE);
        status.setProgress((int) progress);
        status.getProgressDrawable()
              .setColorFilter(getContext().getResources().getColor(colorRsrc),
                              PorterDuff.Mode.SRC_IN);

        setTitleAppearance(lineOneView,
                           project,
                           R.style.completed_default,
                           R.style.not_completed_default);


        return convertView;
    }
}