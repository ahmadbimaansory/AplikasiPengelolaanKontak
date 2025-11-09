package view;

import controller.KontakController;
import java.io.*;
import model.Kontak;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author BIMA
 */
public class PengelolaanKontakFrame extends javax.swing.JFrame {
    
    private DefaultTableModel model;
    private KontakController controller;

    /**
     * Creates new form PengelolaanKontakFrame
     */
    public PengelolaanKontakFrame() {
        initComponents();
        
        controller = new KontakController();
        model = new DefaultTableModel(new String[]
                   {"No", "Nama", "Nomor Telepon", "Kategori"}, 0);
        tblKontak.setModel(model);
        
        loadContacts();
    }
    
    // Method untuk memuat semua kontak dari database ke tabel
    private void loadContacts() {
        try {
            // Bersihkan tabel sebelum memuat ulang data
            model.setRowCount(0);

            // Ambil semua data kontak dari controller
            List<Kontak> contacts = controller.getAllContacts();

            int rowNumber = 1;
            for (Kontak contact : contacts) {
                model.addRow(new Object[]{
                    rowNumber++,
                    contact.getNama(),
                    contact.getNomorTelepon(),
                    contact.getKategori()
                });
            }
        } catch (SQLException e) {
            showError(e.getMessage());
        }
    }

    // Method untuk menampilkan pesan error
    private void showError(String message) {
        JOptionPane.showMessageDialog(
            this,
            message,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    // Method untuk menambahkan kontak baru
    private void addContact() {
        String nama = txtNama.getText().trim();
        String nomorTelepon = txtNomorTelepon.getText().trim();
        String kategori = (String) cmbKategori.getSelectedItem();

        // Validasi nomor telepon
        if (!validatePhoneNumber(nomorTelepon)) {
            return;
        }

        try {
            // Cek apakah nomor telepon sudah ada
            if (controller.isDuplicatePhoneNumber(nomorTelepon, null)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Kontak dengan nomor telepon ini sudah ada.",
                    "Kesalahan",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            // Tambahkan kontak ke database
            controller.addContact(nama, nomorTelepon, kategori);

            // Muat ulang data di tabel
            loadContacts();

            JOptionPane.showMessageDialog(
                this,
                "Kontak berhasil ditambahkan!"
            );

            // Bersihkan input setelah berhasil menambah
            clearInputFields();

        } catch (SQLException ex) {
            showError("Gagal menambahkan kontak: " + ex.getMessage());
        }
    }

    // Method untuk validasi nomor telepon
    private boolean validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nomor telepon tidak boleh kosong.");
            return false;
        }

        if (!phoneNumber.matches("\\d+")) { // Hanya angka
            JOptionPane.showMessageDialog(this, "Nomor telepon hanya boleh berisi angka.");
            return false;
        }

        if (phoneNumber.length() < 8 || phoneNumber.length() > 15) { // Panjang 8–15
            JOptionPane.showMessageDialog(this, "Nomor telepon harus memiliki panjang antara 8 hingga 15 karakter.");
            return false;
        }

        return true;
    }

    // Method untuk mengosongkan field input
    private void clearInputFields() {
        txtNama.setText("");
        txtNomorTelepon.setText("");
        cmbKategori.setSelectedIndex(0);
    }
    
    // Method untuk mengedit kontak yang dipilih di tabel
    private void editContact() {
        int selectedRow = tblKontak.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Pilih kontak yang ingin diperbarui.",
                "Kesalahan",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int id = (int) model.getValueAt(selectedRow, 0);
        String nama = txtNama.getText().trim();
        String nomorTelepon = txtNomorTelepon.getText().trim();
        String kategori = (String) cmbKategori.getSelectedItem();

        if (!validatePhoneNumber(nomorTelepon)) {
            return;
        }

        try {
            if (controller.isDuplicatePhoneNumber(nomorTelepon, id)) {
                JOptionPane.showMessageDialog(
                    this,
                    "Kontak dengan nomor telepon ini sudah ada.",
                    "Kesalahan",
                    JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            controller.updateContact(id, nama, nomorTelepon, kategori);
            loadContacts();
            JOptionPane.showMessageDialog(
                this,
                "Kontak berhasil diperbarui!"
            );
            clearInputFields();
        } catch (SQLException ex) {
            showError("Gagal memperbarui kontak: " + ex.getMessage());
        }
    }

    // Method untuk menampilkan data kontak ke field input saat baris di tabel dipilih
    private void populateInputFields(int selectedRow) {
        // Ambil data dari JTable
        String nama = model.getValueAt(selectedRow, 1).toString();
        String nomorTelepon = model.getValueAt(selectedRow, 2).toString();
        String kategori = model.getValueAt(selectedRow, 3).toString();

        // Set data ke komponen input
        txtNama.setText(nama);
        txtNomorTelepon.setText(nomorTelepon);
        cmbKategori.setSelectedItem(kategori);
    }

    // Method untuk menghapus kontak yang dipilih dari tabel
    private void deleteContact() {
        int selectedRow = tblKontak.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                this,
                "Pilih kontak yang ingin dihapus.",
                "Kesalahan",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        int id = (int) model.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Apakah Anda yakin ingin menghapus kontak ini?",
            "Konfirmasi Hapus",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                controller.deleteContact(id);
                loadContacts();
                JOptionPane.showMessageDialog(this, "Kontak berhasil dihapus!");
                clearInputFields();
            } catch (SQLException e) {
                showError("Gagal menghapus kontak: " + e.getMessage());
            }
        }
    }

    // Method untuk mencari kontak berdasarkan kata kunci (nama, nomor telepon, atau kategori)
    private void searchContact() {
        String keyword = txtPencarian.getText().trim();

        if (!keyword.isEmpty()) {
            try {
                List<Kontak> contacts = controller.searchContacts(keyword);

                // Bersihkan tabel sebelum menampilkan hasil pencarian
                model.setRowCount(0);

                for (Kontak contact : contacts) {
                    model.addRow(new Object[]{
                        contact.getId(),
                        contact.getNama(),
                        contact.getNomorTelepon(),
                        contact.getKategori()
                    });
                }

                if (contacts.isEmpty()) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Tidak ada kontak ditemukan.",
                        "Pencarian",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                }

            } catch (SQLException ex) {
                showError("Gagal mencari kontak: " + ex.getMessage());
            }

        } else {
            // Jika kolom pencarian kosong, tampilkan semua kontak
            loadContacts();
        }
    }
    
