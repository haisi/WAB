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
  public static final long UPDATE_INTERVAL = 5000;
  public static final long FASTEST_INTERVAL = 1000;
  public static final int MAX_NEAREST_USER_DISTANZ = 2500;
  public static final int SEARCH_RADIUS = 100;


  private User connectedUser = new User();
  private User localuser = new User();

}
