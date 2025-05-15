package com.example.pull_system;

public class NoPartInfo {
    private String qty_total;
    private String qty_real;
    private String std_pack;
    private String container;
    private String mfg_ship;
    private String etd_tdc;
    private String etdDcsc;
    private String mfg_plant;

    // Constructor vacío
    public NoPartInfo() {}

    // Getters y Setters
    public String getQtyTotal() { return qty_total; }
    public void setQtyTotal(String qty_total) { this.qty_total = qty_total; }

    public String getQtyReal() { return qty_real; }
    public void setQtyReal(String qty_real) { this.qty_real = qty_real; }

    public String getStdPack() { return std_pack; }
    public void setStdPack(String std_pack) { this.std_pack = std_pack; }

    public String getContainer() { return container; }
    public void setContainer(String container) { this.container = container; }

    public String getMfgShip() { return mfg_ship; }
    public void setMfgShip(String mfg_ship) { this.mfg_ship = mfg_ship; }

    public String getEtdTdc() { return etd_tdc; }
    public void setEtdTdc(String etd_tdc) { this.etd_tdc = etd_tdc; }

    public String getEtdDcsc() { return etdDcsc; }
    public void setEtdDcsc(String etdDcsc) { this.etdDcsc = etdDcsc; }

    public String getMfgPlant() { return mfg_plant; }
    public void setMfgPlant(String mfg_plant) { this.mfg_plant = mfg_plant; }
}