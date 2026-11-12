/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventaris;

/**
 *
 * @author admin
 */
public class Main {
    public static void main(String[] args) {
        try {
            java.sql.Connection c = Koneksi.getConnection();
            System.out.println("Koneksi Berhasil! Database terhubung.");
        } catch (Exception e) {
            System.out.println("Koneksi Gagal: " + e.getMessage());
        }
    }
}