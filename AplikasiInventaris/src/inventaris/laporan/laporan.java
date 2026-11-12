
package inventaris.laporan;

import inventaris.Koneksi;

import java.awt.Desktop;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.JOptionPane;

public class laporan {

    public static void cetakLaporan() {

        StringBuilder html = new StringBuilder();

        String tanggal = LocalDate.now().format(
                DateTimeFormatter.ofPattern(
                        "dd MMMM yyyy",
                        new Locale("id", "ID")
                )
        );

        html.append("""
            <!DOCTYPE html>
            <html lang="id">
            <head>
                <meta charset="UTF-8">
                <title>Laporan Inventaris Toko</title>

                <style>
                    * {
                        box-sizing: border-box;
                    }

                    body {
                        font-family: Arial, sans-serif;
                        margin: 30px;
                        color: #333;
                        background: #fff;
                    }

                    .header {
                        text-align: center;
                        border-bottom: 3px solid #2563eb;
                        padding-bottom: 15px;
                        margin-bottom: 25px;
                    }

                    .header h1 {
                        margin: 0;
                        font-size: 24px;
                        color: #1e3a8a;
                    }

                    .header h2 {
                        margin: 8px 0;
                        font-size: 18px;
                        color: #333;
                    }

                    .header p {
                        margin: 5px 0;
                        font-size: 13px;
                    }

                    .print-button {
                        text-align: right;
                        margin-bottom: 20px;
                    }

                    .print-button button {
                        padding: 10px 20px;
                        background-color: #2563eb;
                        color: white;
                        border: none;
                        border-radius: 5px;
                        cursor: pointer;
                        font-size: 14px;
                    }

                    .print-button button:hover {
                        background-color: #1d4ed8;
                    }

                    h3 {
                        margin-top: 30px;
                        margin-bottom: 10px;
                        color: #1e3a8a;
                    }

                    table {
                        width: 100%;
                        border-collapse: collapse;
                        margin-bottom: 25px;
                        font-size: 12px;
                    }

                    th {
                        background-color: #2563eb;
                        color: white;
                        padding: 10px;
                        border: 1px solid #ddd;
                        text-align: center;
                    }

                    td {
                        padding: 8px;
                        border: 1px solid #ddd;
                        vertical-align: top;
                    }

                    td:first-child {
                        text-align: center;
                    }

                    tr:nth-child(even) {
                        background-color: #f3f4f6;
                    }

                    .empty {
                        text-align: center;
                        padding: 15px;
                        color: #777;
                    }

                    .footer {
                        margin-top: 30px;
                        padding-top: 10px;
                        border-top: 1px solid #ddd;
                        text-align: center;
                        font-size: 11px;
                        color: #777;
                    }

                    @page {
                        size: A4 landscape;
                        margin: 15mm;
                    }

                    @media print {
                        body {
                            margin: 0;
                        }

                        .print-button {
                            display: none;
                        }

                        h3 {
                            break-after: avoid;
                        }

                        tr {
                            break-inside: avoid;
                        }
                    }
                </style>
            </head>
            <body>
            """);

        // HEADER LAPORAN
        html.append("""
            <div class="header">
                <h1>APLIKASI INVENTARIS TOKO</h1>
                <h2>LAPORAN DATA INVENTARIS</h2>
            """);

        html.append("<p>Tanggal Cetak: ")
                .append(tanggal)
                .append("</p></div>");

        // TOMBOL CETAK
        html.append("""
            <div class="print-button">
                <button onclick="window.print()">
                    Cetak Laporan
                </button>
            </div>
            """);

        // MENAMPILKAN SELURUH LAPORAN
        try {
            laporanBarang(html);
            laporanKategori(html);
            laporanSupplier(html);
            laporanTransaksi(html);

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Gagal mengambil data laporan: " + e.getMessage(),
                    "Kesalahan Database",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        // FOOTER
        html.append("""
            <div class="footer">
                Laporan ini dibuat secara otomatis oleh Aplikasi Inventaris Toko.
            </div>
            </body>
            </html>
            """);

        // MEMBUAT FILE HTML DAN MEMBUKANYA DI BROWSER
        try {

            Path file = Files.createTempFile(
                    "laporan-inventaris-",
                    ".html"
            );

            Files.writeString(
                    file,
                    html.toString(),
                    StandardCharsets.UTF_8
            );

            if (Desktop.isDesktopSupported()
                    && Desktop.getDesktop().isSupported(
                            Desktop.Action.OPEN
                    )) {

                Desktop.getDesktop().open(file.toFile());

            } else {

                JOptionPane.showMessageDialog(
                        null,
                        "Laporan berhasil dibuat:\n"
                        + file.toAbsolutePath()
                );
            }

        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                    null,
                    "Gagal membuat laporan: " + e.getMessage(),
                    "Kesalahan",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // LAPORAN DATA BARANG
    private static void laporanBarang(StringBuilder html)
            throws SQLException {

        html.append("""
            <h3>1. Data Barang</h3>

            <table>
                <tr>
                    <th>No.</th>
                    <th>Kode Barang</th>
                    <th>Nama Barang</th>
                    <th>Kategori</th>
                    <th>Supplier</th>
                    <th>Harga Beli</th>
                    <th>Harga Jual</th>
                    <th>Stok</th>
                </tr>
            """);

        String sql = """
            SELECT
                b.kode_barang,
                b.nama_barang,
                k.nama_kategori,
                s.nama_supplier,
                b.harga_beli,
                b.harga_jual,
                b.stok
            FROM barang b
            JOIN kategori k
                ON b.id_kategori = k.id_kategori
            JOIN supplier s
                ON b.id_supplier = s.id_supplier
            ORDER BY b.id_barang ASC
            """;

        int no = 1;

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                html.append("<tr>");

                html.append("<td>").append(no++).append("</td>");

                html.append("<td>")
                        .append(escapeHtml(rs.getString("kode_barang")))
                        .append("</td>");

                html.append("<td>")
                        .append(escapeHtml(rs.getString("nama_barang")))
                        .append("</td>");

                html.append("<td>")
                        .append(escapeHtml(rs.getString("nama_kategori")))
                        .append("</td>");

                html.append("<td>")
                        .append(escapeHtml(rs.getString("nama_supplier")))
                        .append("</td>");

                html.append("<td>")
                        .append(formatRupiah(
                                rs.getBigDecimal("harga_beli")
                        ))
                        .append("</td>");

                html.append("<td>")
                        .append(formatRupiah(
                                rs.getBigDecimal("harga_jual")
                        ))
                        .append("</td>");

                html.append("<td>")
                        .append(rs.getInt("stok"))
                        .append("</td>");

                html.append("</tr>");
            }

            if (no == 1) {
                html.append("""
                    <tr>
                        <td colspan="8" class="empty">
                            Belum ada data barang.
                        </td>
                    </tr>
                    """);
            }
        }

        html.append("</table>");
    }

    // LAPORAN DATA KATEGORI
    private static void laporanKategori(StringBuilder html)
            throws SQLException {

        html.append("""
            <h3>2. Data Kategori</h3>

            <table>
                <tr>
                    <th>No.</th>
                    <th>Nama Kategori</th>
                </tr>
            """);

        String sql = """
            SELECT nama_kategori
            FROM kategori
            ORDER BY id_kategori ASC
            """;

        int no = 1;

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                html.append("<tr>");

                html.append("<td>")
                        .append(no++)
                        .append("</td>");

                html.append("<td>")
                        .append(escapeHtml(
                                rs.getString("nama_kategori")
                        ))
                        .append("</td>");

                html.append("</tr>");
            }

            if (no == 1) {
                html.append("""
                    <tr>
                        <td colspan="2" class="empty">
                            Belum ada data kategori.
                        </td>
                    </tr>
                    """);
            }
        }

