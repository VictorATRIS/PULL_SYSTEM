package com.example.pull_system;

public class DailyOrder {
    private String part_no;
    private String customer;
    private int order_qty;
    private int real_qty;
    private String artesas;

    // Constructor
    public DailyOrder(String part_no, String customer, int order_qty, int real_qty, String artesas) {
        this.part_no = part_no;
        this.customer = customer;
        this.order_qty = order_qty;
        this.real_qty = real_qty;
        this.artesas = artesas;
    }

    public DailyOrder() {
        this.part_no = "";
        this.customer = "";
        this.order_qty = 0;
        this.real_qty = 0;
        this.artesas = "";
    }

    // Getters y Setters
    public String getPartNo() {
        return part_no;
    }

    public void setPartNo(String part_no) {
        this.part_no = part_no;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public int getOrderQty() {
        return order_qty;
    }

    public void setOrderQty(int order_qty) {
        this.order_qty = order_qty;
    }

    public int getRealQty() {
        return real_qty;
    }

    public void setRealQty(int real_qty) {
        this.real_qty = real_qty;
    }

    public String getArtesas() {
        return artesas;
    }

    public void setArtesas(String artesas) {
        this.artesas = artesas;
    }

}