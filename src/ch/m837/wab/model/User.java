package ch.m837.wab.model;

import ch.m837.wab.WABApp;

public class User {
  private String userID = "";
  private String userName = "";
  private Gender gender = Gender.Male;
  private Gender genderLookingFor = Gender.Both;
  private Double latitude = 0d;
  private Double longitude = 0d;
  private Double maxSearchRadius = WABApp.SEARCH_RADIUS;
  private Double distanceToNearestUser = WABApp.MAX_NEAREST_USER_DISTANZ + 1;
  private String whiteListedName = "";
  private Boolean online = true;


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

  public Double getLatitude() {
    return latitude;
  }

  public void setLatitude(Double latitude) {
    this.latitude = latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public void setLongitude(Double longitude) {
    this.longitude = longitude;
  }

  public Double getMaxSearchRadius() {
    return maxSearchRadius;
  }

  public void setMaxSearchRadius(Double maxSearchRadius) {
    this.maxSearchRadius = maxSearchRadius;
  }

  public Double getDistanceToNearestUser() {
    return distanceToNearestUser;
  }

  public void setDistanceToNearestUser(Double distanceToNearestUser) {
    this.distanceToNearestUser = distanceToNearestUser;
  }

  public String getWhiteListedName() {
    return whiteListedName;
  }

  public void setWhiteListedName(String whiteListedName) {
    this.whiteListedName = whiteListedName;
  }

  public Boolean isOnline() {
    return online;
  }

  public void setOnline(Boolean online) {
    this.online = online;
  }

}
