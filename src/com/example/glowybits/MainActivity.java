package com.example.glowybits;


import android.os.Bundle;
import android.app.Activity;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {
  private PoiSync poi_sync;
  
  @Override
  public void onStart() {
    super.onStart();
    Log.i("MainActivity", "MainActivity::onStart()");
  }
  
  public final PoiSync getPoiSync() {
    return poi_sync;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Log.i("MainActivity", "MainActivity::onCreate()");
    super.onCreate(savedInstanceState);
    poi_sync = new PoiSync(this);
    
    setContentView(R.layout.activity_main);  
    MainSettingsActivity main_settings = (MainSettingsActivity) getFragmentManager().findFragmentById(R.id.main_settings);
    main_settings.setPoiSync(poi_sync);

    
    poi_sync.start();
  }

  @Override
  protected void onResume() {
    super.onResume();    
  }

  @Override
  protected void onDestroy() {
    poi_sync.running = false;
    poi_sync.interrupt();
    try {
      poi_sync.join(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    super.onDestroy();
  }



  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    //getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    setContentView(R.layout.activity_main_settings);
  }

}
