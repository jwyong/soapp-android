package com.soapp.SoappModel;

/**
 * Created by chang on 19/09/2017.
 */

public class RestaurantModel {

    private String propic;
    private String Latitude;
    private String Longitude;
    private String Name;
    private String Location;
    private String State;
    private String rating;
    private String owner_jid;
    private String ID;
    private String MainCuisine;
    private String video;
    private int Fav;

    public RestaurantModel(String propic, String Latitude, String Longitude, String Name,
                           String Location, String State, String rating, String owner_jid, String ID,
                           String MainCuisine, String video, int Fav) {
        this.propic = propic;
        this.Latitude = Latitude;
        this.Longitude = Longitude;
        this.Name = Name;
        this.Location = Location;
        this.State = State;
        this.rating = rating;
        this.owner_jid = owner_jid;
        this.ID = ID;
        this.MainCuisine = MainCuisine;
        this.video = video;
        this.Fav = Fav;
    }

    public String getPropic() {
        return propic;
    }

    public void setPropic(String propic) {
        this.propic = propic;
    }

    public String getLatitude() {
        return Latitude;
    }

    public void setLatitude(String latitude) {
        Latitude = latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getOwner_jid() {
        return owner_jid;
    }

    public void setOwner_jid(String owner_jid) {
        this.owner_jid = owner_jid;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getMainCuisine() {
        return MainCuisine;
    }

    public void setMainCuisine(String mainCuisine) {
        MainCuisine = mainCuisine;
    }

    public int getFav() {
        return Fav;
    }

    public void setFav(int fav) {
        Fav = fav;
    }

    public String getVideo() {
        return video;
    }

    public void setVideo(String video) {
        this.video = video;
    }
}
