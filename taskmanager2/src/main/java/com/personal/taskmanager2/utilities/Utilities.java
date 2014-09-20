package com.personal.taskmanager2.utilities;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseUser;
import com.personal.taskmanager2.R;
import com.personal.taskmanager2.adapters.ProjectAdapter.BaseProjectAdapter;
import com.personal.taskmanager2.adapters.ProjectAdapter.DetailProjectAdapter;
import com.personal.taskmanager2.adapters.ProjectAdapter.SimpleProjectAdapter;
import com.personal.taskmanager2.homescreen.EditProjectFragment;
import com.personal.taskmanager2.parseObjects.Project;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;
import java.util.concurrent.Executors;


public class Utilities {

    public static void shareProject(Project project, Context context) {

        String uid = project.getUid();
        String password = project.getPassword();
        String projectName = project.getName();
        String body =
                "Hello,\n\n" + ParseUser.getCurrentUser().get("Name")
                + " has invited you to join their project, "
                + projectName + "."
                +
                "\nYou can join them on the TaskManager app with the following information."
                + "\nUnique Identifier: " + uid
                + "\nPassword: " + password
                + "\n\nSincerely,\nThe Task Manager App Team\n";
        Intent
                emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", "", null));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT,
                             "You have been invited to join project " +
                             projectName);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(emailIntent,
                                                   "Share project..."));
    }

    public static String getValFromEditText(MyEditText editText)
            throws EmptyEditTextException {

        String uid = editText.getText().toString();
        if (uid.isEmpty()) {
            throw new EmptyEditTextException(editText);
        }
        return uid;
    }

    public static void refreshFragment(FragmentManager fragmentManager) {

        FragmentTransaction ft = fragmentManager.beginTransaction();
        Fragment currentFrag = fragmentManager.findFragmentById(R.id.container);
        ft.detach(currentFrag);
        ft.attach(currentFrag);
        ft.commit();
    }

    public static void appendUsersToTextView(final TextView textView,
                                             final Project project,
                                             final String TAG) {

        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {

                String users = (String) msg.obj;
                String cur = textView.getText().toString();
                cur += users;
                textView.setText(cur);
            }
        };

        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    StringBuilder builder = new StringBuilder();
                    JSONArray users = project.getUserName();
                    int length = users.length();
                    for (int i = 0; i < length; i++) {
                        builder.append(users.getString(i));
                        if (i != length - 1) {
                            builder.append(", ");
                        }
                    }
                    Message msg = handler.obtainMessage(1, builder.toString());
                    handler.sendMessage(msg);
                }
                catch (JSONException e) {
                    Log.e(TAG, "JSONException", e);
                }
            }
        };

        Executors.newSingleThreadExecutor().execute(runnable);
    }

    public static void safeEditProject(Project project,
                                       FragmentManager fragmentManager,
                                       Context context) {

        if (ParseUser.getCurrentUser()
                     .getObjectId()
                     .equals(project.getAdmin().getObjectId())) {
            fragmentManager.beginTransaction()
                           .addToBackStack(null)
                           .replace(R.id.container,
                                    EditProjectFragment.newInstance(
                                            project))
                           .commit();
        }
        else {
            Toast.makeText(context,
                           "Only administrator can edit the project",
                           Toast.LENGTH_LONG).show();
        }
    }

    public static BaseProjectAdapter createProjectAdapter(int spinPos,
                                            Context context,
                                            List<Project> projects,
                                            FragmentManager fm,
                                            ListView listView) {

        if (spinPos == 0) {
            return new SimpleProjectAdapter(context,
                                            projects,
                                            fm,
                                            listView);
        }
        else {
            return new DetailProjectAdapter(context,
                                            projects,
                                            fm,
                                            listView);
        }
    }

    public static final String description =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer eget rutrum orci. Aenean neque justo, tincidunt sed felis nec, mollis aliquet dolor. Etiam at purus ornare, commodo felis a, cursus odio. Fusce venenatis nisi sed nisl consectetur, sed condimentum felis vestibulum. Aliquam bibendum, libero eu congue viverra, nibh velit molestie nibh, ac ullamcorper lectus nisi id sapien. Morbi at malesuada arcu. Nam ac sollicitudin tellus, nec pulvinar metus. Sed tristique sit amet ipsum nec eleifend.\n" +
            "\n" +
            "Curabitur suscipit ac sem vel cursus. Integer nec laoreet nunc. Phasellus congue pharetra est quis lobortis. Morbi id felis dolor. Suspendisse sollicitudin sodales urna, non rutrum dui imperdiet vitae. Mauris sed nisl tortor. Donec ut sem nec sapien facilisis lobortis sed dictum justo. Fusce volutpat, arcu at lobortis tincidunt, mauris eros auctor diam, sed placerat nulla mi ultrices felis. Sed dui erat, volutpat eu malesuada quis, efficitur id massa. Morbi nulla tortor, cursus nec vehicula quis, gravida ac lectus. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae;";
}
