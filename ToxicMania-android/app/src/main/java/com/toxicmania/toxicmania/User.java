package com.toxicmania.toxicmania;

import android.util.Log;

import java.util.Objects;

/**
 * Created by Vihanga Liyanage on 10/23/2017.
 */

public class User {
    private String ID;
    private String name;
    private int reputation;
    private int level;
    private int levelProgress;
    private String url;
    private String email;
    private String TAG = "User";

    public User(String ID, String name, String email, String url) {
        this.ID = ID;
        this.name = name;
        this.email = email;
        this.url = url;
        reputation = 0;
        level = 1;
        levelProgress = 0;
    }

    public User(String userObjectString) {
        // Log.i(TAG, userObjectString);
        if (!Objects.equals(userObjectString, "")) {
            String[] temp = userObjectString.split(",");
            ID = temp[0];
            name = temp[1];
            email = temp[2];
            reputation = Integer.parseInt(temp[3]);
            level = Integer.parseInt(temp[4]);
            levelProgress = Integer.parseInt(temp[5]);
            url = temp[6];
        } else {
            ID = null;
        }
    }

    @Override
    public String toString() {
        return ID + "," + name + "," + email + "," + reputation + "," + level + "," + levelProgress + "," + url;
    }

    public void setLevelProgress(int levelProgress) {
        this.levelProgress = levelProgress;
    }

    public int getLevelProgress() {
        return levelProgress;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReputation(int reputation) {
        this.reputation = reputation;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public int getReputation() {
        return reputation;
    }

    public int getLevel() {
        return level;
    }
}
