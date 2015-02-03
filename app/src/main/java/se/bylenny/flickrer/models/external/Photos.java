package se.bylenny.flickrer.models.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Photos {
    public long page;
    public long pages;
    public long perpage;
    public long total;
    public List<Photo> photo;

    public Photos() {

    }
}
