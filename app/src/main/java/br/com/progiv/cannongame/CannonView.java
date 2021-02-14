package br.com.progiv.cannongame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.ArrayList;
import java.util.Random;

public class CannonView extends SurfaceView implements SurfaceHolder.Callback{

    private static final String TAG = "CannonView"; //para registrar erros

    // constantes para interação do jogo
    public static final int MISS_PENALTY = 2; //segundos subtraídos em caso de erro
    public static final int HIT_REWARD = 3; //segundos adicionados em caso de acerto

    // constantes para o canhão
    public static final double CANNON_BASE_RADIUS_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_WIDTH_PERCENT = 3.0 / 40;
    public static final double CANNON_BARREL_LENGTH_PERCENT = 1.0 / 10;

    //constantes para a bala
    public static final double CANNONBALL_RADIUS_PERCENT = 3.0 / 80;
    public static final double CANNONBALL_SPEED_PERCENT = 3.0 / 2;

    //constantes para os alvos
    public static final double TARGET_WIDTH_PERCENT = 1.0 / 40;
    public static final double TARGET_LENGTH_PERCENT = 3.0 / 20;
    public static final double TARGET_FIRST_X_PERCENT = 3.0 / 5;
    public static final double TARGET_SPACING_PERCENT = 1.0 / 60;
    public static final double TARGET_PIECES = 9;
    public static final double TARGET_MIN_SPEED_PERCENT = 3.0 / 4;
    public static final double TARGET_MAX_SPEED_PERCENT = 6.0 / 4;

    //constantes para a barreira
    public static final double BLOCKER_WIDTH_PERCENT = 1.0 / 40;
    public static final double BLOCKER_LENGTH_PERCENT = 1.0 / 4;
    public static final double BLOCKER_X_PERCENT = 1.0 / 2;
    public static final double BLOCKER_SPEED_PERCENT = 1.0;

    //o tamanho do texto é 1/18 da largura da tela
    public static final double TEXT_SIZE_PERCENT = 1.0 / 18;

    private CannonThread cannonThread; //controla o loop do jogo - threads
    private Activity activity; // para exibir a caixa de diálogo GameOver na Thread da tela
    private boolean dialogDisplayed = false;

    //objetos do jogo
    private Cannon cannon;
    private Blocker blocker;
    private ArrayList<Target> targets;

    //variáveis de dimensão
    private int screenWidth;
    private int screenHeight;

    //variáveis para loop do jogo e controle de estatísticas
    private boolean gameOver;
    private double timeLeft; //tempo restante, em segundos
    private int shotsFired; //tiros disparados pelo usuários
    private double totalElapsedTime; //segundos decorridos

    // constantes e variáveis para gerenciar sons
    public static final int TARGET_SOUND_ID = 0;
    public static final int CANNON_SOUND_ID = 1;
    public static final int BLOCKER_SOUND_ID = 2;
    private SoundPool soundPool; //reproduz os efeitos sonoros
    private SparseIntArray soundMap; //mapeia os identificadores para soundPool

    // variáveis paint utilizadas ao desenhar cada item na tela
    private Paint textPaint; //objeto Paint usado para desenhar texto
    private Paint backgroundPaint; //objeto Paint usado para limpar a área de desenho

    //construtor
    public CannonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        activity = (Activity)context; //armazena referencia para MainActivity

        //registra o receptor de SurfaceHolder
        getHolder().addCallback(this);

        //configurar atributo de áudio
        AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        attrBuilder.setUsage(AudioAttributes.USAGE_GAME);

        //inicializar o SoundPool para reproduzir os três efeitos do app
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(1);
        builder.setAudioAttributes(attrBuilder.build());
        soundPool = builder.build();

        //cria objeto Map de sons e carrega os sons previamente
        soundMap = new SparseIntArray(3); //criando um array de som
        soundMap.put(TARGET_SOUND_ID, soundPool.load(context, R.raw.target_hit,1));
        soundMap.put(CANNON_SOUND_ID, soundPool.load(context, R.raw.cannon_fire,1));
        soundMap.put(BLOCKER_SOUND_ID, soundPool.load(context, R.raw.blocker_hit,1));

