package com.example.txdemo.service;

import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

//Hard Way - Atomicity being wired in manually
//business logic = debit + credit
//surrounding logic = opening a connection, turn off auto-commit, commit on success, roll back on failure, close connection in final block
@Service
public class ManualTransferService {
    private final DataSource dataSource;

    public ManualTransferService(DataSource dataSource){
        this.dataSource = dataSource;
    }

    public void transfer(long fromId, long toId, BigDecimal amount, boolean simulateFailure){
        Connection conn = null;
        try {
            conn = dataSource.getConnection();
            conn.setAutoCommit(false);

            debit(conn, fromId, amount);

            //simulating a crash after money left source
            if(simulateFailure){
                throw new RuntimeException("Something failed mid-transfer!");
            }

            credit(conn, toId, amount); //commit if both succeeded
        } catch (Exception e) {
            rollbackQuietly(conn); //undo everything
            throw new RuntimeException("Transfer failed: " + e.getMessage(), e);
        } finally {
            closeQuietly(conn); //cleanup
        }
    }

    private void debit(Connection conn, long id, BigDecimal amount) throws Exception{
        try (PreparedStatement ps = conn.prepareStatement
                ("UPDATE account SET balance = balance - ? WHERE id = ?")){
            ps.setBigDecimal(1, amount);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    private void credit(Connection conn, long id, BigDecimal amount) throws Exception{
        try (PreparedStatement ps = conn.prepareStatement
                ("UPDATE account SET balance = balance + ? WHERE id = ?")){
            ps.setBigDecimal(1, amount);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    private void rollbackQuietly(Connection conn) {
        if(conn != null){
            try {
                conn.rollback();
            } catch (Exception ignored){
                System.out.println(ignored.getMessage());
            }
        }
    }

    private void closeQuietly(Connection conn) {
        if(conn != null){
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (Exception ignored){
                System.out.println(ignored.getMessage());
            }
        }
    }
}
