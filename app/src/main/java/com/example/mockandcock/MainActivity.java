package com.example.mockandcock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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

public class MainActivity extends AppCompatActivity implements View.OnClickListener, BoissonAdapter.onClickBoissonListener {

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private TextView textView;
    private BoissonAdapter boissonAdapter;
    private List<Boisson> boissonList;
    private NetworkChangeReceiver networkChangeReceiver;
    private ExecutorService executorService;
    private boolean isReceiverRegistered = false;

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

        // Initialiser la Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialiser les éléments graphiques
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        textView = findViewById(R.id.textView);
        Button boutonRecherche = findViewById(R.id.buttonResearch);
        boutonRecherche.setOnClickListener(this);

        // Configurer le RecyclerView
        boissonList = new ArrayList<>();
        boissonAdapter = new BoissonAdapter(this, boissonList, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(boissonAdapter);

        // Initialiser le BroadcastReceiver
        networkChangeReceiver = new NetworkChangeReceiver();

        // Initialiser l'ExecutorService
        executorService = Executors.newSingleThreadExecutor();

        // Appeler l'URL initiale *seulement si connecté*
        if (isConnected()) {
            String url = "https://www.thecocktaildb.com/api/json/v1/1/search.php?f=a";
            call(url);
        } else {
            showNoInternetMessage();
        }

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

    @Override
    protected void onResume() {
        super.onResume();
        // Enregistrer le receiver pour écouter les changements de connectivité
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkChangeReceiver, filter);
        isReceiverRegistered = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Toujours désenregistrer le receiver quand l'activité n'est plus visible
        if (isReceiverRegistered) {
            unregisterReceiver(networkChangeReceiver);
            isReceiverRegistered = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Arrêter l'ExecutorService lorsque l'activité est détruite
        if (executorService != null) {
            executorService.shutdown();
        }
    }

    // Méthode helper pour vérifier la connexion
    private boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonResearch) {
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public void onBoissonClick(String boissonId) {
        Intent intent = new Intent(this, DrinksDetailsActivity.class);
        intent.putExtra("boissonId", boissonId);
        startActivity(intent);
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
            Log.e("MainActivity", "Error: " + e.getMessage());
        }
        return result.toString();
    }

    public void call(String param) {
        executorService.execute(() -> {
            String data = getDataFromHTTP(param);
            mainHandler.post(() -> display(data));
        });
    }

    public void display(String toDisplay) {
        try {
            Log.d("MainActivity", "JSON Input: " + toDisplay);
            boissonList.clear();

            if (toDisplay == null || toDisplay.trim().isEmpty()) {
                throw new JSONException("Chaîne de caractère vide");
            }

            JSONObject jso = new JSONObject(toDisplay);

            if (!jso.has("drinks")) {
                throw new JSONException("Pas de liste 'drinks'");
            }

            JSONArray ja = jso.getJSONArray("drinks");

            for (int i = 0; i < ja.length(); i++) {
                JSONObject boissonJSON = ja.getJSONObject(i);
                try {
                    Boisson boisson = Boisson.fromJSON(boissonJSON);
                    Log.e("Boisson", boisson.afficherInfos());
                    boissonList.add(boisson);
                } catch (JSONException e) {
                    Log.e("MainActivity", "Error creating Boisson from JSON: " + e.getMessage());
                }
            }

            boissonAdapter.notifyDataSetChanged();
            textView.setVisibility(View.GONE); // Masquer le TextView si tout va bien

        } catch (JSONException e) {
            Log.e("MainActivity", "Error parsing JSON: " + e.getMessage());
            showErrorMessage(R.string.bois_pas_sale_alcoolique_erreur_du_json);
        } catch (Exception e) {
            Log.e("MainActivity", "Unexpected error: " + e.getMessage());
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

    private void showNoInternetMessage() {
        Toast.makeText(this, "Pas de connexion internet", Toast.LENGTH_LONG).show();
        if (textView != null) {
            textView.setText(R.string.veuillez_v_rifier_votre_connexion_internet);
        }
    }

    private void showErrorMessage(int resId) {
        if (textView != null) {
            textView.setText(resId);
            textView.setVisibility(View.VISIBLE); // Rendre le TextView visible
        }
    }

    // Classe interne pour le BroadcastReceiver
    private class NetworkChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())) {
                boolean connected = isConnected();
                if (connected) {
                    // La connexion est (re)établie
                    Toast.makeText(context, "Connecté à internet", Toast.LENGTH_SHORT).show();
                    if (boissonList.isEmpty()) {
                        String url = "https://www.thecocktaildb.com/api/json/v1/1/search.php?f=a";
                        call(url);
                    }
                } else {
                    // La connexion est perdue
                    Toast.makeText(context, "Connexion internet perdue", Toast.LENGTH_SHORT).show();
                    // Mettre à jour l'UI pour indiquer l'état hors ligne
                    if (textView != null) {
                        textView.setText(R.string.hors_ligne_certaines_fonctionnalit_s_peuvent_tre_indisponibles);
                    }
                }
            }
        }
    }
}
