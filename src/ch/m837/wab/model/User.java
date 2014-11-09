package ch.m837.wab.model;

import ch.m837.wab.WABApp;

public class User {
  private String userID = "";
  private String userName = "";
  private Gender gender = Gender.Male;
  private Gender genderLookingFor = Gender.Both;
  private double latitude = 0;
  private double longitude = 0;
  private double maxSearchRadius = WABApp.SEARCH_RADIUS;
  private double distanceToNearestUser = WABApp.MAX_NEAREST_USER_DISTANZ + 1;
  private String whiteListedName = "";

  public String getUserID() {
    return userID;
  }

  public void setUserID(String userID) {
    this.userID = userID;
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(Gender gender) {
    this.gender = gender;
  }

  public Gender getGenderLookingFor() {
    return genderLookingFor;
  }

  public void setGenderLookingFor(Gender genderLookingFor) {
    this.genderLookingFor = genderLookingFor;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getMaxSearchRadius() {
    return maxSearchRadius;
  }

  public void setMaxSearchRadius(double maxSearchRadius) {
    this.maxSearchRadius = maxSearchRadius;
  }

  public double getDistanceToNearestUser() {
    return distanceToNearestUser;
  }

  public void setDistanceToNearestUser(double distanceToNearestUser) {
    this.distanceToNearestUser = distanceToNearestUser;
  }

  public String getWhiteListedName() {
    return whiteListedName;
  }

  public void setWhiteListedName(String whiteListedName) {
    this.whiteListedName = whiteListedName;
  }

}
