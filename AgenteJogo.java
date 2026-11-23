import java.awt.*;

public class AgenteJogo {

    public static final int LARGURA = 800;
    public static final int ALTURA = 600;

    public static double anguloLancamento = Math.toRadians(45);

    //Bola e raquete
    public double bolaX, bolaY;
    public double raqueteX, raqueteY = 520;
    public int raqueteLargura = 100;
    public int raqueteAltura = 15;
    public double velRaquete = 11.0;
    public static final double VELOCIDADE_MAXIMA = 13.5;

    public double velX, velY;
    public double velocidadeBola = 6.0;

    //Blocos individual de cada agente
    public int linhas = 4;
    public int colunas = 8;
    public boolean[][] blocos = new boolean[linhas][colunas];

    public int blocoLargura = 80;
    public int blocoAltura = 25;
    public int blocoEspaco = 10;
    public int margemEsquerda = 45;
    public int margemTopo = 50;

    //Evolução e controle de vida
    public boolean vivo = true;
    public int blocosQuebrados = 0;
    public int rebatidas = 0;
    public int framesVivos = 0;
    public int framesSemPontuar = 0;
    public double fitness = 0;

    public static final double[] ANGULOS_TESTE = {
            Math.toRadians(42),
            Math.toRadians(-42),
            Math.toRadians(20)
    };
    public int tentativaAtual = 0;
    public double bonusAlinhamento = 0;

    public RedeNeural cerebro;

    // [NOVO] Controle para jogador humano
    public boolean isHumano = false;
    public boolean movendoEsquerda = false;
    public boolean movendoDireita = false;

    public AgenteJogo(){
        this.cerebro = new RedeNeural();
        resetar();
    }

    public AgenteJogo(RedeNeural cerebro){
        this.cerebro = cerebro;
        resetar();
    }

    public void resetar(){
        tentativaAtual = 0;
        vivo = true;
        blocosQuebrados = 0;
        rebatidas = 0;
        framesVivos = 0;
        bonusAlinhamento = 0;
        fitness = 0;

        iniciarTentativa();
    }

    public void iniciarTentativa(){
        bolaX = 400;
        bolaY = 300;
        raqueteX = 350;
        framesSemPontuar = 0;
        velocidadeBola = 6.0;

        double angulo = ANGULOS_TESTE[tentativaAtual];
        velX = velocidadeBola * Math.sin(angulo);
        velY = -velocidadeBola * Math.cos(angulo);

        reiniciarBlocos();
    }

    public void reiniciarBlocos(){
        for(int i = 0; i < linhas; i++){
            for(int j = 0; j < colunas; j++){
                blocos[i][j] = true;
            }
        }
    }

    public void moverEsquerda(){
        raqueteX -= velRaquete;
        if(raqueteX < 0) raqueteX = 0;
    }

    public void moverDireita(){
        raqueteX += velRaquete;
        if(raqueteX > LARGURA - raqueteLargura){
            raqueteX = LARGURA - raqueteLargura;
        }
    }

    public double[] getEntradas(){
        double centroBola = bolaX + 7.5;
        double centroRaquete = raqueteX + (raqueteLargura / 2.0);

        double distRelativa = ((centroBola - centroRaquete) / LARGURA) * 2.5;
        distRelativa = Math.max(-1.0, Math.min(1.0, distRelativa));

        return new double[]{
                centroBola / LARGURA,   //Posição X da bola
                bolaY / ALTURA,         //Posição Y da bola
                velX / velocidadeBola,  //Velocidade X
                velY / velocidadeBola,  //Velocidade Y
                distRelativa //Distância relativa entre a bola e a raquete
        };
    }

