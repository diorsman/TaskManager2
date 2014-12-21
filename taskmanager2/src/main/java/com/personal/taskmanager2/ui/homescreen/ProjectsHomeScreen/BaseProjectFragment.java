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

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.adapters.ActionBarSpinner;
import com.personal.taskmanager2.adapters.ProjectAdapter.BaseProjectAdapter;
import com.personal.taskmanager2.adapters.ProjectAdapter.ProjectAdapterFactory;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.ui.DividerItemDecoration;
import com.personal.taskmanager2.ui.homescreen.SearchFragment;
import com.personal.taskmanager2.ui.projectDetails.ProjectDetailActivity;
import com.personal.taskmanager2.utilities.Utilities;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public abstract class BaseProjectFragment extends Fragment
        implements SwipeRefreshLayout.OnRefreshListener, BaseProjectAdapter.OnItemClickListener {

    private static final String TAG = "BaseProjectFragment";

    private static final int LIST_VIEW_POS   = 0;
    private static final int DETAIL_VIEW_POS = 1;

    private static final int SORT_BY_DUE_DATE    = 0;
    private static final int SORT_BY_NAME        = 1;
    private static final int SORT_BY_DESCRIPTION = 2;
    private static final int SORT_BY_COLOR       = 3;

    private String mToolbarTitle;

    private SwipeRefreshLayout mRefreshLayoutList;
    private RecyclerView       mRecyclerView;
    private TextView           mLoadProjects;
    private TextView           mNoProjects;

    private int mLayoutResourceId;

    private BaseProjectAdapter mAdapter;
    private Context            mContext;

    private boolean mQueriedList   = false;
    private boolean mQueriedDetail = false;

    private boolean mArchive;
    private boolean mTrash;

    private int mSelectedPosition = 0;
    private int mSortBy           = 0;

    private ActionMode mActionMode;

    public ActionMode getActionMode() {
        return mActionMode;
    }

    private ActionMode.Callback mActionModeCallBack = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            menu.clear();
            MenuInflater inflater = actionMode.getMenuInflater();
            inflater.inflate(R.menu.project_context_menu_single, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            menu.clear();
            MenuInflater inflater = actionMode.getMenuInflater();
            if (mAdapter.getNumSelected() > 1) {
                inflater.inflate(R.menu.project_context_menu_multiple, menu);
            }
            else {
                inflater.inflate(R.menu.project_context_menu_single, menu);
            }
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            LinearLayoutManager llm = (LinearLayoutManager) mRecyclerView.getLayoutManager();
            int firstVisPos = llm.findFirstVisibleItemPosition();
            int lastVisPos = llm.findLastVisibleItemPosition();
            mAdapter.clearSelection(firstVisPos, lastVisPos);
            mActionMode = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

        getFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                if (getFragmentManager().getBackStackEntryCount() == 0) {
                    if (mAdapter != null) {
                        mAdapter.notifyDataSetChanged();
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
    public void onItemClick(View v) {
        int position = mRecyclerView.getChildPosition(v);

        if (mAdapter.isItemSelected(position)) {
            unSelectItem(position);
        }
        else {
            Intent intent =
                    new Intent(BaseProjectFragment.this.getActivity(),
                               ProjectDetailActivity.class);
            intent.putExtra("project",
                            mAdapter.getItem(position));
            startActivity(intent);
        }
    }

    @Override
    public void onItemLongClick(View v) {
        int position = mRecyclerView.getChildPosition(v);
        toggleSelection(position);
    }

    @Override
    public void onAvatarClick(View avatar,int position) {
        toggleSelection(position);
    }

    private void toggleSelection(int position) {
        if (mAdapter.isItemSelected(position)) {
            unSelectItem(position);
        }
        else {
            mAdapter.selectItem(position);
            if (mActionMode == null) {
                mActionMode = Utilities.getToolbar(getActivity()).startActionMode(
                        mActionModeCallBack);
            }
            else {
                mActionMode.invalidate();
            }
        }
    }

    private void unSelectItem(int position) {
        mAdapter.unselectedItem(position);
        if (mAdapter.getNumSelected() == 0) {
            if (mActionMode != null) {
                mActionMode.finish();
            }
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
                    for (int i = 1; i < 301; ++i) {
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

    private void queryProjects() {
        ParseQuery<Project> projectQuery = initQuery();
        setSortMethod(projectQuery);

        mNoProjects.setVisibility(View.GONE);
        mRefreshLayoutList.setRefreshing(true);

        projectQuery.findInBackground(new FindCallback<Project>() {
            @Override
            public void done(List<Project> projectList, ParseException e) {
                mRefreshLayoutList.setRefreshing(false);
                mLoadProjects.setVisibility(View.GONE);

                if (e == null) {
                    if (!projectList.isEmpty()) {
                        mAdapter = ProjectAdapterFactory.createProjectAdapter(mSelectedPosition,
                                                                              getActivity(),
                                                                              projectList,
                                                                              BaseProjectFragment.this);
                        mRecyclerView.setAdapter(mAdapter);
                        mNoProjects.setVisibility(View.GONE);
                    }
                    else {
                        mNoProjects.setVisibility(View.VISIBLE);
                    }
                }
                else {
                    Log.e(TAG,
                          "Error code = " + Integer.toString(e.getCode()));
                    Log.e(TAG, "Error message " + e.getMessage());
                    Log.e(TAG, "Error occurred", e);
                }
            }
        });
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
    }
}
