package models;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
public class EmbeddedFile {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "fileName")
    private String fileName;

    @Column(name = "fileType")
    private String fileType; // MIME type

    @Lob
    private byte[] fileContent;

    // optional=false means that the relation has to exist and is not optional.
    // Lazy fetch type means that when retrieving the File from database it doesn't fetch the note until explicity stated
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "note_id", nullable = false) // Name of the Join Table and nullable = false means that the note_id cannot be null in the table
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Note note;

    public EmbeddedFile() {

    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileName() {
        return fileName;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public Note getNote() {
        return note;
    }

    public EmbeddedFile(String fileName, String fileType , byte[] fileContent, Note note) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.note = note;
        this.fileContent = fileContent;
    }

    public EmbeddedFile(String fileName, String fileType, byte[] fileContent) {
        this.fileType = fileType;
        this.fileName = fileName;
        this.fileContent = fileContent;
    }

    public EmbeddedFile(String fileName, String fileType, Long id) {
        this.fileName = fileName;
        this.fileType = fileType;
        this.id = id;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public String getFileType() {
        return fileType;
    }

    public Long getId() {
        return id;
    }

    public void setFileId(long id) {
        this.id = id;
    }

    public static String getNameWithoutExtension(String file) {
        int dotIndex = file.lastIndexOf('.');
        return (dotIndex == -1) ? file : file.substring(0, dotIndex);
    }
}
