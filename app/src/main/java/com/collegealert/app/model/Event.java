package com.collegealert.app.model;

/**
 * Event.java - Data Model
 *
 * This class represents a single campus event/alert.
 * It is a simple "Plain Old Java Object" (POJO) that holds event data.
 *
 * Firebase Realtime Database uses this class to:
 * - Convert Java objects TO JSON when saving data (serialization)
 * - Convert JSON FROM Firebase BACK to Java objects (deserialization)
 *
 * IMPORTANT: Firebase requires:
 * 1. A public no-argument constructor
 * 2. Public getter methods for all fields
 */
public class Event {

    // ===== FIELDS =====
    // These store the data for each event

    /** Unique identifier for the event (set by Firebase) */
    private String eventId;

    /** Title/name of the event (e.g., "Mathematics Final Exam") */
    private String title;

    /** Full description of the event */
    private String description;

    /**
     * Category of the event.
     * One of: "Seminar", "Exam", "Fest", "Notice"
     * Used to filter events and apply color-coding.
     */
    private String category;

    /**
     * Date and time of the event.
     * Stored as a String for simplicity (e.g., "2024-03-15 10:00 AM")
     */
    private String dateTime;

    /** Location where the event takes place (e.g., "Auditorium Hall A") */
    private String location;

    /**
     * Timestamp in milliseconds since epoch.
     * Used for sorting events chronologically.
     * Long type can store the large Unix timestamp value.
     */
    private long timestamp;

    // ===== CONSTRUCTORS =====

    /**
     * No-argument constructor.
     * REQUIRED by Firebase for deserialization.
     * Firebase needs this to create an empty object and then set fields.
     */
    public Event() {
        // Firebase needs this empty constructor - do not remove!
    }

    /**
     * Full constructor for creating a new Event object in code.
     *
     * @param title       The event title
     * @param description The event description
     * @param category    The event category (Seminar/Exam/Fest/Notice)
     * @param dateTime    The event date and time as a string
     * @param location    The event location
     * @param timestamp   Unix timestamp in milliseconds
     */
    public Event(String title, String description, String category,
                 String dateTime, String location, long timestamp) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.dateTime = dateTime;
        this.location = location;
        this.timestamp = timestamp;
    }

    // ===== GETTERS =====
    // These methods let Firebase (and our code) READ the field values.
    // Firebase looks for methods named "get" + FieldName.

    public String getEventId() {
        return eventId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getLocation() {
        return location;
    }

    public long getTimestamp() {
        return timestamp;
    }

    // ===== SETTERS =====
    // These methods let Firebase (and our code) WRITE/UPDATE field values.
    // Firebase looks for methods named "set" + FieldName.

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * toString() - Useful for debugging.
     * When you Log.d("TAG", event.toString()), this is printed.
     */
    @Override
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", title='" + title + '\'' +
                ", category='" + category + '\'' +
                ", dateTime='" + dateTime + '\'' +
                '}';
    }
}
