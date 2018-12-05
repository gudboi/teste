package pt.ipbeja.aula5.data.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "messages",
        foreignKeys = @ForeignKey(entity = Contact.class, parentColumns = "id",
                childColumns = "contactId",
                onDelete = ForeignKey.CASCADE))
public class Message {

    @PrimaryKey(autoGenerate = true)
    private long id;


    private long contactId;

    private String text;

    public Message(long id, long contactId, String text) {
        this.id = id;
        this.contactId = contactId;
        this.text = text;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
