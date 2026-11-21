import javax.swing.*;

public class Breakout extends JFrame {

    public Breakout(){
        add(new TelaJogo());

        setTitle("Atari Breakout - Inteligência Artificial");
        setDefaultCloseOperation(EXIT_ON_CLOSE); //Fecha o jogo ao clicar no X
        setLocationRelativeTo(null);//Abre o jogo no meio da tela
        setResizable(false);//Impede de mexer nas dimensões da tela
        pack();
    }

    public static void main(String[] args){
        new Breakout().setVisible(true);//Cria a janela e deixa visível
    }
}
