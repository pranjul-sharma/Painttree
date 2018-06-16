package com.painttree.paintit;

/**
 * Created by HP on 11/13/2017.
 */
public class Users {
    private String image;
    private String name;
     public  Users(){

    }
    public Users(String image,String name) {
        this.name=name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


}
