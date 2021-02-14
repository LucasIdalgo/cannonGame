package br.com.progiv.cannongame;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioAttributes;
import android.media.SoundPool;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import java.lang.annotation.Target;

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

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}
