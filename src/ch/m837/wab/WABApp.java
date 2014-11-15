package ch.m837.wab;

import ch.m837.wab.model.User;
import android.app.Application;

public class WABApp extends Application {
  private static WABApp singleInstance = null;

  public static WABApp getInstance() {
    return singleInstance;
  }

  @Override
  public void onCreate() {
    super.onCreate();
    singleInstance = this;
  }

  public User getConnectedUser() {
    return connectedUser;
  }

  public User getLocaluser() {
    return localuser;
  }

  public void setConnectedUser(User connectedUser) {
    this.connectedUser = connectedUser;
  }

  public void setLocaluser(User localuser) {
    this.localuser = localuser;
  }

  // FIELDS
  public static final long UPDATE_INTERVAL = 5000l;
  public static final long FASTEST_INTERVAL = 1000l;
  public static final Double MAX_NEAREST_USER_DISTANZ = 12500d;
  public static final Double SEARCH_RADIUS = 100d;
public static final long WATINTERVAL_LONG = 3000l;
public static final long WATINTERVAL_SHORT = 1000l;


  private User connectedUser = new User();
  private User localuser = new User();

}
