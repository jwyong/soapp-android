package com.soapp.base;

import com.soapp.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/* Created by Soapp on 4 July 2017. */
public class BaseActivity extends AppCompatActivity {

    protected Toolbar mToolbar;

    // try to using java toolbar to seting title backpress and any item [jason]

    protected void setupToolbar() {
        mToolbar = findViewById(R.id.toolbar);
        mToolbar.setTitleTextAppearance(this, R.style.toolbarText);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
