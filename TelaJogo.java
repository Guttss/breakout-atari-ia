import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//ActionListener serve para ouvir as batidas do Timer
public class TelaJogo extends JPanel implements ActionListener{

    //Lista que guarda todos os agentes
    private AlgoritmoGenetico ag = new AlgoritmoGenetico();
    private double melhorFitnessGeracao = 0;
    
    private int velocidadeSimulacao = 1; //1x = normal, ? = turbo

    private Timer timer;

    public TelaJogo(){
        //Define o tamanho exatoda area de jogo
        setPreferredSize(new Dimension(AgenteJogo.LARGURA, AgenteJogo.ALTURA));
        setBackground(Color.BLACK);

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "alternarVelocidade");
        getActionMap().put("alternarVelocidade", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                velocidadeSimulacao = (velocidadeSimulacao == 1)? 30 : 1;
            }
        });

        //Roda o jogo a 60 frames
        timer = new Timer(16, this);
        timer.start();
    }

    public AgenteJogo getMelhorAgenteVivo(){
        AgenteJogo melhor = null;
        for(AgenteJogo agente : ag.populacao){
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
        for(AgenteJogo agente: ag.populacao){
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
        int bolaAtual = (lider != null) ? (lider.tentativaAtual + 1) : 1;
        int totalBolas = AgenteJogo.ANGULOS_TESTE.length;
        double velBola = (lider != null) ? lider.velocidadeBola : 0.0;


        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 14));
        g2d.drawString(
                String.format("Geracao: %d | Vivos: %d/%d | Bola: %d/%d | Vel: %1f | Fit: %.0f | Sim: %dx [ESPACO]",
                        ag.geracao, vivos, ag.tamanhoPopulacao, bolaAtual, totalBolas,
                        velBola, melhorFitnessGeracao , velocidadeSimulacao),
                    15, 25

        );
    }

    //Onde acontece todo o jogo
    @Override
    public void actionPerformed(ActionEvent e) {
        //Executa 'velocidadeSimulacao' passos de fisica antes de desenhar 1 frame na tela
        for (int passo = 0; passo < velocidadeSimulacao; passo++) {
            boolean algumVivo = false;
            melhorFitnessGeracao = 0;

            //Atualiza a fisica e a decisao de cada agente vivo
            for (AgenteJogo agente : ag.populacao) {
                if (agente.vivo) {
                    agente.atualizar();

                    //Atualiza o recorde atual
                    if (agente.fitness > melhorFitnessGeracao) {
                        melhorFitnessGeracao = agente.fitness;
                    }

                    //Verifica se ele continuou vivo
                    if (agente.vivo) algumVivo = true;
                }
            }

            if (!algumVivo) {
                ag.evoluir();
            }
        }
        repaint();
    }
}