        //texto de tempo
        textPaint = new Paint();
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.WHITE);
    }

    //obtém a largura de tela do jogo
    public int getScreenWidth() {
        return screenWidth;
    }

    //obtém a altura de tela do jogo
    public int getScreenHeight(){
        return screenHeight;
    }

    //reproduz um som com o soundId em soundMap
    public void playSound(int soundId){
        soundPool.play(soundMap.get(soundId), 1, 1, 1, 0, 1f);
    }

    //novo jogo
    public void newGame(){
        //construir canhão
        cannon = new Cannon(this,
                (int)(CANNON_BASE_RADIUS_PERCENT*screenHeight),
                (int)(CANNON_BARREL_LENGTH_PERCENT*screenWidth),
                (int)(CANNON_BARREL_WIDTH_PERCENT*screenHeight));

        Random random = new Random(); //determinar velocidades aleatótias

        //iniciar alvos
        targets = new ArrayList<>();

        //inicializar targetX para o primeiro alvo a esquerda
        int targetX = (int)(TARGET_FIRST_X_PERCENT*screenWidth);

        //calcular a coordenada Y dos alvos
        int targetY = (int)((0.5 - TARGET_LENGTH_PERCENT/2)*screenHeight);

        //adicionar TARGET_PIECES alvos a lista de alvos
        for(int n=0;n<TARGET_PIECES;n++){
            //determinar a velocidade aleatória entre os valores min e max para o alvo 'n'
            double velocity = screenHeight * (random.nextDouble()*
                    (TARGET_MAX_SPEED_PERCENT-TARGET_MIN_SPEED_PERCENT)+TARGET_MIN_SPEED_PERCENT);

            //alternar as cores dos alvos entre ESCURA E CLARA
            int color = (n%2==0)?
                    getResources().getColor(R.color.dark, getContext().getTheme()):
                    getResources().getColor(R.color.light, getContext().getTheme());

            //inverter a velocidade inicial para o próximo alvo
            velocity *= -1;

            //cria e adiciona um novo alvo a lista de alvos
            targets.add(
              new Target(this, color, HIT_REWARD, targetX, targetY,
                      (int)(TARGET_WIDTH_PERCENT*screenWidth),
                      (int)(TARGET_LENGTH_PERCENT*screenWidth),
                      (int)velocity)
            );

            //aumentar coordenada X para posicionar próximo alvo mais a direita

            targetX += (TARGET_WIDTH_PERCENT + TARGET_SPACING_PERCENT)*screenWidth;
        }

        //criar uma barreira
        blocker = new Blocker(this,Color.BLACK,MISS_PENALTY,
                (int)(BLOCKER_X_PERCENT*screenWidth),
                (int)((0.5-BLOCKER_LENGTH_PERCENT/2)*screenHeight),
                (int)(BLOCKER_WIDTH_PERCENT*screenWidth),
                (int)(BLOCKER_LENGTH_PERCENT*screenHeight),
                (float)(BLOCKER_SPEED_PERCENT*screenHeight)
        );

        //criar contagem regressiva em 20 segundos
        timeLeft=20;

        //configurar número inicial de tiros disparados
        shotsFired=0;

        //configurar tempo decorrido como zero
        totalElapsedTime=0.0;

        //inicia um novo jogo após ultimo
        if(gameOver){
            gameOver=false;
            cannonThread=new CannonThread(getHolder()); //cria nova thread
            cannonThread.start(); //inicia thread de loop do jogo
        }
        hideSystemBars();
    }

    //chamado repetidamente por CannonThread para atualizar elementos do jogo
    private  void updatePositions(double elapsedTimeMS){
        double interval = elapsedTimeMS*1000.0; //converte em segundos

        //atualizar a posição da bala, se estiver na tela
        if(cannon.getCannonBall()!=null)
            cannon.getCannonBall().update(interval);

        //atualizar a posição da barreira
        blocker.update(interval);

        //atualizar a posição dos alvos
        for(GameElement target:targets)
            target.update(interval);

        //subtrair o tempo restante
        timeLeft -= interval;

        //se o cronometro zerar
        if (timeLeft<=0){
            timeLeft=0.0;
            gameOver=true; //jogo terminou
            cannonThread.setRunning(false); //termina thread
            showGameOverDialog("Você perdeu"); //mostra caixa de diálogo
        }

        //se todas as peças foram atingidas
        if(targets.isEmpty()){
            cannonThread.setRunning(false);
            showGameOverDialog("Você ganhou");
            gameOver=true;
        }
    }

    //alinhar o cano e disparar uma bala, caso não haja uma na tela
    public void alignAndFireCannonBall(MotionEvent event){
        //obtem o local do toque nessa view
        Point touchPoint = new Point((int)event.getX(),(int)event.getY());

        //calcular a distancia do toque a partir do centro
        double centerMinusY = (screenHeight/2-touchPoint.y);
        double angle =0; //inicializa o ângulo com 0
    }
}
