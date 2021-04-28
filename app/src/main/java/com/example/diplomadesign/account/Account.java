package com.example.diplomadesign.account;

import org.litepal.crud.LitePalSupport;

public class Account extends LitePalSupport {
    private String telephone;
    private String name;
    private byte[] headshot;//头像

    public String getTelephone(){
        return telephone;
    }
    public void setTelephone(String telephone){
        this.telephone=telephone;
    }
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
    }
    public byte[] getHeadshot() {
        return headshot;
    }
    public void setHeadshot(byte[] headshot) {
        this.headshot = headshot;
    }
    public Account(){
        super();
    }
    public Account(String name, String telephone, byte[] headshot){
        super();
        this.telephone=telephone;
        this.name=name;
        this.headshot=headshot;
    }
}
