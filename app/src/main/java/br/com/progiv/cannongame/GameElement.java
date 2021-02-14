package br.com.progiv.cannongame;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class GameElement {
    protected CannonView view; //view que contém esse GameElement
    protected  Paint paint = new Paint(); // objeto Paint para desenhar
    protected Rect shape; //os limites retangulares do GameElement
    protected float velocity; //velocidade vertical
    protected int soundId; // id do som associado

    //construtor
    public GameElement(CannonView view, int color, int soundId, int x, int y, int width, int lenght, float velocity){
        this.view=view;
        paint.setColor(color);
        shape = new Rect(x, y, x+width, y+lenght);
        this.soundId=soundId;
        this.velocity=velocity;
    }

    //atualizar a posição de GameElement e verificar se há colisões com a parede
    public void update(double interval){
        //atualizar posição vertical
        shape.offset(0,(int)(velocity*interval));

        //se GameElement colide com a parede, inverte a direção:
        if(shape.top<0 && velocity<0 || shape.bottom>view.getScreenHeight() && velocity>0)
            velocity *= -1;
    }

    //desenhar o objeto Canvas
    public void draw(Canvas canvas){
        canvas.drawRect(shape, paint);
    }

    //reproduzir som correspondente ao tipo de objeto
    public void playSound(){
        view.playSound(soundId);
    }
}
