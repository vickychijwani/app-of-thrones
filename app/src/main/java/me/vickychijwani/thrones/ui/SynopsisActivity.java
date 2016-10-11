package me.vickychijwani.thrones.ui;

import android.os.Bundle;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.data.entity.Episode;

public class SynopsisActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synopsis);

        SynopsisFragment fragment = (SynopsisFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            Episode episode = getIntent().getParcelableExtra(SynopsisFragment.KEY_EPISODE);
            fragment = SynopsisFragment.newInstance(episode);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

}
