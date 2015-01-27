package com.personal.taskmanager2.ui.homescreen.ProjectsHomeScreen;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
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
import com.parse.ParseUser;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.adapters.ActionBarSpinner;
import com.personal.taskmanager2.adapters.ProjectAdapter.BaseProjectAdapter;
import com.personal.taskmanager2.adapters.ProjectAdapter.ProjectAdapterFactory;
import com.personal.taskmanager2.adapters.ProjectAdapter.SectionedRecycleViewAdapter;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.ui.ItemTouchListener;
import com.personal.taskmanager2.ui.homescreen.SearchFragment;
import com.personal.taskmanager2.utilities.ProjectQueryHelper;
import com.personal.taskmanager2.utilities.Utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class BaseProjectFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener,
                   BaseProjectAdapter.OnItemClickListener,
                   ProjectQueryHelper.ProjectQueryCallback {

    private static final String TAG = "BaseProjectFragment";

    private static final int SIMPLE_VIEW_POS = 0;
    private static final int DETAIL_VIEW_POS = 1;

    private static final int SORT_BY_DUE_DATE    = 0;
    private static final int SORT_BY_NAME        = 1;
    private static final int SORT_BY_DESCRIPTION = 2;
    private static final int SORT_BY_COLOR       = 3;

    private String mToolbarTitle;

    private   SwipeRefreshLayout mRefreshLayout;
    protected RecyclerView       mRecyclerView;
    protected TextView           mLoadProjects;
    private   TextView           mNoProjects;

    protected BaseProjectAdapter          mProjectAdapter;
    protected SectionedRecycleViewAdapter mSectionedAdapter;
    protected ActionMode                  mActionMode;
    protected ActionMode.Callback mActionModeCallback = initCab();

    private boolean mArchive;
    private boolean mTrash;

    private int mSelectedPosition = -1;
    private int mSortBy           = 0;

    private ProjectQueryHelper mProjectQueryHelper = new ProjectQueryHelper();

    abstract ActionMode.Callback initCab();

    public ActionMode getActionMode() {
        return mActionMode;
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
        int layoutResourceId = args.getInt("resourceId");
        mArchive = args.getBoolean("archive");
        mTrash = args.getBoolean("trash");
        mToolbarTitle = args.getString("title");

        View rootView = inflater.inflate(layoutResourceId, container, false);

        mRefreshLayout =
                (SwipeRefreshLayout) rootView.findViewById(R.id.refresh_layout_list_view);
        mLoadProjects = (TextView) rootView.findViewById(R.id.myProjectLoad);
        mNoProjects = (TextView) rootView.findViewById(R.id.no_projects_text);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.project_recycler_view);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ItemTouchListener
                listener = new ItemTouchListener(mRecyclerView,
                                                 mRefreshLayout,
                                                 new ItemTouchListener.DismissCallbacks() {
                                                     @Override
                                                     public boolean canDismiss(int position) {
                                                         return !mSectionedAdapter.isSectionHeaderPosition(
                                                                 position);
                                                     }

                                                     @Override
                                                     public void onDismiss(RecyclerView listView,
                                                                           int[] reverseSortedPositions) {

                                                     }
                                                 });
        mRecyclerView.addOnItemTouchListener(listener);
        mRecyclerView.setOnScrollListener(listener.makeScrollListener());

        // refresh layout setup
        mRefreshLayout.setOnRefreshListener(this);
        mRefreshLayout.setColorSchemeResources(android.R.color.holo_purple,
                                               android.R.color.holo_green_light,
                                               android.R.color.holo_orange_light,
                                               android.R.color.holo_red_light);

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

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
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
        mRefreshLayout.setRefreshing(true);
        mProjectQueryHelper.initMainProjectQuery(this, mArchive, mTrash);
    }

    @Override
    public void onProjectsRetrieved(List<Project> projects,
                                    int numProjectsOverdue,
                                    int numProjectsDueToday,
                                    int numProjectsDueThisWeek,
                                    int numProjectsDueThisMonth,
                                    int numProjectsDueLater,
                                    int numProjectsCompleted) {
        List<SectionedRecycleViewAdapter.Section> sections = new ArrayList<>();

        //Sections
        if (numProjectsOverdue > 0) {
            sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                 "Overdue",
                                                                 numProjectsOverdue));
        }
        if (numProjectsDueToday > 0) {
            if (sections.isEmpty()) {
                sections.add(new SectionedRecycleViewAdapter.Section(0,
                                                                     "Due Today",
                                                                     numProjectsDueToday));
            }
            else {
                sections.add(new SectionedRecycleViewAdapter.Section(
                        numProjectsOverdue,
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
                        numProjectsOverdue + numProjectsDueToday,
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
                        numProjectsOverdue + numProjectsDueToday + numProjectsDueThisWeek,
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
                        numProjectsOverdue + numProjectsDueToday + numProjectsDueThisWeek +
                        numProjectsDueThisMonth, "Due Later", numProjectsDueLater));
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
                        projects.size() - numProjectsCompleted,
                        "Completed",
                        numProjectsCompleted));
            }
        }
        // add footer view
        sections.add(new SectionedRecycleViewAdapter.Section(projects.size(),
                                                             "",
                                                             0));

        mProjectAdapter = ProjectAdapterFactory.createProjectAdapter(mSelectedPosition,
                                                                     getActivity(),
                                                                     projects,
                                                                     this);
        mSectionedAdapter = new SectionedRecycleViewAdapter(getActivity(), mProjectAdapter);
        mSectionedAdapter.setSections(sections);
        mProjectAdapter.setSectionAdapter(mSectionedAdapter);

        mRecyclerView.setAdapter(mSectionedAdapter);
        mLoadProjects.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mNoProjects.setVisibility(View.GONE);
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onNoProjectsFound() {
        mRefreshLayout.setRefreshing(false);
        mLoadProjects.setVisibility(View.GONE);
        mNoProjects.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }

    @Override
    public void onProjectQueryError(Exception e) {
        mRefreshLayout.setRefreshing(false);
        Log.e(TAG, "Error Occured Querying Projects", e);
        Toast.makeText(getActivity(),
                       "Error occured querying projects. Please try again.",
                       Toast.LENGTH_LONG).show();
        mLoadProjects.setVisibility(View.GONE);
        mNoProjects.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
    }
}
