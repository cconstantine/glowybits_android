package com.example.glowybits;

import android.os.Bundle;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;

public class MainActivity extends FragmentActivity implements
    ActionBar.TabListener {
  private ViewPager viewPager;
  private TabsPagerAdapter mAdapter;

  @Override
  public void onStart() {
    super.onStart();
    Log.i("MainActivity", "MainActivity::onStart()");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.i("MainActivity", "MainActivity::onCreate()");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    viewPager = (ViewPager) findViewById(R.id.pager);
    mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

    viewPager.setAdapter(mAdapter);
    ActionBar actionBar = getActionBar();

    actionBar.setDisplayShowHomeEnabled(false);
    actionBar.setDisplayShowTitleEnabled(false);
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    // Set Tab Icon and Titles
    actionBar.addTab(actionBar.newTab().setText("Thingies")
        .setTabListener(this));
    actionBar.addTab(actionBar.newTab().setText("Colors").setTabListener(this));

    viewPager
        .setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
          @Override
          public void onPageSelected(int position) {
            getActionBar().setSelectedNavigationItem(position);
          }
        });

    startService(new Intent(getBaseContext(), PoiSyncService.class));
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onDestroy() {
    stopService(new Intent(getBaseContext(), PoiSyncService.class));

    super.onDestroy();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setContentView(R.layout.activity_main_settings);
  }

  @Override
  public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
  }

  @Override
  public void onTabSelected(Tab tab, FragmentTransaction ft) {
    viewPager.setCurrentItem(tab.getPosition());
  }

  @Override
  public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
  }

}
