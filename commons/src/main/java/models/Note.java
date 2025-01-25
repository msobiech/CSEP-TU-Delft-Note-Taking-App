package models;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String title;

    @Column(length=1<<20)
    private String content;

    /*
    Set to establish relation many to many. It is populated in a lazy way.
    So when explicitly accessed the database will automatically populate the set.
    Theoretically when you set the set to a set of Collection objects it will automatically
    connect them to the corresponding collections. It is not adviced since to do so since
    it is quite inconvenient and inefficient to fetch everytime so many Collections.
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE,
            CascadeType.REFRESH
    })
    @JoinTable(
            name = "collection_notes",
            joinColumns = @JoinColumn(name = "note_id"),
            inverseJoinColumns = @JoinColumn(name = "collection_id")
    )
    private Set<Collection> collections = new HashSet<>();

    /*
    This set of Ids is made in order to conveniently connect notes to collections. In the Service
    Implementation in the update note the Ids from the list are fetched and then passed to database
    to get the corresponding collection objects. That way we only fetch it in updating the note in the
    database, and we do not need to hold whole Collection Objects in the client locally.
    It is transient so it will not be serialized. That way it won't be saved in the database.
    Its only use is a convenient way of handling update requests.
     */
    @Transient
    private Set<Long> collectionIds = new HashSet<>();

    /**
     * Empty constructor for Object Mapping
     */
    public Note() {}

    /**
     * Alternate constructor for not object mapping
     * @param title title of the note
     * @param content content of the note
     */
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * Gets set of Ids of collections that the note is supposed to be connected to.
     * @return the set of Ids of collections
     */
    public Set<Long> getCollectionIds() {
        return collectionIds;
    }

    /**
     * Sets the set of Ids of collections
     * @param collectionIds the set of ids to set to
     */
    public void setCollectionIds(Set<Long> collectionIds) {
        this.collectionIds = collectionIds;
    }


    /**
     * Gets the title of the object.
     * @return the title as a String.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the object.
     * @param title the title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the content of the object.
     * @return the content as a String.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the object.
     * @param content the content to set.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the unique identifier (ID) of the object.
     * @return the ID as a long.
     */
    public long getId() {
        return id;
    }

    /**
     * Get set of collections that are connected to the Note
     * @return the set of collections
     */
    public Set<Collection> getCollections() {
        return collections;
    }

    /**
     * Set the set of collections connected to the Note.
     * @param collections the set of collections to set to.
     */
    public void setCollections(Set<Collection> collections) {
        this.collections = collections;
    }

    /**
     * Method that asserts equality between object and parameter
     * @param o object to compare to
     * @return true if objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        return new EqualsBuilder().append(id, note.id).append(title, note.title).append(content, note.content).isEquals();
    }

    /**
     * Hashes the collection
     * @return hash of the collection
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(title).append(content).toHashCode();
    }



    /**
     * Generates human-friendly representation of the object
     * @return the string with the representation
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("title", title)
                .append("content", content)
                .append("collections", collections)
                .toString();
    }

    public void setId(long id) {
        this.id = id;
    }

    public void addCollection(Collection collection){
        if (collection != null) {
            this.collections.add(collection);
            this.collectionIds.add(collection.getId());
        } else{
            this.collections = new HashSet<>();
        }
    }



}
