package model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Notification {

    private final String id;
    private final String recipientId;
    private final String recipientType; // "RESTAURANT", "DELIVERY"
    private final String message;
    private final LocalDateTime timestamp;
    private boolean read;

    public Notification(String recipientId, String recipientType, String message) {
        this.id = UUID.randomUUID().toString();
        this.recipientId = recipientId;
        this.recipientType = recipientType;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.read = false;
    }

    public String getId() {
        return id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getRecipientType() {
        return recipientType;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return read;
    }

    public void markAsRead() {
        this.read = true;
    }
}
