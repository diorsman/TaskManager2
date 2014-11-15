package com.personal.taskmanager2.ui.projectDetails;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.utilities.Utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ProjectOverviewFragment extends Fragment
        implements View.OnClickListener {

    private static final String TAG = "ProjectOverviewFragment";

    private Project mProject;

    private TextView       mProjectNameView;
    private TextView       mProjectDescriptionView;
    private TextView       mProjectOverview;
    private RelativeLayout mOverviewLayout;
    private Button         mViewFiles;
    private Button         mViewChat;
    private Button         mEditProject;
    private Button         mShareProject;
    private Button         mArchiveProject;
    private Button         mDeleteProject;

    public static ProjectOverviewFragment newInstance(Project project) {

        Bundle args = new Bundle();
        args.putParcelable("project", project);

        ProjectOverviewFragment frag = new ProjectOverviewFragment();
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
            ParseQuery<ParseUser> adminQuery = ParseUser.getQuery();
            adminQuery.getInBackground(mProject.getAdminUid(),
                                       new GetCallback<ParseUser>() {
                                           @Override
                                           public void done(ParseUser parseUser,
                                                            ParseException e) {

                                               mProject.setAdministrator(parseUser);
                                           }
                                       });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_project_overview, container, false);

        mProjectNameView = (TextView) rootView.findViewById(R.id.project_name);
        mProjectNameView.setText(mProject.getName());

        mProjectDescriptionView = (TextView) rootView.findViewById(R.id.project_description);
        mProjectDescriptionView.setText(mProject.getDescription());

        mProjectOverview = (TextView) rootView.findViewById(R.id.project_overview);
        DateFormat format = new SimpleDateFormat("'Due' 'on' MMMM dd, yyyy 'at' hh:mm a");
        String overviewText = format.format(mProject.getDueDate());
        overviewText += "\nAdministrator: " + mProject.getAdminName();
        overviewText += "\nCompleted " + mProject.getNumCompletedTasks() + " out of " +
                        mProject.getNumTotalTask() + " tasks";
        overviewText += "\nUsers: ";
        mProjectOverview.setText(overviewText);
        Utilities.appendUsersToTextView(mProjectOverview, mProject, TAG);

        mOverviewLayout = (RelativeLayout) rootView.findViewById(R.id.content_overview);
        mOverviewLayout.setOnClickListener(this);

        mViewFiles = (Button) rootView.findViewById(R.id.view_files);
        mViewChat = (Button) rootView.findViewById(R.id.view_chat);
        mEditProject = (Button) rootView.findViewById(R.id.edit_project);
        mShareProject = (Button) rootView.findViewById(R.id.share_project);
        mArchiveProject = (Button) rootView.findViewById(R.id.archive);
        mDeleteProject = (Button) rootView.findViewById(R.id.delete);
        mViewFiles.setOnClickListener(this);
        mViewChat.setOnClickListener(this);
        mEditProject.setOnClickListener(this);
        mShareProject.setOnClickListener(this);
        mArchiveProject.setOnClickListener(this);
        mDeleteProject.setOnClickListener(this);

        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.content_overview:
                getFragmentManager().beginTransaction()
                                    .addToBackStack(TAG)
                                    .replace(R.id.container,
                                             ProjectDetailFragment.newInstance(mProject))
                                    .commit();

                break;

            case R.id.view_files:
                break;

            case R.id.view_chat:
                break;

            case R.id.edit_project:
                mProject.safeEdit(getFragmentManager(), getActivity());
                break;

            case R.id.share_project:
                mProject.share(getActivity());
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Toolbar toolbar = Utilities.getToolbar(getActivity());
        toolbar.inflateMenu(R.menu.project_overview);
        toolbar.setTitle(mProject.getName());

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                return true;
            case R.id.action_create:
                Toast.makeText(getActivity(), "Create Task", Toast.LENGTH_SHORT).show();
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
