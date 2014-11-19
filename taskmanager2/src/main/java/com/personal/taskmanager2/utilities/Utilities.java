package com.personal.taskmanager2.utilities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.model.parse.Project;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.concurrent.Executors;


public class Utilities {

    public static String getValFromEditText(EditText editText)
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

    public static final String description =
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer eget rutrum orci. Aenean neque justo, tincidunt sed felis nec, mollis aliquet dolor. Etiam at purus ornare, commodo felis a, cursus odio. Fusce venenatis nisi sed nisl consectetur, sed condimentum felis vestibulum. Aliquam bibendum, libero eu congue viverra, nibh velit molestie nibh, ac ullamcorper lectus nisi id sapien. Morbi at malesuada arcu. Nam ac sollicitudin tellus, nec pulvinar metus. Sed tristique sit amet ipsum nec eleifend.\n" +
            "\n" +
            "Curabitur suscipit ac sem vel cursus. Integer nec laoreet nunc. Phasellus congue pharetra est quis lobortis. Morbi id felis dolor. Suspendisse sollicitudin sodales urna, non rutrum dui imperdiet vitae. Mauris sed nisl tortor. Donec ut sem nec sapien facilisis lobortis sed dictum justo. Fusce volutpat, arcu at lobortis tincidunt, mauris eros auctor diam, sed placerat nulla mi ultrices felis. Sed dui erat, volutpat eu malesuada quis, efficitur id massa. Morbi nulla tortor, cursus nec vehicula quis, gravida ac lectus. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae;";

    public static Toolbar getToolbar(Activity activity) {
        return (Toolbar) activity.findViewById(R.id.toolbar);
    }

    public static void enableToolbarTitle(Activity activity, boolean enabled, String TAG) {
        ActionBarActivity parent = (ActionBarActivity) activity;
        if (parent == null) {
            Log.e(TAG, "activity is null");
            return;
        }
        parent.getSupportActionBar().setDisplayShowTitleEnabled(enabled);
    }

    public static int getThemeFromProjectColor(int color) {
        switch (color) {
            case R.color.project_blue:
                return R.style.theme_blue;
            case R.color.project_red:
                return R.style.theme_red;
            case R.color.project_green:
                return R.style.theme_green;
            case R.color.project_orange:
                return R.style.theme_orange;
            case R.color.project_purple:
                return R.style.theme_purple;
            case R.color.project_yellow:
                return R.style.theme_yellow;
            default:
                throw new IllegalArgumentException("Illegal Project Color");
        }
    }
}
