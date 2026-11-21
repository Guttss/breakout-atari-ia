import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

//ActionListener serve para ouvir as batidas do Timer
public class TelaJogo extends JPanel implements ActionListener{

    //Lista que guarda todos os agentes
    private List<AgenteJogo> populacao = new ArrayList<>();
    private int tamanhoPopulacao = 20;
    private int geracao = 1;
    private double melhorFitnessGeracao = 0;

    private Timer timer;

    public TelaJogo(){
        //Define o tamanho exatoda area de jogo
        setPreferredSize(new Dimension(AgenteJogo.LARGURA, AgenteJogo.ALTURA));
        setBackground(Color.BLACK);

        iniciarPopulacao();

        //Roda o jogo a 60 frames
        timer = new Timer(16, this);
        timer.start();
    }

    //Cria 50 agentes iniciais
    public void iniciarPopulacao(){
        populacao.clear();
        for(int i = 0; i < tamanhoPopulacao; i++){
            populacao.add(new AgenteJogo());
        }
    }

    public AgenteJogo getMelhorAgenteVivo(){
        AgenteJogo melhor = null;
        for(AgenteJogo agente : populacao){
            if(agente.vivo && (melhor == null || agente.fitness > melhor.fitness)){
                melhor = agente;
            }
        }
        return melhor;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);

        //Converte Graphics para Graphics2D
        Graphics2D g2d = (Graphics2D) g;

        AgenteJogo lider = getMelhorAgenteVivo();
        int vivos = 0;

        //Desenha todos os agentes secundarios
        for(AgenteJogo agente: populacao){
            if(agente.vivo){
                vivos++;
                if(agente != lider){
                    agente.desenhar(g2d, false);
                }
            }
        }

        //Desenha o lider por cima de todos
        if(lider != null){
            lider.desenhar(g2d, true);
        }

        //Informacoes na tela
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.drawString(
                String.format("Geracao: %d | Vivos: %d %d | Melhor Fitness: %.1f",
                        geracao, vivos, tamanhoPopulacao, melhorFitnessGeracao),
                    15, 25

        );
    }

    //Onde acontece todo o jogo
    @Override
    public void actionPerformed(ActionEvent e) {

        boolean algumVivo = false;
        melhorFitnessGeracao = 0;

        //Atualiza a fisica e a decisao de cada agente vivo
        for(AgenteJogo agente : populacao){
            if(agente.vivo){
                agente.atualizar();

                //Atualiza o recorde atual
                if(agente.fitness > melhorFitnessGeracao){
                    melhorFitnessGeracao = agente.fitness;
                }

                //Verifica se ele continuou vivo
                if(agente.vivo) algumVivo = true;
            }
        }

        if(!algumVivo){
            geracao++;

            //Ponto de conexao do Algoritmo Genetico
            for(AgenteJogo agente: populacao) agente.resetar();
        }
        repaint();
    }
}
