package ch.m837.wab.util;

import android.app.NotificationManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat.Builder;

public class UserNotification {
  private Vibrator vibrator;
  private Builder mBuilder;
  private int mNotificationId;
  private NotificationManager mNotifyMgr;

  public UserNotification(int mNotificationId) {
    this.mNotificationId = mNotificationId;
  }

  public Vibrator getVibrator() {
    return vibrator;
  }

  public void setVibrator(Vibrator vibrator) {
    this.vibrator = vibrator;
  }

  public Builder getmBuilder() {
    return mBuilder;
  }

  public void setmBuilder(Builder mBuilder) {
    this.mBuilder = mBuilder;
  }

  public int getmNotificationId() {
    return mNotificationId;
  }

  public void setmNotificationId(int mNotificationId) {
    this.mNotificationId = mNotificationId;
  }

  public NotificationManager getmNotifyMgr() {
    return mNotifyMgr;
  }

  public void setmNotifyMgr(NotificationManager mNotifyMgr) {
    this.mNotifyMgr = mNotifyMgr;
  }
}