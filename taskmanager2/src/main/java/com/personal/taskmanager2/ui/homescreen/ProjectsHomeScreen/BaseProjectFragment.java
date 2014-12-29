package com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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
import com.personal.taskmanager2.adapters.ProjectAdapter.SectionedRecycleViewAdapter;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.ui.homescreen.SearchFragment;
import com.personal.taskmanager2.ui.projectDetails.ProjectDetailActivity;
import com.personal.taskmanager2.ui.widget.DividerItemDecoration;
import com.personal.taskmanager2.utilities.RecyclerViewTouchListener;
import com.personal.taskmanager2.utilities.Utilities;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BaseProjectFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
                   BaseProjectAdapter.OnItemClickListener,
                   RecyclerViewTouchListener.DismissCallbacks {

    private static final String TAG = "BaseProjectFragment";

    private static final int LIST_VIEW_POS   = 0;
    private static final int DETAIL_VIEW_POS = 1;

    private static final int SORT_BY_DUE_DATE    = 0;
    private static final int SORT_BY_NAME        = 1;
    private static final int SORT_BY_DESCRIPTION = 2;
    private static final int SORT_BY_COLOR       = 3;

    private String mToolbarTitle;

    private   SwipeRefreshLayout mRefreshLayoutList;
    protected RecyclerView       mRecyclerView;
    private   TextView           mLoadProjects;
    private   TextView           mNoProjects;

    private int mLayoutResourceId;

    protected BaseProjectAdapter          mProjectAdapter;
    protected SectionedRecycleViewAdapter mSectionedAdapter;
    private   Context                     mContext;
    protected ActionMode                  mActionMode;
    protected ActionMode.Callback         mActionModeCallback;

    private boolean mArchive;
    private boolean mTrash;

    protected int mSelectedPosition = -1;
    private   int mSortBy           = 0;


    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private static final int      KEEP_ALIVE_TIME      = 1;
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;

    private Future mQueryProjectsFuture;
    private Future mGetNumProjectsOverdueFuture;
    private Future mGetNumProjectsDueTodayFuture;
    private Future mGetNumProjectsDueThisWeekFuture;
    private Future mGetNumProjectsDueThisMonthFuture;
    private Future mGetNumProjectsCompletedFuture;

    private final BlockingQueue<Runnable> mQueryQueue = new LinkedBlockingQueue<>();
    private       ThreadPoolExecutor      mExecutor   = new ThreadPoolExecutor(NUMBER_OF_CORES,
                                                                               NUMBER_OF_CORES,
                                                                               KEEP_ALIVE_TIME,
                                                                               KEEP_ALIVE_TIME_UNIT,
                                                                               mQueryQueue);

    private static final int EXCEPTION_OCCURRED          = 0;
    private static final int PROJECTS_LOADED             = 1;
    private static final int NO_PROJECTS_FOUND           = 2;
    private static final int NUM_PROJECTS_OVERDUE        = 3;
    private static final int NUM_PROJECTS_DUE_TODAY      = 4;
    private static final int NUM_PROJECTS_DUE_THIS_WEEK  = 5;
    private static final int NUM_PROJECTS_DUE_THIS_MONTH = 6;
    private static final int NUM_PROJECTS_COMPLETED      = 7;

    public ActionMode getActionMode() {
        return mActionMode;
    }

    public void onCreate(Bundle savedInstanceState) {

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getFragmentManager().getBackStackEntryCount() == 0) {
                    if (mProjectAdapter != null) {
                        mProjectAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        mLayoutResourceId = args.getInt("resourceId");
        mArchive = args.getBoolean("archive");
        mTrash = args.getBoolean("trash");
        mToolbarTitle = args.getString("title");

        View rootView = inflater.inflate(mLayoutResourceId, container, false);

        mRefreshLayoutList =
                (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout_list_view);
        mLoadProjects = (TextView) rootView.findViewById(R.id.myProjectLoad);
        mNoProjects = (TextView) rootView.findViewById(R.id.no_projects_text);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.project_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(),
                                                                  DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.addOnItemTouchListener(new RecyclerViewTouchListener(mRecyclerView,
                                                                           mRefreshLayoutList,
                                                                           this));

        // refresh layout setup
        mRefreshLayoutList.setOnRefreshListener(this);
        mRefreshLayoutList.setColorSchemeResources(android.R.color.holo_purple,
                                                   android.R.color.holo_green_light,
                                                   android.R.color.holo_orange_light,
                                                   android.R.color.holo_red_light);

        mContext = getActivity();

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public boolean canDismiss(int position) {
        return !mSectionedAdapter.isSectionHeaderPosition(position);
    }

    @Override
    public void onDismiss(RecyclerView listView, int[] reverseSortedPositions) {

    }

    @Override
    public void onItemClick(View v) {
        int position =
                mSectionedAdapter.sectionedPositionToPosition(mRecyclerView.getChildPosition(v));

        if (mProjectAdapter.isItemSelected(position)) {
            unSelectItem(position);
        }
        else {
            Intent intent =
                    new Intent(BaseProjectFragment.this.getActivity(),
                               ProjectDetailActivity.class);
            intent.putExtra("project",
                            mProjectAdapter.getItem(position));
            startActivity(intent);
        }
    }

    @Override
    public void onItemLongClick(View v) {
        int position = mRecyclerView.getChildPosition(v);
        toggleSelection(position);
    }

    @Override
    public void onAvatarClick(View avatar, int position) {
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        position = mSectionedAdapter.sectionedPositionToPosition(position);
        if (mProjectAdapter.isItemSelected(position)) {
            unSelectItem(position);
        }
        else {
            mProjectAdapter.selectItem(position);
            if (mActionMode == null) {
                mActionMode =
                        Utilities.getToolbar(getActivity()).startActionMode(mActionModeCallback);
            }
            else {
                mActionMode.invalidate();
            }
        }
    }

    private void unSelectItem(int position) {
        mProjectAdapter.unselectedItem(position);
        if (mProjectAdapter.getNumSelected() == 0) {
            //if (mActionMode != null) {
            mActionMode.finish();
            //}
        }
        else {
            mActionMode.invalidate();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Toolbar toolbar = Utilities.getToolbar(getActivity());

        //action bar setup
        setUpActionBar(toolbar);

        //search view setup
        setUpSearchView(toolbar);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setUpActionBar(Toolbar toolbar) {
        Utilities.enableToolbarTitle(getActivity(), false, TAG);
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
                                     getResources().getStringArray(R.array.action_bar_spinner_items),
                                     mToolbarTitle);
        actionBarSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(actionBarSpinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        //Default List View
                        navItemSelected(LIST_VIEW_POS);
                        break;
                    case 1:
                        // Detail View
                        navItemSelected(DETAIL_VIEW_POS);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(mSelectedPosition);
    }

    private void navItemSelected(int position) {
        if (mSelectedPosition != position) {
            queryProjects();
        }
        mSelectedPosition = position;
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
                mSelectedPosition = -1;
                getFragmentManager().beginTransaction()
                                    .addToBackStack(null)
                                    .replace(R.id.container, SearchFragment.newInstance(query))
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
                queryProjects();
                return true;

            case R.id.action_sort_by:
                openSortDialog();
                return true;

            case R.id.action_create_more_projects:
                createProjects();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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
                    int counter = 0;
                    Calendar cal = Calendar.getInstance();
                    for (int i = 1; i < 301; ++i, ++counter) {
                        String projectName = "Project ";
                        projectName += Integer.toString(i);
                        String uid = Integer.toString(i);
                        String password = "test";
                        Date date = cal.getTime();
                        String name = (String) user.get("Name");
                        Project project = new Project(projectName, uid, password, date, user, name);
                        project.setDescription(Utilities.description);
                        project.setColor(sColorValues[i % 6]);
                        project.save();
                        if (counter == 2) {
                            cal.add(Calendar.DATE, 1);
                            counter = 0;
                        }
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
                                            queryProjects();
                                        }
                                    }
                                   );
        dialog.show();
    }

    @Override
    public void onRefresh() {
        queryProjects();
    }

    public void queryProjects() {
        mRefreshLayoutList.setRefreshing(true);
        mQueryProjectsFuture = mExecutor.submit(queryProjectRunnable);
        mGetNumProjectsDueTodayFuture = mExecutor.submit(getNumProjectsDueTodayRunnable);
        mGetNumProjectsDueThisWeekFuture = mExecutor.submit(getNumProjectsDueThisWeekRunnable);
        mGetNumProjectsDueThisMonthFuture = mExecutor.submit(getNumProjectDueThisMonthRunnable);
        mGetNumProjectsOverdueFuture = mExecutor.submit(getNumProjectsOverdue);
        mGetNumProjectsCompletedFuture = mExecutor.submit(getNumProjectsCompleted);
    }

    private Runnable queryProjectRunnable = new Runnable() {
        @Override
        public void run() {
            queryProjectsInBackground(mHandler);
        }
    };

    private Runnable getNumProjectsDueTodayRunnable = new Runnable() {
        @Override
        public void run() {
            getProjectCountDueToday(mHandler);
        }
    };

    private Runnable getNumProjectsDueThisWeekRunnable = new Runnable() {
        @Override
        public void run() {
            getProjectCountDueThisWeek(mHandler);
        }
    };

    private Runnable getNumProjectDueThisMonthRunnable = new Runnable() {
        @Override
        public void run() {
            getProjectCountDueThisMonth(mHandler);
        }
    };

    private Runnable getNumProjectsOverdue = new Runnable() {
        @Override
        public void run() {
            getProjectCountOverdue(mHandler);
        }
    };

    private Runnable getNumProjectsCompleted = new Runnable() {
        @Override
        public void run() {
            getProjectCountCompleted(mHandler);
        }
    };


    private Handler mHandler = new Handler() {
        private List<Project> projectList = null;
        private int numProjectsOverdue = -1;
        private int numProjectsDueToday = -1;
        private int numProjectsDueThisWeek = -1;
        private int numProjectsDueThisMonth = -1;
        private int numProjectsCompleted = -1;
        private boolean projectsQueried = false;
        private boolean numProjectsOverdueFound = false;
        private boolean numProjectDueTodayFound = false;
        private boolean numProjectsDueThisWeekFound = false;
        private boolean numProjectsDueThisMonthFound = false;
        private boolean numProjectsCompletedFound = false;

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case EXCEPTION_OCCURRED:
                    ParseException e = (ParseException) msg.obj;
                    Log.e(TAG,
                          "Error code = " + Integer.toString(e.getCode()));
                    Log.e(TAG, "Error message " + e.getMessage());
                    Log.e(TAG, "Error occurred", e);
                    Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                    mRefreshLayoutList.setRefreshing(false);
                    mLoadProjects.setVisibility(View.GONE);
                    mNoProjects.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    projectsQueried = false;
                    mQueryProjectsFuture.cancel(true);
                    mGetNumProjectsOverdueFuture.cancel(true);
                    mGetNumProjectsDueTodayFuture.cancel(true);
                    mGetNumProjectsDueThisWeekFuture.cancel(true);
                    mGetNumProjectsDueThisMonthFuture.cancel(true);
                    mGetNumProjectsCompletedFuture.cancel(true);
                    break;

                case NO_PROJECTS_FOUND:
                    mRefreshLayoutList.setRefreshing(false);
                    mLoadProjects.setVisibility(View.GONE);
                    mNoProjects.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                    projectsQueried = false;
                    mQueryProjectsFuture.cancel(true);
                    mGetNumProjectsOverdueFuture.cancel(true);
                    mGetNumProjectsDueTodayFuture.cancel(true);
                    mGetNumProjectsDueThisWeekFuture.cancel(true);
                    mGetNumProjectsDueThisMonthFuture.cancel(true);
                    mGetNumProjectsCompletedFuture.cancel(true);
                    break;

                case PROJECTS_LOADED:
                    projectList = (List<Project>) msg.obj;
                    projectsQueried = true;
                    displayData();
                    break;

                case NUM_PROJECTS_OVERDUE:
                    numProjectsOverdue = (int) msg.obj;
                    numProjectsOverdueFound = true;
                    displayData();
                    break;

                case NUM_PROJECTS_DUE_TODAY:
                    numProjectsDueToday = (int) msg.obj;
                    numProjectDueTodayFound = true;
                    displayData();
                    break;

                case NUM_PROJECTS_DUE_THIS_WEEK:
                    numProjectsDueThisWeek = (int) msg.obj;
                    numProjectsDueThisWeekFound = true;
                    displayData();
                    break;

                case NUM_PROJECTS_DUE_THIS_MONTH:
                    numProjectsDueThisMonth = (int) msg.obj;
                    numProjectsDueThisMonthFound = true;
                    displayData();
                    break;

                case NUM_PROJECTS_COMPLETED:
                    numProjectsCompleted = (int) msg.obj;
                    numProjectsCompletedFound = true;
                    displayData();
                    break;
            }
        }

        private void displayData() {
            if (projectsQueried && numProjectDueTodayFound && numProjectsDueThisWeekFound &&
                numProjectsDueThisMonthFound && numProjectsOverdueFound &&
                numProjectsCompletedFound) {
                int numProjectsDueLater =
                        projectList.size() - numProjectsDueToday - numProjectsDueThisMonth -
                        numProjectsDueThisWeek - numProjectsCompleted;

                mRefreshLayoutList.setRefreshing(false);
                mLoadProjects.setVisibility(View.GONE);

                mProjectAdapter = ProjectAdapterFactory.createProjectAdapter(mSelectedPosition,
                                                                             getActivity(),
                                                                             projectList,
                                                                             BaseProjectFragment.this);
                List<SectionedRecycleViewAdapter.Section> sections = new ArrayList<>();

                //Sections
                if (numProjectsOverdue > 0) {
                    sections.add(new SectionedRecycleViewAdapter.Section(0, "Overdue"));
                }
                if (numProjectsDueToday > 0) {
                    if (sections.isEmpty()) {
                        sections.add(new SectionedRecycleViewAdapter.Section(0, "Due Today"));
                    }
                    else {
                        sections.add(new SectionedRecycleViewAdapter.Section(
                                numProjectsOverdue - numProjectsCompleted,
                                "Due Today"));
                    }
                }
                if (numProjectsDueThisWeek > 0) {
                    if (sections.isEmpty()) {
                        sections.add(new SectionedRecycleViewAdapter.Section(0, "Due This Week"));
                    }
                    else {
                        sections.add(new SectionedRecycleViewAdapter.Section(
                                numProjectsOverdue + numProjectsDueToday - numProjectsCompleted,
                                "Due This Week"));
                    }
                }
                if (numProjectsDueThisMonth > 0) {
                    if (sections.isEmpty()) {
                        sections.add(new SectionedRecycleViewAdapter.Section(0, "Due This Month"));
                    }
                    else {
                        sections.add(new SectionedRecycleViewAdapter.Section(
                                numProjectsOverdue + numProjectsDueToday + numProjectsDueThisWeek -
                                numProjectsCompleted,
                                "Due This Month"));
                    }
                }
                if (numProjectsDueLater > 0) {
                    if (sections.isEmpty()) {
                        sections.add(new SectionedRecycleViewAdapter.Section(0, "Due Later"));
                    }
                    else {
                        sections.add(new SectionedRecycleViewAdapter.Section(
                                numProjectsOverdue + numProjectsDueToday + numProjectsDueThisWeek +
                                numProjectsDueThisMonth - numProjectsCompleted, "Due Later"));
                    }
                }
                if (numProjectsCompleted > 0) {
                    if (sections.isEmpty()) {
                        sections.add(new SectionedRecycleViewAdapter.Section(0, "Completed"));
                    }
                    else {
                        sections.add(new SectionedRecycleViewAdapter.Section(
                                projectList.size() - numProjectsCompleted, "Completed"));
                    }
                }

                //Add your adapter to the sectionAdapter
                SectionedRecycleViewAdapter.Section[] dummy =
                        new SectionedRecycleViewAdapter.Section[sections.size()];
                mSectionedAdapter =
                        new SectionedRecycleViewAdapter(BaseProjectFragment.this.getActivity(),
                                                        R.layout.list_item_section,
                                                        R.id.list_item_section,
                                                        mProjectAdapter);
                mSectionedAdapter.setSections(sections.toArray(dummy));
                mProjectAdapter.setSectionAdapter(mSectionedAdapter);

                //Apply this adapter to the RecyclerView
                mRecyclerView.setAdapter(mSectionedAdapter);
                mRecyclerView.setVisibility(View.VISIBLE);
                mNoProjects.setVisibility(View.GONE);

                projectsQueried = false;
                numProjectDueTodayFound = false;
                numProjectsDueThisWeekFound = false;
                numProjectsDueThisMonthFound = false;
                numProjectsOverdueFound = false;
                numProjectsCompletedFound = false;
            }
        }
    };

    private void getProjectCountOverdue(Handler handler) {
        try {
            Calendar end = Calendar.getInstance();
            printCal(end, "overdue = ");

            ParseQuery<Project> projectQuery = initQuery();
            projectQuery.whereLessThan(Project.DUE_DATE_COL, end.getTime());
            int numProjectsOverdue = projectQuery.count();
            Log.d(TAG, "num projects overdue = " + numProjectsOverdue);

            Message msg = handler.obtainMessage(NUM_PROJECTS_OVERDUE, numProjectsOverdue);
            handler.sendMessage(msg);
        }
        catch (ParseException e) {
            handleParseException(e, handler);
        }
    }

    private void getProjectCountDueToday(Handler handler) {
        try {
            Calendar startToday = Calendar.getInstance();
            Calendar endToday = Calendar.getInstance();
            setCalendarToEndOfDay(endToday);

            ParseQuery<Project> projectQuery = initQuery();
            projectQuery.whereGreaterThanOrEqualTo(Project.DUE_DATE_COL, startToday.getTime());
            projectQuery.whereLessThanOrEqualTo(Project.DUE_DATE_COL, endToday.getTime());
            int numProjectsDueToday = projectQuery.count();
            Log.d(TAG, "Num projects due today = " + numProjectsDueToday);

            Message msg = handler.obtainMessage(NUM_PROJECTS_DUE_TODAY, numProjectsDueToday);
            handler.sendMessage(msg);
        }
        catch (ParseException e) {
            handleParseException(e, handler);
        }
    }

    private void getProjectCountDueThisWeek(Handler handler) {
        try {
            Calendar startWeek = Calendar.getInstance();
            startWeek.add(Calendar.DATE, 1);
            setCalendarToBeginningOfDay(startWeek);
            printCal(startWeek, "start week =");

            Calendar lastDayOfWeek = Calendar.getInstance();
            int curDay = lastDayOfWeek.get(Calendar.DAY_OF_WEEK);
            lastDayOfWeek.add(Calendar.DATE, Calendar.SATURDAY - curDay);
            setCalendarToEndOfDay(lastDayOfWeek);
            printCal(lastDayOfWeek, "end week =");

            ParseQuery<Project> projectQuery = initQuery();
            projectQuery.whereGreaterThanOrEqualTo(Project.DUE_DATE_COL, startWeek.getTime());
            projectQuery.whereLessThanOrEqualTo(Project.DUE_DATE_COL, lastDayOfWeek.getTime());
            int numProjectsDueThisWeek = projectQuery.count();
            Log.d(TAG, "Num projects due this week = " + numProjectsDueThisWeek);

            Message msg = handler.obtainMessage(NUM_PROJECTS_DUE_THIS_WEEK, numProjectsDueThisWeek);
            handler.sendMessage(msg);
        }
        catch (ParseException e) {
            handleParseException(e, handler);
        }
    }

    private void getProjectCountDueThisMonth(Handler handler) {
        try {
            Calendar startMonth = Calendar.getInstance();
            int curDay = startMonth.get(Calendar.DAY_OF_WEEK);
            startMonth.add(Calendar.DATE, Calendar.SATURDAY - curDay + 1);
            setCalendarToBeginningOfDay(startMonth);
            printCal(startMonth, "start month=");

            Calendar endMonth = Calendar.getInstance();
            endMonth.set(Calendar.DAY_OF_MONTH, endMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
            setCalendarToEndOfDay(endMonth);
            printCal(endMonth, "end month=");

            ParseQuery<Project> projectQuery = initQuery();
            projectQuery.whereGreaterThanOrEqualTo(Project.DUE_DATE_COL, startMonth.getTime());
            projectQuery.whereLessThanOrEqualTo(Project.DUE_DATE_COL, endMonth.getTime());
            int numProjectsDueThisMonth = projectQuery.count();
            Log.d(TAG, "Num projects due this month = " + numProjectsDueThisMonth);

            Message msg =
                    handler.obtainMessage(NUM_PROJECTS_DUE_THIS_MONTH, numProjectsDueThisMonth);
            handler.sendMessage(msg);
        }
        catch (ParseException e) {
            handleParseException(e, handler);
        }
    }

    private void getProjectCountCompleted(Handler handler) {
        try {
            ParseQuery<Project> projectQuery = initQuery();
            projectQuery.whereEqualTo(Project.STATUS_COL, true);
            int numProjectsCompleted = projectQuery.count();
            Log.d(TAG, "Num projects completed = " + numProjectsCompleted);

            Message msg = handler.obtainMessage(NUM_PROJECTS_COMPLETED, numProjectsCompleted);
            handler.sendMessage(msg);
        }
        catch (ParseException e) {
            handleParseException(e, handler);
        }
    }

    private void setCalendarToBeginningOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private void setCalendarToEndOfDay(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
    }

    private void printCal(Calendar cal, String init) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        Log.d(TAG, init + " " + dateFormat.format(cal.getTime()));
    }

    private void queryProjectsInBackground(Handler handler) {
        try {
            ParseQuery<Project> projectQuery = initQuery();
            setSortMethod(projectQuery);
            List<Project> projectList = projectQuery.find();

            Message msg;
            if (projectList.isEmpty()) {
                msg = handler.obtainMessage(NO_PROJECTS_FOUND);
            }
            else {
                msg = handler.obtainMessage(PROJECTS_LOADED, projectList);
            }
            handler.sendMessage(msg);
        }
        catch (ParseException e) {
            handleParseException(e, handler);
        }
    }

    private void handleParseException(ParseException e, Handler handler) {
        Log.e(TAG, e.getMessage(), e);
        Message msg = handler.obtainMessage(EXCEPTION_OCCURRED, e);
        handler.sendMessage(msg);
    }

    private ParseQuery<Project> initQuery() {
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

        projectQuery.setLimit(1000);

        return projectQuery;
    }

    private void setSortMethod(ParseQuery<Project> projectQuery) {
        if (mSortBy == SORT_BY_DUE_DATE) {
            projectQuery.orderByAscending(Project.STATUS_COL);
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
    }
}
