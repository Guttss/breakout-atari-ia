import java.util.Arrays;
import java.util.Random;

public class AlgoritmoGenetico {

    public int tamanhoPopulacao = 50;
    public AgenteJogo[] populacao;
    public int geracao;

    private Random numAleatorio = new Random();

    public AlgoritmoGenetico(){
        //Cria 50 lugares
        populacao = new AgenteJogo[tamanhoPopulacao];
        geracao = 1;

        for(int i=0; i<tamanhoPopulacao; i++){
            //Cada vez ele cria um indivíduo com uma RedeNeural que tem pesos
            //totalmente aleatórios
            populacao[i] = new AgenteJogo();
        }
    }

    //Escolher um bom "Pai" e "Mãe"
    public AgenteJogo selecaoIndividuo(){
        AgenteJogo ind1 = populacao[numAleatorio.nextInt(tamanhoPopulacao)];
        AgenteJogo ind2 = populacao[numAleatorio.nextInt(tamanhoPopulacao)];
        AgenteJogo ind3 = populacao[numAleatorio.nextInt(tamanhoPopulacao)];

        AgenteJogo vencedor = ind1;
        if(ind2.fitness > vencedor.fitness) vencedor = ind2;
        if(ind3.fitness > vencedor.fitness) vencedor = ind3;

        return vencedor;
    }

    public AgenteJogo crossover(AgenteJogo pai, AgenteJogo mae){

        //Nasce com um cerebro novo e aleatorio
        AgenteJogo filho = new AgenteJogo();

        //Misturando genes pesoV (Entrada -> Escondido)
        for(int i=0; i<RedeNeural.numEntradas; i++) {
            for (int j = 0; j < RedeNeural.numEscondidos; j++) {
                if (numAleatorio.nextBoolean()) {
                    filho.cerebro.pesoV[i][j] = pai.cerebro.pesoV[i][j]; //Herda do pai
                } else {
                    filho.cerebro.pesoV[i][j] = mae.cerebro.pesoV[i][j]; //Herda da mãe
                }
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
        return filho;
    }

    public double limitarPeso(double valor){
        return Math.max(-3.0, Math.min(3.0, valor));
    }

    public void mutacao(AgenteJogo filho, double taxaMutacao){
        Random numAleatorio = new Random();

        //Percorre os genes PesoV
        //Se for abaixo da taxa (ex: 0.05) sofre mutação
        for(int i=0; i<RedeNeural.numEntradas; i++){
            for(int j=0; j<RedeNeural.numEscondidos; j++){
                if(numAleatorio.nextDouble() < taxaMutacao){
                    //Soma ou subtrai um pequeno valor aleatorio no peso
                    double operacao = (numAleatorio.nextDouble() * 2.0 - 1.0) * 0.3;
                    filho.cerebro.pesoV[i][j] = limitarPeso(filho.cerebro.pesoV[i][j] + operacao);
                }
            }
        }

        //Percorre os genes PesoV_Bias
        for (int j = 0; j < RedeNeural.numEscondidos; j++) {
            if (numAleatorio.nextDouble() < taxaMutacao) {
                double operacao = (numAleatorio.nextDouble() * 2.0 - 1.0) * 0.3;
                filho.cerebro.pesoV_bias[j] = limitarPeso(filho.cerebro.pesoV_bias[j] + operacao);
            }
        }

        //Percorre os genes PesoW
        for(int i=0; i<RedeNeural.numEscondidos; i++){
            for(int j=0; j<RedeNeural.numSaidas; j++){
                if(numAleatorio.nextDouble() < taxaMutacao){
                    //Soma ou subtrai um pequeno valor aleatorio no peso
                    double operacao = (numAleatorio.nextDouble() * 2.0 - 1.0) * 0.3;
                    filho.cerebro.pesoW[i][j] = limitarPeso(filho.cerebro.pesoW[i][j] + operacao);
                }
            }
        }

        //Percorre os genes PesoW_Bias
        for (int j = 0; j < RedeNeural.numSaidas; j++) {
            if (numAleatorio.nextDouble() < taxaMutacao) {
                double operacao = (numAleatorio.nextDouble() * 2.0 - 1.0) * 0.3;
                filho.cerebro.pesoW_bias[j] = limitarPeso(filho.cerebro.pesoW_bias[j] + operacao);
            }
        }
    }

    public void evoluir(){
        //Ordena a populacao do fitness maior para o menor
        Arrays.sort(populacao, (a, b) -> Double.compare(b.fitness, a.fitness));

        //Sorteia um novo angulo para a proxima geracao nao decorar o mapa
        double graus = (numAleatorio.nextDouble() * 100.0) - 50.0;
        if(Math.abs(graus) < 15.0) graus = 25.0;
        AgenteJogo.anguloLancamento = Math.toRadians(graus);

        AgenteJogo[] novaPopulacao = new AgenteJogo[tamanhoPopulacao];

        //Elitismo
        novaPopulacao[0] = new AgenteJogo(populacao[0].cerebro.clonar());
        novaPopulacao[1] = new AgenteJogo(populacao[1].cerebro.clonar());

        for(int i = 2; i < tamanhoPopulacao; i++){
            AgenteJogo pai = selecaoIndividuo();
            AgenteJogo mae = selecaoIndividuo();

            AgenteJogo filho = crossover(pai, mae);
            mutacao(filho, 0.10); //10% de chance de mutar cada peso

            novaPopulacao[i] = filho;
        }

        populacao = novaPopulacao;
        geracao++;
    }
}
