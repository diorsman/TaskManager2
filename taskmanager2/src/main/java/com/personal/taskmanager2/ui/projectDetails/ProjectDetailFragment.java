package com.personal.taskmanager2.ui.projectDetails;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.utilities.Utilities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class ProjectDetailFragment extends Fragment {

    private static final String TAG = "ProjectDetailFragment";

    private Project mProject;

    private TextView mProjectDescriptionView;
    private TextView mProjectOverview;

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

                                               mProject.setAdministrator(parseUser);
                                           }
                                       });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_project_detail, container, false);

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

        setUpActionBar();

        return rootView;
    }

    private void setUpActionBar() {

        setHasOptionsMenu(true);
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        toolbar.getMenu().clear();
        toolbar.setTitle(toolbar.getTitle() + " Details");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
            default:
                return onOptionsItemSelected(item);
        }
    }
}
