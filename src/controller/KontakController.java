package controller;

import model.Kontak;
import model.KontakDAO;
import java.sql.SQLException;
import java.util.List;

private KontakDAO contactDAO;

public KontakController() {
    contactDAO = new KontakDAO();
}

// Method mengambil semua data kontak
public List<Kontak> getAllContacts() throws SQLException {
    return contactDAO.getAllContacts();
}

// Method menambah kontak
public void addContact(String nama, String nomorTelepon, String kategori)
throws SQLException {
    Kontak contact = new Kontak(0, nama, nomorTelepon, kategori);
    contactDAO.addContact(contact);
}

// Method mengupdate kontak
public void updateContact(int id, String nama, String nomorTelepon,
String kategori) throws SQLException {
    Kontak contact = new Kontak(id, nama, nomorTelepon, kategori);
    contactDAO.updateContact(contact);
}

// Method menghapus kontak
public void deleteContact(int id) throws SQLException {
    contactDAO.deleteContact(id);
}

// Method pencarian kontak
public List<Kontak> searchContacts(String keyword) throws SQLException {
    return contactDAO.searchContacts(keyword);
}

public boolean isDuplicatePhoneNumber(String nomorTelepon, Integer
excludeId) throws SQLException {
    return contactDAO.isDuplicatePhoneNumber(nomorTelepon, excludeId);
}