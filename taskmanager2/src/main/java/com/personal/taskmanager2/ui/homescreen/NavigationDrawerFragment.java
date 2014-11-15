package com.personal.taskmanager2.ui.homescreen;


import android.app.Activity;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseUser;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.utilities.Utilities;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment
        implements View.OnClickListener {

    private static final String TAG = "NavigationDrawerFragment";

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION =
            "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER =
            "navigation_drawer_learned";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private View         mDrawerView;
    private View         mFragmentContainerView;
    private TextView     mProjects;
    private TextView     mArchive;
    private TextView     mTrash;
    private TextView     mProfile;
    private TextView     mSettings;
    private TextView     mHelp;
    private TextView     mActivated;

    private int     mCurrentSelectedPosition;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private boolean mIsNavVisible;

    // User Email Address
    private String mEmail;
    private String mName;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition =
                    savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        ParseUser curUser = ParseUser.getCurrentUser();
        mEmail = curUser.getEmail();
        mName = curUser.getString("Name");

        // Keep Track of backstack and set nav drawer icon accordingly
        getFragmentManager().addOnBackStackChangedListener(new android.app.FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {

                setActionBarArrowDependingOnFragmentsBackStack();
            }
        });

        // Select either the default item (0) or the last selected item.
        //selectItem(mCurrentSelectedPosition);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mDrawerView = inflater.inflate(R.layout.fragment_navigation_drawer,
                                       container,
                                       false);

        // Put name and email on top of Nav Drawer
        TextView name =
                (TextView) mDrawerView.findViewById(R.id.nav_drawer_name);
        TextView email =
                (TextView) mDrawerView.findViewById(R.id.nav_drawer_email);
        email.setText(mEmail);
        name.setText(mName);

        mProjects = (TextView) mDrawerView.findViewById(R.id.projects);
        mArchive = (TextView) mDrawerView.findViewById(R.id.archive);
        mTrash = (TextView) mDrawerView.findViewById(R.id.trash);
        mProfile = (TextView) mDrawerView.findViewById(R.id.profile);
        mSettings = (TextView) mDrawerView.findViewById(R.id.settings);
        mHelp = (TextView) mDrawerView.findViewById(R.id.help);

        mProjects.setOnClickListener(this);
        mArchive.setOnClickListener(this);
        mTrash.setOnClickListener(this);
        mProfile.setOnClickListener(this);
        mSettings.setOnClickListener(this);
        mHelp.setOnClickListener(this);

        selectItem(mCurrentSelectedPosition);

        return mDrawerView;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.projects:
                selectItem(0);
                break;
            case R.id.archive:
                selectItem(1);
                break;
            case R.id.trash:
                selectItem(2);
                break;
            case R.id.profile:
                selectItem(3);
                break;
            case R.id.settings:
                selectItem(4);
                break;
            case R.id.help:
                selectItem(5);
        }
    }

    private void selectItem(int position) {

        if (mActivated != null) {
            mActivated.setActivated(false);
        }

        switch (position) {
            case 0:
                mProjects.setActivated(true);
                mActivated = mProjects;
                break;
            case 1:
                mArchive.setActivated(true);
                mActivated = mArchive;
                break;
            case 2:
                mTrash.setActivated(true);
                mActivated = mTrash;
                break;
            case 3:
                mProfile.setActivated(true);
                mActivated = mProfile;
                break;
            case 4:
                mSettings.setActivated(true);
                mActivated = mSettings;
                break;
            case 5:
                mHelp.setActivated(true);
                mActivated = mHelp;
                break;
        }

        mCurrentSelectedPosition = position;

        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }


    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout) {

        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                                      GravityCompat.START);
        // set up the drawer's list view with items and click listener

        /*ActionBar actionBar = ();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);*/

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {

                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                mIsNavVisible = false;
                getActivity().invalidateOptionsMenu();

                syncState();
            }

            @Override
            public void onDrawerOpened(View drawerView) {

                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit()
                      .putBoolean(PREF_USER_LEARNED_DRAWER, true)
                      .apply();
                }

                mIsNavVisible = true;
                getActivity().invalidateOptionsMenu();

                syncState();
            }

            @Override
            public void onDrawerStateChanged(int newState) {

                super.onDrawerStateChanged(newState);

                boolean isOpened = mDrawerLayout.isDrawerOpen(mDrawerView);
                boolean isVisible = mDrawerLayout.isDrawerVisible(mDrawerView);

                if (!isOpened && !isVisible) {
                    if (newState == DrawerLayout.STATE_IDLE) {
                        // drawer just hid completely
                        mIsNavVisible = false;
                        getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                    }
                    else {
                        // drawer just entered screen
                        mIsNavVisible = true;
                        getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
                    }
                }

                syncState();
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {

                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(
                    "Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {

        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        setUpActionBar(!mIsNavVisible);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    // If there is a fragment in the backstack, change nav drawer icon to up arrow
    private void setActionBarArrowDependingOnFragmentsBackStack() {

        int backStackEntryCount = getFragmentManager().getBackStackEntryCount();
        boolean shouldEnableDrawerIndicator = backStackEntryCount == 0;
        mDrawerToggle.setDrawerIndicatorEnabled(shouldEnableDrawerIndicator);
    }

    private void setUpActionBar(boolean visible) {
        if (getActivity() == null) {
            return;
        }
        if (!visible) {
            Toolbar toolbar = Utilities.getToolbar(getActivity());
            Menu menu = toolbar.getMenu();
            toolbar.findViewById(R.id.actionbar_spinner).setVisibility(View.GONE);
            menu.clear();
            Utilities.enableToolbarTitle(getActivity(), true, TAG);
            toolbar.setTitle("TaskManager");
        }
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {

        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }
}
