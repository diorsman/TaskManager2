package com.personal.taskmanager2.projectDetails;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Typeface;
import android.os.Bundle;
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
import com.personal.taskmanager2.parseObjects.Project;
import com.personal.taskmanager2.utilities.Utilities;

public class ProjectOverviewFragment extends Fragment {

    private static final String TAG = "ProjectOverviewFragment";

    private Project mProject;

    private Typeface mRobotoLight;
    private Typeface mRoboto;
    private TextView mProjectDescriptionView;
    private TextView mProjectOverview;

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
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_project_overview,
                                         container,
                                         false);

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

        setUpActionBar();

        return rootView;
    }

    private void setUpActionBar() {

        setHasOptionsMenu(true);

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setTitle(mProject.getName() + " Overview");
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
