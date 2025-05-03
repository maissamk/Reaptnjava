package utils;

public class UserStatusService {
    // Enum for user status
    public enum UserStatus {
        ACTIVE("Active"),
        INACTIVE("Inactive"),
        BLOQUE("Blocked");

        private final String displayName;

        UserStatus(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    // Static nested class for event data
    public static class StatusChangeEventData {
        private final UserStatus newStatus;
        private final int userId;

        public StatusChangeEventData(UserStatus newStatus, int userId) {
            this.newStatus = newStatus;
            this.userId = userId;
        }

        public UserStatus getNewStatus() {
            return newStatus;
        }

        public int getUserId() {
            return userId;
        }
    }
}