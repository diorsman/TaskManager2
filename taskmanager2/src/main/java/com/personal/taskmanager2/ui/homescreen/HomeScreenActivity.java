package com.personal.taskmanager2.ui.homescreen;

import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.ParseUser;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen.ArchiveFragment;
import com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen.MyProjectsFragment;
import com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen.TrashFragment;
import com.personal.taskmanager2.ui.signIn.SignInActivity;
import com.personal.taskmanager2.utilities.Utilities;

public class HomeScreenActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String TAG = "HomeScreenActivity";

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home_screen);

        Toolbar toolbar = Utilities.getToolbar(this);
        toolbar.setBackgroundColor(getResources().getColor(R.color.theme_primary));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        mNavigationDrawerFragment =
                (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                                        (DrawerLayout) findViewById(R.id.drawer_layout));

        getWindow().setBackgroundDrawable(null);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        switch (position) {
            case 0:
                fragmentManager.beginTransaction()
                               .replace(R.id.container,
                                        MyProjectsFragment.newInstance())
                               .commit();
                break;
            case 1:
                fragmentManager.beginTransaction().replace(R.id.container,
                                                           ArchiveFragment.newInstance()).commit();
                break;
            case 2:
                fragmentManager.beginTransaction()
                               .replace(R.id.container, TrashFragment.newInstance())
                               .commit();
                break;
            case 3:
            case 4:
                fragmentManager.beginTransaction()
                               .replace(R.id.container,
                                        AccountSettingsFragment.newInstance())
                               .commit();
                break;
            case 5:
                fragmentManager.beginTransaction().replace(R.id.container,
                                                           HelpFragment.newInstance()).commit();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_logout:
                logOutAction();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {

        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        }
        else {
            logOutAction();
        }
    }

    private void logOutAction() {

        logOut();
        Intent intent = new Intent(this, SignInActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }

    private void logOut() {

        Toast.makeText(this, "You have been logged out.", Toast.LENGTH_SHORT).show();
        ParseUser.logOut();
    }
}
