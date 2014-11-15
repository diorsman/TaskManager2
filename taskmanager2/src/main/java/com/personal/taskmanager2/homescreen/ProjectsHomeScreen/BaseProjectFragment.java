package com.personal.taskmanager2.homescreen.ProjectsHomeScreen;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.adapters.ActionBarSpinner;
import com.personal.taskmanager2.adapters.ProjectAdapter.BaseProjectAdapter;
import com.personal.taskmanager2.adapters.ProjectAdapter.ProjectAdapterFactory;
import com.personal.taskmanager2.homescreen.AddProjects.CreateProjectFragment;
import com.personal.taskmanager2.homescreen.AddProjects.JoinProjectFragment;
import com.personal.taskmanager2.homescreen.SearchFragment;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.projectDetails.ProjectDetailActivity;
import com.personal.taskmanager2.utilities.ListViewAnimationHelper;
import com.personal.taskmanager2.utilities.Utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseProjectFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
                   AbsListView.OnScrollListener,
                   ListView.OnItemClickListener {

    private static final String TAG = "BaseProjectFragment";

    private static final int LIST_VIEW_POS   = 0;
    private static final int DETAIL_VIEW_POS = 1;

    private static final int SORT_BY_DUE_DATE    = 0;
    private static final int SORT_BY_NAME        = 1;
    private static final int SORT_BY_DESCRIPTION = 2;
    private static final int SORT_BY_COLOR       = 3;

    private static final int LOAD_LIMIT = 25;

    private SwipeRefreshLayout mRefreshLayoutList;
    private TextView           mLoadProjects;
    private TextView           mNoProjects;
    private ListView           mListView;
    private View               mFooterView;

    private int mLayoutResourceId;

    private BaseProjectAdapter mProjectAdapter;
    private Parcelable         mListViewState;
    private Context            mContext;
    private Bundle             savedState;

    private boolean mQueriedList       = false;
    private boolean mQueriedDetail     = false;
    private boolean mIsLoadingMore     = false;
    private boolean mAllProjectsLoaded = false;

    private boolean mArchive;
    private boolean mTrash;

    private static final int EXCEPTION_OCCURRED  = 0;
    private static final int LOADED_MORE_ITEMS   = 1;
    private static final int FIRST_LOAD          = 2;
    private static final int ALL_PROJECTS_LOADED = 3;
    private static final int NO_PROJECTS_FOUND   = 4;

    private int mSelectedPosition = 0;
    private int mSortBy           = 0;
    private int mCurrentPage      = 0;
    private int mNumRemoved       = 0;

    private ExecutorService mExecutor;
    private Runnable        mQueryRunnable;

    private ListViewAnimationHelper<Project> mAnimHelper;
    private static final int ANIM_DURATION = 350;


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle args = getArguments();
        mLayoutResourceId = args.getInt("resourceId");
        mArchive = args.getBoolean("archive");
        mTrash = args.getBoolean("trash");

        mAnimHelper = new ListViewAnimationHelper<>(android.R.anim.slide_out_right,
                                                    ANIM_DURATION,
                                                    getActivity(),
                                                    new ListViewAnimationHelper.ListViewAnimationListener() {
                                                        @Override
                                                        public void onAnimationStart() {
                                                        }

                                                        @Override
                                                        public void onAnimationEnd() {
                                                            mNumRemoved++;
                                                        }

                                                        @Override
                                                        public void onAnimationRepeat() {
                                                        }
                                                    });

        mQueryRunnable = new Runnable() {
            @Override
            public void run() {

                queryProjectsInBackground(mUiHandler);
            }
        };

        View rootView = inflater.inflate(mLayoutResourceId, container, false);

        mRefreshLayoutList =
                (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout_list_view);
        mLoadProjects = (TextView) rootView.findViewById(R.id.myProjectLoad);
        mListView = (ListView) rootView.findViewById(R.id.project_list_view);
        mNoProjects = (TextView) rootView.findViewById(R.id.no_projects_text);

        // ListView set up
        mFooterView = inflater.inflate(R.layout.project_list_view_footer, null, false);
        mListView.setOnScrollListener(this);
        mListView.setEmptyView(mLoadProjects);
        mListView.setOnItemClickListener(this);

        mProjectAdapter = null;

        // refresh layout setup
        mRefreshLayoutList.setOnRefreshListener(this);
        mRefreshLayoutList.setColorSchemeResources(android.R.color.holo_purple,
                                                   android.R.color.holo_green_light,
                                                   android.R.color.holo_orange_light,
                                                   android.R.color.holo_red_light);

        mContext = getActivity();

        // allows fragment to receive onCreateOptionsMenu
        setHasOptionsMenu(true);

        if (savedInstanceState != null && savedState == null) {
            savedState = savedInstanceState.getBundle("state");
        }

        if (savedState != null) {
            mLayoutResourceId = savedState.getInt("resourceId");
            mCurrentPage = savedState.getInt("currentPage");
            mNumRemoved = savedState.getInt("numRemoved");
            mListViewState = savedState.getParcelable("listViewState");
            mListView.setAdapter(null);
            mListView.removeFooterView(mFooterView);
        }

        return rootView;
    }

    @Override
    public void onResume() {

        super.onResume();
        mExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onPause() {

        super.onPause();
        mQueriedList = false;
        mQueriedDetail = false;
        mExecutor.shutdownNow();
        mExecutor.shutdown();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);
        outState.putBundle("state", saveState());
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();
        savedState = saveState();
        mExecutor.shutdownNow();
        mExecutor.shutdown();
    }

    private Bundle saveState() {

        mListViewState = mListView.onSaveInstanceState();

        Bundle args = new Bundle();

        args.putInt("resourceId", mLayoutResourceId);
        args.putInt("currentPage", mCurrentPage);
        args.putInt("numRemoved", mNumRemoved);
        args.putParcelable("listViewState", mListViewState);

        return args;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        //action bar setup
        setUpActionBar(toolbar);

        //search view setup
        setUpSearchView(toolbar);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setUpActionBar(Toolbar toolbar) {
        ActionBarActivity parent = (ActionBarActivity) getActivity();
        if (parent == null) {
            return;
        }
        parent.getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.findViewById(R.id.actionbar_spinner).setVisibility(View.VISIBLE);
        toolbar.setBackgroundColor(getResources().getColor(R.color.theme_primary));
        toolbar.inflateMenu(R.menu.home_screen);
        initSpinnerAdapter(toolbar);
    }

    private void initSpinnerAdapter(Toolbar toolbar) {
        Spinner spinner = (Spinner) toolbar.findViewById(R.id.actionbar_spinner);
        ActionBarActivity parent = (ActionBarActivity) getActivity();
        ArrayAdapter<String> actionBarSpinner =
                new ActionBarSpinner(parent.getSupportActionBar().getThemedContext(),
                                     getResources().getStringArray(R.array.action_bar_spinner_items));
        actionBarSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(actionBarSpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        //Default List View
                        navItemSelected(LIST_VIEW_POS, mQueriedList);
                        mQueriedList = true;
                        mQueriedDetail = false;
                        break;
                    case 1:
                        // Detail View
                        navItemSelected(DETAIL_VIEW_POS, mQueriedDetail);
                        mQueriedDetail = true;
                        mQueriedList = false;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        spinner.setSelection(mSelectedPosition);
    }

    private void navItemSelected(int position, boolean hasQueried) {

        mSelectedPosition = position;
        if (!hasQueried) {
            if (mListView.getAdapter() != null && !mListView.getAdapter().isEmpty()) {
                mListViewState = mListView.onSaveInstanceState();
            }
            mListView.setAdapter(null);
            mListView.setEmptyView(mLoadProjects);
            mNoProjects.setVisibility(View.GONE);
            mIsLoadingMore = false;
            mAllProjectsLoaded = false;
            queryProjects();
        }
    }

    private void setUpSearchView(Toolbar toolbar) {
        //search view setup
        final MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(
                            View v,
                            boolean hasFocus) {

                        if (!hasFocus) {
                            MenuItemCompat.collapseActionView(searchItem);
                        }
                    }
                });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                MenuItemCompat.collapseActionView(searchItem);

                getFragmentManager().beginTransaction()
                                    .replace(R.id.container,
                                             SearchFragment.newInstance(query))
                                    .addToBackStack(
                                            TAG)
                                    .commit();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle presses on the action bar items

        switch (item.getItemId()) {
            case R.id.action_search:
                return true;

            case R.id.action_refresh:
                refresh();
                return true;

            case R.id.action_sort_by:
                openSortDialog();
                return true;

            /* Remember to Remove */
            case R.id.action_create_more_projects:
                createProjects();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void menuSetup() {

        View view = getActivity().findViewById(R.id.action_search);
        PopupMenu menu = new PopupMenu(getActivity(), view);
        menu.inflate(R.menu.add_projects);
        menu.show();
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_create:
                        CreateProjectFragment.newInstance().show(getFragmentManager(),
                                                                 "CreateProjectFragment");
                        return true;
                    case R.id.action_join:
                        JoinProjectFragment.newInstance().show(getFragmentManager(),
                                                               "JoinProjectFragment");
                        return true;
                    default:
                        return false;
                }
            }
        });
    }

    private void refresh() {

        mIsLoadingMore = false;
        mAllProjectsLoaded = false;
        queryProjects();
    }

    private void openSortDialog() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle("Sort By");
        dialog.setSingleChoiceItems(R.array.sort_by_array,
                                    mSortBy,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(
                                                DialogInterface dialogInterface,
                                                int which) {

                                            switch (which) {
                                                case SORT_BY_DUE_DATE:
                                                    mSortBy = SORT_BY_DUE_DATE;
                                                    break;
                                                case SORT_BY_NAME:
                                                    mSortBy = SORT_BY_NAME;
                                                    break;
                                                case SORT_BY_DESCRIPTION:
                                                    mSortBy =
                                                            SORT_BY_DESCRIPTION;
                                                    break;
                                                case SORT_BY_COLOR:
                                                    mSortBy = SORT_BY_COLOR;
                                                    break;
                                            }

                                            dialogInterface.dismiss();
                                            mCurrentPage = 0;
                                            refresh();
                                        }
                                    }
                                   );
        dialog.show();
    }

    private void createProjects() {

        final Handler uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                if (msg.what == 1) {
                    Toast.makeText(getActivity(), "Done", Toast.LENGTH_SHORT).show();
                }
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    final ParseUser user = ParseUser.getCurrentUser();
                    final String[] sColorValues =
                            {"Blue", "Orange", "Yellow", "Green", "Red", "Purple"};
                    user.fetchIfNeeded();
                    for (int i = 1; i < 300; ++i) {
                        String projectName = "Project ";
                        projectName += Integer.toString(i);
                        String uid = Integer.toString(i);
                        String password = "test";
                        Date date = Calendar.getInstance().getTime();
                        String name = (String) user.get("Name");
                        Project project = new Project(projectName, uid, password, date, user, name);
                        project.setDescription(Utilities.description);
                        project.setColor(sColorValues[i % 6]);
                        project.save();
                    }
                    uiHandler.sendEmptyMessage(1);
                }
                catch (ParseException e) {
                    //
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void onRefresh() {

        mListViewState = null;
        mCurrentPage = 0;
        mNumRemoved = 0;
        refresh();
    }


    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

        if (scrollState == SCROLL_STATE_IDLE && !mAllProjectsLoaded && !mIsLoadingMore) {

            // Load next page of projects
            if (mListView.getLastVisiblePosition() >= mListView.getCount() - 1) {
                mCurrentPage++;
                mIsLoadingMore = true;
                queryProjects();
            }

        }
    }

    @Override
    public void onScroll(AbsListView view,
                         int firstVisibleItemIndex,
                         int visibleItemCount,
                         int totalItemCount) {

    }

    private Handler mUiHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            mRefreshLayoutList.setRefreshing(false);
            mLoadProjects.setVisibility(View.GONE);
            mListView.setEmptyView(mNoProjects);
            List<Project> projects;

            switch (msg.what) {
                case EXCEPTION_OCCURRED:
                    ParseException e = (ParseException) msg.obj;
                    Log.e(TAG,
                          "Error code = " + Integer.toString(e.getCode()));
                    Log.e(TAG, "Error message " + e.getMessage());
                    Log.e(TAG, "Error occurred", e);
                    break;

                case LOADED_MORE_ITEMS:
                    projects = (List<Project>) msg.obj;
                    mProjectAdapter.addItems(projects);
                    mProjectAdapter.notifyDataSetChanged();
                    mIsLoadingMore = false;
                    break;

                case FIRST_LOAD:
                    projects = (List<Project>) msg.obj;
                    mProjectAdapter =
                            ProjectAdapterFactory.createProjectAdapter(mSelectedPosition,
                                                                       getActivity(),
                                                                       projects,
                                                                       mAnimHelper);
                    mListView.setAdapter(mProjectAdapter);
                    mAnimHelper.setListView(mListView);
                    mAnimHelper.setAdapter(mProjectAdapter);

                    if (mListViewState != null) {
                        mListView.onRestoreInstanceState(mListViewState);
                        mListViewState = null;
                    }

                    if (mListView.getFooterViewsCount() == 0 &&
                        mProjectAdapter.getCount() >= LOAD_LIMIT) {
                        mListView.addFooterView(mFooterView);
                    }
                    break;

                case ALL_PROJECTS_LOADED:
                    mListView.removeFooterView(mFooterView);
                    mIsLoadingMore = false;
                    mAllProjectsLoaded = true;
                    break;

                case NO_PROJECTS_FOUND:
                    mListView.setEmptyView(mNoProjects);
                    mListView.removeFooterView(mFooterView);
                    mLoadProjects.setVisibility(View.GONE);
                    break;
            }
        }
    };


    private void queryProjects() {

        if (!mIsLoadingMore) {
            mRefreshLayoutList.setRefreshing(true);
        }
        if (!mExecutor.isShutdown()) {
            mExecutor.execute(mQueryRunnable);
        }
    }

    private void queryProjectsInBackground(Handler handler) {

        try {
            ParseQuery<Project> projectAdmin = ParseQuery.getQuery(Project.class);
            projectAdmin.whereEqualTo(Project.ADMIN_COL, ParseUser.getCurrentUser());

            ParseQuery<Project> projectUser = ParseQuery.getQuery(Project.class);
            projectUser.whereEqualTo(Project.USERS_ID_COL,
                                     ParseUser.getCurrentUser().getObjectId());

            List<ParseQuery<Project>> orQueries = new ArrayList<>();
            orQueries.add(projectAdmin);
            orQueries.add(projectUser);

            ParseQuery<Project> projectQuery = ParseQuery.or(orQueries);
            projectQuery.whereEqualTo(Project.ARCHIVED_COL, mArchive);
            projectQuery.whereEqualTo(Project.TRASH_COL, mTrash);

            // set sort method
            if (mSortBy == SORT_BY_DUE_DATE) {
                projectQuery.orderByAscending(Project.DUE_DATE_COL);
            }
            else if (mSortBy == SORT_BY_COLOR) {
                projectQuery.orderByAscending(Project.COLOR_COL);
            }
            else if (mSortBy == SORT_BY_NAME) {
                projectQuery.orderByAscending(Project.NAME_COL);
            }
            else if (mSortBy == SORT_BY_DESCRIPTION) {
                projectQuery.orderByAscending(Project.DESCRIPTION_COL);
            }
            projectQuery.addAscendingOrder(Project.DUE_DATE_COL);

            if (mListViewState == null) {
                // set load limit
                projectQuery.setLimit(LOAD_LIMIT);

                // pagination
                if (mIsLoadingMore) {
                    projectQuery.setSkip((mCurrentPage * LOAD_LIMIT) - mNumRemoved);
                }
            }
            else {
                if (mListView.getAdapter() == null ||
                    mListView.getAdapter().getCount() - mListView.getFooterViewsCount() < 1) {
                    projectQuery.setSkip(0);
                    projectQuery.setLimit(((mCurrentPage + 1) * LOAD_LIMIT) - mNumRemoved);
                }
                else {
                    projectQuery.setSkip((mCurrentPage * LOAD_LIMIT) - mNumRemoved);
                    projectQuery.setLimit(LOAD_LIMIT);
                }
            }

            List<Project> projects = projectQuery.find();

            if (!projects.isEmpty()) {
                if (mProjectAdapter != null && !mAllProjectsLoaded && mIsLoadingMore) {
                    Message msg = handler.obtainMessage(LOADED_MORE_ITEMS, projects);
                    handler.sendMessage(msg);
                }
                else {
                    Message msg = handler.obtainMessage(FIRST_LOAD, projects);
                    handler.sendMessage(msg);
                }
            }
            else if (!mAllProjectsLoaded && mIsLoadingMore) {
                Message msg = handler.obtainMessage(ALL_PROJECTS_LOADED);
                handler.sendMessage(msg);
            }
            else {
                Message msg = handler.obtainMessage(NO_PROJECTS_FOUND);
                handler.sendMessage(msg);
            }
        }
        catch (ParseException e) {
            Message msg = handler.obtainMessage(EXCEPTION_OCCURRED, e);
            handler.sendMessage(msg);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        Project project = (Project) mListView.getItemAtPosition(position);
        Intent intent = new Intent(getActivity(), ProjectDetailActivity.class);
        intent.putExtra("project", project);
        startActivity(intent);
    }
}
