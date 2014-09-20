package com.personal.taskmanager2.projectDetails;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.parseObjects.Project;

public class ProjectDetailActivity extends Activity {

    private static final String TAG = "ProjectDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        Project project = getIntent().getExtras().getParcelable("project");

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                                .add(R.id.container,
                                     ProjectOverviewFragment.newInstance(project))
                                .commit();
        }
    }


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.project_detail, menu);
        return true;
    }*/

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        }
        else {
            finish();
        }
    }
}
