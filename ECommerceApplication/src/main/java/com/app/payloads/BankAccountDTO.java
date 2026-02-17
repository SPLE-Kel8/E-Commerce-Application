package com.app.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for bank account information.
 * 
 * Sesuai requirement (d):
 * "Pada fitur pembayaran dengan transfer bank, pelanggan diminta untuk memilih nama bank
 * di antara daftar nama bank yang didukung oleh sistem. Sistem lalu memberikan nomor
 * rekening toko untuk nama bank yang dipilih."
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BankAccountDTO {
    
    /** Kode bank (BCA, BNI, MANDIRI, dll) */
    private String bankCode;
    
    /** Nama lengkap bank */
    private String bankName;
    
    /** Nomor rekening toko */
    private String accountNumber;
    
    /** Nama pemilik rekening */
    private String accountName;
}
