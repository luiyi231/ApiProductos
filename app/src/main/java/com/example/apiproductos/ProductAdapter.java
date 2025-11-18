package com.example.apiproductos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class ProductAdapter extends ArrayAdapter<Producto> {

    // Constructor
    public ProductAdapter(Context context, List<Producto> productos) {
        super(context, 0, productos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Reutilizar vista
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_producto, parent, false);
        }

        // Obtener item actual
        Producto p = getItem(position);

        // Referencias a vistas
        TextView txtProducto = convertView.findViewById(R.id.txtProducto);
        TextView txtPrecio = convertView.findViewById(R.id.txtPrecio);
        TextView txtCategoria = convertView.findViewById(R.id.txtCategoria);

        // Llenar datos
        txtProducto.setText(p.getProducto());
        txtPrecio.setText("Precio: " + p.getPrecio() + " Bs");
        txtCategoria.setText("Categoría: " + p.getCategoria());

        return convertView;
    }
}
