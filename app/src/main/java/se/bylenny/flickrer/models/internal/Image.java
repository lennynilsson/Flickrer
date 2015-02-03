package se.bylenny.flickrer.models.internal;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import se.bylenny.flickrer.models.DataModel;

@DatabaseTable(tableName = "images")
public class Image implements DataModel {

    @DatabaseField(generatedId = true, columnName = "_id")
    private long id;

    @DatabaseField(canBeNull = false)
    private String url;

    @DatabaseField(canBeNull = false)
    private int width;

    @DatabaseField(canBeNull = false)
    private int height;

    @DatabaseField(foreign = true, foreignAutoRefresh=true, canBeNull=false, maxForeignAutoRefreshLevel = 1)
    private Post post;

    public Image() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
