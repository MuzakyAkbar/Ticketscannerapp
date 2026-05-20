package com.example.ticketscannerapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ticketscannerapp.R;
import com.example.ticketscannerapp.utils.LocalAttendeeDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CheckedInAdapter extends RecyclerView.Adapter<CheckedInAdapter.ViewHolder> {

    private final List<LocalAttendeeDatabase.LocalAttendee> attendees;
    private final Context context;

    public CheckedInAdapter(List<LocalAttendeeDatabase.LocalAttendee> attendees, Context context) {
        this.attendees = attendees;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checked_in_ticket, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LocalAttendeeDatabase.LocalAttendee attendee = attendees.get(position);

        holder.tvName.setText(attendee.getFullName());
        holder.tvEmail.setText(attendee.email != null ? attendee.email : "-");
        holder.tvTicketId.setText("ID: " + attendee.publicId);

        // Prioritas: checkedInAt dari server → localCheckInTime → "Just now"
        if (attendee.checkedInAt != null && !attendee.checkedInAt.isEmpty()) {
            holder.tvCheckInTime.setText("Checked-in: " + formatIso(attendee.checkedInAt));
        } else if (attendee.localCheckInTime > 0) {
            holder.tvCheckInTime.setText("Checked-in: " + formatLocalTime(attendee.localCheckInTime));
        } else {
            holder.tvCheckInTime.setText("Checked-in: Just now");
        }

        holder.viewStatus.setBackgroundColor(context.getResources().getColor(R.color.success_green));
    }

    @Override
    public int getItemCount() {
        return attendees.size();
    }

    /** ISO 8601 → local timezone string */
    private String formatIso(String iso) {
        try {
            SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US);
            parser.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = parser.parse(iso);
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            fmt.setTimeZone(TimeZone.getDefault());
            return fmt.format(date);
        } catch (Exception e) {
            return iso.length() >= 19 ? iso.substring(0, 19).replace('T', ' ') : iso;
        }
    }

    private String formatLocalTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvTicketId, tvCheckInTime;
        View viewStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvCheckInTime = itemView.findViewById(R.id.tvCheckInTime);
            viewStatus = itemView.findViewById(R.id.viewStatus);
        }
    }
}