package id.net.gmedia.zigistreamingbox.streaming;

public class ItemModel {
    String id,title,icon,url,kategori, m_package,url_playstore, url_web;

    public ItemModel(String id, String title, String icon, String url, String kategori, String m_package, String url_playstore, String url_web){
        this.id = id;
        this.title = title;
        this.icon = icon;
        this.url = url;
        this.kategori =kategori;
        this.m_package = m_package;
        this.url_playstore= url_playstore;
        this.url_web= url_web;
    }

    public ItemModel(String id, String icon){
        this.id = id;
        this.icon = icon;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public String getM_package() {
        return m_package;
    }

    public void setM_package(String m_package) {
        this.m_package = m_package;
    }

    public String getUrl_playstore() {
        return url_playstore;
    }

    public void setUrl_playstore(String url_playstore) {
        this.url_playstore = url_playstore;
    }

    public String getUrl_web() {
        return url_web;
    }

    public void setUrl_web(String url_web) {
        this.url_web = url_web;
    }
}
