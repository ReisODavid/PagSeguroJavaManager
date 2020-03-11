package com.lilithandbelial.jd.Ferramentas.FerramentasPagamento;

public class PagSeguroCartao {
    String nome,numero,mexExp,anoExp,bandeira;

    public PagSeguroCartao(String nome,String numero,String mexExp, String anoExp,String bandeira){
        this.nome = nome;
        this.numero = numero;
        this.mexExp = mexExp;
        this.anoExp = anoExp;
        this.bandeira = bandeira;

    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getMexExp() {
        return mexExp;
    }

    public void setMexExp(String mexExp) {
        this.mexExp = mexExp;
    }

    public String getAnoExp() {
        return anoExp;
    }

    public void setAnoExp(String anoExp) {
        this.anoExp = anoExp;
    }

    public String getBandeira() {
        return bandeira;
    }

    public void setBandeira(String bandeira) {
        this.bandeira = bandeira;
    }
}
