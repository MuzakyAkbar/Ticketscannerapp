package com.example.ticketscannerapp.models;

import java.util.List;

public class LoginResponse {
    private String token;
    private String token_type;
    private int expires_in;
    private UserData user;
    private List<Account> accounts;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return token_type;
    }

    public void setTokenType(String token_type) {
        this.token_type = token_type;
    }

    public int getExpiresIn() {
        return expires_in;
    }

    public void setExpiresIn(int expires_in) {
        this.expires_in = expires_in;
    }

    public UserData getUser() {
        return user;
    }

    public void setUser(UserData user) {
        this.user = user;
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public static class UserData {
        private int id;
        private String timezone;
        private String first_name;
        private String last_name;
        private String full_name;
        private String email;
        private boolean is_email_verified;
        private boolean has_pending_email_change;
        private String locale;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public String getFirstName() {
            return first_name;
        }

        public void setFirstName(String first_name) {
            this.first_name = first_name;
        }

        public String getLastName() {
            return last_name;
        }

        public void setLastName(String last_name) {
            this.last_name = last_name;
        }

        public String getFullName() {
            return full_name;
        }

        public void setFullName(String full_name) {
            this.full_name = full_name;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isEmailVerified() {
            return is_email_verified;
        }

        public void setEmailVerified(boolean is_email_verified) {
            this.is_email_verified = is_email_verified;
        }

        public boolean isHasPendingEmailChange() {
            return has_pending_email_change;
        }

        public void setHasPendingEmailChange(boolean has_pending_email_change) {
            this.has_pending_email_change = has_pending_email_change;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
        }
    }

    public static class Account {
        private int id;
        private String name;
        private String currency_code;
        private String timezone;
        private String updated_at;
        private boolean is_account_email_confirmed;
        private boolean is_saas_mode_enabled;
        private boolean requires_manual_verification;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCurrencyCode() {
            return currency_code;
        }

        public void setCurrencyCode(String currency_code) {
            this.currency_code = currency_code;
        }

        public String getTimezone() {
            return timezone;
        }

        public void setTimezone(String timezone) {
            this.timezone = timezone;
        }

        public String getUpdatedAt() {
            return updated_at;
        }

        public void setUpdatedAt(String updated_at) {
            this.updated_at = updated_at;
        }

        public boolean isAccountEmailConfirmed() {
            return is_account_email_confirmed;
        }

        public void setAccountEmailConfirmed(boolean is_account_email_confirmed) {
            this.is_account_email_confirmed = is_account_email_confirmed;
        }

        public boolean isSaasModeEnabled() {
            return is_saas_mode_enabled;
        }

        public void setSaasModeEnabled(boolean is_saas_mode_enabled) {
            this.is_saas_mode_enabled = is_saas_mode_enabled;
        }

        public boolean isRequiresManualVerification() {
            return requires_manual_verification;
        }

        public void setRequiresManualVerification(boolean requires_manual_verification) {
            this.requires_manual_verification = requires_manual_verification;
        }
    }
}