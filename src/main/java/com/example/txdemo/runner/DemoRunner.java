package com.example.txdemo.runner;

import com.example.txdemo.service.TransactionalTransferService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

//prints a tiny before-after demo to console when app is run
//FAILED transfer - changes nothing (rolled back)
//SUCCESSFUL transfer - commits both updates
@Component
public class DemoRunner implements CommandLineRunner {
    private final TransactionalTransferService txService;
    private final JdbcTemplate jdbc;

    public DemoRunner(TransactionalTransferService txService, JdbcTemplate jdbc) {
        this.txService = txService;
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) throws Exception {
        line();
        System.out.println("@Transactional money-transfer demo");
        line();

        printBalances("Initial balances");

        System.out.println("\n>> Attempting a transfer that FAILS mid-way (Alice to Bob, 100)...");
        try{
            txService.transfer(1,2, new BigDecimal("100.00"), true);
        } catch(Exception e){
            System.out.println("Caught: " + e.getMessage());
        }
        printBalances("After FAILED transfer (nothing changes since Spring rolled back)");

        System.out.println("\n>> Attempting a transfer that SUCCEEDS (Alice to Bob, 100)...");
        txService.transfer(1, 2, new BigDecimal("100.00"), false);
        printBalances("After a SUCCESS transfer (both updates committed together)");
        line();
    }

    private void printBalances(String label){
        BigDecimal alice = jdbc.queryForObject("SELECT balance FROM account WHERE id=1", BigDecimal.class);
        BigDecimal bob = jdbc.queryForObject("SELECT balance FROM account WHERE id=2", BigDecimal.class);
        System.out.printf("%n%s%n Alice: %8s    Bob: %8s%n", label, alice, bob);
    }

    public void line(){
        System.out.println("------------------------------");
    }
}
