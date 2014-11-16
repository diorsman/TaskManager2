package com.personal.taskmanager2.ui.projectDetails;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.utilities.Utilities;

public class ProjectDetailActivity extends ActionBarActivity {

    private static final String TAG = "ProjectDetailActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        Project project = getIntent().getExtras().getParcelable("project");

        Toolbar toolbar = Utilities.getToolbar(this);
        toolbar.setNavigationIcon(getV7DrawerToggleDelegate().getThemeUpIndicator());
        toolbar.setBackgroundColor(getResources().getColor(project.getColorRsrc()));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                                .add(R.id.container,
                                     ProjectOverviewFragment.newInstance(project))
                                .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