    // ============================
    // EXPORT DATA KE FILE CSV
    // ============================
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Simpan File CSV");

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();

            // Tambahkan ekstensi .csv jika belum ada
            if (!fileToSave.getAbsolutePath().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                // Header CSV
                writer.write("ID,Nama,Nomor Telepon,Kategori\n");

                // Tulis setiap baris dari tabel ke CSV
                for (int i = 0; i < model.getRowCount(); i++) {
                    writer.write(
                        model.getValueAt(i, 0) + "," +
                        model.getValueAt(i, 1) + "," +
                        model.getValueAt(i, 2) + "," +
                        model.getValueAt(i, 3) + "\n"
                    );
                }

                JOptionPane.showMessageDialog(
                    this,
                    "Data berhasil diekspor ke: " + fileToSave.getAbsolutePath(),
                    "Export Berhasil",
                    JOptionPane.INFORMATION_MESSAGE
                );

            } catch (IOException ex) {
                showError("Gagal menulis file: " + ex.getMessage());
            }
        }
    }


    // ============================
    // IMPORT DATA DARI FILE CSV
    // ============================
    private void importFromCSV() {
        showCSVGuide();

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Apakah Anda yakin file CSV yang dipilih sudah sesuai dengan format?",
            "Konfirmasi Impor CSV",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Pilih File CSV");

            int userSelection = fileChooser.showOpenDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToOpen = fileChooser.getSelectedFile();

                try (BufferedReader reader = new BufferedReader(new FileReader(fileToOpen))) {
                    String line = reader.readLine(); // Baca header

                    // Validasi header CSV
                    if (!validateCSVHeader(line)) {
                        JOptionPane.showMessageDialog(
                            this,
                            "Format header CSV tidak valid.\nPastikan header adalah: ID,Nama,Nomor Telepon,Kategori",
                            "Kesalahan CSV",
                            JOptionPane.ERROR_MESSAGE
                        );
                        return;
                    }

                    int rowCount = 0;
                    int errorCount = 0;
                    int duplicateCount = 0;
                    StringBuilder errorLog = new StringBuilder("Baris dengan kesalahan:\n");

                    // Baca baris data
                    while ((line = reader.readLine()) != null) {
                        rowCount++;
                        String[] data = line.split(",");

                        if (data.length != 4) {
                            errorCount++;
                            errorLog.append("Baris ").append(rowCount + 1)
                                    .append(": Format kolom tidak sesuai.\n");
                            continue;
                        }

                        String nama = data[1].trim();
                        String nomorTelepon = data[2].trim();
                        String kategori = data[3].trim();

                        // Validasi isi data
                        if (nama.isEmpty() || nomorTelepon.isEmpty()) {
                            errorCount++;
                            errorLog.append("Baris ").append(rowCount + 1)
                                    .append(": Nama atau Nomor Telepon kosong.\n");
                            continue;
                        }

                        if (!validatePhoneNumber(nomorTelepon)) {
                            errorCount++;
                            errorLog.append("Baris ").append(rowCount + 1)
                                    .append(": Nomor Telepon tidak valid.\n");
                            continue;
                        }

                        // Cek duplikat nomor telepon
                        try {
                            if (controller.isDuplicatePhoneNumber(nomorTelepon, null)) {
                                duplicateCount++;
                                errorLog.append("Baris ").append(rowCount + 1)
                                        .append(": Kontak sudah ada.\n");
                                continue;
                            }
                        } catch (SQLException ex) {
                            Logger.getLogger(PengelolaanKontakFrame.class.getName())
                                  .log(Level.SEVERE, null, ex);
                        }

                        // Tambahkan data ke database
                        try {
                            controller.addContact(nama, nomorTelepon, kategori);
                        } catch (SQLException ex) {
                            errorCount++;
                            errorLog.append("Baris ").append(rowCount + 1)
                                    .append(": Gagal menyimpan ke database - ")
                                    .append(ex.getMessage()).append("\n");
                        }
                    }

                    // Refresh tabel
                    loadContacts();

                    // Tampilkan hasil impor
                    if (errorCount > 0 || duplicateCount > 0) {
                        errorLog.append("\nTotal baris dengan kesalahan: ")
                                .append(errorCount).append("\n");
                        errorLog.append("Total baris duplikat: ")
                                .append(duplicateCount).append("\n");

                        JOptionPane.showMessageDialog(
                            this,
                            errorLog.toString(),
                            "Kesalahan Impor",
                            JOptionPane.WARNING_MESSAGE
                        );
                    } else {
                        JOptionPane.showMessageDialog(
                            this,
                            "Semua data berhasil diimpor.",
                            "Impor Berhasil",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }

                } catch (IOException ex) {
                    showError("Gagal membaca file: " + ex.getMessage());
                }
            }
        }
    }


    // ============================
    // PANDUAN FORMAT CSV
    // ============================
    private void showCSVGuide() {
        String guideMessage =
            "Format CSV untuk impor data:\n" +
            "- Header wajib: ID,Nama,Nomor Telepon,Kategori\n" +
            "- ID dapat kosong (akan diisi otomatis)\n" +
            "- Nama dan Nomor Telepon wajib diisi\n" +
            "- Contoh isi file CSV:\n" +
            " 1,Andi,08123456789,Teman\n" +
            " 2,Budi Doremi,08567890123,Keluarga\n\n" +
            "Pastikan file CSV sesuai format sebelum melakukan impor.";

        JOptionPane.showMessageDialog(
            this,
            guideMessage,
            "Panduan Format CSV",
            JOptionPane.INFORMATION_MESSAGE
        );
    }


    // ============================
    // VALIDASI HEADER CSV
    // ============================
    private boolean validateCSVHeader(String header) {
        return header != null &&
               header.trim().equalsIgnoreCase("ID,Nama,Nomor Telepon,Kategori");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        lblJudul = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        txtNama = new javax.swing.JTextField();
        txtNomorTelepon = new javax.swing.JTextField();
        txtPencarian = new javax.swing.JTextField();
        cmbKategori = new javax.swing.JComboBox<>();
        btnTambah = new javax.swing.JButton();
        btnEdit = new javax.swing.JButton();
        btnHapus = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblKontak = new javax.swing.JTable();
        btnExport = new javax.swing.JButton();
        btnImport = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Aplikasi Pengelolaan Kontak");

        lblJudul.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        lblJudul.setText(" APLIKASI PENGELOLAAN KONTAK");
        jPanel1.add(lblJudul);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        jLabel1.setText("Nama Kontak");

        jLabel2.setText("Nomor Telepon");

        jLabel3.setText("Kategori");

        jLabel4.setText("Pencarian");

        txtPencarian.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtPencarianKeyTyped(evt);
            }
        });

        cmbKategori.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Keluarga", "Teman", "Kantor" }));

        btnTambah.setText("Tambah");
        btnTambah.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnTambahActionPerformed(evt);
            }
        });

        btnEdit.setText("Edit");
        btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnEditActionPerformed(evt);
            }
        });

        btnHapus.setText("Hapus");
        btnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHapusActionPerformed(evt);
            }
        });

        tblKontak.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        tblKontak.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblKontakMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblKontak);

        btnExport.setText("Export");
        btnExport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExportActionPerformed(evt);
            }
        });

        btnImport.setText("Import");
        btnImport.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnImportActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 477, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addGap(30, 30, 30)
                                .addComponent(txtNama))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel4))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(txtNomorTelepon)
                                    .addComponent(cmbKategori, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(txtPencarian)))))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addGap(144, 144, 144)
                        .addComponent(btnTambah)
                        .addGap(44, 44, 44)
                        .addComponent(btnEdit)
                        .addGap(43, 43, 43)
                        .addComponent(btnHapus)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(btnExport)
                .addGap(18, 18, 18)
                .addComponent(btnImport)
                .addGap(23, 23, 23))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtNama, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtNomorTelepon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cmbKategori, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnTambah)
                    .addComponent(btnEdit)
                    .addComponent(btnHapus))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtPencarian, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnExport)
                    .addComponent(btnImport))
                .addContainerGap(11, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnTambahActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTambahActionPerformed
        // TODO add your handling code here:
        addContact();
    }//GEN-LAST:event_btnTambahActionPerformed

    private void btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnEditActionPerformed
        // TODO add your handling code here:
        editContact();
    }//GEN-LAST:event_btnEditActionPerformed

    private void tblKontakMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tblKontakMouseClicked
        // TODO add your handling code here:
        int selectedRow = tblKontak.getSelectedRow();
        if (selectedRow != -1) {
            populateInputFields(selectedRow);
        }
    }//GEN-LAST:event_tblKontakMouseClicked

    private void btnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHapusActionPerformed
        // TODO add your handling code here:
        deleteContact();
    }//GEN-LAST:event_btnHapusActionPerformed

    private void txtPencarianKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtPencarianKeyTyped
        // TODO add your handling code here:
        searchContact();
    }//GEN-LAST:event_txtPencarianKeyTyped

    private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
        // TODO add your handling code here:
        exportToCSV();
    }//GEN-LAST:event_btnExportActionPerformed

    private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportActionPerformed
        // TODO add your handling code here:
        importFromCSV();
    }//GEN-LAST:event_btnImportActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PengelolaanKontakFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PengelolaanKontakFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEdit;
    private javax.swing.JButton btnExport;
    private javax.swing.JButton btnHapus;
    private javax.swing.JButton btnImport;
    private javax.swing.JButton btnTambah;
    private javax.swing.JComboBox<String> cmbKategori;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblJudul;
    private javax.swing.JTable tblKontak;
    private javax.swing.JTextField txtNama;
    private javax.swing.JTextField txtNomorTelepon;
    private javax.swing.JTextField txtPencarian;
    // End of variables declaration//GEN-END:variables
}
