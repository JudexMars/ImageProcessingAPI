package org.judexmars.imagecrud.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.judexmars.imagecrud.dto.account.AccountDto;
import org.judexmars.imagecrud.dto.account.CreateAccountDto;
import org.judexmars.imagecrud.exception.AccountAlreadyExistsException;
import org.judexmars.imagecrud.exception.AccountNotFoundException;
import org.judexmars.imagecrud.exception.ConfirmPasswordException;
import org.judexmars.imagecrud.exception.NoSuchRoleException;
import org.judexmars.imagecrud.mapper.AccountMapper;
import org.judexmars.imagecrud.model.AccountEntity;
import org.judexmars.imagecrud.model.RoleEntity;
import org.judexmars.imagecrud.repository.AccountRepository;
import org.judexmars.imagecrud.repository.RoleRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;

    private final PasswordEncoder passwordEncoder;

    private final AccountMapper accountMapper;

    private final RoleRepository roleRepository;

    /**
     * Load {@link UserDetails by username}
     *
     * @param username provided username
     * @return {@link UserDetails}
     * @throws UsernameNotFoundException if there's no user with such username
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws AccountNotFoundException {
        return accountRepository.findByUsername(username).orElseThrow(() -> new AccountNotFoundException(username));
    }

    /**
     * Get account by username
     *
     * @param username username of the selected account
     * @return account as {@link AccountDto}
     */
    public AccountDto getByUsername(String username) {
        return accountMapper.toAccountDto(getEntityByUsername(username));
    }

    /**
     * Create new account from provided DTO
     *
     * @param createAccountDto dto containing all the information needed for creation
     * @return created account as {@link AccountDto}
     */
    public AccountDto createAccount(CreateAccountDto createAccountDto) {
        if (accountRepository.findByUsername(createAccountDto.username()).isPresent()) {
            throw new AccountAlreadyExistsException(createAccountDto.username());
        }
        if (!createAccountDto.password().equals(createAccountDto.confirmPassword())) {
            throw new ConfirmPasswordException();
        }
        log.info("Create account begins (exceptions passed)");
        var account = accountMapper.toAccountEntity(createAccountDto);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        account.setEnabled(true);
        var roles = new ArrayList<RoleEntity>();
        roles.add(getDefaultRole());
        account.setRoles(roles);
        var createdAccount = accountRepository.save(account);
        log.info("Created account: " + createdAccount);
        return accountMapper.toAccountDto(createdAccount);
    }

    /**
     * Get role entity by its name
     *
     * @param name name of the role
     * @return {@link RoleEntity}
     */
    public RoleEntity getRoleByName(String name) {
        return roleRepository.findByName(name).orElseThrow(() -> new NoSuchRoleException(name));
    }

    /**
     * Get default role entity
     *
     * @return {@link RoleEntity}
     */
    public RoleEntity getDefaultRole() {
        return getRoleByName("ROLE_VIEWER");
    }

    AccountEntity getEntityByUsername(String username) {
        return (AccountEntity) loadUserByUsername(username);
    }
}
