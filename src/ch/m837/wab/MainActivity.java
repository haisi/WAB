package ch.m837.wab;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import android.app.Activity;
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
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import ch.m837.wab.model.Gender;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;



public class MainActivity extends Activity implements ConnectionCallbacks,
    OnConnectionFailedListener, LocationListener {
  private static final long UPDATE_INTERVAL = 1000;
  private static final long FASTEST_INTERVAL = 1000;
  TextView tvUser, tvSearchRadius;
  EditText etName, etWhitelistedName;
  SeekBar sbMaxDistance;
  RadioButton radio0Male, radio1Female, radio2Male, radio3Female, radio4Both;
  RadioGroup radioGroup1, radioGroup2;
  private GoogleApiClient mLocationClient;
  private Location mCurrentLocation;
  LocationRequest mLocationRequest;
  Vibrator vibrator;

  private Handler handler = new Handler();
  private Thread thread;


  private LocationManager locationManager;
  private String provider;
  private double distance = 0;
  private String nearestUser = "";
  private String nearestUserGender = "";
  private String nearestuserID = "";
  private double nearestUserDistance = 0;
  private Gender myGender = Gender.Male;
  private Gender targetGender = Gender.Both;
  private double maxDistance;
  NotificationCompat.Builder mBuilder;
  // Sets an ID for the notification
  int mNotificationId = 001;
  // Gets an instance of the NotificationManager service
  NotificationManager mNotifyMgr;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);


    etName = (EditText) findViewById(R.id.etName);
    tvSearchRadius = (TextView) findViewById(R.id.tvSearchRadius);
    tvUser = (TextView) findViewById(R.id.tvUser);
    radioGroup1 = (RadioGroup) findViewById(R.id.radioGroup1);
    radioGroup2 = (RadioGroup) findViewById(R.id.radioGroup2);
    sbMaxDistance = (SeekBar) findViewById(R.id.sbMaxDistance);
    etWhitelistedName = (EditText) findViewById(R.id.etWhitelistedName);

    // check if you are connected or not
    if (!isConnected()) {
      Toast.makeText(this, "You are not connected!", Toast.LENGTH_LONG).show();
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
      mLocationRequest.setInterval(UPDATE_INTERVAL);
      mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

      mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
    }

    thread = getThread();

    radioGroup1.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {

        switch (checkedId) {
          case R.id.radio0Male:
            myGender = Gender.Male;
            break;
          case R.id.radio1Female:
            myGender = Gender.Female;
            break;
        }
      }
    });

    radioGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
          case R.id.radio2Male:
            targetGender = Gender.Male;
            break;
          case R.id.radio3Female:
            targetGender = Gender.Female;
            break;
          case R.id.radio4Both:
            targetGender = Gender.Both;
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
        maxDistance = progress;
        tvSearchRadius.setText("Search Radius: " + maxDistance);
      }
    });
  }

  private Thread getThread() {
    return new Thread(new Runnable() {
      private Long waitinterval = 10000l;

      @Override
      public void run() {
        if (mCurrentLocation != null) {
          if (nearestUserDistance >= 2500) {
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
          Toast.makeText(getBaseContext(), "Please enable GPS and Internet and fill in a name",
              Toast.LENGTH_SHORT).show();
        }
        handler.postDelayed(this, waitinterval);
      }
    });
  }

  public String contactServer(String host, String... data) {
    InputStream inputStream = null;
    String result = "";
    try {
      HttpClient httpclient = new DefaultHttpClient();
      JSONObject jsonObject = new JSONObject();

      String latitude = data[0];
      String longitude = data[1];
      String userID = UniqueID.getDeviceId(getBaseContext());
      if (etName.getText().toString().isEmpty()) {
        return "Fill in a name.";
      }
      String userName = etName.getText().toString();
      String whitelistedName = "";
      if (!etWhitelistedName.getText().toString().isEmpty()) {
        whitelistedName = etWhitelistedName.getText().toString();
      }
      maxDistance = sbMaxDistance.getProgress();


      jsonObject.accumulate("userID", userID);
      jsonObject.accumulate("latitude", latitude);
      jsonObject.accumulate("longitude", longitude);
      jsonObject.accumulate("userName", userName);
      jsonObject.accumulate("gender", myGender.toString());
      jsonObject.accumulate("targetGender", targetGender.toString());
      jsonObject.accumulate("maxDistance", maxDistance);
      jsonObject.accumulate("whitelistedName", whitelistedName);


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

        distance = answer.getDouble("distance");
        nearestUser = answer.getString("userName");
        nearestUserGender = answer.getString("gender");
        nearestUserDistance = answer.getDouble("nearestUserDistance");

        if (!nearestuserID.equals(answer.getString("userID"))) {
          nearestuserID = answer.getString("userID");
          runOnUiThread(new Runnable() {
            @Override
            public void run() {
              mNotifyMgr.cancelAll();
              mBuilder =
                  new NotificationCompat.Builder(getApplicationContext())
                      .setSmallIcon(R.drawable.street_view)
                      .setContentTitle("WAB found a member nearby")
                      .setContentText(
                          "Nearest User: " + nearestUser + " [" + (int) distance + "m]["
                              + nearestUserGender + "]");
              // Builds the notification and issues it.
              mNotifyMgr.notify(mNotificationId, mBuilder.build());
              vibrator.vibrate(800);
            }
          });
        }
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            tvUser.setText("Nearest User: " + nearestUser + " [" + (int) distance + "m]["
                + nearestUserGender + "]");
          }
        });
      } else {
        result = "Did not work!";
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            tvUser.setText("Nearest User: No User found");
            nearestuserID = "";
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
    if (networkInfo != null && networkInfo.isConnected())
      return true;
    else
      return false;
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
    Log.d("Location Update", "CHANGED");
    mCurrentLocation = location;
  }

  @Override
  protected void onStart() {
    super.onStart();
    handler.removeCallbacksAndMessages(null);
    thread = getThread();
    handler.post(thread);
    if (android.os.Build.VERSION.SDK_INT < 17) {

    } else {
      mLocationClient.connect();
    }
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (android.os.Build.VERSION.SDK_INT < 17) {

    } else {
      LocationServices.FusedLocationApi.removeLocationUpdates(mLocationClient, this);
      mLocationClient.disconnect();
    }
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
  }
}
