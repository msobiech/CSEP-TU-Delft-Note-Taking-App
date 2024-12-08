package models;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String title;

    @Column(length=1<<20)
    private String content;

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

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Note note = (Note) o;

        return new EqualsBuilder().append(id, note.id).append(title, note.title).append(content, note.content).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(title).append(content).toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .append("title", title)
                .append("content", content)
                .toString();
    }
}
