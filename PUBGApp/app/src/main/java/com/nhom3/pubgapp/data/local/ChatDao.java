package com.nhom3.pubgapp.data.local;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertConversations(List<ConversationEntity> conversations);

    @Query("SELECT * FROM conversation_cache ORDER BY lastMessageAt DESC")
    List<ConversationEntity> getConversations();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertMessages(List<MessageEntity> messages);

    @Query("SELECT * FROM message_cache WHERE conversationId = :id ORDER BY sentAtMillis ASC")
    List<MessageEntity> getMessages(String id);

    // A @Query holds one statement, so the two deletes sit in helpers that clearAll runs together.
    @Query("DELETE FROM conversation_cache")
    void clearConversations();

    @Query("DELETE FROM message_cache")
    void clearMessages();

    @Transaction
    default void clearAll() {
        clearConversations();
        clearMessages();
    }
}
