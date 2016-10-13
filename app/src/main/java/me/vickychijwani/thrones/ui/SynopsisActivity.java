package me.vickychijwani.thrones.ui;

import android.os.Bundle;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.data.entity.Episode;
import me.vickychijwani.thrones.util.CrashLedger;

public class SynopsisActivity extends BaseActivity {

    private static final String TAG = "SynopsisActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synopsis);

        SynopsisFragment fragment = (SynopsisFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (fragment == null) {
            CrashLedger.Log.d(TAG, "Creating new SynopsisFragment");
            Episode episode = getIntent().getParcelableExtra(SynopsisFragment.KEY_EPISODE);
            if (episode == null) {
                throw new IllegalArgumentException("Received null episode");
            }
            fragment = SynopsisFragment.newInstance(episode);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

}
