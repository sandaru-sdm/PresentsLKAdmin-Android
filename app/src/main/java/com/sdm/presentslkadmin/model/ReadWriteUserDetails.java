package com.sdm.presentslkadmin.model;

public class ReadWriteUserDetails {
    public String email, doB, mobile, address, userType, key;

    //Constructor
    public ReadWriteUserDetails(){}

    public ReadWriteUserDetails(String textEmmail, String textDoB, String textMobile, String textAddress, String textUserType){
        this.email = textEmmail;
        this.doB = textDoB;
        this.mobile = textMobile;
        this.address = textAddress;
        this.userType = textUserType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDoB() {
        return doB;
    }

    public void setDoB(String doB) {
        this.doB = doB;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
