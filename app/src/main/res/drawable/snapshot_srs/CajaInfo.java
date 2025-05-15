package com.example.snapshot_srs;

public class CajaInfo {
    public String caja;
    public String product_no;
    public int qty;
    public String dl;

    public String pallet;
    public CajaInfo(String caja, String product_no, int qty, String dl,String pallet) {
        this.caja = caja;
        this.product_no = product_no;
        this.qty = qty;
        this.dl = dl;
        this.pallet = pallet;
    }

    public CajaInfo() {
        this.caja = "";
        this.product_no = "";
        this.qty = 0;
        this.dl = "";
        this.pallet = "";
    }
}
