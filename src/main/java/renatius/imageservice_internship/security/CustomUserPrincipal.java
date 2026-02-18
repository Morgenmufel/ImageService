package renatius.imageservice_internship.security;

import java.util.UUID;

public record CustomUserPrincipal(UUID id, String username) {
}
