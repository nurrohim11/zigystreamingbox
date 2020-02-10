package id.net.gmedia.zigistreamingbox.streaming;

public class KategoriModel {
    String id;
    String nama;
    public KategoriModel(String id, String nama){
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
