import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//ActionListener serve para ouvir as batidas do Timer
public class TelaJogo extends JPanel implements ActionListener {

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

    private Timer timer;

    public TelaJogo(){
        setBackground(Color.BLACK);
        reiniciarBlocos();

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

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        //Desenha a bolinha em circulo nas coordenadas
        g.setColor(Color.WHITE);
        g.fillOval((int) bolaX, (int) bolaY, 15, 15);

        //Desenha a raquete em retangulo
        g.setColor(Color.GREEN);
        g.fillRect((int) raqueteX, (int) raqueteY, 100, 15);

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
            if(bolaX + 15 >= raqueteX && bolaX <= raqueteX + 100){
                velY = -Math.abs(velY); //Sempre que a bola bater na raquete ela volte
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
        repaint();
    }
}
