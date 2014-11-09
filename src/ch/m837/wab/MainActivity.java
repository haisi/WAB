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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import ch.m837.wab.model.Gender;
import ch.m837.wab.model.User;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends FragmentActivity implements ConnectionCallbacks,
    OnConnectionFailedListener, LocationListener, TabListener {


  /**
   * The {@link android.support.v4.view.PagerAdapter} that will provide fragments for each of the
   * three primary sections of the app. We use a {@link android.support.v4.app.FragmentPagerAdapter}
   * derivative, which will keep every loaded fragment in memory. If this becomes too memory
   * intensive, it may be best to switch to a
   * {@link android.support.v4.app.FragmentStatePagerAdapter}.
   */
  AppSectionsPagerAdapter mAppSectionsPagerAdapter;

  /**
   * The {@link ViewPager} that will display the three primary sections of the app, one at a time.
   */
  ViewPager mViewPager;



  private WABApp wab = WABApp.getInstance();
  private static TextView tvUser;
  private static ProgressBar pgbNextFetch;

  private GoogleApiClient mLocationClient;
  private Location mCurrentLocation;
  private LocationRequest mLocationRequest;
  private Vibrator vibrator;

  private Handler handler = new Handler();
  private Thread thread;


  private LocationManager locationManager;
  private String provider;

  private NotificationCompat.Builder mBuilder;
  // Sets an ID for the notification
  private int mNotificationId = 001;
  // Gets an instance of the NotificationManager service
  private NotificationManager mNotifyMgr;


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
    vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    // check if you are connected or not
    if (!isConnected()) {
      Toast.makeText(getApplicationContext(), "You are not connected!", Toast.LENGTH_LONG).show();
    }
    mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


    if (android.os.Build.VERSION.SDK_INT > 9) {
      StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
      StrictMode.setThreadPolicy(policy);
      System.out.println("*** My thread is now configured to allow connection");
    }

    if (android.os.Build.VERSION.SDK_INT < 17) {

      // Get the location manager
      locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
      // Define the criteria how to select the locatioin provider -> use
      // default
      Criteria criteria = new Criteria();
      provider = locationManager.getBestProvider(criteria, false);
      Location location = locationManager.getLastKnownLocation(provider);

      // Initialize the location fields
      if (location != null) {
        System.out.println("Provider " + provider + " has been selected.");
        onLocationChanged(location);
      }
    } else {
      mLocationClient =
          new GoogleApiClient.Builder(getApplicationContext()).addApi(LocationServices.API)
              .addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();

      mLocationRequest = new LocationRequest();
      mLocationRequest.setInterval(WABApp.UPDATE_INTERVAL);
      mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

      mLocationRequest.setFastestInterval(WABApp.FASTEST_INTERVAL);
      mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mLocationClient);
    }

    handler.removeCallbacksAndMessages(null);
    thread = getThread();
    handler.postDelayed(thread, 2000);// WORKAROUND delayed that the view can load, before the
                                      // thread gets executed(because of the progressbar)
    if (android.os.Build.VERSION.SDK_INT < 17) {

    } else {
      mLocationClient.connect();
    }


  }

  static boolean alreadyAlerted = false;

  private Thread getThread() {
    return new Thread(new Runnable() {
      @Override
      public void run() {
        if (mCurrentLocation != null) {
          if (wab.getConnectedUser().getDistanceToNearestUser() >= WABApp.MAX_NEAREST_USER_DISTANZ
              || wab.getLocaluser().getMaxSearchRadius() == 0
              || wab.getLocaluser().getUserName().equals("")) {
            waitinterval = 30000l;
          } else {
            waitinterval = 6000l;
          }

          String latitude = String.valueOf(mCurrentLocation.getLatitude());
          String longitude = String.valueOf(mCurrentLocation.getLongitude());
          String response;
          try {
            response =
                new HttpAsyncTask().execute("http://1-dot-wab-server.appspot.com/wab_server",
                    latitude, longitude).get();
          } catch (Exception e) {
            System.err.println(e);
          }
        } else {
          LocationManagerChecker locationManagerChecker =
              new LocationManagerChecker(getApplicationContext());
          if (!locationManagerChecker.isLocationServiceAvailable() && !alreadyAlerted) {
            locationManagerChecker.createLocationServiceError(MainActivity.this);
            MainActivity.alreadyAlerted = true;
          }
        }
        loadProgressBar(waitinterval.intValue() / 1000);
        handler.postDelayed(this, waitinterval);
      }
    });
  }

  static int elapsedTime = 0;
  static Timer t = new Timer();
  static TimerTask task = new TimerTask() {

    @Override
    public void run() {
      if (elapsedTime * 1000 <= waitinterval) {
        pgbNextFetch.incrementProgressBy(1);
        elapsedTime++;
      }
    }
  };

  private void loadProgressBar(final int waitInterval) {
    elapsedTime = 0;
    pgbNextFetch.setMax(waitInterval);
    pgbNextFetch.setProgress(0);
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
      jsonObject.accumulate("targetGender", wab.getLocaluser().getGenderLookingFor().toString());
      jsonObject.accumulate("maxSearchRadius", wab.getLocaluser().getMaxSearchRadius());
      jsonObject.accumulate("whitelistedName", wab.getLocaluser().getWhiteListedName());


      String json = jsonObject.toString();
      String uri =
          Uri.parse(host).buildUpon().appendQueryParameter("data", json).build().toString();
      URI url = URI.create(uri);
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
              mNotifyMgr.cancelAll();
              mBuilder =
                  new NotificationCompat.Builder(getApplicationContext())
                      .setSmallIcon(R.drawable.street_view)
                      .setContentTitle("WAB found a member nearby")
                      .setContentText(
                          "Nearest User: " + wab.getConnectedUser().getUserName() + " ["
                              + (int) wab.getConnectedUser().getDistanceToNearestUser() + "m]["
                              + wab.getConnectedUser().getGender() + "]");
              // Builds the notification and issues it.
              mNotifyMgr.notify(mNotificationId, mBuilder.build());
              vibrator.vibrate(1000);
            }
          });
        }
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            tvUser.setText("Nearest User: " + wab.getConnectedUser().getUserName() + " ["
                + (int) wab.getConnectedUser().getDistanceToNearestUser() + "m]["
                + wab.getConnectedUser().getGender() + "]");
          }
        });
      } else {
        result = "Did not work!";
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            tvUser.setText("Nearest User: No User found");
            wab.setConnectedUser(new User());
          }
        });
      }
    } catch (Exception e) {
      Log.d("InputStream", e.getLocalizedMessage());
    }
    return result;
  }

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
        LocationServices.FusedLocationApi.requestLocationUpdates(mLocationClient, mLocationRequest,
            this);
      }
    }
  }

  @Override
  public void onConnectionSuspended(int i) {
    Toast.makeText(this, "Disconnected. Please re-connect.", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onLocationChanged(Location location) {
    mCurrentLocation = location;
  }

  @Override
  protected void onStart() {
    super.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();

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
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (android.os.Build.VERSION.SDK_INT < 17) {

    } else {
      LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
      mLocationClient.disconnect();
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

  /**
   * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one of the primary
   * sections of the app.
   */
  public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

    public AppSectionsPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int i) {
      switch (i) {
        case 1:
          return new WABMainFragment();
        case 0:
          return new SettingsFragment();

        default:
          // The other sections of the app are dummy placeholders.
          Fragment fragment = new DummySectionFragment();
          Bundle args = new Bundle();
          args.putInt(DummySectionFragment.ARG_SECTION_NUMBER, i + 1);
          fragment.setArguments(args);
          return fragment;
      }
    }

    @Override
    public int getCount() {
      return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      switch (position) {
        case 0:
          return "Settings";
        case 1:
          return "WAB";
        case 2:
          return "Chat";
      }
      return "ERROR";
    }
  }

  /**
   * Main fragment WABMain
   */
  public static class WABMainFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_section_main, container, false);
      pgbNextFetch = (ProgressBar) rootView.findViewById(R.id.pgbNextFetch);
      tvUser = (TextView) rootView.findViewById(R.id.tvUser);
      t.scheduleAtFixedRate(task, 0, 1000);
      return rootView;
    }
  }
  /**
   * Settings fragment SettingsFragment
   */
  public static class SettingsFragment extends Fragment {

    private RadioGroup radioGroup1, radioGroup2;
    private WABApp wab = WABApp.getInstance();
    private SeekBar sbMaxDistance;
    private TextView tvSearchRadius;
    private EditText etName, etWhitelistedName;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_section_settings, container, false);



      etWhitelistedName = (EditText) rootView.findViewById(R.id.etWhitelistedName);
      radioGroup1 = (RadioGroup) rootView.findViewById(R.id.radioGroup1);
      radioGroup2 = (RadioGroup) rootView.findViewById(R.id.radioGroup2);
      sbMaxDistance = (SeekBar) rootView.findViewById(R.id.sbMaxDistance);
      tvSearchRadius = (TextView) rootView.findViewById(R.id.tvSearchRadius);
      etName = (EditText) rootView.findViewById(R.id.etName);



      radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {

          switch (checkedId) {
            case R.id.radio0Male:
              wab.getLocaluser().setGender(Gender.Male);
              break;
            case R.id.radio1Female:
              wab.getLocaluser().setGender(Gender.Female);
              break;
          }
        }
      });

      radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
          switch (checkedId) {
            case R.id.radio2Male:
              wab.getLocaluser().setGenderLookingFor(Gender.Male);
              break;
            case R.id.radio3Female:
              wab.getLocaluser().setGenderLookingFor(Gender.Female);
              break;
            case R.id.radio4Both:
              wab.getLocaluser().setGenderLookingFor(Gender.Both);
              break;
          }
        }

      });


      sbMaxDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
          // TODO Auto-generated method stub
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
          // TODO Auto-generated method stub
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
          wab.getLocaluser().setMaxSearchRadius(progress);
          tvSearchRadius.setText("Search Radius: " + progress);
        }
      });

      etName.addTextChangedListener(new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          // TODO Auto-generated method stub
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable s) {
          wab.getLocaluser().setUserName(s.toString());
        }
      });

      etWhitelistedName.addTextChangedListener(new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
          // TODO Auto-generated method stub
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          // TODO Auto-generated method stub
        }

        @Override
        public void afterTextChanged(Editable s) {
          wab.getLocaluser().setWhiteListedName(s.toString());
        }
      });

      return rootView;
    }
  }
  /**
   * JUST AS AN EXAMPLE
   * 
   * A dummy fragment representing a section of the app, but that simply displays
   * 
   * 
   * dummy text.
   */
  public static class DummySectionFragment extends Fragment {

    public static final String ARG_SECTION_NUMBER = "section_number";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View rootView = inflater.inflate(R.layout.fragment_section_dummy, container, false);
      Bundle args = getArguments();
      // ((TextView) rootView.findViewById(android.R.id.text1)).setText(getString(
      // R.string.dummy_section_text, args.getInt(ARG_SECTION_NUMBER)));
      return rootView;
    }
  }

}
