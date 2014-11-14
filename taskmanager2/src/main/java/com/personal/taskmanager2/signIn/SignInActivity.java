package com.personal.taskmanager2.signIn;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.parse.ParseUser;
import com.personal.taskmanager2.R;

public class SignInActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_sign_in);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.theme_primary));
        setSupportActionBar(toolbar);

        ParseUser curr = ParseUser.getCurrentUser();
        if (curr != null) {
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
            ParseUser.logOut();
        }
    }

}
