package org.judexmars.imagecrud.dto.account;

import java.util.UUID;

/**
 * Account DTO response.
 *
 * @param id id of the account
 * @param username username of the account
 */
public record AccountDto(
    UUID id,
    String username
) {
}