    //Executa 1 frame da simulação desse agente
    public void atualizar(){
        if(!vivo) return;

        framesVivos++;
        framesSemPontuar++;

        // [ALTERADO] Se for humano usa o teclado; senão usa a Rede Neural
        if (isHumano) {
            if (movendoEsquerda) moverEsquerda();
            if (movendoDireita) moverDireita();
        } else {
            //Le os 5 sensores e calcula as 3 saidas da rede
            double[] saidas = cerebro.CalcularSaida(getEntradas());

            int melhorAcao = 0;
            for(int i = 1; i < saidas.length; i++){
                if(saidas[i] > saidas[melhorAcao]){
                    melhorAcao = i;
                }
            }

            if(melhorAcao == 0){
                moverEsquerda();
            } else if(melhorAcao == 2){
                moverDireita();
            }
        }

        //atualizar velocidade da bola
        bolaX += velX;
        bolaY += velY;

        //Paredes
        if(bolaX <= 0){
            bolaX = 0;
            velX = Math.abs(velX);
        }
        else if(bolaX >= LARGURA - 15){
            bolaX = LARGURA - 15;
            velX = -Math.abs(velX);
        }

        //Teto
        if(bolaY <= 0){
            bolaY = 0;
            velY = Math.abs(velY);
        }

        //Colisão com a raquete
        if (bolaY + 15 >= raqueteY && bolaY <= raqueteY + raqueteAltura &&
                bolaX + 15 >= raqueteX && bolaX <= raqueteX + raqueteLargura) {

            //Aumenta a velocidade da bola
            velocidadeBola = Math.min(VELOCIDADE_MAXIMA, velocidadeBola + 0.25);

            double centroBola = bolaX + 7.5;
            double centroRaquete = raqueteX + (raqueteLargura / 2.0);
            double impacto = (centroBola - centroRaquete) / (raqueteLargura / 2.0);
            impacto = Math.max(-1.0, Math.min(1.0, impacto));

            double angulo = impacto * Math.toRadians(60);
            velX = velocidadeBola * Math.sin(angulo);
            velY = -velocidadeBola * Math.cos(angulo);
            bolaY = raqueteY - 15;

            rebatidas++;
            framesSemPontuar = 0;
        }
        loopBlocos:
        //Colisão com os blocos
        for(int i = 0; i < linhas; i++){
            for(int j = 0; j < colunas; j++){
                if(blocos[i][j]){
                    int blocoX = margemEsquerda + j * (blocoLargura + blocoEspaco);
                    int blocoY = margemTopo + i * (blocoAltura + blocoEspaco);

                    if (bolaX + 15 >= blocoX && bolaX <= blocoX + blocoLargura &&
                            bolaY + 15 >= blocoY && bolaY <= blocoY + blocoAltura){

                        blocos[i][j] = false;
                        blocosQuebrados++;
                        framesSemPontuar = 0;
                        velY = -velY;

                        if(blocosQuebrados % (linhas * colunas) == 0){
                            reiniciarBlocos();
                        }
                        break loopBlocos;
                    }
                }
            }
        }
        calcularFitness();

        //Bola caiu ou o agente entrou em loop
        if(bolaY > ALTURA || framesSemPontuar > 900){
            //Recompensa proximidade apenas se a bola caiu no fundo
            if(bolaY  > ALTURA){
                double centroBola = bolaX + 7.5;
                double centroRaquete = raqueteX + (raqueteLargura / 2.0);
                double distancia = Math.abs(centroBola - centroRaquete);

                bonusAlinhamento += Math.max(0, 10 - (distancia / 40.0));
                calcularFitness();
            }

            tentativaAtual++;
            if(tentativaAtual < ANGULOS_TESTE.length){
                iniciarTentativa();
            }else {
                vivo = false; //So encerra apos passar os 3 angulos
            }
        }
    }

    public void calcularFitness(){
        double centroBola = bolaX + 7.5;
        double centroRaquete = raqueteX + (raqueteLargura / 2.0);
        double erroNormalizado = Math.abs(centroBola - centroRaquete) / LARGURA;

        if(velY > 0){
            bonusAlinhamento += (1.0 - erroNormalizado) * 0.25;
        }

        if(raqueteX <= 1 || raqueteX >= LARGURA - raqueteLargura - 1){
            bonusAlinhamento -= 0.15;
        }

        fitness = (blocosQuebrados * 100.0) + (rebatidas * 20.0) + bonusAlinhamento;
    }

    public void desenhar(Graphics2D g2d, boolean isLider) {
        if(!vivo) return;

        if(isLider){
            //Opacidade para o agente vivo
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

            for(int i = 0; i < linhas; i++){
                for(int j = 0; j < colunas; j++){
                    if(blocos[i][j]){
                        int blocoX = margemEsquerda + j * (blocoLargura + blocoEspaco);
                        int blocoY = margemTopo + i * (blocoAltura + blocoEspaco);

                        if(i == 0) g2d.setColor(Color.RED);
                        else if(i == 1) g2d.setColor(Color.ORANGE);
                        else if(i == 2) g2d.setColor(Color.YELLOW);
                        else g2d.setColor(Color.GRAY);

                        g2d.fillRect(blocoX, blocoY, blocoLargura, blocoAltura);
                    }
                }
            }

            // [ALTERADO] Raquete ciano para o humano, verde para a IA
            g2d.setColor(isHumano ? Color.CYAN : Color.GREEN);
            g2d.fillRect((int)raqueteX, (int) raqueteY, raqueteLargura, raqueteAltura);


            g2d.setColor(Color.WHITE);
            g2d.fillOval((int) bolaX, (int) bolaY, 15, 15);
        }
        else{
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.20f));

            g2d.setColor(Color.GREEN);
            g2d.fillRect((int)raqueteX, (int) raqueteY, raqueteLargura, raqueteAltura);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillOval((int) bolaX, (int) bolaY, 15, 15);
        }
    }
}