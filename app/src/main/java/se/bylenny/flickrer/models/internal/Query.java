package se.bylenny.flickrer.models.internal;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import se.bylenny.flickrer.models.DataModel;

@DatabaseTable(tableName = "queries")
public class Query implements DataModel {

    @DatabaseField(id = true)
    private String freeText;

    @DatabaseField
    private long lastPage;

    @DatabaseField
    private long lastIndex;

    @DatabaseField
    private long pageCount;

    @DatabaseField
    private long postCount;

    @ForeignCollectionField(eager = false)
    private ForeignCollection<Post> posts;

    public Query() {

    }

    public long getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(long lastIndex) {
        this.lastIndex = lastIndex;
    }

    public String getFreeText() {
        return freeText;
    }

    public void setFreeText(String freeText) {
        this.freeText = freeText;
    }

    public long getLastPage() {
        return lastPage;
    }

    public void setLastPage(long lastPage) {
        this.lastPage = lastPage;
    }

    public ForeignCollection<Post> getPosts() {
        return posts;
    }

    public void setPosts(ForeignCollection<Post> posts) {
        this.posts = posts;
    }

    public long getPageCount() {
        return pageCount;
    }

    public void setPageCount(long pageCount) {
        this.pageCount = pageCount;
    }

    public long getPostCount() {
        return postCount;
    }

    public void setPostCount(long postCount) {
        this.postCount = postCount;
    }
}
