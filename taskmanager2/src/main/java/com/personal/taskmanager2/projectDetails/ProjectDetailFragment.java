package com.personal.taskmanager2.projectDetails;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RefreshCallback;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.parseObjects.Project;
import com.personal.taskmanager2.utilities.Utilities;

public class ProjectDetailFragment extends Fragment
        implements View.OnClickListener {

    private static final String TAG = "ProjectDetailFragment";

    private Project mProject;

    private Typeface       mRobotoLight;
    private Typeface       mRoboto;
    private TextView       mProjectNameView;
    private TextView       mProjectDescriptionView;
    private TextView       mProjectOverview;
    private RelativeLayout mOverviewLayout;
    private Button         mViewFiles;
    private Button         mViewChat;
    private Button         mEditProject;
    private Button         mShareProject;

    public static ProjectDetailFragment newInstance(Project project) {

        Bundle args = new Bundle();
        args.putParcelable("project", project);

        ProjectDetailFragment frag = new ProjectDetailFragment();
        frag.setArguments(args);

        return frag;
    }

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        Bundle args = getArguments();
        mProject = args.getParcelable("project");

        ParseUser admin = mProject.getAdmin();
        if (admin == null) {
            Log.i(TAG, "admin is null");
            ParseQuery<ParseUser> adminQuery = ParseUser.getQuery();
            adminQuery.getInBackground(mProject.getAdminUid(),
                                       new GetCallback<ParseUser>() {
                                           @Override
                                           public void done(ParseUser parseUser,
                                                            ParseException e) {

                                               mProject.setAdministrator(
                                                       parseUser);

                                               Log.i(TAG,
                                                     "project admin = " +
                                                     mProject.getAdmin()
                                                             .getString("Name"));
                                           }
                                       });
        }

        mRobotoLight = Typeface.createFromAsset(getActivity().getAssets(),
                                                "Roboto-Light.ttf");
        mRoboto = Typeface.createFromAsset(getActivity().getAssets(),
                                           "Roboto-Regular.ttf");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_project_detail,
                                         container,
                                         false);

        mProjectNameView = (TextView) rootView.findViewById(R.id.project_name);
        mProjectNameView.setText(mProject.getName());
        mProjectNameView.setTypeface(mRobotoLight);

        TextView descriptionLabel =
                (TextView) rootView.findViewById(R.id.description_label);
        descriptionLabel.setTypeface(mRobotoLight);

        mProjectDescriptionView =
                (TextView) rootView.findViewById(R.id.project_description);
        mProjectDescriptionView.setText(mProject.getDescription());
        mProjectDescriptionView.setTypeface(mRoboto);

        TextView overviewLabel =
                (TextView) rootView.findViewById(R.id.project_completion_overview_label);
        overviewLabel.setTypeface(mRobotoLight);

        mProjectOverview =
                (TextView) rootView.findViewById(R.id.project_overview);
        mProjectOverview.setTypeface(mRoboto);
        String overviewText = "Administrator: " + mProject.getAdminName();
        overviewText +=
                "\nCompleted " + mProject.getNumCompletedTasks() + " out of " +
                mProject.getNumTotalTask() + " tasks";
        overviewText += "\nUsers: ";
        mProjectOverview.setText(overviewText);
        Utilities.appendUsersToTextView(mProjectOverview, mProject, TAG);

        TextView viewMore = (TextView) rootView.findViewById(R.id.view_more);
        viewMore.setTypeface(mRoboto);

        mOverviewLayout =
                (RelativeLayout) rootView.findViewById(R.id.content_overview);
        mOverviewLayout.setOnClickListener(this);

        TextView mTasksLabel =
                (TextView) rootView.findViewById(R.id.project_tasks);
        mTasksLabel.setTypeface(mRobotoLight);

        mViewFiles = (Button) rootView.findViewById(R.id.view_files);
        mViewChat = (Button) rootView.findViewById(R.id.view_chat);
        mEditProject = (Button) rootView.findViewById(R.id.edit_project);
        mShareProject = (Button) rootView.findViewById(R.id.share_project);
        mViewFiles.setOnClickListener(this);
        mViewChat.setOnClickListener(this);
        mEditProject.setOnClickListener(this);
        mShareProject.setOnClickListener(this);
        mViewFiles.setTypeface(mRobotoLight);
        mViewChat.setTypeface(mRobotoLight);
        mEditProject.setTypeface(mRobotoLight);
        mShareProject.setTypeface(mRobotoLight);

        setUpActionBar();

        return rootView;
    }

    private void setUpActionBar() {

        setHasOptionsMenu(true);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(mProject.getName());
        actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(
                mProject.getColorRsrc())));
        actionBar.setDisplayShowHomeEnabled(false);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.content_overview:
                getFragmentManager().beginTransaction()
                                    .addToBackStack(TAG)
                                    .replace(R.id.container,
                                             ProjectOverviewFragment.newInstance(
                                                     mProject))
                                    .commit();

                break;

            case R.id.view_files:
                break;

            case R.id.view_chat:
                break;

            case R.id.edit_project:
                Utilities.safeEditProject(mProject,
                                          getFragmentManager(),
                                          getActivity());
                break;

            case R.id.share_project:
                Utilities.shareProject(mProject, getActivity());
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.project_detail, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
            case R.id.action_create:
                Toast.makeText(getActivity(), "Create Task", Toast.LENGTH_SHORT)
                     .show();
                return true;
            case R.id.action_refresh:
                mProject.refreshInBackground(new RefreshCallback() {
                    @Override
                    public void done(ParseObject parseObject,
                                     ParseException e) {

                        Log.i(TAG, "Project Refreshed");
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
