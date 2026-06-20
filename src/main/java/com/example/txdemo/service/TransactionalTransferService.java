package com.example.txdemo.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

//Easy Way - Atomicity by declaration
//@Transactional annotation tells Spring to treat whole method as ONE unit
//Spring wraps it in proxy that opens before a method runs, commits if normal, rolls back if RuntimeException gets thrown
@Service
public class TransactionalTransferService {
    private final JdbcTemplate jdbc;

    public TransactionalTransferService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void transfer(long fromId, long toId, BigDecimal amount, boolean simulateFailure){
        jdbc.update("UPDATE account SET balance = balance - ? WHERE id = ?", amount, fromId);

        if (simulateFailure){
            throw new RuntimeException("Something failed mid-transfer!");
        }

        jdbc.update("UPDATE account SET balance = balance + ? WHERE id = ?", amount, toId);
    }
}
