package inventaris;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author admin
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class Koneksi {
    // Ganti "inventaris_toko" jika nama database-mu berbeda
    private static final String URL = "jdbc:mysql://localhost:3306/inventaris_toko";
    private static final String USER = "root";
    private static final String PASS = ""; // Biasanya kosong di XAMPP

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
