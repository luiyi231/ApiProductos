package com.example.apiproductos;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ProductAdapter adapter;
    ArrayList<Producto> listaProductos = new ArrayList<>();
    // Nota: Asegúrate de que esta URL es correcta. Algunas APIs requieren HTTPS.
    String apiUrl = "http://demoapi.somee.com/api/productos";

    EditText txtProducto, txtPrecio, txtUnidad, txtCategoria;
    Button btnAccion, btnLimpiar;

    // Variable para saber si estamos editando
    Producto productoSeleccionado = null;

    ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Ajuste de insets para edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        inicializarVistas();
        configurarRecyclerView();

        // Cargar datos iniciales
        cargarProductos();

        btnAccion.setOnClickListener(v -> {
            if (validarCampos()) {
                if (productoSeleccionado == null) {
                    agregarProducto();
                } else {
                    actualizarProducto();
                }
            }
        });

        btnLimpiar.setOnClickListener(v -> limpiarFormulario());
    }

    private void inicializarVistas() {
        recyclerView = findViewById(R.id.recyclerViewProductos);
        txtProducto = findViewById(R.id.txtProducto);
        txtPrecio = findViewById(R.id.txtPrecio);
        txtUnidad = findViewById(R.id.txtUnidad);
        txtCategoria = findViewById(R.id.txtCategoria);
        btnAccion = findViewById(R.id.btnAgregar);
        btnLimpiar = findViewById(R.id.btnLimpiar);
    }

    private void configurarRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProductAdapter(listaProductos, new ProductAdapter.OnItemClickListener() {
            @Override
            public void onEditar(Producto producto) {
                productoSeleccionado = producto;
                txtProducto.setText(producto.getProducto());
                txtPrecio.setText(String.valueOf(producto.getPrecio()));
                txtUnidad.setText(producto.getUnidadMedida());
                txtCategoria.setText(producto.getCategoria());
                btnAccion.setText("Actualizar Producto");
            }

            @Override
            public void onEliminar(int idProducto) {
                confirmarEliminacion(idProducto);
            }
        });
        recyclerView.setAdapter(adapter);
    }

    // --- CRUD OPERATIONS ---

    // 1. READ (GET)
    private void cargarProductos() {
        executor.execute(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                if (con.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    StringBuilder respuesta = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null) respuesta.append(linea);
                    reader.close();

                    JSONArray array = new JSONArray(respuesta.toString());
                    ArrayList<Producto> temp = new ArrayList<>();

                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);
                        Producto p = new Producto();
                        p.setIdProducto(obj.optInt("idProducto"));
                        p.setIdEmpresa(obj.optInt("idEmpresa"));
                        p.setProducto(obj.optString("producto1")); // Ojo con el nombre de campo en tu API
                        p.setPrecio(obj.optDouble("precio"));
                        p.setUnidadMedida(obj.optString("unidadMedida"));
                        p.setCategoria(obj.optString("categoria"));
                        temp.add(p);
                    }

                    runOnUiThread(() -> adapter.setProductos(temp));
                }
                con.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Error cargando datos", Toast.LENGTH_SHORT).show());
            }
        });
    }

    // 2. CREATE (POST)
    private void agregarProducto() {
        executor.execute(() -> {
            try {
                JSONObject json = crearJsonProducto();
                // Hardcode idEmpresa si es necesario por tu API
                json.put("idEmpresa", 1);

                realizarPeticion("POST", apiUrl, json.toString());

                runOnUiThread(() -> {
                    Toast.makeText(this, "Producto Agregado", Toast.LENGTH_SHORT).show();
                    limpiarFormulario();
                    cargarProductos();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 3. UPDATE (PUT)
    private void actualizarProducto() {
        if (productoSeleccionado == null) return;

        executor.execute(() -> {
            try {
                JSONObject json = crearJsonProducto();
                json.put("idProducto", productoSeleccionado.getIdProducto());
                json.put("idEmpresa", productoSeleccionado.getIdEmpresa());

                // Asumiendo que tu API usa /api/productos/{id} para PUT
                String urlUpdate = apiUrl + "/" + productoSeleccionado.getIdProducto();
                // O si tu API requiere el ID en el query string: apiUrl + "?id=" + ...

                realizarPeticion("PUT", urlUpdate, json.toString());

                runOnUiThread(() -> {
                    Toast.makeText(this, "Producto Actualizado", Toast.LENGTH_SHORT).show();
                    limpiarFormulario();
                    cargarProductos();
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 4. DELETE (DELETE)
    private void confirmarEliminacion(int idProducto) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar")
                .setMessage("¿Estás seguro de eliminar este producto?")
                .setPositiveButton("Sí", (dialog, which) -> eliminarProducto(idProducto))
                .setNegativeButton("No", null)
                .show();
    }

    private void eliminarProducto(int idProducto) {
        executor.execute(() -> {
            try {
                // Asumiendo que tu API usa /api/productos/{id} para DELETE
                URL url = new URL(apiUrl + "/" + idProducto);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("DELETE");

                int codigo = con.getResponseCode();

                runOnUiThread(() -> {
                    if (codigo >= 200 && codigo < 300) {
                        Toast.makeText(this, "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                        cargarProductos();
                    } else {
                        Toast.makeText(this, "Error al eliminar: " + codigo, Toast.LENGTH_SHORT).show();
                    }
                });
                con.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // --- UTILIDADES ---

    private void realizarPeticion(String metodo, String urlString, String jsonBody) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(metodo);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);

        try (OutputStream os = con.getOutputStream()) {
            byte[] input = jsonBody.getBytes("utf-8");
            os.write(input, 0, input.length);
        }

        int code = con.getResponseCode();
        // Leer respuesta si es necesario, aunque para este ejemplo basta con el código
        con.disconnect();
    }

    private JSONObject crearJsonProducto() throws Exception {
        JSONObject json = new JSONObject();
        json.put("producto1", txtProducto.getText().toString()); // Verifica si tu API pide "producto1" o "nombre"
        json.put("precio", Double.parseDouble(txtPrecio.getText().toString()));
        json.put("unidadMedida", txtUnidad.getText().toString());
        json.put("categoria", txtCategoria.getText().toString());
        return json;
    }

    private boolean validarCampos() {
        if (txtProducto.getText().toString().isEmpty() ||
                txtPrecio.getText().toString().isEmpty()) {
            Toast.makeText(this, "Complete los campos obligatorios", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void limpiarFormulario() {
        txtProducto.setText("");
        txtPrecio.setText("");
        txtUnidad.setText("");
        txtCategoria.setText("");
        productoSeleccionado = null;
        btnAccion.setText("Agregar Producto");
        txtProducto.requestFocus();
    }
}