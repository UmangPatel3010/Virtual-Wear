package com.example.virtualwear;

import java.io.Serializable;

public class AllClothesModel implements Serializable {
    String file_name,img_url,img_name,user_email;

    public AllClothesModel() {
    }

    public AllClothesModel(String file_name, String img_url, String img_name,String user_email) {
        this.file_name = file_name;
        this.img_url = img_url;
        this.img_name = img_name;
        this.user_email = user_email;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

    public String getImg_name() {
        return img_name;
    }

    public void setImg_name(String img_name) {
        this.img_name = img_name;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }
}
