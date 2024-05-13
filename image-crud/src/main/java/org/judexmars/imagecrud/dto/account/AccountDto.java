package org.judexmars.imagecrud.dto.account;

import java.util.UUID;

public record AccountDto(
        UUID id,
        String username
) {
}
