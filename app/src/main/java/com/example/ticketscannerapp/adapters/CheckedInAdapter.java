package com.example.ticketscannerapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ticketscannerapp.R;
import com.example.ticketscannerapp.models.Ticket;
import java.util.ArrayList;
import java.util.List;

public class CheckedInAdapter extends RecyclerView.Adapter<CheckedInAdapter.ViewHolder> {

    private List<Ticket> tickets = new ArrayList<>();

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets != null ? tickets : new ArrayList<>();
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
        holder.bind(tickets.get(position));
    }

    @Override
    public int getItemCount() {
        return tickets.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTicketId, tvName, tvType, tvTime;

        ViewHolder(View itemView) {
            super(itemView);
            tvTicketId = itemView.findViewById(R.id.tvTicketId);
            tvName = itemView.findViewById(R.id.tvName);
            tvType = itemView.findViewById(R.id.tvType);
            tvTime = itemView.findViewById(R.id.tvTime);
        }

        void bind(Ticket ticket) {
            tvTicketId.setText(ticket.getTicketId());
            tvName.setText(ticket.getName());
            tvType.setText(ticket.getType());
            tvTime.setText("✓ " + ticket.getCheckInTime());
        }
    }
}