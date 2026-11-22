import java.util.Random;

public class RedeNeural {

    // 5 entradas / 10 escondidos / 3 saidas
    public static final int numEntradas = 5;
    public static final int numEscondidos = 10;
    public static final int numSaidas = 3;

    // Entrada -> Escondida
    public double[][] pesoV = new double[numEntradas][numEscondidos];
    public double[] pesoV_bias = new double[numEscondidos];

    //Escondida -> Saida
    public double[][] pesoW = new double[numEscondidos][numSaidas];
    public double[] pesoW_bias = new double[numSaidas];

    public double[] z_in = new double[numEscondidos];
    public double[] z = new double[numEscondidos];
    public double[] y_in = new double[numSaidas];
    public double[] y = new double[numSaidas];

    public RedeNeural() {
        InicializarPesos();
    }

    public void InicializarPesos() {
        Random numAleatorio = new Random();

        //Entrada -> Escondida
        for (int i=0; i<numEntradas; i++) {
            for (int j=0; j<numEscondidos; j++) {
                //Calcular o pesoV para cada camada escondida
                pesoV[i][j] = ((Math.pow(-1, numAleatorio.nextInt(10))) * numAleatorio.nextDouble()) / 3.0;
            }
        }

        for (int j=0; j<numEscondidos; j++) {
            pesoV_bias[j] = ((Math.pow(-1, numAleatorio.nextInt(10))) * numAleatorio.nextDouble()) / 3.0;
        }

        //Escondida -> Saída
        for (int j=0; j<numEscondidos; j++) {
            for (int k=0; k<numSaidas; k++) {
                pesoW[j][k] = ((Math.pow(-1, numAleatorio.nextInt(10))) * numAleatorio.nextDouble()) / 3.0;
            }
        }

        for (int k=0; k<numSaidas; k++) {
            pesoW_bias[k] = ((Math.pow(-1, numAleatorio.nextInt(10))) * numAleatorio.nextDouble()) / 3.0;
        }
    }

    public double FuncaoDeAtivacao(double x) {
        double exponencial = Math.exp(-1 * x);
        return (2.0 / (1.0 + exponencial)) - 1.0;
    }

    //Mede o grau de confiança do neuronio
    public double DerivadaDaFuncaoDeAtivacao(double x){
        double funcaoX = FuncaoDeAtivacao(x);
        return 0.5 * ((1.0 + funcaoX) * (1.0 - funcaoX));
    }

    //FEEDFORWARD
    public double[] CalcularSaida(double[] entradas){
        for(int j=0; j<numEscondidos; j++) {
            z_in[j] = pesoV_bias[j];
            for (int i = 0; i < numEntradas; i++) {
                z_in[j] = z_in[j] + (entradas[i] * pesoV[i][j]);
            }
            z[j] = FuncaoDeAtivacao(z_in[j]);
        }
        for(int k=0; k<numSaidas; k++){
            y_in[k] = pesoW_bias[k];
            for(int j=0; j<numEscondidos; j++){
                y_in[k] = y_in[k] + (z[j] * pesoW[j][k]);
            }
            y[k] = FuncaoDeAtivacao(y_in[k]);
        }
        //Ex: Retorna [0.8, -0.2, -0.9]. A raquete irá se mover para esquerda, pois o maior número
        //0.8 que está na posição 0 está la.
        return y;
    }

    //BACKPROPAGATION
    public double TreinarBackpropagation(double[] entradas, double[] saidaDesejada, double taxaAprendizagem){
        CalcularSaida(entradas);

        //Calcula erro na camada de saída
        double[] deltaSaida = new double[numSaidas];
        double erroTotal = 0.0;

        for(int k=0; k<numSaidas; k++){
            double erro = saidaDesejada[k] - y[k];

            erroTotal = erroTotal + Math.abs(erro);

            deltaSaida[k] = erro * DerivadaDaFuncaoDeAtivacao(y_in[k]);
        }

        //Calcula o erro na camada escondida
        double[] deltaEscondida = new double[numEscondidos];
        for(int j=0; j<numEscondidos; j++){
            double somatorioErro = 0.0;

            for(int k=0; k<numSaidas; k++){
                somatorioErro = somatorioErro + (deltaSaida[k] * pesoW[j][k]);
            }
            deltaEscondida[j] = somatorioErro * DerivadaDaFuncaoDeAtivacao(z_in[j]);
        }

        //Corrigindo os pesos e os bias
        //PesoW
        for(int j=0; j<numEscondidos; j++){
            for(int k=0; k<numSaidas; k++){
                pesoW[j][k] = pesoW[j][k] + (taxaAprendizagem * deltaSaida[k] * z[j]);
            }
        }

        for(int k=0; k<numSaidas; k++){
            pesoW_bias[k] = pesoW_bias[k] + (taxaAprendizagem * deltaSaida[k]);
        }

        //PesoV
        for(int i=0; i<numEntradas; i++){
            for(int j=0; j<numEscondidos; j++){
                pesoV[i][j] = pesoV[i][j] + (taxaAprendizagem * deltaEscondida[j] * entradas[i]);
            }
        }

        for(int j=0; j<numEscondidos; j++){
            pesoV_bias[j] = pesoV_bias[j] + (taxaAprendizagem + deltaEscondida[j]);
        }

        return erroTotal;
    }

    //Cria uma copia dos pesos da rede
    public RedeNeural clonar(){
        RedeNeural copia = new RedeNeural();

        for(int i = 0; i < numEntradas; i++){
            System.arraycopy(this.pesoV[i], 0, copia.pesoV[i], 0, numEscondidos);
        }
        System.arraycopy(this.pesoV_bias, 0, copia.pesoV_bias, 0, numEscondidos);

        for(int j = 0; j < numEscondidos; j++){
            System.arraycopy(this.pesoW[j], 0, copia.pesoW[j], 0, numSaidas);
        }
        System.arraycopy(this.pesoW_bias, 0, copia.pesoW_bias, 0, numSaidas);

        return copia;
    }

    //Aplica pequenas variacaoes aleatorias nos pesos
    public void mutar(double taxaMutacao, double intensidade){
        Random rand = new Random();

        for(int i = 0; i < numEntradas; i++){
            for(int j = 0; j < numEscondidos; j++){
                if(rand.nextDouble() < taxaMutacao){
                    pesoV[i][j] = (rand.nextDouble() * 2 - 1) * intensidade;
                }
            }
        }

        for(int j = 0; j < numEscondidos; j++){
            if(rand.nextDouble() < taxaMutacao){
                pesoV_bias[j] += (rand.nextDouble() * 2 - 1) * intensidade;
            }
        }

        for (int j = 0; j < numEscondidos; j++){
            for(int k = 0; k < numSaidas; k++){
                pesoW[j][k] += (rand.nextDouble() * 2 - 1) * intensidade;
            }
        }

        for(int k = 0; k < numSaidas; k++){
            if(rand.nextDouble() < taxaMutacao){
                pesoW_bias[k] += (rand.nextDouble() * 2 - 1) * intensidade;
            }
        }
    }
}

