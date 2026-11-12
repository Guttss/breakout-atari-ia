import java.util.Random;

public class AlgoritmoGenetico {

    public int tamanhoPopulacao = 50;
    public Individuo[] populacao;
    public int geracao;

    public AlgoritmoGenetico(){
        //Cria 50 lugares
        populacao = new Individuo[tamanhoPopulacao];
        geracao = 1;

        for(int i=0; i<tamanhoPopulacao; i++){
            //Cada vez ele cria um indivíduo com uma RedeNeural que tem pesos
            //totalmente aleatórios
            populacao[i] = new Individuo();
        }
    }

    //Escolher um bom "Pai" e "Mãe"
    public Individuo SelecaoIndividuo(){
        Random numAleatorio = new Random();

        int indice1 = numAleatorio.nextInt(tamanhoPopulacao);
        Individuo individuo1 = populacao[indice1];

        int indice2 = numAleatorio.nextInt(tamanhoPopulacao);
        Individuo individuo2 = populacao[indice2];

        int indice3 = numAleatorio.nextInt(tamanhoPopulacao);
        Individuo individuo3 = populacao[indice3];

        Individuo vencedor = individuo1;

        //Verifica se a nota do indivíduo é maior do que a do vencedor
        if(individuo2.fitness > vencedor.fitness){
            vencedor = individuo2;
        }

        if(individuo3.fitness > vencedor.fitness){
            vencedor = individuo3;
        }

        return vencedor;
    }

    public Individuo Crossover(Individuo pai, Individuo mae){

        //Nasce com um cerebro novo e aleatorio
        Individuo filho = new Individuo();
        Random numAleatorio = new Random();

        //Misturando genes pesoV (Entrada -> Escondido)
        for(int i=0; i<RedeNeural.numEntradas; i++){
            for(int j=0; j<RedeNeural.numEscondidos; j++){
                if(numAleatorio.nextBoolean()){
                    filho.cerebro.pesoV[i][j] = pai.cerebro.pesoV[i][j]; //Herda do pai
                }
                else{
                    filho.cerebro.pesoV[i][j] = mae.cerebro.pesoV[i][j]; //Herda da mãe
                }
            }

            for (int j = 0; j < RedeNeural.numEscondidos; j++) {
                if (numAleatorio.nextBoolean()) {
                    filho.cerebro.pesoV_bias[j] = pai.cerebro.pesoV_bias[j];
                } else {
                    filho.cerebro.pesoV_bias[j] = mae.cerebro.pesoV_bias[j];
                }
            }

            //Misturando genes PesoW (Escondidas -> Saidas)
            for (int j = 0; j < RedeNeural.numEscondidos; j++) {
                for (int k = 0; k < RedeNeural.numSaidas; k++) {
                    if (numAleatorio.nextBoolean()) {
                        filho.cerebro.pesoW[j][k] = pai.cerebro.pesoW[j][k];
                    } else {
                        filho.cerebro.pesoW[j][k] = mae.cerebro.pesoW[j][k];
                    }
                }
            }

            for (int k = 0; k < RedeNeural.numSaidas; k++) {
                if (numAleatorio.nextBoolean()) {
                    filho.cerebro.pesoW_bias[k] = pai.cerebro.pesoW_bias[k];
                } else {
                    filho.cerebro.pesoW_bias[k] = mae.cerebro.pesoW_bias[k];
                }
            }
        }
        return filho;
    }

    public void Mutacao(Individuo filho, double taxaMutacao){
        Random numAleatorio = new Random();

        //Percorre os genes PesoV
        //Se for abaixo da taxa (ex: 0.05) sofre mutação
        for(int i=0; i<RedeNeural.numEntradas; i++){
            for(int j=0; j<RedeNeural.numEscondidos; j++){
                if(numAleatorio.nextDouble() < taxaMutacao){
                    //Soma ou subtrai um pequeno valor aleatorio no peso
                    double operacao = (numAleatorio.nextDouble() * 2.0 - 1.0) * 0.3;
                    filho.cerebro.pesoV[i][j] = filho.cerebro.pesoV[i][j] + operacao;
                }
            }
        }

        //Percorre os genes PesoV_Bias
        for (int j = 0; j < RedeNeural.numEscondidos; j++) {
            if (numAleatorio.nextDouble() < taxaMutacao) {
                double operacao = (numAleatorio.nextDouble() * 2.0 - 1.0) * 0.3;
                filho.cerebro.pesoV_bias[j] = filho.cerebro.pesoV_bias[j] + operacao;
            }
        }

        //Percorre os genes PesoW
        for(int i=0; i<RedeNeural.numEntradas; i++){
            for(int j=0; j<RedeNeural.numEscondidos; j++){
                if(numAleatorio.nextDouble() < taxaMutacao){
                    //Soma ou subtrai um pequeno valor aleatorio no peso
                    double operacao = (numAleatorio.nextDouble() * 2.0 - 1.0) * 0.3;
                    filho.cerebro.pesoW[i][j] = filho.cerebro.pesoW[i][j] + operacao;
                }
            }
        }

        //Percorre os genes PesoV_Bias
        for (int j = 0; j < RedeNeural.numEscondidos; j++) {
            if (numAleatorio.nextDouble() < taxaMutacao) {
                double operacao = (numAleatorio.nextDouble() * 2.0 - 1.0) * 0.3;
                filho.cerebro.pesoW_bias[j] = filho.cerebro.pesoW_bias[j] + operacao;
            }
        }
    }
}
