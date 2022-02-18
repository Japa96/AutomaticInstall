package model;

public class ResultTests {

    private int quantidadeArquivos;
    private int sucesso = 0;
    private int falha = 0;

    public ResultTests(){

    }

    public int getQuantidadeArquivos() {
        return quantidadeArquivos;
    }

    public void setQuantidadeArquivos(int quantidadeArquivos) {
        this.quantidadeArquivos = quantidadeArquivos;
    }

    public int getSucesso() {
        return sucesso;
    }

    public void setSucesso() {
        this.sucesso += 1;
    }

    public int getFalha() {
        return falha;
    }

    public void setFalha() {
        this.falha += 1;
    }
}
