package me.vickychijwani.thrones.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.data.entity.Episode;

public class SynopsisActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synopsis);

        SynopsisFragment synopsisFragment = (SynopsisFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (synopsisFragment == null) {
            Episode episode = getIntent().getParcelableExtra(SynopsisFragment.KEY_EPISODE);
            synopsisFragment = SynopsisFragment.newInstance(episode);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, synopsisFragment)
                    .commit();
        }
    }

}
