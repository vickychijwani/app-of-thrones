package me.vickychijwani.thrones.ui;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import me.vickychijwani.thrones.R;
import me.vickychijwani.thrones.util.AppUtils;

public class AboutActivity extends BaseActivity implements View.OnClickListener {

    public static final String URL_GITHUB_CONTRIBUTING = "https://github.com/vickychijwani/app-of-thrones/blob/master/CONTRIBUTING.md#reporting-bugs";
    public static final String URL_GITHUB_REPO = "https://github.com/vickychijwani/app-of-thrones";

    public static final String URL_MY_WEBSITE = "http://vickychijwani.me";
    public static final String URL_TWITTER_PROFILE = "https://twitter.com/vickychijwani";
    public static final String URL_GITHUB_PROFILE = "https://github.com/vickychijwani";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView versionView = (TextView) findViewById(R.id.about_version);

        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        PackageInfo packageInfo = AppUtils.getPackageInfo(this);
        String version = getString(R.string.version_unknown);
        if (packageInfo != null) {
            version = packageInfo.versionName;
        }
        versionView.setText(version);

        // bind click listeners
        findViewById(R.id.about_open_source_libs).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.about_open_source_libs:
                Intent intent = new Intent(this, OpenSourceLibsActivity.class);
                startActivity(intent);
                break;
            case R.id.about_me:
                openUrl(URL_GITHUB_PROFILE);
                break;
            case R.id.about_github:
                openUrl(URL_GITHUB_REPO);
                break;
            case R.id.about_twitter:
                openUrl(URL_TWITTER_PROFILE);
                break;
            case R.id.about_website:
                openUrl(URL_MY_WEBSITE);
                break;
            case R.id.about_report_bugs:
                openUrl(URL_GITHUB_CONTRIBUTING);
                break;
            case R.id.about_play_store:
                final String appPackageName = getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
                break;
            case R.id.about_email_developer:
                AppUtils.emailDeveloper(this);
                break;
        }
    }

}
