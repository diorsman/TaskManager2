package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.app.Activity;
import android.widget.ListView;

import com.personal.taskmanager2.parseObjects.Project;

import java.util.List;

public class ProjectAdapterFactory {

    public static final int SIMPLE_ADAPTER = 0;
    public static final int DETAIL_ADAPTER = 1;

    public static BaseProjectAdapter createProjectAdapter(int adapterType,
                                                          Activity context,
                                                          List<Project> projects,
                                                          ListView listView) {

        if (adapterType == SIMPLE_ADAPTER) {
            return new SimpleProjectAdapter(context, projects, listView);
        }
        else {
            return new DetailProjectAdapter(context, projects, listView);
        }
    }

}
