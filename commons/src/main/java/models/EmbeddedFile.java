package models;

import jakarta.persistence.*;

@Entity
public class EmbeddedFile {
    @Id
    private String fileName;

    @Column(name = "alias")
    private String fileAlias;

    @Lob
    private byte[] fileContent;

    // optional=false means that the relation has to exist and is not optional.
    // Lazy fetch type means that when retrieving the File from database it doesn't fetch the note until explicity stated
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "note_id", nullable = false) // Name of the Join Table and nullable = false means that the note_id cannot be null in the table
    private Note note;

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileAlias(String fileAlias) {
        this.fileAlias = fileAlias;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileAlias() {
        return fileAlias;
    }

    public byte[] getFileContent() {
        return fileContent;
    }
}
