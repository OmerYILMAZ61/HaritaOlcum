package com.example.omer.haritaolcum.sqlite;

public class MarkerBilgileri {
    private String isim;
    private String icerik;
    private Double x,y;

    public MarkerBilgileri(String isim, String icerik, Double x, double y){
        this.isim = isim;
        this.icerik = icerik;
        this.x = x;
        this.y = y;
    }

    public void setIcerik(String icerik) {
        this.icerik = icerik;
    }

    public void setIsim(String isim) {
        this.isim = isim;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public String getIcerik() {
        return icerik;
    }

    public String getIsim() {
        return isim;
    }

}
