package com.example.ticketscannerapp.utils;

import android.app.Activity;
import android.media.MediaPlayer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import com.example.ticketscannerapp.R;
import com.example.ticketscannerapp.models.Ticket;

public class BannerHelper {

    public static void showSuccessBanner(Activity activity, CardView container, Ticket ticket) {
        container.removeAllViews();
        View bannerView = LayoutInflater.from(activity).inflate(R.layout.banner_success, container, true);

        TextView tvTitle = bannerView.findViewById(R.id.tvBannerTitle);
        TextView tvMessage = bannerView.findViewById(R.id.tvBannerMessage);
        TextView tvName = bannerView.findViewById(R.id.tvBannerName);
        TextView tvTicketId = bannerView.findViewById(R.id.tvBannerTicketId);

        tvTitle.setText("✓ CHECK-IN BERHASIL");
        tvMessage.setText("Tiket berhasil di-check-in");
        tvName.setText(ticket.getName());
        tvTicketId.setText(ticket.getTicketId() + " - " + ticket.getType());

        playSound(activity, R.raw.success);
    }

    public static void showErrorBanner(Activity activity, CardView container, String message) {
        container.removeAllViews();
        View bannerView = LayoutInflater.from(activity).inflate(R.layout.banner_error, container, true);

        TextView tvTitle = bannerView.findViewById(R.id.tvBannerTitle);
        TextView tvMessage = bannerView.findViewById(R.id.tvBannerMessage);

        tvTitle.setText("✗ CHECK-IN GAGAL");
        tvMessage.setText(message);

        playSound(activity, R.raw.error);
    }

    public static void showWarningBanner(Activity activity, CardView container, String message, Ticket ticket) {
        container.removeAllViews();
        View bannerView = LayoutInflater.from(activity).inflate(R.layout.banner_warning, container, true);

        TextView tvTitle = bannerView.findViewById(R.id.tvBannerTitle);
        TextView tvMessage = bannerView.findViewById(R.id.tvBannerMessage);
        TextView tvName = bannerView.findViewById(R.id.tvBannerName);
        TextView tvTime = bannerView.findViewById(R.id.tvBannerTime);

        tvTitle.setText("⚠ SUDAH CHECK-IN");
        tvMessage.setText(message);
        tvName.setText(ticket.getName());
        tvTime.setText("Waktu check-in: " + ticket.getCheckInTime());

        playSound(activity, R.raw.warning);
    }

    /**
     * Mode Check-Ticket: tampilkan hasil sederhana tanpa ubah data.
     * state: "valid"       → ✓ hijau  (ticket ada, belum check-in)
     *        "already"     → ⚠ oranye (ticket ada, sudah check-in)
     *        "not_found"   → ✗ merah  (ticket tidak ditemukan)
     */
    public static void showCheckTicketBanner(Activity activity, CardView container, String state) {
        container.removeAllViews();
        View bannerView = LayoutInflater.from(activity).inflate(R.layout.banner_check_ticket, container, true);

        TextView tvIcon   = bannerView.findViewById(R.id.tvCheckTicketIcon);
        TextView tvStatus = bannerView.findViewById(R.id.tvCheckTicketStatus);
        TextView tvSub    = bannerView.findViewById(R.id.tvCheckTicketSub);

        switch (state) {
            case "valid":
                tvIcon.setText("✓");
                tvIcon.setTextColor(android.graphics.Color.parseColor("#4CAF50"));
                tvStatus.setText("TIKET VALID");
                tvSub.setText("Belum check-in");
                playSound(activity, R.raw.success);
                break;
            case "already":
                tvIcon.setText("⚠");
                tvIcon.setTextColor(android.graphics.Color.parseColor("#FF9800"));
                tvStatus.setText("SUDAH CHECK-IN");
                tvSub.setText("Tiket ini sudah pernah check-in");
                playSound(activity, R.raw.warning);
                break;
            case "not_found":
            default:
                tvIcon.setText("✗");
                tvIcon.setTextColor(android.graphics.Color.parseColor("#F44336"));
                tvStatus.setText("TIKET TIDAK VALID");
                tvSub.setText("Tiket tidak ditemukan");
                playSound(activity, R.raw.error);
                break;
        }
    }

    private static void playSound(Activity activity, int soundResourceId) {
        try {
            MediaPlayer mp = MediaPlayer.create(activity, soundResourceId);

            if (mp != null) {
                mp.setOnCompletionListener(MediaPlayer::release);
                mp.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}