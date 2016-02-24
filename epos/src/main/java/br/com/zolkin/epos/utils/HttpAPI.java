package br.com.zolkin.epos.utils;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import br.com.zolkin.epos.R;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Zolkin on 02/02/16.
 */
public class HttpAPI {
    public static final String  API_URL = "https://posmobilehomolog.zolkin.com.br";

    private static HttpAPI instance = null;
    private Map <String, String> headers = new HashMap<String, String>();
    private HttpBinService service;



    public class Ambients {
        public String nome;
        public String url;
    }

    public class DepartmentStore {
        public String usuario;
        public String senha;
        public Estabelecimento estabelecimento;
        public Configuracoes configuracoes;
    }

    public class Estabelecimento {
        public int id;
        public String nome;
        public String logoImageUrl;
    }

    public class Configuracoes {
        public int consumoMinimo;
        public int consumoMaximo;
        public String campoCustomizavel;
        public boolean permiteAlterarLocalizacao;
        public boolean possuiAtendente;
        public boolean permiteAlterarData;
        public boolean permiteDividirConta;
        public boolean autenticaCpfSenha;
        public boolean autenticaQRCode;
        public boolean valorTotalComServico;
        public boolean valorTotalSemServico;
        public boolean valorTotalExcluiServico;
        public boolean geraComprovateSMS;
    }

    public class Balance {
        public int consumidorId;
        public String nome;
        public String cpf;
        public double saldo;
    }


    public interface HttpBinService {
        @GET("/configuracao/ambientes")
        Call<List<Ambients>> getAmbients();

        @FormUrlEncoded
        @POST("/inicializacao/senha-tecnica")
        public Call<Void> postData(
                @Field("senha") String senha
        );

        @FormUrlEncoded
        @POST("/configuracao/inicializar-estabelecimento")
        public Call<DepartmentStore> postInitializeDepartmentStore(
                @Field("estabelecimentoId") String estabelecimentoId,
                @Field("token") String token,
                @Field("posId") String posId
        );

        @FormUrlEncoded
        @POST("/configuracao/desvincular-estabelecimento")
        public Call<Void>  postUnlinkDepartment (
                @Field("estabelecimentoId") String estabelecimentoId,
                @Field("posId") String posId
        );

        @GET("/teste/comunicacao")
        public Call<Void> testeComunicacao();

        @GET("/teste/transacao")
        public Call<Void> testeTransacao();

        @GET("/consumidor/saldo")
        public Call<Balance> consumidorSaldo(@Query("cpf") String cpf);
    }

    private HttpAPI(Context context) {
        KeyStore ksTrush;
        SSLContext ssl;

        try {
            ksTrush = KeyStore.getInstance("BKS");
            InputStream is = context.getResources().openRawResource(R.raw.epos);
            ksTrush.load(is, "zolkin".toCharArray());

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ksTrush);

            ssl = SSLContext.getInstance("TLS");
            ssl.init(null, tmf.getTrustManagers(), null);

            OkHttpClient httpClient = new OkHttpClient.Builder()
                    .addInterceptor(new Interceptor() {
                        @Override
                        public okhttp3.Response intercept(Chain chain) throws IOException {
                            Request.Builder builder = chain.request().newBuilder();
                            for (Map.Entry<String, String> entry : headers.entrySet()) {
                                builder.addHeader(entry.getKey(), entry.getValue());
                            }

                            return chain.proceed(builder.build());
                        }
                    })
                    .sslSocketFactory(ssl.getSocketFactory())
                    .build();


            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    .client(httpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            service = retrofit.create(HttpBinService.class);
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException |
                CertificateException | KeyManagementException e) {
            e.printStackTrace();
            Log.e(Constants.App, e.getMessage());
        }
    }

    public static HttpAPI getInstance(Context context) {
        if(instance == null) {
            instance = new HttpAPI(context);
        }
        return instance;
    }

    public HttpBinService getService() {
        return service;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public void addHeaders(Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    public void removeHeader(String key) {
        headers.remove(key);
    }

    public void clearHeaders() {
        headers.clear();
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void basicAuthentication(String user, String passwd) {
        String credentials = user + ":" + passwd;
        String base64Encoded = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
        addHeader("Authorization", "Basic " + base64Encoded);
    }
}
