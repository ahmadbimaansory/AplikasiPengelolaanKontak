package model;

private int id;
private String nama;
private String nomorTelepon;
private String kategori;

// Constructor
public Kontak(int id, String nama, String nomorTelepon, String kategori) {
    this.id = id;
    this.nama = nama;
    this.nomorTelepon = nomorTelepon;
    this.kategori = kategori;
}

// Getters dan Setters
public int getId() {
    return id;
}

public void setId(int id) {
    this.id = id;
}

public String getNama() {
    return nama;
}

public void setNama(String nama) {
    this.nama = nama;
}

public String getNomorTelepon() {
    return nomorTelepon;
}

public void setNomorTelepon(String nomorTelepon) {
    this.nomorTelepon = nomorTelepon;
}

public String getKategori() {
    return kategori;
}

public void setKategori(String kategori) {
    this.kategori = kategori;
}