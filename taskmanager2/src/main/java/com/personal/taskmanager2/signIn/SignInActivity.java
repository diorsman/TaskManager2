package com.personal.taskmanager2.signIn;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.parse.ParseUser;
import com.personal.taskmanager2.R;

public class SignInActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_sign_in);

        ParseUser curr = ParseUser.getCurrentUser();
        if (curr != null) {
            Toast.makeText(this,
                           "Logged Out",
                           Toast.LENGTH_SHORT).show();
            ParseUser.logOut();
        }

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                                .add(R.id.container, new SignInFragment())
                                .commit();
        }
    }

}
