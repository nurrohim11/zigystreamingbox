package id.net.gmedia.zigistreamingbox.live.model;

public class KategoriChannelModel {
    String id;
    String nama;
    public KategoriChannelModel(String id, String nama){
        this.nama = nama;
        this.id = id;
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
}
