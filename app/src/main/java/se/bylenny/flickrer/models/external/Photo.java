package se.bylenny.flickrer.models.external;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Photo {
    public String id;
    public String owner;
    public String secret;
    public String server;
    public int farm;
    public String title;
    public int ispublic;
    public int isfriend;
    public int isfamily;
    public int license;
    public Description description;
    public String ownername;

    public String url_sq;
    public int height_sq;
    public int width_sq;

    public String url_s;
    public  int height_s;
    public int width_s;

    public String url_q;
    public int height_q;
    public int width_q;

    public String url_m;
    public int height_m;
    public int width_m;

    public String url_n;
    public int height_n;
    public int width_n;

    public String url_z;
    public int height_z;
    public int width_z;

    public String url_c;
    public int height_c;
    public int width_c;

    public String url_l;
    public int height_l;
    public int width_l;

    public String url_o;
    public int height_o;
    public int width_o;

    public Photo() {
    }


}
