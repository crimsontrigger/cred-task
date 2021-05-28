package com.example.test;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.fragment.NavHostFragment;
import androidx.transition.Slide;
import androidx.transition.Transition;
import androidx.transition.TransitionManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.example.test.CircularSeekBar.OnProgressChangeListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CircularSeekBar circularSeek = (CircularSeekBar) findViewById(R.id.seekbar);

        circularSeek.setOnProgressChangeListener(new CircularSeekBar.OnProgressChangeListener() {

            @Override
            public void onProgressChanged(CircularSeekBar seekBar, int progress, boolean isUser) {
                String string_progress = Integer.toString(progress);
                TextView progress_text = (TextView) findViewById(R.id.txtProgress);
                progress_text.setText(string_progress);
            }

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {

            }
        });


        Button mainButton = (Button) findViewById(R.id.button_first);
        View firstFragement = (View) findViewById(R.id.nav_host_fragment);
        View f1collapsedView = firstFragement.findViewById(R.id.collapsedview_first);
        View f1uncollapsedView = firstFragement.findViewById(R.id.uncollapsedview_first);
        View f2collapsedView = firstFragement.findViewById(R.id.collapsedview_second);
        View f2uncollapsedView = firstFragement.findViewById(R.id.uncollapsedview_second);
        View f3uncollapsedView = firstFragement.findViewById(R.id.uncollapsedview_third);


        f1collapsedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (f2uncollapsedView.getVisibility() == View.VISIBLE) {
                    Transition transition = new Slide(Gravity.BOTTOM);
                    transition.setDuration(400);
                    transition.addTarget(R.id.uncollapsedview_second);
                    TransitionManager.beginDelayedTransition(findViewById(R.id.nav_host_fragment), transition);
                    f2uncollapsedView.setVisibility(View.GONE);
                } else {
                    Transition transition = new Slide(Gravity.BOTTOM);
                    transition.setDuration(400);
                    transition.addTarget(R.id.uncollapsedview_third);
                    TransitionManager.beginDelayedTransition(findViewById(R.id.nav_host_fragment), transition);
                    f3uncollapsedView.setVisibility(View.GONE);
                    f2collapsedView.setVisibility(View.GONE);
                }
                f1collapsedView.setVisibility(View.GONE);
                f1uncollapsedView.setVisibility(View.VISIBLE);
            }
        });

        f2collapsedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Transition transition = new Slide(Gravity.BOTTOM);
                transition.setDuration(400);
                transition.addTarget(R.id.uncollapsedview_third);
                TransitionManager.beginDelayedTransition(findViewById(R.id.nav_host_fragment), transition);
                f3uncollapsedView.setVisibility(View.GONE);
                f2collapsedView.setVisibility(View.GONE);
                f2uncollapsedView.setVisibility(View.VISIBLE);
            }
        });

        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (f1uncollapsedView.getVisibility() == View.VISIBLE) {
                    f1collapsedView.setVisibility(View.VISIBLE);
                    Transition transition = new Slide(Gravity.BOTTOM);
                    transition.setDuration(400);
                    transition.addTarget(R.id.uncollapsedview_second);
                    TransitionManager.beginDelayedTransition(findViewById(R.id.nav_host_fragment), transition);
                    f2uncollapsedView.setVisibility(View.VISIBLE);
                    mainButton.setText(R.string.select_your_bank);
                    f1uncollapsedView.setVisibility(View.GONE);

                } else if (f2uncollapsedView.getVisibility() == View.VISIBLE) {
                    f2collapsedView.setVisibility(View.VISIBLE);
                    Transition transition = new Slide(Gravity.BOTTOM);
                    transition.setDuration(400);
                    transition.addTarget(R.id.uncollapsedview_third);
                    TransitionManager.beginDelayedTransition(findViewById(R.id.nav_host_fragment), transition);
                    f3uncollapsedView.setVisibility(View.VISIBLE);
                    mainButton.setText(R.string.one_click_kyc);
                    f2uncollapsedView.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        View firstFragement = (View) findViewById(R.id.nav_host_fragment);
        View f1collapsedView = firstFragement.findViewById(R.id.collapsedview_first);
        View f1uncollapsedView = firstFragement.findViewById(R.id.uncollapsedview_first);
        View f2collapsedView = firstFragement.findViewById(R.id.collapsedview_second);
        View f2uncollapsedView = firstFragement.findViewById(R.id.uncollapsedview_second);
        View f3uncollapsedView = firstFragement.findViewById(R.id.uncollapsedview_third);
        Button mainButton = (Button) findViewById(R.id.button_first);

        if (f2uncollapsedView.getVisibility() == View.VISIBLE) {
            Transition transition = new Slide(Gravity.BOTTOM);
            transition.setDuration(400);
            transition.addTarget(R.id.uncollapsedview_second);
            TransitionManager.beginDelayedTransition(findViewById(R.id.nav_host_fragment), transition);
            f2uncollapsedView.setVisibility(View.GONE);
            f1collapsedView.setVisibility(View.GONE);
            f1uncollapsedView.setVisibility(View.VISIBLE);
            mainButton.setText(R.string.next);
        } else if (f3uncollapsedView.getVisibility() == View.VISIBLE) {
            Transition transition = new Slide(Gravity.BOTTOM);
            transition.setDuration(400);
            transition.addTarget(R.id.uncollapsedview_third);
            TransitionManager.beginDelayedTransition(findViewById(R.id.nav_host_fragment), transition);
            f3uncollapsedView.setVisibility(View.GONE);
            f2collapsedView.setVisibility(View.GONE);
            f2uncollapsedView.setVisibility(View.VISIBLE);
            mainButton.setText(R.string.select_your_bank);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}