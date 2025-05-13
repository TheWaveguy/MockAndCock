package com.example.mockandcock;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DrinksDetailsActivity extends AppCompatActivity  implements View.OnClickListener{

    private ExecutorService executorService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drinks_details);

        // Initialiser la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String boissonId = getIntent().getStringExtra("boissonId");

        if (boissonId != null) {
            executorService = Executors.newSingleThreadExecutor();
            String url = "https://www.thecocktaildb.com/api/json/v1/1/lookup.php?i=" + boissonId;
            fetchDrinkDetails(url);
        }

        Button boutonRecherche = findViewById(R.id.buttonResearch);
        boutonRecherche.setOnClickListener(this);

        Button buttonBackToMain = findViewById(R.id.buttonBackToMain);
        buttonBackToMain.setOnClickListener(view -> {
            // Retour à MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        });

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonResearch) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        }
    }


    private void fetchDrinkDetails(String url) {
        executorService.execute(() -> {
            String data = getDataFromHTTP(url);
            runOnUiThread(() -> displayDrinkDetails(data));
        });
    }

    private String getDataFromHTTP(String param) {
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
            Log.e("DrinksDetailsActivity", "Error: " + e.getMessage());
        }
        return result.toString();
    }

    private void displayDrinkDetails(String toDisplay) {
        try {
            JSONObject jso = new JSONObject(toDisplay);
            if (jso.has("drinks")) {
                JSONObject boissonJSON = jso.getJSONArray("drinks").getJSONObject(0);
                Boisson boisson = Boisson.fromJSON(boissonJSON);

                Log.e("DDA", boisson.afficherInfos());

                ImageView imageView = findViewById(R.id.image_boisson);
                TextView nomTextView = findViewById(R.id.nom_boisson);
                TextView mockcockTextView = findViewById(R.id.mockcock_boisson);
                TextView verreTextView = findViewById(R.id.verre_boisson);
                TextView ingredientsTextView = findViewById(R.id.ingredients_boisson);
                TextView instructionsTextView = findViewById(R.id.instructions_boisson);

                Picasso.get().load(boisson.getUrlImage()).into(imageView);
                nomTextView.setText(boisson.getNom());
                mockcockTextView.setText(boisson.getMockCock());
                verreTextView.setText(boisson.getVerre());

                StringBuilder ingredientsBuilder = new StringBuilder();
                for (Map.Entry<String, String> entry : boisson.getIngredients().entrySet()) {
                    ingredientsBuilder.append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
                }
                ingredientsTextView.setText(ingredientsBuilder.toString());

                String instructions = boisson.getInstructions();
                if (Objects.equals(instructions, "null")) {
                    instructions = "Utilise ton imagination pour les instructions";
                }
                instructionsTextView.setText(instructions);
            }
        } catch (JSONException e) {
            Log.e("DrinksDetailsActivity", "Error parsing JSON: " + e.getMessage());
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
