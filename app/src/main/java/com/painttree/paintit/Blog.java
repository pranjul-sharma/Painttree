package com.painttree.paintit;

/**
 * Created by HP on 11/9/2017.
 */
public class Blog {
    String desc;
    String image;
    String title;
    String username;
    String profileImage;
    private long time;




    public Blog(){

    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Blog(String title, String desc, String image, String username, String profileImage, long time) {
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.username = username;
        this.profileImage=profileImage;
        this.time=time;


    }


    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return this.image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


}
