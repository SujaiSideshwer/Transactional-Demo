package com.example.txdemo;

import com.example.txdemo.service.ManualTransferService;
import com.example.txdemo.service.TransactionalTransferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class TransferServiceTest {
    @Autowired ManualTransferService manualService;
    @Autowired TransactionalTransferService txService;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void resetBalances(){
        jdbc.update("UPDATE account SET balance = 1000.00 WHERE id = 1");
        jdbc.update("UPDATE account SET balance = 500.00 WHERE id = 2");
    }

    private BigDecimal balanceOf(long id){
        return jdbc.queryForObject("SELECT balance FROM account WHERE id = ?", BigDecimal.class, id);
    }

    private void assertBalance(long id, String expected){
        assertEquals(0, balanceOf(id).compareTo(new BigDecimal(expected)),
                "Unexpected balance for account " + id);
    }

    @Test
    void transactional_rollsBack_onFailure(){
        assertThrows(RuntimeException.class, () -> txService.transfer(1, 2, new BigDecimal("100.00"), true));
        assertBalance(1, "1000.00");
        assertBalance(2, "500.00");
    }

    @Test
    void transactional_commits_onSuccess(){
        txService.transfer(1, 2, new BigDecimal("100.00"), false);
        assertBalance(1, "900.00");
        assertBalance(2, "600.00");
    }

    @Test
    void manual_rollsBack_onFailure(){
        assertThrows(RuntimeException.class,
                () -> manualService.transfer(1, 2, new BigDecimal("100.00"), true));
        assertBalance(1, "1000.00");
        assertBalance(2, "500.00");
    }

    @Test
    void manual_commits_onSuccess(){
        manualService.transfer(1, 2, new BigDecimal("100.00"), false);
        assertBalance(1, "900.00");
        assertBalance(2, "600.00");
    }
}
