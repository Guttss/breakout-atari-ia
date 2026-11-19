import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

//ActionListener serve para ouvir as batidas do Timer
public class TelaJogo extends JPanel implements ActionListener, KeyListener {

    double velocidadeTotal = 6.0;

    //Posição da bola e da raquete
    public double bolaX = 400, bolaY = 300;
    public double raqueteX = 350, raqueteY = 520;

    //Velocidade da bola
    public double velX = 4, velY = -4;

    public int linhas = 4;
    public int colunas = 8;
    public boolean[][] blocos = new boolean[linhas][colunas];

    //Tamanho e posicao inicial dos blocos
    public int blocoLargura = 80;
    public int blocoAltura = 25;
    public int blocoEspaco = 10; //Entre cada bloco
    public int margemEsquerda = 45;
    public int margemTopo = 50;

    public int blocosQuebrados = 0;

    public int raqueteLargura = 100;
    public double velRaquete = 8;

    private boolean indoEsquerda = false;
    private boolean indoDireita = false;

    private Timer timer;

    public TelaJogo(){
        setBackground(Color.BLACK);
        reiniciarBlocos();
        setFocusable(true);
        addKeyListener(this);

        //Roda o jogo a 60 frames
        timer = new Timer(16, this);
        timer.start();
    }

    public void reiniciarBlocos(){
        blocosQuebrados = 0;
        for(int i = 0; i < linhas; i++){
            for (int j = 0; j < colunas; j++){
                blocos[i][j] = true;
            }
        }
    }

    public void moverEsquerda(){
        raqueteX -= velRaquete;
        if(raqueteX < 0){
            raqueteX = 0;
        }
    }

    public void moverDireita(){
        raqueteX += velRaquete;
        if(getWidth() > 0 && raqueteX > getWidth() - raqueteLargura){
            raqueteX = getWidth() - raqueteLargura;
        }
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        //Desenha a bolinha em circulo nas coordenadas
        g.setColor(Color.WHITE);
        g.fillOval((int) bolaX, (int) bolaY, 15, 15);

        //Desenha a raquete em retangulo
        g.setColor(Color.GREEN);
        g.fillRect((int) raqueteX, (int) raqueteY, raqueteLargura, 15);

        //Desenhar os blocos
        for(int i = 0; i < linhas; i++){
            for(int j = 0; j < colunas; j++){
                if(blocos[i][j]){ //Desenha se o bloco ainda nao foi quebrado
                    int blocoX = margemEsquerda + j * (blocoLargura + blocoEspaco);
                    int blocoY = margemTopo + i * (blocoAltura + blocoEspaco);

                    //Muda a cor conforme a linha
                    if(i == 0) g.setColor(Color.RED);
                    else if(i == 1) g.setColor(Color.ORANGE);
                    else if(i == 2) g.setColor(Color.YELLOW);
                    else g.setColor(Color.CYAN);

                    g.fillRect(blocoX, blocoY, blocoLargura, blocoAltura);
                }
            }
        }
    }

    //Onde acontece todo o jogo
    @Override
    public void actionPerformed(ActionEvent e) {

        bolaX = bolaX + velX;
        bolaY = bolaY + velY;

        //Faz a bolinha quicar nas laterais
        if(bolaX <= 0 || bolaX >= getWidth() - 15){
            velX = -velX;
        }

        //Quicar no teto
        if(bolaY <= 0){
            velY = -velY;
        }

        //Quicar na raquete
        if(bolaY + 15 >= raqueteY && bolaY <= raqueteY + 15){
            if(bolaX + 15 >= raqueteX && bolaX <= raqueteX + raqueteLargura){

                //Acha o centro de cada objeto
                double centroBola = bolaX + 7.5;
                double centroRaquete = raqueteX + (raqueteLargura / 2.0);

                double impacto = (centroBola - centroRaquete) / (raqueteLargura / 2.0);
                impacto = Math.max(-1.0, Math.min(1.0, impacto));

                //Converte o impacto em um angulo de ate 60 graus
                double anguloMax = Math.toRadians(60);
                double angulo = impacto * anguloMax;

                velX = velocidadeTotal * Math.sin(angulo);
                velY = -velocidadeTotal * Math.cos(angulo); //Negativo para a bola subir

                //Reposiciona a bola em cima da raquete
                bolaY = raqueteY - 15;

            }
        }

        //A borda ultrapassou o limite
        if(bolaY > getHeight()){
            bolaY = 400;
            bolaX = 300;
            velY = -4;
            reiniciarBlocos();
        }

        //Colisao com os blocos
        for(int i = 0; i < linhas; i++){
            for(int j = 0; j < colunas; j++){
                if(blocos[i][j]){
                    int blocoX = margemEsquerda + j * (blocoLargura + blocoEspaco);
                    int blocoY = margemTopo + i * (blocoAltura + blocoEspaco);

                    //Verifica se a bola encostou no bloco
                    if(bolaX + 15 >= blocoX && bolaX <= blocoX + blocoLargura &&
                        bolaY + 15 >= blocoY && bolaY <= blocoY + blocoAltura){
                        blocos[i][j] = false; //Quebra o bloco
                        blocosQuebrados++;
                        velY = -velY;
                        break;
                    }
                }
            }
        }

        if(indoEsquerda) moverEsquerda();
        if(indoDireita) moverDireita();

        repaint();
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) indoEsquerda = true;
        if(e.getKeyCode() == KeyEvent.VK_RIGHT) indoDireita = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) indoEsquerda = false;
        if(e.getKeyCode() == KeyEvent.VK_RIGHT) indoDireita = false;
    }
}
