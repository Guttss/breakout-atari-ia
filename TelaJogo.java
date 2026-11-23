import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//ActionListener serve para ouvir as batidas do Timer
public class TelaJogo extends JPanel implements ActionListener{

    //Lista que guarda todos os agentes
    private AlgoritmoGenetico ag = new AlgoritmoGenetico();
    private double melhorFitnessGeracao = 0;

    private int velocidadeSimulacao = 1; //1x = normal, 30 = turbo

    private Timer timer;

    // [NOVO] Variáveis do modo Jogador vs IA
    private boolean modoVersus = false;
    private AgenteJogo ultimoLider;
    private AgenteJogo oponenteIA;
    private AgenteJogo jogadorHumano;

    public TelaJogo(){
        //Define o tamanho exato da area de jogo
        setPreferredSize(new Dimension(AgenteJogo.LARGURA, AgenteJogo.ALTURA));
        setBackground(Color.BLACK);

        configurarTeclas();

        //Roda o jogo a 60 frames
        timer = new Timer(16, this);
        timer.start();
    }

    // [NOVO] Mapeia todas as teclas usando InputMap (evita problemas de foco do KeyListener)
    private void configurarTeclas() {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        // Espaço: alterna velocidade apenas durante o treino
        im.put(KeyStroke.getKeyStroke("SPACE"), "alternarVelocidade");
        am.put("alternarVelocidade", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!modoVersus) {
                    velocidadeSimulacao = (velocidadeSimulacao == 1) ? 30 : 1;
                }
            }
        });

        // Tecla V: entra no modo Jogador vs IA usando o melhor cérebro atual
        im.put(KeyStroke.getKeyStroke("V"), "iniciarVersus");
        am.put("iniciarVersus", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!modoVersus) {
                    iniciarModoVersus();
                }
            }
        });

        // Tecla R: revanche no modo Versus
        im.put(KeyStroke.getKeyStroke("R"), "revanche");
        am.put("revanche", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (modoVersus && oponenteIA != null && jogadorHumano != null) {
                    oponenteIA.resetar();
                    jogadorHumano.resetar();
                }
            }
        });

        // Tecla T: volta para o treinamento
        im.put(KeyStroke.getKeyStroke("T"), "voltarTreino");
        am.put("voltarTreino", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (modoVersus) {
                    voltarModoTreino();
                }
            }
        });

        // Controles do Jogador Humano (Setas ou A/D)
        String[] teclasEsquerda = {"LEFT", "A"};
        for (String tecla : teclasEsquerda) {
            im.put(KeyStroke.getKeyStroke("pressed " + tecla), "esqPress");
            im.put(KeyStroke.getKeyStroke("released " + tecla), "esqRel");
        }
        am.put("esqPress", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jogadorHumano != null) jogadorHumano.movendoEsquerda = true;
            }
        });
        am.put("esqRel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jogadorHumano != null) jogadorHumano.movendoEsquerda = false;
            }
        });

        String[] teclasDireita = {"RIGHT", "D"};
        for (String tecla : teclasDireita) {
            im.put(KeyStroke.getKeyStroke("pressed " + tecla), "dirPress");
            im.put(KeyStroke.getKeyStroke("released " + tecla), "dirRel");
        }
        am.put("dirPress", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jogadorHumano != null) jogadorHumano.movendoDireita = true;
            }
        });
        am.put("dirRel", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (jogadorHumano != null) jogadorHumano.movendoDireita = false;
            }
        });
    }

    // [NOVO] Prepara os dois tabuleiros lado a lado e ajusta a janela
    private void iniciarModoVersus() {
        AgenteJogo melhor = getMelhorAgenteVivo();
        if (melhor == null) melhor = ultimoLider;
        if (melhor == null) return;

        modoVersus = true;
        velocidadeSimulacao = 1; // Trava em 1x para o humano conseguir jogar

        // Cria a IA desafiante usando o cérebro do melhor agente, mas zerada na bola 1/3
        oponenteIA = new AgenteJogo(melhor.cerebro);
        oponenteIA.isHumano = false;

        // Cria o jogador humano nas exatas mesmas condições
        jogadorHumano = new AgenteJogo(null);
        jogadorHumano.isHumano = true;

        // Duas telas de 800x600 em escala 0.8 = 1280x480 (cabe em qualquer monitor)
        redimensionarJanela((int) (AgenteJogo.LARGURA * 2 * 0.8), (int) (AgenteJogo.ALTURA * 0.8));
    }

    // [NOVO] Retorna ao treino de onde parou
    private void voltarModoTreino() {
        modoVersus = false;
        redimensionarJanela(AgenteJogo.LARGURA, AgenteJogo.ALTURA);
    }

    // [NOVO] Redimensiona o JFrame automaticamente sem precisar alterar Breakout.java
    private void redimensionarJanela(int largura, int altura) {
        setPreferredSize(new Dimension(largura, altura));
        Window janela = SwingUtilities.getWindowAncestor(this);
        if (janela != null) {
            janela.pack();
            janela.setLocationRelativeTo(null);
        }
    }

    public AgenteJogo getMelhorAgenteVivo(){
        AgenteJogo melhor = null;
        for(AgenteJogo agente : ag.populacao){
            if(agente.vivo && (melhor == null || agente.fitness > melhor.fitness)){
                melhor = agente;
            }
        }
        if (melhor != null) ultimoLider = melhor;
        return melhor;
    }

    @Override
    public void paintComponent(Graphics g){
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (modoVersus) {
            desenharModoVersus(g2d);
            return;
        }

        // --- MODO TREINO ORIGINAL ---
        AgenteJogo lider = getMelhorAgenteVivo();
        int vivos = 0;

        for(AgenteJogo agente: ag.populacao){
            if(agente.vivo){
                vivos++;
                if(agente != lider){
                    agente.desenhar(g2d, false);
                }
            }
        }

        if(lider != null){
            lider.desenhar(g2d, true);
        }

        int bolaAtual = (lider != null) ? (lider.tentativaAtual + 1) : 1;
        int totalBolas = AgenteJogo.ANGULOS_TESTE.length;
        double velBola = (lider != null) ? lider.velocidadeBola : 0.0;

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 13));
        g2d.drawString(
                String.format("Ger: %d | Vivos: %d/%d | Bola: %d/%d | Fit: %.0f | Sim: %dx [ESPACO] | [V] Desafiar IA",
                        ag.geracao, vivos, ag.tamanhoPopulacao, bolaAtual, totalBolas,
                        melhorFitnessGeracao, velocidadeSimulacao),
                15, 25
        );
    }

    // [NOVO] Renderiza a tela dividida (IA à esquerda, Humano à direita)
    private void desenharModoVersus(Graphics2D g2d) {
        g2d.scale(0.8, 0.8); // Escala para 1280x480 mantendo as coordenadas 800x600 originais

        // 1. Lado Esquerdo: IA
        oponenteIA.desenhar(g2d, true);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setColor(Color.GREEN);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2d.drawString(String.format("IA (Ger %d) | Blocos: %d | Bola: %d/3",
                ag.geracao, oponenteIA.blocosQuebrados, Math.min(3, oponenteIA.tentativaAtual + 1)), 20, 28);

        if (!oponenteIA.vivo) {
            desenharAvisoFim(g2d, "IA FINALIZOU");
        }

        // Divisória central
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(AgenteJogo.LARGURA - 2, 0, 4, AgenteJogo.ALTURA);

        // 2. Lado Direito: Desloca a origem X em +800px e desenha o Humano
        g2d.translate(AgenteJogo.LARGURA, 0);
        jogadorHumano.desenhar(g2d, true);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.setColor(Color.CYAN);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
        g2d.drawString(String.format("VOCÊ [Setas/AD] | Blocos: %d | Bola: %d/3",
                jogadorHumano.blocosQuebrados, Math.min(3, jogadorHumano.tentativaAtual + 1)), 20, 28);

        if (!jogadorHumano.vivo) {
            desenharAvisoFim(g2d, "VOCÊ FINALIZOU");
        }

        // Volta a origem para o centro da tela inteira para mostrar o resultado final
        g2d.translate(-AgenteJogo.LARGURA, 0);
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setFont(new Font("Monospaced", Font.PLAIN, 14));
        g2d.drawString("[R] Revanche   |   [T] Voltar ao Treino", 615, AgenteJogo.ALTURA - 12);

        if (!oponenteIA.vivo && !jogadorHumano.vivo) {
            String resultado;
            if (jogadorHumano.blocosQuebrados > oponenteIA.blocosQuebrados) {
                resultado = "VOCÊ VENCEU! (" + jogadorHumano.blocosQuebrados + " x " + oponenteIA.blocosQuebrados + " blocos)";
            } else if (oponenteIA.blocosQuebrados > jogadorHumano.blocosQuebrados) {
                resultado = "A IA VENCEU! (" + oponenteIA.blocosQuebrados + " x " + jogadorHumano.blocosQuebrados + " blocos)";
            } else {
                resultado = "EMPATE! (" + jogadorHumano.blocosQuebrados + " blocos)";
            }

            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(450, 230, 700, 120);
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 26));
            g2d.drawString(resultado, 500, 285);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 16));
            g2d.drawString("Pressione [R] para jogar novamente ou [T] para treinar mais", 495, 325);
        }
    }

    private void desenharAvisoFim(Graphics2D g2d, String texto) {
        g2d.setColor(new Color(0, 0, 0, 150));
        g2d.fillRect(0, 0, AgenteJogo.LARGURA, AgenteJogo.ALTURA);
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 28));
        g2d.drawString(texto, 280, 300);
    }

    //Onde acontece todo o jogo
    @Override
    public void actionPerformed(ActionEvent e) {
        if (modoVersus) {
            if (oponenteIA != null && oponenteIA.vivo) oponenteIA.atualizar();
            if (jogadorHumano != null && jogadorHumano.vivo) jogadorHumano.atualizar();
            repaint();
            return;
        }

        // --- LOOP DE TREINO ORIGINAL ---
        for (int passo = 0; passo < velocidadeSimulacao; passo++) {
            boolean algumVivo = false;
            melhorFitnessGeracao = 0;

            for (AgenteJogo agente : ag.populacao) {
                if (agente.vivo) {
                    agente.atualizar();

                    if (agente.fitness > melhorFitnessGeracao) {
                        melhorFitnessGeracao = agente.fitness;
                    }

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