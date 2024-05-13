package org.judexmars.imagecrud.mapper;

import org.judexmars.imagecrud.dto.account.AccountDto;
import org.judexmars.imagecrud.dto.account.CreateAccountDto;
import org.judexmars.imagecrud.model.AccountEntity;
import org.mapstruct.Mapper;

/**
 * Mapper for {@link AccountEntity} and {@link AccountDto}.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

  AccountDto toAccountDto(AccountEntity accountEntity);

  AccountEntity toAccountEntity(CreateAccountDto createAccountDto);
}
