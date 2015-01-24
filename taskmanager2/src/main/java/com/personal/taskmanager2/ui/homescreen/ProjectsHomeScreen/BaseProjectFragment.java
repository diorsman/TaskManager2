package com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
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
import com.personal.taskmanager2.utilities.NotifyingThreadPoolExecutor;
import com.personal.taskmanager2.ui.ItemTouchListener;
import com.personal.taskmanager2.utilities.Utilities;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class BaseProjectFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
                   BaseProjectAdapter.OnItemClickListener,
                   NotifyingThreadPoolExecutor.Callback {

    private static final String TAG = "BaseProjectFragment";

    private static final int SIMPLE_VIEW_POS = 0;
    private static final int DETAIL_VIEW_POS = 1;

    private static final int SORT_BY_DUE_DATE    = 0;
    private static final int SORT_BY_NAME        = 1;
    private static final int SORT_BY_DESCRIPTION = 2;
    private static final int SORT_BY_COLOR       = 3;

    private String mToolbarTitle;

    private   SwipeRefreshLayout mRefreshLayoutList;
    protected RecyclerView       mRecyclerView;
    protected TextView           mLoadProjects;
    private   TextView           mNoProjects;

    private int mLayoutResourceId;

    protected BaseProjectAdapter          mProjectAdapter;
    protected SectionedRecycleViewAdapter mSectionedAdapter;
    private   Context                     mContext;
    protected ActionMode                  mActionMode;
    protected ActionMode.Callback mActionModeCallback = initCab();

    private boolean mArchive;
    private boolean mTrash;

    private int mSelectedPosition = -1;
    private int mSortBy           = 0;

    private static int NUMBER_OF_CORES = Runtime.getRuntime().availableProcessors();

    private final BlockingQueue<Runnable> mQueryQueue = new ArrayBlockingQueue<>(6);
    private       ThreadPoolExecutor      mExecutor   = new NotifyingThreadPoolExecutor(
            NUMBER_OF_CORES,
            NUMBER_OF_CORES,
            0L,
            TimeUnit.MILLISECONDS,
            mQueryQueue,
            this);

    Future<Integer> mGetNumProjectsOverdue;
    Future<Integer> mGetNumProjectsDueToday;
    Future<Integer> mGetNumProjectsDueThisWeek;
    Future<Integer> mGetNumProjectsDueThisMonth;
    Future<Integer> mGetNumProjectsCompleted;
    Future<Integer> mGetNumProjects;

    private Callable<Integer> mQueryProjectCallable = new Callable<Integer>() {
        @Override
        public Integer call() throws ParseException {
            return queryProjectsInBackground();
        }
    };

    private Callable<Integer> mGetNumProjectsDueTodayCallable = new Callable<Integer>() {
        @Override
        public Integer call() throws ParseException {
            return getProjectCountDueToday();
        }
    };

    private Callable<Integer> mGetNumProjectsDueThisWeekCallable = new Callable<Integer>() {
        @Override
        public Integer call() throws ParseException {
            return getProjectCountDueThisWeek();
        }
    };

    private Callable<Integer> mGetNumProjectDueThisMonthCallable = new Callable<Integer>() {
        @Override
        public Integer call() throws ParseException {
            return getProjectCountDueThisMonth();
        }
    };

    private Callable<Integer> mGetNumProjectsOverdueCallable = new Callable<Integer>() {
        @Override
        public Integer call() throws ParseException {
            return getProjectCountOverdue();
        }
    };

    private Callable<Integer> mGetNumProjectsCompletedCallable = new Callable<Integer>() {
        @Override
        public Integer call() throws ParseException {
            return getProjectCountCompleted();
        }
    };

    abstract ActionMode.Callback initCab();

    public ActionMode getActionMode() {
        return mActionMode;
    }

    protected void setActionMode(ActionMode actionMode) {
        mActionMode = actionMode;
    }

    @Override
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
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ItemTouchListener
                listener = new ItemTouchListener(mRecyclerView, mRefreshLayoutList, new ItemTouchListener.DismissCallbacks() {
            @Override
            public boolean canDismiss(int position) {
                return !mSectionedAdapter.isSectionHeaderPosition(position);
            }

            @Override
            public void onDismiss(RecyclerView listView, int[] reverseSortedPositions) {

            }
        });
        mRecyclerView.addOnItemTouchListener(listener);
        mRecyclerView.setOnScrollListener(listener.makeScrollListener());

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Toolbar toolbar = Utilities.getToolbar(getActivity());

        //action bar setup
        initActionBar(toolbar);

        //search view setup
        setUpSearchView(toolbar);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void initActionBar(Toolbar toolbar) {
        Utilities.enableToolbarTitle(getActivity(), false, TAG);
        toolbar.findViewById(R.id.actionbar_spinner).setVisibility(View.VISIBLE);
        toolbar.setBackgroundColor(getResources().getColor(R.color.theme_primary));
        toolbar.inflateMenu(R.menu.home_screen);
        initSpinner(toolbar);
    }

    private void initSpinner(Toolbar toolbar) {
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
                        navItemSelected(SIMPLE_VIEW_POS);
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
        spinner.setSelection(0);
    }

    private void navItemSelected(int position) {
        if (mSelectedPosition != position) {
            mSelectedPosition = position;
            queryProjects();
        }
        else {
            mSelectedPosition = position;
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

        mGetNumProjectsOverdue = mExecutor.submit(mGetNumProjectsOverdueCallable);
        mGetNumProjectsDueToday = mExecutor.submit(mGetNumProjectsDueTodayCallable);
        mGetNumProjectsDueThisWeek = mExecutor.submit(mGetNumProjectsDueThisWeekCallable);
        mGetNumProjectsDueThisMonth =
                mExecutor.submit(mGetNumProjectDueThisMonthCallable);
        mGetNumProjectsCompleted = mExecutor.submit(mGetNumProjectsCompletedCallable);
        mGetNumProjects = mExecutor.submit(mQueryProjectCallable);
    }

    @Override
    public void onAllTasksComplete() {
        try {
            int numProjectsOverdue = mGetNumProjectsOverdue.get();
            int numProjectsDueToday = mGetNumProjectsDueToday.get();
            int numProjectsDueThisWeek = mGetNumProjectsDueThisWeek.get();
            int numProjectsDueThisMonth = mGetNumProjectsDueThisMonth.get();
            int numProjectsCompleted = mGetNumProjectsCompleted.get();
            int numItems = mGetNumProjects.get();

            if (numItems == 0) {
                mRefreshLayoutList.setRefreshing(false);
                mLoadProjects.setVisibility(View.GONE);
                mNoProjects.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                return;
            }

            int numProjectsDueLater = numItems - numProjectsOverdue - numProjectsDueToday -
                                      numProjectsDueThisWeek - numProjectsDueThisMonth;

            List<SectionedRecycleViewAdapter.Section> sections = new ArrayList<>();

            //Sections
            if (numProjectsOverdue > 0) {
                sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                     "Overdue",
                                                                     numProjectsOverdue -
                                                                     numProjectsCompleted));
            }
            if (numProjectsDueToday > 0) {
                if (sections.isEmpty()) {
                    sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                         "Due Today",
                                                                         numProjectsDueToday));
                }
                else {
                    sections.add(new SectionedRecycleViewAdapter.Section(
                            numProjectsOverdue - numProjectsCompleted,
                            "Due Today",
                            numProjectsDueToday));
                }
            }
            if (numProjectsDueThisWeek > 0) {
                if (sections.isEmpty()) {
                    sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                         "Due This Week",
                                                                         numProjectsDueThisWeek));
                }
                else {
                    sections.add(new SectionedRecycleViewAdapter.Section(
                            numProjectsOverdue + numProjectsDueToday -
                            numProjectsCompleted,
                            "Due This Week",
                            numProjectsDueThisWeek));
                }
            }
            if (numProjectsDueThisMonth > 0) {
                if (sections.isEmpty()) {
                    sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                         "Due This Month",
                                                                         numProjectsDueThisMonth));
                }
                else {
                    sections.add(new SectionedRecycleViewAdapter.Section(
                            numProjectsOverdue + numProjectsDueToday +
                            numProjectsDueThisWeek - numProjectsCompleted,
                            "Due This Month",
                            numProjectsDueThisMonth));
                }
            }
            if (numProjectsDueLater > 0) {
                if (sections.isEmpty()) {
                    sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                         "Due Later",
                                                                         numProjectsDueLater));
                }
                else {
                    sections.add(new SectionedRecycleViewAdapter.Section(
                            numProjectsOverdue + numProjectsDueToday +
                            numProjectsDueThisWeek + numProjectsDueThisMonth -
                            numProjectsCompleted, "Due Later", numProjectsDueLater));
                }
            }
            if (numProjectsCompleted > 0) {
                if (sections.isEmpty()) {
                    sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                         "Completed",
                                                                         numProjectsCompleted));
                }
                else {
                    sections.add(new SectionedRecycleViewAdapter.Section(
                            mProjectAdapter.getItemCount() - numProjectsCompleted,
                            "Completed",
                            numProjectsCompleted));
                }
            }

            mSectionedAdapter.setSections(sections);
            mProjectAdapter.setSectionAdapter(mSectionedAdapter);

            mRecyclerView.setAdapter(mSectionedAdapter);
            mLoadProjects.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mNoProjects.setVisibility(View.GONE);
            mRefreshLayoutList.setRefreshing(false);
        }
        catch (InterruptedException | ExecutionException e) {
            // error
            Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
            mLoadProjects.setVisibility(View.GONE);
            mNoProjects.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        }
    }

    private int getProjectCountOverdue() throws ParseException {
        Calendar end = Calendar.getInstance();
        printCal(end, "overdue = ");

        ParseQuery<Project> projectQuery = initQuery();
        projectQuery.whereLessThan(Project.DUE_DATE_COL, end.getTime());
        int count = projectQuery.count();
        Log.d(TAG, "num projects overdue = " + count);

        return count;
    }

    private int getProjectCountDueToday() throws ParseException {
        Calendar startToday = Calendar.getInstance();
        Calendar endToday = Calendar.getInstance();
        setCalendarToEndOfDay(endToday);

        ParseQuery<Project> projectQuery = initQuery();
        projectQuery.whereGreaterThanOrEqualTo(Project.DUE_DATE_COL, startToday.getTime());
        projectQuery.whereLessThanOrEqualTo(Project.DUE_DATE_COL, endToday.getTime());
        int count = projectQuery.count();
        Log.d(TAG, "Num projects due today = " + count);

        return count;
    }

    private int getProjectCountDueThisWeek() throws ParseException {
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
        int count = projectQuery.count();
        Log.d(TAG, "Num projects due this week = " + count);

        return count;
    }

    private int getProjectCountDueThisMonth() throws ParseException {
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
        int count = projectQuery.count();
        Log.d(TAG, "Num projects due this month = " + count);

        return count;
    }

    private int getProjectCountCompleted() throws ParseException {
        ParseQuery<Project> projectQuery = initQuery();
        projectQuery.whereEqualTo(Project.STATUS_COL, true);
        int count = projectQuery.count();
        Log.d(TAG, "Num projects completed = " + count);
        return count;
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

    DateFormat dateFormat = DateFormat.getDateTimeInstance();

    private void printCal(Calendar cal, String init) {
        Log.d(TAG, init + " " + dateFormat.format(cal.getTime()));
    }

    private int queryProjectsInBackground() throws ParseException {
        ParseQuery<Project> projectQuery = initQuery();
        setSortMethod(projectQuery);
        List<Project> projectList = projectQuery.find();

        if (!projectList.isEmpty()) {
            mProjectAdapter = ProjectAdapterFactory.createProjectAdapter(mSelectedPosition,
                                                                         getActivity(),
                                                                         projectList,
                                                                         BaseProjectFragment.this);
            mSectionedAdapter =
                    new SectionedRecycleViewAdapter(BaseProjectFragment.this.getActivity(),
                                                    mProjectAdapter);

        }

        return projectList.size();
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
