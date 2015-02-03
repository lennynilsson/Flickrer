package se.bylenny.flickrer.models.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlickrResponse {
    public String stat;
    public Photos photos;
    public int code;
    public String message;
    public FlickrResponse() {

    }
}
