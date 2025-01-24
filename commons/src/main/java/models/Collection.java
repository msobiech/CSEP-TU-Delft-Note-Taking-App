package models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler" , "notes"})
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String name;

    private boolean isDefault;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
    }, mappedBy = "collections")
    private Set<Note> notes = new HashSet<>();

    /**
     * Gets collection id
     * @return the collection id
     */
    public long getId() {
        return id;
    }

    /**
     * Set the collection id
     * @param id to set to
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the name of the collection
     * @return name of the collection
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the collection
     * @param name to set to
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get set of connected Notes.It is lazily fetched in database.
     * Use the method in service implementation to fetch the notes.
     * @return set of Notes
     */
    public Set<Note> getNotes() {
        return notes;
    }

    /**
     * Set the set of connected Notes.
     * @param notes set of notes to set to
     */
    public void setNotes(Set<Note> notes) {
        this.notes = notes;
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
        Collection that = (Collection) o;
        return id == that.id && Objects.equals(name, that.name) && Objects.equals(notes, that.notes);
    }

    /**
     * Hashes the collection
     * @return hash of the collection
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name, notes);
    }

    /**
     * Generates human-friendly representation of the object
     * @return the string with the representation
     */
    @Override
    public String toString() {
        return "Collection{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public void setDefault (boolean value){
        this.isDefault = value;
    }

    public boolean getIsDefault(){
        return isDefault;
    }
}
