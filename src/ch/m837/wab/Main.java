package ch.m837.wab;

import ch.m837.wab.fragments.LoginFragment;

import com.facebook.AppEventsLogger;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class Main extends FragmentActivity {
  private static final int LOGIN = 0;
  private static final int WAB_APP = 1;

  private Fragment[] fragments = new Fragment[2];

  private boolean isResumed = false;
  private UiLifecycleHelper uiHelper;
  private Session.StatusCallback callback = new Session.StatusCallback() {
    @Override
    public void call(Session session, SessionState state, Exception exception) {
      onSessionStateChange(session, state, exception);
    }
  };

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    uiHelper = new UiLifecycleHelper(this, callback);
    uiHelper.onCreate(savedInstanceState);
    
    setContentView(R.layout.activity_main);

    FragmentManager fm = getSupportFragmentManager();
    LoginFragment splashFragment = (LoginFragment) fm.findFragmentById(R.id.loginFragment);
    fragments[LOGIN] = splashFragment;
    fragments[WAB_APP] = fm.findFragmentById(R.id.wabMainFragment);

    FragmentTransaction transaction = fm.beginTransaction();
    for(int i = 0; i < fragments.length; i++) {
        transaction.hide(fragments[i]);
    }
    transaction.commit();
    
    
  }

  @Override
  public void onResume() {
    super.onResume();
    uiHelper.onResume();
    isResumed = true;

    // Call the 'activateApp' method to log an app event for use in analytics and advertising
    // reporting. Do so in
    // the onResume methods of the primary Activities that an app may be launched into.
    AppEventsLogger.activateApp(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    uiHelper.onPause();
    isResumed = false;

    // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
    // reporting. Do so in the onPause methods of the primary Activities that an app may be launched
    // into.
    AppEventsLogger.deactivateApp(this);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    uiHelper.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    uiHelper.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    uiHelper.onSaveInstanceState(outState);
  }

  @Override
  protected void onResumeFragments() {
    super.onResumeFragments();
    Session session = Session.getActiveSession();

    if (session != null && session.isOpened()) {
      // if the session is already open, try to show the selection fragment
      showFragment(WAB_APP, false);
    } else {
      // otherwise present the splash screen and ask the user to login, unless the user explicitly
      // skipped.
      showFragment(LOGIN, false);
    }
  }

  private void onSessionStateChange(Session session, SessionState state, Exception exception) {
    if (isResumed) {
      FragmentManager manager = getSupportFragmentManager();
      int backStackSize = manager.getBackStackEntryCount();
      for (int i = 0; i < backStackSize; i++) {
        manager.popBackStack();
      }
      // check for the OPENED state instead of session.isOpened() since for the
      // OPENED_TOKEN_UPDATED state, the selection fragment should already be showing.
      if (state.equals(SessionState.OPENED)) {
        showFragment(WAB_APP, false);
      } else if (state.isClosed()) {
        showFragment(LOGIN, false);
      }
    }
  }

  private void showFragment(int fragmentIndex, boolean addToBackStack) {
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction transaction = fm.beginTransaction();
    for (int i = 0; i < fragments.length; i++) {
      if (i == fragmentIndex) {
        transaction.show(fragments[i]);
      } else {
        transaction.hide(fragments[i]);
      }
    }
    if (addToBackStack) {
      transaction.addToBackStack(null);
    }
    transaction.commit();
  }
}
