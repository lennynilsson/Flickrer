package se.bylenny.flickrer.models.internal;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import se.bylenny.flickrer.models.DataModel;

@DatabaseTable(tableName = "posts")
public class Post implements DataModel {

    @DatabaseField(generatedId = true, columnName = "_id")
    private long id;

    @DatabaseField
    private long index;

    @DatabaseField
    private String creator;

    @DatabaseField
    private String title;

    @DatabaseField
    private String description;

    @DatabaseField(foreign = true, foreignAutoRefresh=true, canBeNull=false, maxForeignAutoRefreshLevel = 1)
    private Query query;

    @ForeignCollectionField(eager = true)
    private ForeignCollection<Image> images;

    public Post() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public ForeignCollection<Image> getImages() {
        return images;
    }

    public void setImages(ForeignCollection<Image> images) {
        this.images = images;
    }
}