        html.append("</table>");
    }

    // LAPORAN DATA SUPPLIER
    private static void laporanSupplier(StringBuilder html)
            throws SQLException {

        html.append("""
            <h3>3. Data Supplier</h3>

            <table>
                <tr>
                    <th>No.</th>
                    <th>Nama Supplier</th>
                    <th>Alamat</th>
                    <th>No. Telepon</th>
                </tr>
            """);

        String sql = """
            SELECT nama_supplier, alamat, no_telp
            FROM supplier
            ORDER BY id_supplier ASC
            """;

        int no = 1;

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                html.append("<tr>");

                html.append("<td>")
                        .append(no++)
                        .append("</td>");

                html.append("<td>")
                        .append(escapeHtml(
                                rs.getString("nama_supplier")
                        ))
                        .append("</td>");

                html.append("<td>")
                        .append(escapeHtml(
                                rs.getString("alamat")
                        ))
                        .append("</td>");

                html.append("<td>")
                        .append(escapeHtml(
                                rs.getString("no_telp")
                        ))
                        .append("</td>");

                html.append("</tr>");
            }

            if (no == 1) {
                html.append("""
                    <tr>
                        <td colspan="4" class="empty">
                            Belum ada data supplier.
                        </td>
                    </tr>
                    """);
            }
        }

        html.append("</table>");
    }

    // LAPORAN TRANSAKSI STOK
    private static void laporanTransaksi(StringBuilder html)
            throws SQLException {

        html.append("""
            <h3>4. Data Transaksi Stok</h3>

            <table>
                <tr>
                    <th>No.</th>
                    <th>Nama Barang</th>
                    <th>Jenis Transaksi</th>
                    <th>Jumlah</th>
                    <th>Tanggal</th>
                    <th>Keterangan</th>
                </tr>
            """);

        String sql = """
            SELECT
                b.nama_barang,
                t.jenis_transaksi,
                t.jumlah,
                t.tanggal,
                t.keterangan
            FROM transaksi_stok t
            JOIN barang b
                ON t.id_barang = b.id_barang
            ORDER BY t.id_transaksi ASC
            """;

        int no = 1;

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                html.append("<tr>");

                html.append("<td>")
                        .append(no++)
                        .append("</td>");

                html.append("<td>")
                        .append(escapeHtml(
                                rs.getString("nama_barang")
                        ))
                        .append("</td>");

                html.append("<td>")
                        .append(escapeHtml(
                                rs.getString("jenis_transaksi")
                        ))
                        .append("</td>");

                html.append("<td>")
                        .append(rs.getInt("jumlah"))
                        .append("</td>");

                html.append("<td>")
                        .append(rs.getDate("tanggal"))
                        .append("</td>");

                html.append("<td>")
                        .append(escapeHtml(
                                rs.getString("keterangan")
                        ))
                        .append("</td>");

                html.append("</tr>");
            }

            if (no == 1) {
                html.append("""
                    <tr>
                        <td colspan="6" class="empty">
                            Belum ada data transaksi stok.
                        </td>
                    </tr>
                    """);
            }
        }

        html.append("</table>");
    }

    // FORMAT RUPIAH
    private static String formatRupiah(BigDecimal angka) {

        if (angka == null) {
            return "Rp0";
        }

        return "Rp" + String.format(
                new Locale("id", "ID"),
                "%,.0f",
                angka
        );
    }

    // MENGAMANKAN TEKS HTML
    private static String escapeHtml(String teks) {

        if (teks == null) {
            return "-";
        }

        return teks
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
