package com.personal.taskmanager2.adapters.ProjectAdapter;

import android.app.Activity;
import android.util.Log;

import com.personal.taskmanager2.model.parse.Project;

import java.util.List;

public class ProjectAdapterFactory {

    private static final String TAG = "ProjectAdapterFactory";

    public static final int SIMPLE_ADAPTER = 0;
    public static final int DETAIL_ADAPTER = 1;

    public static BaseProjectAdapter createProjectAdapter(int adapterType,
                                                          Activity context,
                                                          List<Project> projects,
                                                          BaseProjectAdapter.OnItemClickListener listener) {

        if (adapterType == SIMPLE_ADAPTER) {
            return new SimpleProjectAdapter(context, projects, listener);
        }
        else if (adapterType == DETAIL_ADAPTER) {
            return new DetailProjectAdapter(context, projects, listener);
        }
        else {
            Log.e(TAG, "Passed in incorrect adapter type.");
            throw new IllegalArgumentException();
        }
    }

}
