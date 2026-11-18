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

    private Timer timer;

    public TelaJogo(){
        setBackground(Color.BLACK);

        //Roda o jogo a 60 frames
        timer = new Timer(16, this);
        timer.start();
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

        if(bolaY > getHeight()){
            bolaY = 400;
            bolaX = 300;
            velY = -4;
        }

        repaint();
    }
}
