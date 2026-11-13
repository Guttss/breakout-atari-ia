import javax.swing.*;
import java.awt.event.ActionListener;

public class TelaJogo extends JPanel implements ActionListener {

    //Posição da bola e da raquete
    public double bolaX = 400, bolaY = 300;
    public double raqueteX = 350, raqueteY = 550;

    private Timer timer;
}
