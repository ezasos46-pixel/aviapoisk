package com.example.demo8.services;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class HttpClient {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private final String baseUrl;
    private final String apiKey;
    private final OkHttpClient client;

    public HttpClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                .build();
    }

    public String get(String endpoint) throws IOException {
        return executeGetWithRetry(() -> {
            Request request = baseRequest(baseUrl + "/rest/v1/" + endpoint).get().build();
            try (Response response = client.newCall(request).execute()) {
                return readResponse(response);
            }
        });
    }

    public String getWithRange(String endpoint, int from, int to) throws IOException {
        return executeGetWithRetry(() -> {
            Request request = baseRequest(baseUrl + "/rest/v1/" + endpoint)
                    .header("Range", from + "-" + to)
                    .header("Prefer", "count=none")
                    .get()
                    .build();
            try (Response response = client.newCall(request).execute()) {
                return readResponse(response);
            }
        });
    }

    /** Несколько попыток для GET: обрывы и таймауты при нестабильной сети / прокси. */
    private String executeGetWithRetry(IoSupplier<String> action) throws IOException {
        IOException last = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                return action.get();
            } catch (IOException e) {
                last = e;
                if (attempt < 2 && isRetriableNetworkError(e)) {
                    try {
                        Thread.sleep(400L * (attempt + 1));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        }
        throw last;
    }

    private static boolean isRetriableNetworkError(IOException e) {
        for (Throwable c = e; c != null; c = c.getCause()) {
            if (c instanceof java.net.SocketTimeoutException) return true;
            if (c instanceof java.net.ConnectException) return true;
            if (c instanceof java.net.UnknownHostException) return true;
            if (c instanceof java.net.SocketException) {
                String m = c.getMessage();
                if (m != null && (m.contains("reset") || m.contains("Broken pipe"))) return true;
            }
        }
        return false;
    }

    @FunctionalInterface
    private interface IoSupplier<T> {
        T get() throws IOException;
    }

    public String post(String endpoint, String json) throws IOException {
        Request request = baseRequest(baseUrl + "/rest/v1/" + endpoint)
                .header("Prefer", "return=representation")
                .post(RequestBody.create(json, JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            return readResponse(response);
        }
    }

    /** POST с upsert — для seedCities/seedFlights (ignore-duplicates). */
    public String postUpsert(String endpoint, String json) throws IOException {
        Request request = baseRequest(baseUrl + "/rest/v1/" + endpoint)
                .header("Prefer", "resolution=merge-duplicates,return=minimal")
                .post(RequestBody.create(json, JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            int code = response.code();
            if (code < 200 || code >= 300) {
                String err = response.body() != null ? response.body().string() : "";
                throw new IOException("HTTP " + code + ": " + err);
            }
            return "HTTP " + code;
        }
    }

    public String patch(String endpoint, String json) throws IOException {
        Request request = baseRequest(baseUrl + "/rest/v1/" + endpoint)
                .header("Prefer", "return=representation")
                .patch(RequestBody.create(json, JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            return readResponse(response);
        }
    }

    public String postRpc(String rpc, String json) throws IOException {
        Request request = baseRequest(baseUrl + "/rest/v1/rpc/" + rpc)
                .post(RequestBody.create(json, JSON))
                .build();
        try (Response response = client.newCall(request).execute()) {
            return readResponse(response);
        }
    }

    private Request.Builder baseRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("apikey", apiKey)
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");
    }

    private static String readResponse(Response response) throws IOException {
        int code = response.code();
        ResponseBody body = response.body();
        String text = body != null ? body.string() : "";
        if (code < 200 || code >= 300) {
            throw new IOException("HTTP " + code + ": " + text);
        }
        return text;
    }
}
