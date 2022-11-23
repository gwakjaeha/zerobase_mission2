package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.account.type.AccountStatus.IN_USE;
import static com.example.account.type.ErrorCode.*;

@Service
//꼭 필요한 arg 가 들어간 생성자를 만들어줌, final 타입은 생성자에서만 값을 초기화 할 수 있기 때문에 이런 변수들에 대한 생성자를 만들어줌
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;

    /**
     * 사용자가 있는지 조회
     * 계좌의 번호를 생성하고
     * 계좌를 저장하고, 그 정보를 넘긴다.
     */
    @Transactional //테이블에 저장
    public AccountDto createAccount(Long userId, Long initialBalance){
        AccountUser accountUser = getAccountUser(userId);

        String newAccountNumber = createNewAccountNumber();

        validateCreateAccount(accountUser, newAccountNumber);

        return AccountDto.fromEntity(
                accountRepository.save(Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(IN_USE)
                        .accountNumber(newAccountNumber)
                        .balance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build())
        );
    }

    private String createNewAccountNumber(){

        String NewAccount = "";
        int count = 0;

        //랜덤 계좌번호를 생성하여 db에서 찾아보고 이미 등록되어 있으면 다시 생성.
        //사용가능한 번호를 찾을때까지 최대 10000번 반복하고 그때까지 못찾으면 사용가능한 계좌번호의 수가 부족하다는 경고메시지를 보냄(validate 과정에서).
        while(count < 10000) {

            for (int i = 0; i < 10; i++) {   // 10자리 숫자 랜덤 생성
                NewAccount += (int) (Math.random() * 10);
            }

            Integer numberOfAccount = accountRepository.countByAccountNumber(NewAccount);

            if(numberOfAccount == 0) break; //db에 중복되는 계좌번호가 없으면 해당 계좌를 사용.

            NewAccount = "";
            count += 1;
        }

        return NewAccount;
    }

    private void validateCreateAccount(AccountUser accountUser, String newAccountNumber) {
        if(accountRepository.countByAccountUserAndAccountStatus(accountUser, IN_USE) >= 10) {
            throw new AccountException(MAX_ACCOUNT_PER_USER_10);
        }
        if("".equals(newAccountNumber)) {
            throw new AccountException(INSUFFICIENT_ACCOUNT_NUMBER);
        }
    }

    @Transactional
    public Account getAccount(Long id){
        if(id < 0){
            throw new RuntimeException("Minus");
        }
        return accountRepository.findById(id).get();
    }

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = getAccountUser(userId);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ACCOUNT_NOT_FOUND));

        validateDeleteAccount(accountUser, account);

        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnregisteredAt(LocalDateTime.now());

        accountRepository.save(account); // 굳이 없어도 동작함. 테스트를 위해 넣어줌.

        return AccountDto.fromEntity(account);
    }

    private void validateDeleteAccount(AccountUser accountUser, Account account) {
        if(!Objects.equals(accountUser.getId(), account.getAccountUser().getId())) {
            throw new AccountException(USER_ACCOUNT_UN_MATCH);
        }
        if(account.getAccountStatus() == AccountStatus.UNREGISTERED){
            throw new AccountException(ACCOUNT_ALREADY_UNREGISTERED);
        }
        if(account.getBalance() > 0) {
            throw new AccountException(BALANCE_NOT_EMPTY);
        }
    }

    @Transactional
    public List<AccountDto> getAccountsByUserId(Long userId) {
        AccountUser accountUser = getAccountUser(userId);

        List<Account> accounts = accountRepository
                .findByAccountUser(accountUser);

        return accounts.stream()
                .map(AccountDto::fromEntity)
                //.map(account -> AccountDto.fromEntity(account)) 와 동일
                .collect(Collectors.toList());
    }

    private AccountUser getAccountUser(Long userId) {
        return accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(USER_NOT_FOUND));
    }
}
