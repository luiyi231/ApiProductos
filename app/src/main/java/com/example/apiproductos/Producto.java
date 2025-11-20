package com.example.apiproductos;

public class Producto {
    private int idProducto;
    private int idEmpresa;
    private String producto;
    private double precio;
    private String unidadMedida;
    private String categoria;

    public Producto() {
    }

    public Producto(int idProducto, int idEmpresa, String producto, double precio, String unidadMedida, String categoria) {
        this.idProducto = idProducto;
        this.idEmpresa = idEmpresa;
        this.producto = producto;
        this.precio = precio;
        this.unidadMedida = unidadMedida;
        this.categoria = categoria;
    }

    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public int getIdEmpresa() { return idEmpresa; }
    public void setIdEmpresa(int idEmpresa) { this.idEmpresa = idEmpresa; }

    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
}