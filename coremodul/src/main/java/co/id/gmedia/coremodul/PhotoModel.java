package co.id.gmedia.coremodul;

import android.graphics.Bitmap;

public class PhotoModel {

    private String url, web, keterangan;
    private Bitmap bitmap;

    public PhotoModel(String url, String web) {
        this.url = url;
        this.web = web;
    }

    public PhotoModel(String url, Bitmap bitmap) {
        this.url = url;
        this.bitmap = bitmap;
    }

    public PhotoModel(String url, String web, String keterangan) {
        this.url = url;
        this.web = web;
        this.keterangan = keterangan;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    public String getKeterangan() {
        return keterangan;
    }

    public void setKeterangan(String keterangan) {
        this.keterangan = keterangan;
    }
}
