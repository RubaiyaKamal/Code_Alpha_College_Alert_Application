package com.collegealert.app.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.collegealert.app.R;
import com.collegealert.app.model.Event;

import java.util.List;

/**
 * EventAdapter.java - RecyclerView Adapter
 *
 * HOW RecyclerView + Adapter works (for beginners):
 * ------------------------------------------------
 * Think of RecyclerView as an empty picture frame gallery.
 * The Adapter is the curator who:
 * 1. Knows how many pictures (events) there are
 * 2. Creates the frame (ViewHolder) to hold each picture
 * 3. Fills each frame with the right picture (event data)
 *
 * "Recycler" means: instead of creating 100 views for 100 events,
 * it reuses ~10 visible views as you scroll (much more efficient!).
 *
 * Steps to use:
 * 1. Create adapter: EventAdapter adapter = new EventAdapter(context, eventList);
 * 2. Set on RecyclerView: recyclerView.setAdapter(adapter);
 * 3. To update data: adapter.updateEvents(newList);
 */
public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    // ===== MEMBER VARIABLES =====

    /** The app context - needed to access resources like colors and strings */
    private Context context;

    /** The list of events to display */
    private List<Event> eventList;

    // ===== CONSTRUCTOR =====

    /**
     * Creates a new EventAdapter.
     *
     * @param context   The activity context (pass 'this' from an Activity)
     * @param eventList The list of Event objects to display
     */
    public EventAdapter(Context context, List<Event> eventList) {
        this.context = context;
        this.eventList = eventList;
    }

    // ===== RECYCLERVIEW ADAPTER METHODS =====
    // These three methods MUST be implemented for any RecyclerView adapter.

    /**
     * Step 1: onCreateViewHolder
     * Called when RecyclerView needs a NEW view holder.
     * We "inflate" (create) the XML layout for one event card here.
     *
     * @param parent   The RecyclerView itself
     * @param viewType Used when you have different layout types (we have just one)
     * @return A new EventViewHolder wrapping the inflated view
     */
    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // LayoutInflater converts our XML layout file into an actual View object
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(itemView);
    }

    /**
     * Step 2: onBindViewHolder
     * Called for each visible item to fill it with data.
     * This is where we set text, colors, etc. for each event card.
     *
     * @param holder   The ViewHolder for this position
     * @param position The index in eventList (0, 1, 2, ...)
     */
    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        // Get the event at this position
        Event event = eventList.get(position);

        // Set the text fields
        holder.tvTitle.setText(event.getTitle());
        holder.tvDescription.setText(event.getDescription());
        holder.tvDateTime.setText(event.getDateTime());
        holder.tvLocation.setText(event.getLocation());
        holder.tvCategory.setText(event.getCategory());

        // Apply color-coding based on category
        // Each category gets a unique background color for easy identification
        applyCategory(holder, event.getCategory());
    }

    /**
     * Step 3: getItemCount
     * Tells RecyclerView how many items are in the list.
     *
     * @return Number of events
     */
    @Override
    public int getItemCount() {
        return eventList != null ? eventList.size() : 0;
    }

    // ===== HELPER METHODS =====

    /**
     * applyCategory - Sets the color scheme based on event category.
     *
     * Color scheme:
     * - Seminar  = Blue   (#1565C0) - Educational sessions
     * - Exam     = Red    (#C62828) - Important assessments
     * - Fest     = Green  (#2E7D32) - Celebrations and fun events
     * - Notice   = Orange (#E65100) - Important announcements
     * - Default  = Grey   (#424242) - Any other category
     *
     * @param holder   The ViewHolder containing the category badge
     * @param category The event's category string
     */
    private void applyCategory(EventViewHolder holder, String category) {
        int backgroundColor;
        int leftBorderColor;

        if (category == null) category = "Notice";

        switch (category) {
            case "Seminar":
                backgroundColor = Color.parseColor("#E3F2FD"); // Light Blue background
                leftBorderColor = Color.parseColor("#1565C0"); // Dark Blue badge
                holder.tvCategory.setBackgroundColor(Color.parseColor("#1565C0"));
                break;

            case "Exam":
                backgroundColor = Color.parseColor("#FFEBEE"); // Light Red background
                leftBorderColor = Color.parseColor("#C62828"); // Dark Red badge
                holder.tvCategory.setBackgroundColor(Color.parseColor("#C62828"));
                break;

            case "Fest":
                backgroundColor = Color.parseColor("#E8F5E9"); // Light Green background
                leftBorderColor = Color.parseColor("#2E7D32"); // Dark Green badge
                holder.tvCategory.setBackgroundColor(Color.parseColor("#2E7D32"));
                break;

            case "Notice":
                backgroundColor = Color.parseColor("#FFF3E0"); // Light Orange background
                leftBorderColor = Color.parseColor("#E65100"); // Dark Orange badge
                holder.tvCategory.setBackgroundColor(Color.parseColor("#E65100"));
                break;

            default:
                backgroundColor = Color.parseColor("#F5F5F5"); // Light Grey background
                leftBorderColor = Color.parseColor("#424242"); // Dark Grey badge
                holder.tvCategory.setBackgroundColor(Color.parseColor("#424242"));
                break;
        }

        // Apply the card background color
        holder.cardView.setCardBackgroundColor(backgroundColor);
    }

    /**
     * updateEvents - Refreshes the adapter with a new list of events.
     * Call this method whenever new data arrives from Firebase.
     *
     * Example usage:
     *   adapter.updateEvents(newEventList);
     *
     * @param newEventList The updated list of events
     */
    public void updateEvents(List<Event> newEventList) {
        this.eventList = newEventList;
        // notifyDataSetChanged() tells RecyclerView to redraw all items
        notifyDataSetChanged();
    }

    // ===== VIEW HOLDER =====

    /**
     * EventViewHolder - Holds references to all Views in one event card.
     *
     * WHY we use ViewHolder:
     * Without it, Android would call findViewById() for EVERY visible item
     * during scrolling. That's slow because it searches the whole view tree.
     * ViewHolder caches the references so we only search once per item.
     */
    public static class EventViewHolder extends RecyclerView.ViewHolder {

        // References to all the views in item_event.xml
        CardView cardView;        // The card container
        TextView tvTitle;          // Event title
        TextView tvDescription;    // Event description
        TextView tvCategory;       // Category badge (colored)
        TextView tvDateTime;       // Date and time
        TextView tvLocation;       // Location

        /**
         * Constructor: finds and caches all views by their XML IDs.
         *
         * @param itemView The inflated item_event.xml view
         */
        public EventViewHolder(@NonNull View itemView) {
            super(itemView);

            // Find each view by its ID defined in item_event.xml
            cardView = itemView.findViewById(R.id.cardView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvDescription = itemView.findViewById(R.id.tvEventDescription);
            tvCategory = itemView.findViewById(R.id.tvEventCategory);
            tvDateTime = itemView.findViewById(R.id.tvEventDateTime);
            tvLocation = itemView.findViewById(R.id.tvEventLocation);
        }
    }
}
