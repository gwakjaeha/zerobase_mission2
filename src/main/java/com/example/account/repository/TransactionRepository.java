package com.example.account.repository;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//Account table 에 접속하기 위한 인터페이스
@Repository //Bean 으로 등록하기 위해
public interface TransactionRepository
        extends JpaRepository<Transaction, Long> { //<레파지토리가 활용하게 될 엔티티, 엔티티 pk의 타입>
    Optional<Transaction> findByTransactionId(String transactionId);
}
