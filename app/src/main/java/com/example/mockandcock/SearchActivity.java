package com.example.mockandcock;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SearchActivity extends AppCompatActivity implements BoissonAdapter.onClickBoissonListener {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private BoissonAdapter boissonAdapter;
    private List<Boisson> boissonList;
    private TextView textView;
    private EditText inputNom;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        // Initialiser la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialiser les éléments graphiques
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        textView = findViewById(R.id.textView);

        // Initialiser l'ExecutorService
        executorService = Executors.newSingleThreadExecutor();

        // Configurer le RecyclerView
        boissonList = new ArrayList<>();
        boissonAdapter = new BoissonAdapter(this, boissonList, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(boissonAdapter);

        Button buttonResearch = findViewById(R.id.research);
        buttonResearch.setOnClickListener(v -> {
            inputNom = findViewById(R.id.inputNom);
            String nom = inputNom.getText().toString();
            if (!nom.isEmpty()) {
                // Appeler l'URL pour afficher tous les cocktails correspondants au nom
                String urlName = "https://www.thecocktaildb.com/api/json/v1/1/search.php?s=" + nom;
                Log.e("SearchActivity", "URL: " + urlName);
                call(urlName);
            } else {
                // erreur : veuillez entrer un nom de boisson
                showErrorMessage(R.string.veuillez_chercher_une_boisson);
            }
        });

        Button buttonBackToMain = findViewById(R.id.buttonBackToMain);
        buttonBackToMain.setOnClickListener(view -> {
            // Retour à MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        Button buttonRandomDrink = findViewById(R.id.buttonRandomDrink);
        buttonRandomDrink.setOnClickListener(v -> {
            String url = "https://www.thecocktaildb.com/api/json/v1/1/random.php";
            fetchRandomDrinkDetails(url);
        });
    }

    public String getDataFromHTTP(String param) {
        StringBuilder result = new StringBuilder();
        HttpURLConnection connexion;
        try {
            URL url = new URL(param);
            connexion = (HttpURLConnection) url.openConnection();
            connexion.setRequestMethod("GET");
            InputStream inputStream = connexion.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bf = new BufferedReader(inputStreamReader);
            String ligne;
            while ((ligne = bf.readLine()) != null) {
                result.append(ligne);
            }
            inputStream.close();
            bf.close();
            connexion.disconnect();
        } catch (Exception e) {
            result = new StringBuilder("Erreur: ").append(e.getMessage());
            Log.e("SearchActivity", "Error: " + e.getMessage());
        }
        Log.e("SearchActivity", "Error 11111111: " + result);
        return result.toString();
    }

    public void call(String param) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String data = getDataFromHTTP(param);
            mainHandler.post(() -> display(data));
        });
    }

    public void display(String toDisplay) {
        try {
            Log.d("SearchActivity", "JSON Input: " + toDisplay);
            boissonList.clear();

            if (toDisplay == null || toDisplay.trim().isEmpty()) {
                throw new JSONException("Chaîne de caractère vide");
            }

            JSONObject jso = new JSONObject(toDisplay);

            if (!jso.has("drinks")) {
                throw new JSONException("cocktail inexistant");
            }

            JSONArray ja = jso.getJSONArray("drinks");

            for (int i = 0; i < ja.length(); i++) {
                JSONObject boissonJSON = ja.getJSONObject(i);
                try {
                    Boisson boisson = Boisson.fromJSON(boissonJSON);
                    boissonList.add(boisson);
                } catch (JSONException e) {
                    Log.e("SearchActivity", "Error creating Boisson from JSON: " + e.getMessage());
                }
            }

            boissonAdapter.notifyDataSetChanged();
            textView.setVisibility(View.GONE); // Masquer le TextView si tout va bien

        } catch (JSONException e) {
            Log.e("SearchActivity", "Error parsing JSON: " + e.getMessage());
            showErrorMessage(R.string.bois_pas_sale_alcoolique_erreur_du_json);
        } catch (Exception e) {
            Log.e("SearchActivity", "Unexpected error: " + e.getMessage());
            showErrorMessage(R.string.bois_pas_sale_alcoolique_ah_il_n_y_a_plus_de_gouttes);
        }
    }

    private void fetchRandomDrinkDetails(String url) {
        executorService.execute(() -> {
            String data = getDataFromHTTP(url);
            mainHandler.post(() -> displayRandomDrinkDetails(data));
        });
    }

    private void displayRandomDrinkDetails(String toDisplay) {
        try {
            JSONObject jso = new JSONObject(toDisplay);
            if (jso.has("drinks")) {
                JSONObject boissonJSON = jso.getJSONArray("drinks").getJSONObject(0);
                Boisson boisson = Boisson.fromJSON(boissonJSON);

                Intent intent = new Intent(this, DrinksDetailsActivity.class);
                intent.putExtra("boissonId", boisson.getId());
                startActivity(intent);
            }
        } catch (JSONException e) {
            Log.e("MainActivity", "Error parsing JSON: " + e.getMessage());
        }
    }
    private void showErrorMessage(int resId) {
        if (textView != null) {
            textView.setText(resId);
            textView.setVisibility(View.VISIBLE); // Rendre le TextView visible
        }
    }

    @Override
    public void onBoissonClick(String boissonId) {
        Intent intent = new Intent(this, DrinksDetailsActivity.class);
        intent.putExtra("boissonId", boissonId);
        startActivity(intent);
    }
}
