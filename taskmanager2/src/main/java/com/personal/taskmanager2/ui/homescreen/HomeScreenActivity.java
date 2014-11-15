package com.personal.taskmanager2.ui.homescreen;

import android.app.Fragment;
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

    private static int mSelectedItem = -1;

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
        switch (position) {
            case 0:
                openFrag(MyProjectsFragment.newInstance(), 0);
                break;
            case 1:
                openFrag(ArchiveFragment.newInstance(), 1);
                break;
            case 2:
                openFrag(TrashFragment.newInstance(), 2);
                break;
            case 3:
                openFrag(AccountSettingsFragment.newInstance(), 3);
                break;
            case 4:
                openFrag(AccountSettingsFragment.newInstance(), 4);
                break;
            case 5:
                openFrag(HelpFragment.newInstance(), 5);
                break;
        }
    }

    private void openFrag(Fragment frag, int pos) {
        if (pos != mSelectedItem) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.container, frag).commit();
            mSelectedItem = pos;
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
        mSelectedItem = -1;
    }
}
