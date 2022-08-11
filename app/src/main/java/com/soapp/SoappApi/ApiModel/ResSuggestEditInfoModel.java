package com.soapp.SoappApi.ApiModel;

import android.widget.Spinner;

/**
 * Created by rlwt on 2/23/18.
 */

public class ResSuggestEditInfoModel {

    //post material
    private String Res_id;
    private String Name;
    private String Cuisine;
    private String Price;
    private String Address;
    private String State;
    private String Alcohol;
    private String phoneNumber;
    private String Wifi;
    private String Outdoor;
    private String Halal;
    private String Mon;
    private String Tue;
    private String Wed;
    private String Thu;
    private String Fri;
    private String Sat;
    private String Sun;

    //response material

    //constructor
    public ResSuggestEditInfoModel(String resId, String name, String cuisine, String price, String address, Spinner mall, String alcohol, String phoneNo, String wifi,
                                   String outdoor, String halal, String mon, String tue, String wed, String thu, String fri, String sat, String sun) {
        this.Res_id = resId;
        this.Name = name;
        this.Cuisine = cuisine;
        this.Price = price;
        this.Address = address;
        this.State = State;
        this.Alcohol = alcohol;
        this.phoneNumber = phoneNo;
        this.Wifi = wifi;
        this.Outdoor = outdoor;
        this.Halal = halal;
        this.Mon = mon;
        this.Tue = tue;
        this.Wed = wed;
        this.Thu = thu;
        this.Fri = fri;
        this.Sat = sat;
        this.Sun = sun;
    }
}
