public class Individuo {

    public RedeNeural cerebro;
    //Grava o quão bem ele foi
    public double fitness;

    public Individuo(RedeNeural cerebro, double fitness) {
        this.cerebro = cerebro;
        this.fitness = fitness;
    }

    public Individuo(){}
}
