package com.example.apiproductos;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<Producto> listaProductos = new ArrayList<>();
    String apiUrl = "http://demoapi.somee.com/api/productos";
    EditText txtProducto, txtPrecio, txtUnidad, txtCategoria;
    Button btnAgregar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        listView = findViewById(R.id.listViewProductos);
        txtProducto = findViewById(R.id.txtProducto);
        txtPrecio = findViewById(R.id.txtPrecio);
        txtUnidad = findViewById(R.id.txtUnidad);
        txtCategoria = findViewById(R.id.txtCategoria);
        btnAgregar = findViewById(R.id.btnAgregar);

        btnAgregar.setOnClickListener(v -> agregarProducto());

        cargarProductos();
    }

    private void cargarProductos() {
        new Thread(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                StringBuilder respuesta = new StringBuilder();
                String linea;
                while ((linea = reader.readLine()) != null) {
                    respuesta.append(linea);
                }
                reader.close();
                con.disconnect();

                JSONArray array = new JSONArray(respuesta.toString());
                ArrayList<Producto> productosTemp = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);
                    Producto p = new Producto();

                    p.setIdProducto(obj.optInt("idProducto", 0));
                    p.setIdEmpresa(obj.optInt("idEmpresa", 0));
                    p.setProducto(obj.optString("producto1", "Sin nombre"));
                    p.setPrecio(obj.optDouble("precio", 0.0));
                    p.setUnidadMedida(obj.optString("unidadMedida", "ND"));
                    p.setCategoria(obj.optString("categoria", "ND"));

                    productosTemp.add(p);
                }


                runOnUiThread(() -> {
                    listaProductos.clear();
                    listaProductos.addAll(productosTemp);
                    ProductAdapter adapter = new ProductAdapter(MainActivity.this, listaProductos);
                    listView.setAdapter(adapter);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void agregarProducto() {
        new Thread(() -> {
            try {
                URL url = new URL(apiUrl);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                con.setDoOutput(true);

                JSONObject nuevo = new JSONObject();
                nuevo.put("idEmpresa", 1);
                nuevo.put("producto1", txtProducto.getText().toString()); // CORRECCIÓN
                nuevo.put("precio", Double.parseDouble(txtPrecio.getText().toString()));
                nuevo.put("unidadMedida", txtUnidad.getText().toString());
                nuevo.put("categoria", txtCategoria.getText().toString());

                con.getOutputStream().write(nuevo.toString().getBytes("UTF-8"));
                con.getOutputStream().flush();
                con.getOutputStream().close();

                int responseCode = con.getResponseCode();
                InputStreamReader isr = null;
                if (responseCode >= 200 && responseCode < 300) {
                    isr = new InputStreamReader(con.getInputStream(), "UTF-8");
                } else if (con.getErrorStream() != null) {
                    isr = new InputStreamReader(con.getErrorStream(), "UTF-8");
                }

                if (isr != null) {
                    BufferedReader reader = new BufferedReader(isr);
                    StringBuilder respuesta = new StringBuilder();
                    String linea;
                    while ((linea = reader.readLine()) != null) {
                        respuesta.append(linea);
                    }
                    reader.close();
                    System.out.println("Respuesta servidor: " + respuesta.toString());
                } else {
                    System.out.println("No se recibió respuesta del servidor. Código: " + responseCode);
                }

                con.disconnect();

                runOnUiThread(() -> {
                    cargarProductos();

                    txtProducto.setText("");
                    txtPrecio.setText("");
                    txtUnidad.setText("");
                    txtCategoria.setText("");
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


}
