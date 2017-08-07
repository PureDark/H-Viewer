package ml.puredark.hviewer.beans;

/**
 * Created by PureDark on 2017/8/7.
 */

public class MarketSource {
    public int msid;
    public String name, jsonUrl;

    public MarketSource(int msid, String name, String jsonUrl){
        this.msid = msid;
        this.name = name;
        this.jsonUrl = jsonUrl;
    }
}
