package com.example.apiproductos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductoViewHolder> {

    private List<Producto> listaProductos;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditar(Producto producto);
        void onEliminar(int idProducto);
    }

    public ProductAdapter(List<Producto> listaProductos, OnItemClickListener listener) {
        this.listaProductos = listaProductos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_producto, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto p = listaProductos.get(position);
        holder.bind(p);
    }

    @Override
    public int getItemCount() {
        return listaProductos.size();
    }

    public void setProductos(List<Producto> productos) {
        this.listaProductos = productos;
        notifyDataSetChanged();
    }

    class ProductoViewHolder extends RecyclerView.ViewHolder {
        TextView txtProducto, txtPrecio, txtCategoria, txtUnidad;
        ImageButton btnEditar, btnEliminar;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtProducto = itemView.findViewById(R.id.txtProducto);
            txtPrecio = itemView.findViewById(R.id.txtPrecio);
            txtCategoria = itemView.findViewById(R.id.txtCategoria);
            txtUnidad = itemView.findViewById(R.id.txtUnidad); // Asegúrate de tener este ID en el XML
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);
        }

        public void bind(Producto p) {
            txtProducto.setText(p.getProducto());
            txtPrecio.setText(String.format("Precio: %.2f Bs", p.getPrecio()));
            txtCategoria.setText("Cat: " + p.getCategoria());

            // Si tienes un TextView para unidad en el diseño, úsalo
            if(txtUnidad != null) txtUnidad.setText(p.getUnidadMedida());

            btnEditar.setOnClickListener(v -> listener.onEditar(p));
            btnEliminar.setOnClickListener(v -> listener.onEliminar(p.getIdProducto()));
        }
    }
}