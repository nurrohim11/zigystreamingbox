package id.net.gmedia.zigistreamingbox.live.model;

/**
 * Created by Maulana on 5/3/2017.
 * Purpose: this class is used for List item
 */

public class LiveItemModel {

    private String id, nama, link, icon, showing_duration, scale_screen;

    public LiveItemModel(String id, String nama,String link, String icon) {
        this.id = id;
        this.nama = nama;
        this.link = link;
        this.icon = icon;
    }

    public LiveItemModel(String id, String nama,String link, String showing_duration, String scale_screen) {
        this.id = id;
        this.nama = nama;
        this.link = link;
        this.showing_duration = showing_duration;
        this.scale_screen = scale_screen;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getShowing_duration() {
        return showing_duration;
    }

    public void setShowing_duration(String showing_duration) {
        this.showing_duration = showing_duration;
    }

    public String getScale_screen() {
        return scale_screen;
    }

    public void setScale_screen(String scale_screen) {
        this.scale_screen = scale_screen;
    }
}
