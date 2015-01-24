package com.personal.taskmanager2.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;

import com.personal.taskmanager2.R;
import com.personal.taskmanager2.model.parse.Project;
import com.personal.taskmanager2.utilities.Utilities;

/**
 * Created by Omid Ghomeshi on 1/21/15.
 */
public class EditProjectActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Project project = getIntent().getExtras().getParcelable("project");
        int colorRsrc = Utilities.getColorRsrcFromColor(project.getColor());
        setTheme(Utilities.getThemeFromProjectColor(colorRsrc));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_project);

        Toolbar toolbar = Utilities.getToolbar(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                                .add(R.id.container,
                                     EditProjectFragment.newInstance(project))
                                .commit();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getEnterTransition().addListener(new TransitionAdapter() {
                @Override
                public void onTransitionEnd(Transition transition) {
                    EditProjectFragment fragment =
                            (EditProjectFragment) getFragmentManager().findFragmentById(R.id.container);
                    fragment.show();
                    getWindow().getEnterTransition().removeListener(this);
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        EditProjectFragment fragment =
                (EditProjectFragment) getFragmentManager().findFragmentById(R.id.container);
        fragment.hide();
        ActivityCompat.finishAfterTransition(this);
    }
}
