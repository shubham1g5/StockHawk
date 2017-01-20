package com.udacity.stockhawk.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import butterknife.ButterKnife;

public class DetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        DetailFragment detailFragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putString(Intent.EXTRA_TEXT, getIntent().getStringExtra(Intent.EXTRA_TEXT));
        detailFragment.setArguments(args);

        getFragmentManager().beginTransaction().replace(android.R.id.content, detailFragment).commit();
    }
}
