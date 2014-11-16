package ch.m837.wab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.os.Vibrator;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import ch.m837.wab.fragments.AppSectionsPagerAdapter;
import ch.m837.wab.model.Gender;
import ch.m837.wab.model.User;
import ch.m837.wab.util.LocationData;
import ch.m837.wab.util.LocationManagerChecker;
import ch.m837.wab.util.UniqueID;
import ch.m837.wab.util.UserNotification;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class WabMainFragment extends FragmentActivity implements ConnectionCallbacks,
    OnConnectionFailedListener, LocationListener, TabListener {


  AppSectionsPagerAdapter mAppSectionsPagerAdapter;

  ViewPager mViewPager;

  private WABApp wab = WABApp.getInstance();
  private static TextView tvUser;
  private static ProgressBar pgbNextFetch;

  private Handler handler = new Handler();
  private Thread thread;


  private LocationData locationData = new LocationData();

  private UserNotification userNotification = new UserNotification(001);

  private static Long waitinterval = 10000l;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);


    // FRAGMENTS
    // Create the adapter that will return a fragment for each of the three primary sections
    // of the app.
    mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());

    // Set up the action bar.
    final ActionBar actionBar = getActionBar();

    // Specify that the Home/Up button should not be enabled, since there is no hierarchical
    // parent.
    actionBar.setHomeButtonEnabled(false);

    // Specify that we will be displaying tabs in the action bar.
    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

    // Set up the ViewPager, attaching the adapter and setting up a listener for when the
    // user swipes between sections.
    mViewPager = (ViewPager) findViewById(R.id.pager);
    mViewPager.setAdapter(mAppSectionsPagerAdapter);
    mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
      @Override
      public void onPageSelected(int position) {
        // When swiping between different app sections, select the corresponding tab.
        // We can also use ActionBar.Tab#select() to do this if we have a reference to the
        // Tab.
        actionBar.setSelectedNavigationItem(position);
      }
    });

    // For each of the sections in the app, add a tab to the action bar.
    for (int i = 0; i < mAppSectionsPagerAdapter.getCount(); i++) {
      // Create a tab with text corresponding to the page title defined by the adapter.
      // Also specify this Activity object, which implements the TabListener interface, as the
      // listener for when this tab is selected.
      actionBar.addTab(actionBar.newTab().setText(mAppSectionsPagerAdapter.getPageTitle(i))
          .setTabListener(this));
    }
    actionBar.selectTab(actionBar.getTabAt(1));

    // WAB
    wab.getLocaluser().setUserID(UniqueID.getDeviceId(getApplicationContext()));
    userNotification.setVibrator((Vibrator) getSystemService(VIBRATOR_SERVICE));
    // check if you are connected or not
    if (!isConnected()) {
      Toast.makeText(getApplicationContext(), "You are not connected!", Toast.LENGTH_LONG).show();
    }
    userNotification.setmNotifyMgr((NotificationManager) getSystemService(NOTIFICATION_SERVICE));


    if (android.os.Build.VERSION.SDK_INT > 9) {
      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
      StrictMode.setThreadPolicy(policy);
      System.out.println("*** My thread is now configured to allow connection");
    }

    if (android.os.Build.VERSION.SDK_INT < 17) {

      // Get the location manager
      locationData.setLocationManager((LocationManager) getSystemService(Context.LOCATION_SERVICE));
      // Define the criteria how to select the locatioin provider -> use
      // default
      Criteria criteria = new Criteria();
      locationData.setProvider(locationData.getLocationManager().getBestProvider(criteria, false));
      Location location =
          locationData.getLocationManager().getLastKnownLocation(locationData.getProvider());

      // Initialize the location fields
      if (location != null) {
        System.out.println("Provider " + locationData.getProvider() + " has been selected.");
        onLocationChanged(location);
      }
    } else {
      locationData.setmLocationClient(new GoogleApiClient.Builder(getApplicationContext())
          .addApi(LocationServices.API).addConnectionCallbacks(this)
          .addOnConnectionFailedListener(this).build());

      locationData.setmLocationRequest(new LocationRequest());
      locationData.getmLocationRequest().setInterval(WABApp.UPDATE_INTERVAL);
      locationData.getmLocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

      locationData.getmLocationRequest().setFastestInterval(WABApp.FASTEST_INTERVAL);
      locationData.setmCurrentLocation(LocationServices.FusedLocationApi
          .getLastLocation(locationData.getmLocationClient()));
    }

    handler.removeCallbacksAndMessages(null);
    thread = getThread();
    handler.postDelayed(thread, 2000);// WORKAROUND delayed that the view can load, before the
                                      // thread gets executed(because of the progressbar)
    if (android.os.Build.VERSION.SDK_INT < 17) {

    } else {
      locationData.getmLocationClient().connect();
    }


  }

  public static boolean alreadyAlerted = false;

  private Thread getThread() {
    return new Thread(new Runnable() {
      @Override
      public void run() {
        if (locationData.getmCurrentLocation() != null) {

          sendToServer();

          if (wab.getConnectedUser().getDistanceToNearestUser() >= WABApp.MAX_NEAREST_USER_DISTANZ
              || wab.getLocaluser().getMaxSearchRadius() == 0
              || wab.getLocaluser().getUserName().equals("")) {
            waitinterval = WABApp.WATINTERVAL_LONG;
          } else {
            waitinterval = WABApp.WATINTERVAL_SHORT;
          }
        } else {
          LocationManagerChecker locationManagerChecker =
              new LocationManagerChecker(getApplicationContext());
          if (!locationManagerChecker.isLocationServiceAvailable() && !alreadyAlerted) {
            locationManagerChecker.createLocationServiceError(WabMainFragment.this);
            WabMainFragment.alreadyAlerted = true;
          }
        }
        loadProgressBar(waitinterval.intValue() / 1000);
        handler.postDelayed(this, waitinterval);
      }
    });
  }

  static int elapsedTime = 0;
  public static Timer t = new Timer();
  public static TimerTask task = new TimerTask() {

    @Override
    public void run() {
      if (elapsedTime * 1000 <= waitinterval) {
        getPgbNextFetch().incrementProgressBy(1);
        elapsedTime++;
      }
    }
  };

  private void loadProgressBar(final int waitInterval) {
    elapsedTime = 0;
    getPgbNextFetch().setMax(waitInterval);
    getPgbNextFetch().setProgress(0);
  }

  public String contactServer(String host, String... data) {
    InputStream inputStream = null;
    String result = "";
    try {
      HttpClient httpclient = new DefaultHttpClient();
      JSONObject jsonObject = new JSONObject();

      wab.getLocaluser().setLatitude(Double.valueOf(data[0]));
      wab.getLocaluser().setLongitude(Double.valueOf(data[1]));

      if (wab.getLocaluser().getUserName().isEmpty()) {
        return "Fill in a name.";
      }

      if (wab.getLocaluser().getMaxSearchRadius() == 0) {
        return "";
      }

      jsonObject.accumulate("userID", wab.getLocaluser().getUserID());
      jsonObject.accumulate("latitude", wab.getLocaluser().getLatitude());
      jsonObject.accumulate("longitude", wab.getLocaluser().getLongitude());
      jsonObject.accumulate("userName", wab.getLocaluser().getUserName());
      jsonObject.accumulate("gender", wab.getLocaluser().getGender().toString());
      jsonObject
          .accumulate("genderLookingFor", wab.getLocaluser().getGenderLookingFor().toString());
      jsonObject.accumulate("maxSearchRadius", wab.getLocaluser().getMaxSearchRadius());
      jsonObject.accumulate("whitelistedName", wab.getLocaluser().getWhiteListedName());
      jsonObject.accumulate("online", wab.getLocaluser().isOnline());

      String json = jsonObject.toString();
      String uri =
          Uri.parse(host).buildUpon().appendQueryParameter("data", json).build().toString();
      URI url = URI.create(uri);

      System.out.println(json);

      HttpGet httpGet = new HttpGet(url);
      HttpResponse httpResponse = httpclient.execute(httpGet);
      inputStream = httpResponse.getEntity().getContent();

      result = convertInputStreamToString(inputStream);
      if (result != null && !result.equals("{}")) {
        JSONObject answer = new JSONObject(result);

        wab.getConnectedUser().setDistanceToNearestUser(answer.getDouble("distanceToNearestUser"));
        wab.getConnectedUser().setUserName(answer.getString("userName"));
        wab.getConnectedUser().setGender(Gender.valueOf(answer.getString("gender")));
        wab.getConnectedUser().setGenderLookingFor(
            Gender.valueOf(answer.getString("genderLookingFor")));
        wab.getConnectedUser().setLatitude(Double.valueOf(answer.getString("latitude")));
        wab.getConnectedUser().setLongitude(Double.valueOf(answer.getString("longitude")));
        wab.getConnectedUser().setMaxSearchRadius(
            Double.valueOf(answer.getString("maxSearchRadius")));


        if (!wab.getConnectedUser().getUserID().equals(answer.getString("userID"))) {
          wab.getConnectedUser().setUserID(answer.getString("userID"));
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              userNotification.getmNotifyMgr().cancelAll();
              userNotification.setmBuilder(new NotificationCompat.Builder(getApplicationContext())
                  .setSmallIcon(R.drawable.street_view)
                  .setContentTitle("WAB found a member nearby")
                  .setContentText(
                      "Nearest User: " + wab.getConnectedUser().getUserName() + " ["
                          + wab.getConnectedUser().getDistanceToNearestUser().intValue() + "m]["
                          + wab.getConnectedUser().getGender() + "]"));
              // Builds the notification and issues it.
              userNotification.getmNotifyMgr().notify(userNotification.getmNotificationId(),
                  userNotification.getmBuilder().build());
              userNotification.getVibrator().vibrate(1000);
            }
          });
        }
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            getTvUser().setText(
                "Nearest User: " + wab.getConnectedUser().getUserName() + " ["
                    + wab.getConnectedUser().getDistanceToNearestUser().intValue() + "m]["
                    + wab.getConnectedUser().getGender() + "]");
          }
        });
      } else {
        result = "Did not work or no user found!";
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            getTvUser().setText("Nearest User: No User found");
            wab.setConnectedUser(new User());
          }
        });
      }
    } catch (Exception e) {
      Log.d("InputStream", e.getLocalizedMessage());
    }
    return result;
  }

  //Checks if you have internet
  public boolean isConnected() {
    ConnectivityManager connMgr =
        (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if (networkInfo != null && networkInfo.isConnected()) {
      return true;
    } else {
      return false;
    }
  }

  private class HttpAsyncTask extends AsyncTask<String, Void, String> {
    @Override
    protected String doInBackground(String... params) {
      String response = contactServer(params[0], params[1], params[2]);
      return response;
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(String result) {}
  }

  private static String convertInputStreamToString(InputStream inputStream) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
    String line = "";
    String result = "";
    while ((line = bufferedReader.readLine()) != null)
      result += line;
    inputStream.close();
    return result;
  }

  @Override
  public void onConnectionFailed(ConnectionResult arg0) {
    System.out.println("Connection failed");
  }

  @Override
  public void onConnected(Bundle bundle) {
    if (android.os.Build.VERSION.SDK_INT < 17) {

    } else {
      if (isConnected()) {// XXX nicht korrekt
        LocationServices.FusedLocationApi.requestLocationUpdates(locationData.getmLocationClient(),
            locationData.getmLocationRequest(), this);
      }
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
    Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onLocationChanged(Location location) {
    locationData.setmCurrentLocation(location);
  }

  @Override
  protected void onStart() {
    super.onStart();

  }

  @Override
  protected void onStop() {
    super.onStop();
    waitinterval = WABApp.WATINTERVAL_LONG;
  }

  @Override
  protected void onPause() {
    super.onPause();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    waitinterval = WABApp.WATINTERVAL_SHORT;
  }

  @Override
  protected void onDestroy() {
    wab.getLocaluser().setOnline(false);
    sendToServer();
    System.out.println("Sent online=false to server");
    if (android.os.Build.VERSION.SDK_INT < 17) {

    } else {
      LocationServices.FusedLocationApi.removeLocationUpdates(locationData.getmLocationClient(),
          this);
      locationData.getmLocationClient().disconnect();
    }
    super.onDestroy();
  }


  private void sendToServer() {
    String latitude = String.valueOf(locationData.getmCurrentLocation().getLatitude());
    String longitude = String.valueOf(locationData.getmCurrentLocation().getLongitude());
    String response;
    try {
      response =
          new HttpAsyncTask().execute("http://1-dot-wab-server.appspot.com/wab_server", latitude,
              longitude).get();
      System.out.println(response);
    } catch (Exception e) {
      System.err.println(e);
    }
  }



  // FRAGMENTS
  @Override
  public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

  @Override
  public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    // When the given tab is selected, switch to the corresponding page in the ViewPager.
    mViewPager.setCurrentItem(tab.getPosition());
  }

  @Override
  public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}



  // GETTER/SETTER
  public static ProgressBar getPgbNextFetch() {
    return pgbNextFetch;
  }

  public static void setPgbNextFetch(ProgressBar pgbNextFetch) {
    WabMainFragment.pgbNextFetch = pgbNextFetch;
  }

  public static TextView getTvUser() {
    return tvUser;
  }

  public static void setTvUser(TextView tvUser) {
    WabMainFragment.tvUser = tvUser;
  }
}
