package com.soapp.SoappModel;

//smoking, wifi, alc, halal
public class FoodAdditionalInfo {

    private int addInfoLogo;
    private String addInfoName;

    public FoodAdditionalInfo(int addInfoLogo, String addInfoName) {
        this.addInfoLogo = addInfoLogo;
        this.addInfoName = addInfoName;
    }

    public int getAddInfoLogo() {
        return addInfoLogo;
    }

    public void setAddInfoLogo(int addInfoLogo) {
        this.addInfoLogo = addInfoLogo;
    }

    public String getAddInfoName() {
        return addInfoName;
    }

    public void setAddInfoName(String addInfoName) {
        this.addInfoName = addInfoName;
    }
}
