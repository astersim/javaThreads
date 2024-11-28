package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.gson.Gson;

import model.Endereco;
import model.Pessoa;
import model.Telefone;

public class Main {
    private static final String SUPABASE_URL = "https://nzkpvblmjwngafhvlvqa.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im56a3B2YmxtanduZ2FmaHZsdnFhIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTcyOTgxNjE5NCwiZXhwIjoyMDQ1MzkyMTk0fQ.qNqnou5R8J5WjwahtT9vyHfJeKS23Sx7RhgHv1CXhhw";
    private static final int THREAD_POOL_SIZE = 2;

    public static void main(String[] args) {
        String filePath = "/app/CSVFile.csv";
        lerCSVEEnviarParaSupabase(filePath);
//        buscarDoSupabase("pessoa", "10");
//        buscarDoSupabase("pessoa", "18");
    }

    public static void lerCSVEEnviarParaSupabase(String filePath) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        int id = 1;

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");

                if (data.length >= 6) {
                    Pessoa pessoa = new Pessoa(id, data[0], data[1], data[2], Integer.parseInt(data[3]));
                    Endereco endereco = new Endereco(id, id, data[4], data[5], data[6], data[7], data[8]);
                    Telefone telefone = new Telefone(id, id, data[9]);

                    executor.submit(() -> enviarParaSupabase("pessoa", pessoa));
//                    executor.submit(() -> enviarParaSupabase("endereco", endereco));
//                    executor.submit(() -> enviarParaSupabase("telefone", telefone));
                    id++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }

    private static void enviarParaSupabase(String tabela, Object entidade) {
        try {
            HttpClient client = HttpClient.newHttpClient();
            String body = new Gson().toJson(entidade);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(SUPABASE_URL + "/rest/v1/" + tabela))
                    .header("Content-Type", "application/json")
                    .header("apikey", SUPABASE_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response Code for " + tabela + ": " + response.statusCode());
            System.out.println("Response Body: " + response.body());
            if (response.statusCode() == 201) {
                System.out.println("Inserido com sucesso na tabela " + tabela);
            } else {
                System.out.println("Erro ao inserir na tabela " + tabela + ": " + response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void buscarDoSupabase(String tabela, String filtro) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String uri = SUPABASE_URL + "/rest/v1/" + tabela;
            if (filtro != null && !filtro.isEmpty()) {
                uri += "?id=eq." + filtro;
            }

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(uri))
                    .header("Content-Type", "application/json")
                    .header("apikey", SUPABASE_KEY)
                    .header("Authorization", "Bearer " + SUPABASE_KEY)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response Code for " + tabela + ": " + response.statusCode());
            System.out.println("Response Body: " + response.body());

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

}
