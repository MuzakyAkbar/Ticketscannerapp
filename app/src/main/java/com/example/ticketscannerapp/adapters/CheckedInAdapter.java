package com.example.ticketscannerapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.example.ticketscannerapp.R;
import com.example.ticketscannerapp.utils.LocalAttendeeDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CheckedInAdapter extends RecyclerView.Adapter<CheckedInAdapter.ViewHolder> {

    /** Callback ke Activity saat tombol Check-In ditekan */
    public interface OnCheckInClickListener {
        void onCheckInClick(LocalAttendeeDatabase.LocalAttendee attendee);
    }

    private final List<LocalAttendeeDatabase.LocalAttendee> allAttendees;
    private final List<LocalAttendeeDatabase.LocalAttendee> attendees;
    private final Context context;
    private OnCheckInClickListener checkInClickListener;

    public CheckedInAdapter(List<LocalAttendeeDatabase.LocalAttendee> attendees, Context context) {
        this.allAttendees = new ArrayList<>(attendees);
        this.attendees = attendees;
        this.context = context;
    }

    public void setOnCheckInClickListener(OnCheckInClickListener listener) {
        this.checkInClickListener = listener;
    }

    /** Panggil ini saat data di-refresh dari DB */
    public void updateData(List<LocalAttendeeDatabase.LocalAttendee> newData, String currentQuery) {
        allAttendees.clear();
        allAttendees.addAll(newData);
        filter(currentQuery);
    }

    /** Filter berdasarkan nama, email, atau public_id */
    public void filter(String query) {
        attendees.clear();
        if (query == null || query.trim().isEmpty()) {
            attendees.addAll(allAttendees);
        } else {
            String q = query.toLowerCase(Locale.getDefault()).trim();
            for (LocalAttendeeDatabase.LocalAttendee a : allAttendees) {
                if ((a.getFullName() != null && a.getFullName().toLowerCase(Locale.getDefault()).contains(q))
                        || (a.email != null && a.email.toLowerCase(Locale.getDefault()).contains(q))
                        || (a.publicId != null && a.publicId.toLowerCase(Locale.getDefault()).contains(q))
                        || (a.gate != null && a.gate.toLowerCase(Locale.getDefault()).contains(q))) {
                    attendees.add(a);
                }
            }
        }
        notifyDataSetChanged();
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

        // Badge "Baru" — tampil 5 menit sejak pertama kali muncul dari server
        if (attendee.isStillNew()) {
            holder.tvName.setText("🆕 " + attendee.getFullName());
        } else {
            holder.tvName.setText(attendee.getFullName());
        }
        holder.tvEmail.setText(attendee.email != null ? attendee.email : "-");
        holder.tvTicketId.setText("ID: " + attendee.publicId);

        // Gate
        if (attendee.gate != null && !attendee.gate.isEmpty()) {
            holder.tvGate.setText("Gate: " + attendee.gate);
            holder.tvGate.setVisibility(View.VISIBLE);
        } else {
            holder.tvGate.setVisibility(View.GONE);
        }

        // Status check-in
        if (attendee.isCheckedIn) {
            // Sudah check-in: tampilkan waktu, sembunyikan tombol
            if (attendee.checkedInAt != null && !attendee.checkedInAt.isEmpty()) {
                holder.tvCheckInTime.setText("✓ Checked-in: " + formatIso(attendee.checkedInAt));
            } else if (attendee.localCheckInTime > 0) {
                holder.tvCheckInTime.setText("✓ Checked-in: " + formatLocalTime(attendee.localCheckInTime));
            } else {
                holder.tvCheckInTime.setText("✓ Checked-in: Just now");
            }
            holder.tvCheckInTime.setTextColor(context.getResources().getColor(R.color.success));
            holder.viewStatus.setBackgroundColor(context.getResources().getColor(R.color.success));
            holder.btnCheckIn.setVisibility(View.GONE);
        } else {
            // Belum check-in: tampilkan tombol
            holder.tvCheckInTime.setText("Belum Check-In");
            holder.tvCheckInTime.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.viewStatus.setBackgroundColor(context.getResources().getColor(android.R.color.darker_gray));
            holder.btnCheckIn.setVisibility(View.VISIBLE);
            holder.btnCheckIn.setOnClickListener(v -> {
                if (checkInClickListener != null) {
                    checkInClickListener.onCheckInClick(attendee);
                }
            });
        }
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
        TextView tvName, tvEmail, tvTicketId, tvCheckInTime, tvGate;
        View viewStatus;
        MaterialButton btnCheckIn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvCheckInTime = itemView.findViewById(R.id.tvCheckInTime);
            tvGate = itemView.findViewById(R.id.tvGate);
            viewStatus = itemView.findViewById(R.id.viewStatus);
            btnCheckIn = itemView.findViewById(R.id.btnCheckIn);
        }
    }
}