package pt.ipbeja.aula5.data.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

import pt.ipbeja.aula5.data.entity.Message;

@Dao
public interface MessageDao {

    @Insert
    long insert(Message message);

    @Query("select * from messages where contactId = :contactId")
    List<Message> getMessagesForContact(long contactId);

    @Query("delete from messages where contactId = :contactId")
    int deleteMessagesForContact(long contactId);

    @Query("select count(*) from messages where contactId = :contactId")
    int messageCountForContact(long contactId);

}
