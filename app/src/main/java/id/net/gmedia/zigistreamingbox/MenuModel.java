package id.net.gmedia.zigistreamingbox;

public class MenuModel {
    String menu,code;
    int id;
    public MenuModel(int id, String menu, String code){
        this.id =id;
        this.code =code;
        this.menu = menu;
    }

    public int getId(){
        return id;
    }

    public  void setId(int id){
        this.id  =id;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
