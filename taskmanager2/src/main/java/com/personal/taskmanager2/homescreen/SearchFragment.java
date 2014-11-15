package com.personal.taskmanager2.homescreen;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.adapters.ProjectAdapter.BaseProjectAdapter;
import com.personal.taskmanager2.adapters.ProjectAdapter.ProjectAdapterFactory;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.projectDetails.ProjectDetailActivity;
import com.personal.taskmanager2.utilities.ListViewAnimationHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class SearchFragment extends Fragment
        implements AdapterView.OnItemClickListener {

    private static final String TAG = "SearchFragment";

    private ListView           mListView;
    private TextView           mNoResults;
    private ProgressBar        mLoading;
    private BaseProjectAdapter mProjectAdapter;

    private String mQuery;

    private ListViewAnimationHelper<Project> mAnimHelper;

    public static SearchFragment newInstance(String query) {

        Bundle args = new Bundle();
        args.putString("query", query);

        SearchFragment frag = new SearchFragment();
        frag.setArguments(args);

        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search, container, false);

        mListView = (ListView) rootView.findViewById(R.id.project_list_view);
        mListView.setOnItemClickListener(this);
        mNoResults = (TextView) rootView.findViewById(R.id.no_projects_text);
        mLoading = (ProgressBar) rootView.findViewById(R.id.load_results);

        mQuery = getArguments().getString("query");

        setHasOptionsMenu(true);
        searchForProjects(mQuery);

        mAnimHelper =
                new ListViewAnimationHelper<>(android.R.anim.slide_out_right,
                                              350,
                                              getActivity(),
                                              new ListViewAnimationHelper.ListViewAnimationListener() {
                                                  @Override
                                                  public void onAnimationStart() {

                                                  }

                                                  @Override
                                                  public void onAnimationEnd() {

                                                  }

                                                  @Override
                                                  public void onAnimationRepeat() {

                                                  }
                                              });

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);

        setUpActionBar(toolbar);
        toolbar.inflateMenu(R.menu.search_screen);
        setUpSearchView(toolbar);

        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setUpActionBar(Toolbar toolbar) {
        ((ActionBarActivity) getActivity()).getSupportActionBar()
                                           .setDisplayShowTitleEnabled(true);
        toolbar.setTitle("Search: " + mQuery);
        toolbar.findViewById(R.id.actionbar_spinner).setVisibility(View.GONE);
    }

    private void setUpSearchView(Toolbar toolbar) {
        //search view setup
        final MenuItem searchItem = toolbar.getMenu().findItem(R.id.action_search);
        final android.support.v7.widget.SearchView
                searchView = (android.support.v7.widget.SearchView) MenuItemCompat.getActionView(
                searchItem);

        searchView.setOnQueryTextFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(
                            View v,
                            boolean hasFocus) {

                        if (!hasFocus) {
                            MenuItemCompat.collapseActionView(searchItem);
                            getActivity().invalidateOptionsMenu();
                        }
                    }
                });

        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                MenuItemCompat.collapseActionView(searchItem);
                searchForProjects(query);
                mQuery = query;
                getActivity().invalidateOptionsMenu();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });
    }

    private void searchForProjects(final String query) {

        mListView.setEmptyView(mLoading);
        mNoResults.setVisibility(View.GONE);

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                List<Project> projects = (List<Project>) msg.obj;
                mProjectAdapter = ProjectAdapterFactory.createProjectAdapter(
                        ProjectAdapterFactory.SIMPLE_ADAPTER,
                        getActivity(),
                        projects,
                        mAnimHelper);
                mListView.setAdapter(mProjectAdapter);
                mListView.setEmptyView(mNoResults);
                mLoading.setVisibility(View.GONE);
                mAnimHelper.setListView(mListView);
                mAnimHelper.setAdapter(mProjectAdapter);
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                searchForProjectsInBackground(query.toLowerCase(), handler);
            }
        };

        Executors.newSingleThreadExecutor().execute(runnable);
    }

    private void searchForProjectsInBackground(String query, Handler handler) {

        try {
            ParseQuery<Project> projectAdmin = ParseQuery.getQuery(Project.class);
            projectAdmin.whereEqualTo(Project.ADMIN_COL, ParseUser.getCurrentUser());

            ParseQuery<Project> projectUser = ParseQuery.getQuery(Project.class);
            projectUser.whereEqualTo(Project.USERS_ID_COL,
                                     ParseUser.getCurrentUser().getObjectId());

            List<ParseQuery<Project>> queries = new ArrayList<>();
            queries.add(projectAdmin);
            queries.add(projectUser);

            ParseQuery<Project> projectQuery = ParseQuery.or(queries);

            ParseQuery<Project> nameQuery = ParseQuery.getQuery(Project.class);
            nameQuery.whereEqualTo("NameSearch", query);

            ParseQuery<Project> descriptionQuery =
                    ParseQuery.getQuery(Project.class);
            descriptionQuery.whereEqualTo("DescriptionSearch", query);

            ParseQuery<Project> colorQuery = ParseQuery.getQuery(Project.class);
            colorQuery.whereEqualTo("ColorSearch", query);

            ParseQuery<Project> adminQuery = ParseQuery.getQuery(Project.class);
            adminQuery.whereEqualTo("AdminSearch", query);

            ParseQuery<Project> userQuery = ParseQuery.getQuery(Project.class);
            userQuery.whereEqualTo("UserSearch", query);

            List<ParseQuery<Project>> searchQueryList = new ArrayList<>();
            searchQueryList.add(nameQuery);
            searchQueryList.add(descriptionQuery);
            searchQueryList.add(colorQuery);
            searchQueryList.add(adminQuery);
            searchQueryList.add(userQuery);

            ParseQuery<Project> searchQuery = ParseQuery.or(searchQueryList);
            searchQuery.whereMatchesKeyInQuery("objectId", "objectId", projectQuery);
            searchQuery.orderByAscending(Project.DUE_DATE_COL);

            List<Project> projects = searchQuery.find();
            Message msg = handler.obtainMessage(1, projects);
            handler.sendMessage(msg);
        }
        catch (ParseException e) {
            Log.e(TAG, Integer.toString(e.getCode()), e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
