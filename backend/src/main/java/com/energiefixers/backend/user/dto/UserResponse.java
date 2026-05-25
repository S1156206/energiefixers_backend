package com.energiefixers.backend.user.dto;

import com.energiefixers.backend.user.models.Role;
import com.energiefixers.backend.user.models.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String email;
    private Role role;
    private String firstName;
    private LocalDateTime registeredAt;
    private LocalDateTime lastLoginAt;
    private int gamificationPoints;
    private Long propertyId;

    /**
     * Zet een User entiteit om naar een veilige UserResponse DTO.
     */
    public static UserResponse fromUser(User user) {
        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setFirstName(user.getFirstName());
        response.setRegisteredAt(user.getRegisteredAt());
        response.setLastLoginAt(user.getLastLoginAt());
        response.setGamificationPoints(user.getGamificationPoints());

        // Check of de property bestaat (Staff/Admin hebben dit niet) om een NullPointerException te voorkomen
        if (user.getProperty() != null) {
            response.setPropertyId(user.getProperty().getId());
        }

        return response;
    }

    public static UserResponse from(User user) {
        return fromUser(user);
    }
}
