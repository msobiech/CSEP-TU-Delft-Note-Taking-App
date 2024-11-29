package models;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@Entity
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;
    public String title;

    @Column(length = 1<<10)
    public String content;

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
