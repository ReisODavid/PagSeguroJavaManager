package com.lilithandbelial.jd.Ferramentas.FerramentasPagamento;

import android.os.AsyncTask;
import android.util.Log;


import java.io.IOException;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class PagSeguroJavaManager {
    final String logTag = "PagSeguro";
    String idsession;
    final String EMAIL = "eudavid.reis@gmail.com";
    final String TOKEN = "6EEC0C94A884454BA0902D0BDA068767";
    // one instance, reuse

    OkHttpClient client = new OkHttpClient().newBuilder().build();
    public PagSeguroJavaManager(){
        /*Edite o construtor conforme suas nescessidades*/
        Log.d("Log","Dentro do construtor do PagSeguroJavaManager");

        }

    /*INICIA SESSÃO*/
    public void CriaSessao(){
        String nomeFuncao = "CriaSessao()";
        String resposta = "Resposta nula, erro ao executar função "+nomeFuncao;
        MediaType mediaType = MediaType.parse("text/plain");
        RequestBody body = RequestBody.create(mediaType, "");
        Request request = new Request.Builder()
                .url("https://ws.sandbox.pagseguro.uol.com.br/v2/sessions?email="+EMAIL+"&token="+TOKEN)
                .method("POST", body)
                .build();

        Log.d(logTag,"Dentro do metodo CriaSessao()");
        PagSeguroAsyncTask task = new PagSeguroAsyncTask(nomeFuncao);
        task.execute(request);
        try {
            resposta = task.get();
            idsession =FatiaStringID(resposta);
            Log.d(logTag,"Sucesso ao retornar valor do AsyncTask, resposta="+idsession);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Log.d(logTag,"Erro ao retornar valor do AsyncTask,erro="+e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(logTag,"Erro ao retornar valor do AsyncTask,erro="+e.getMessage());

        }

        /*Response response = client.newCall(request).execute();

        if (response.isSuccessful()){
            idsession = FatiaStringID(response.body().string());
        }else{
            Log.d("Log","Falha na requisição, erro da resposta="+response.message());
        }*/

    }
    /*Fatia String de resposta do metodo CriaSessão(), devolvendo apenas o ID*/
    public String FatiaStringID(String id){
        int indexA, indexB;

        indexA = id.indexOf("<id>")+4;
        indexB = id.lastIndexOf("</id>");
        Log.d("Log","logs indexes A="+indexA+" B="+indexB+" resposta = "+id);
        return new String(id.substring(indexA,indexB));

    }

    /*RETORNA FORMAS DE PAGAMENTO*/
    /*garantir que o valor passado seja um decimal com duas casas decimais após a virgula*/
    public String GetFormasDePagamento(String valor){
        String nomeFuncao = "GetFormasDePagamento()";
        String resposta="Resposta nula";

        /*Configura a requisição ao server*/
        /**Onde valor é o preço do produto
         * (ou um Decimal com duas casas após a virgula,
         * ou String ex("120.00")), e o IDSESSION é o ID gerado pelo metodo CriaSessão()*/

        Request request = new Request.Builder()
                .url("https://ws.sandbox.pagseguro.uol.com.br/payment-methods?amount="+valor+"&sessionId="+idsession)
                .method("GET", null)
                .addHeader("Accept", "application/vnd.pagseguro.com.br.v1+json;charset=ISO-8859-1")
                .build();

        PagSeguroAsyncTask task = new PagSeguroAsyncTask(nomeFuncao);
        task.execute(request);

        try {
            resposta = task.get();
            Log.d(logTag,"Sucesso ao retornar valor do AsyncTask, resposta="+resposta);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Log.d(logTag,"Erro ao retornar valor do AsyncTask,erro="+e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(logTag,"Erro ao retornar valor do AsyncTask,erro="+e.getMessage());

        }

        return resposta;

    }

    /**Pega a bandeira do cartão,é utilizado para verificar qual a bandeira do cartão que está sendo digitada.
     *  Esse método recebe por parâmetro o BIN (seis primeiros dígitos do cartão) e retorna dados como qual a bandeira,
     *  o tamanho do CVV, se possui data de expiração e qual algoritmo de validação. */
    public String GetBandeiraDoCartão(int BinDoCartao){
        String resposta = "Valor inicial";
        final String nomeFuncao = "GetBandeiraDoCartao()";

        /*Configura request*/
        Request request = new Request.Builder()
                .url("https://df.uol.com.br/df-fe/mvc/creditcard/v1/getBin?tk="+idsession+"&creditCard="+BinDoCartao)
                .method("GET", null)
                .build();

        PagSeguroAsyncTask task = new PagSeguroAsyncTask(nomeFuncao);
        task.execute(request);
        try {
            resposta = task.get();
            Log.d(logTag,"Sucesso ao retornar valor do AsyncTask, resposta="+resposta);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Log.d(logTag,"Erro ao retornar valor do AsyncTask,erro="+e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(logTag,"Erro ao retornar valor do AsyncTask,erro="+e.getMessage());

        }

        return resposta;
    }

    /*Voce pode alterar o metodo para receber um objeto cartãoDeCredito personalizado,ja com as informações necessarias*/
    /**Metodo gera o token do cartão para efetivar a compra, como parametros pede
     * -VALOR= valor do produto, com duas casas decimais após o ponto "250.00"
     *          se preferir, usa um Decimal ao invés de uma String
     * -CARDNUMBER= o numero do cartão a ser usado no pagamento
     * -MES E ANO DE EXPIRAÇÃO= mes = 2 digitos e ano = 4 digitos;
     * -CVV= Código verificador do cartão, seguindo o padrão do cartão
     * -BANDEIRA DO CARTAO = MasterCard,Visa,Cielo e etc*/
    public String GeraTokenDoCartao(PagSeguroCartao cartao, String valor, String CVV){
        String resposta= "Valor inicial";
        final String nomeFuncao ="GeraTokenDoCartao()";

        /*Configura o formato de resposta*/
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");

        /*Configura o formulário do metodo POST*/
        /*Setando toda a configuração da compra*/
        RequestBody body = RequestBody.create(mediaType, "sessionId="+idsession+"&amount="+valor+"&cardNumber="+cartao.getNumero()+
                "+&cardBrand="+cartao.getBandeira()+"&cardCvv="+CVV+"&cardExpirationMonth="+cartao.getMexExp()+"&cardExpirationYear="+cartao.getAnoExp());


        /*Configura o request que irá ao servidor*/
        Request request = new Request.Builder()
                .url("https://df.uol.com.br/v2/cards")
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        PagSeguroAsyncTask task = new PagSeguroAsyncTask(nomeFuncao);
        task.execute(request);
        try {
            resposta = task.get();
            Log.d(logTag,"Sucesso ao retornar valor do AsyncTask, resposta="+resposta);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Log.d(logTag,"Erro ao retornar valor do AsyncTask,erro="+e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(logTag,"Erro ao retornar valor do AsyncTask,erro="+e.getMessage());

        }
        resposta = FatiaTokenCartao(resposta);
        return resposta;

    }
    private String FatiaTokenCartao(String resposta){
        String respost = "Valor inicial";
        int indexA,indexB;
        indexA = resposta.indexOf("<token>")+7;
        indexB = resposta.lastIndexOf("</token>");
        respost = resposta.substring(indexA,indexB);

        Log.d(logTag,"valor do token="+respost);

        return respost;
    }

    /*Requisita as opções de pagamento parcelado*/
    public String OpcoesDeParcelamento(String valor,String bandeiraCartao){
        String resposta = "vALOR INICIAL";
        final String nomeFuncao = "OpcooesDeParcelamento()";
        /*Configura request*/
        Request request = new Request.Builder()
                .url("https://sandbox.pagseguro.uol.com.br/checkout/v2/installments.json?sessionId="+idsession+"&amount="+valor+"&creditCardBrand="+bandeiraCartao)
                .method("GET", null)
                .build();

        PagSeguroAsyncTask task = new PagSeguroAsyncTask(nomeFuncao);
        task.execute(request);
        try {
            resposta = task.get();
            Log.d(logTag,"Sucesso ao retornar valor do AsyncTask, resposta="+resposta);
        } catch (ExecutionException e) {
            e.printStackTrace();
            Log.d(logTag,"Erro ao retornar valor do AsyncTask,erro="+e.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.d(logTag,"Erro ao retornar valor do AsyncTask,erro="+e.getMessage());

        }

        return resposta;

    }

    public void GeraBoleto(){
        String respostaBoleto = "";

        /*Seta formato da resposta*/
        MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");

        /*Seta formulario do metodo POST*/
        RequestBody body = RequestBody.create(mediaType, "paymentMode=default&paymentMethod=boleto&receiverEmail=comprador@sandbox.pagseguro.com.br&currency=BRL&extraAmount=1.00&itemId1=0001&itemDescription1=NotebookPrata&itemAmount1=24300.00&itemQuantity1=1&notificationURL=https://sualoja.com.br/notifica.html&reference=REF1234&senderName=JoseComprador&senderCPF=22111944785&senderAreaCode=11&senderPhone=56273440&senderEmail=comprador@uol.com.br&senderHash={{ADICIONE O HASH}}&shippingAddressStreet=Av.Brig.FariaLima&shippingAddressNumber=1384&shippingAddressComplement=5oandar&shippingAddressDistrict=JardimPaulistano&shippingAddressPostalCode=01452002&shippingAddressCity=SaoPaulo&shippingAddressState=SP&shippingAddressCountry=BRA&shippingType=1&shippingCost=1.00");

        /*Seta request com os dados*/
        Request request = new Request.Builder()
                .url("https://ws.sandbox.pagseguro.uol.com.br/v2/transactions?email="+EMAIL+"&token="+TOKEN)
                .method("POST", body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();
        /*Executa request e captura resposta, ou trata possiveis erros*/
        try {
            Response response = client.newCall(request).execute();
            respostaBoleto = response.body().string();
            Log.d("Log","Sucesso ao gerar boleto, resposta ="+respostaBoleto);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("Log","Falha ao gerar boleto, erro="+e.getMessage());
        }
    }

    /*GETTERs and SETTERs*/
    public String getIdsession(){
        Log.d("Log","TESTE GET IDSESSION="+idsession);
        return  idsession;
    }
    public void setIdsession(String id){
        idsession = id;
    }

    public class PagSeguroAsyncTask extends AsyncTask<Request,String,String>{
        String resposta = "respost nulla, erro no metodo";
        String nomeFuncao;

        public PagSeguroAsyncTask(String nomeFuncao){
            this.nomeFuncao = nomeFuncao;
        }

        @Override
        protected String doInBackground(Request... requests) {
            try {
                Response response = client.newCall(requests[0]).execute();
                resposta = response.body().string();
                Log.d("PagSeguro","sucesso na execução do request da função "+nomeFuncao+", resposta="+resposta);
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("PagSeguro","Falha na execução do request, erro="+e.getMessage());
            }
            return resposta;
        }


    }
}


