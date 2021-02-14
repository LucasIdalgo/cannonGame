package br.com.progiv.cannongame;

import android.graphics.Canvas;
import android.graphics.Rect;

public class CannonBall extends GameElement{
    private  float velocityX;
    private boolean onScreen;


    public CannonBall(CannonView view, int color, int soundId, int x, int y, int radius, int velocityX, float velocityY) {
        super(view, color, soundId, x, y, 2*radius, 2*radius, velocityY);
        this.velocityX=velocityX;
        onScreen=true;
    }

    //método que obtém o raio da bala
    public int getRadius(){
        return(shape.right-shape.left)/2;
    }

    //testar se a bala colide com o GameElement
    public boolean collidesWidth(GameElement element){
        return (Rect.intersects(shape, element.shape) && velocityX>0);
    }

    //retornar se a bala está na tela
    public boolean isOnScreen(){
        return onScreen;
    }

    //inverter a velocidade horizontal da bala
    public void reverseVelocityX(){
        velocityX *= -1;
    }

    //método update para atualizar a velocidade vertical da bala e verificar se há colisões
    @Override
    public void update(double interval) {
        super.update(interval); //atualiza a posição vertical da bala
        //atualizar a posiçao horizontal
        shape.offset((int)(velocityX*interval),0);

        //se a bala sair da tela
        if(shape.top<0 || shape.left <0 || shape.bottom>view.getScreenHeight() || shape.right>view.getScreenWidth()){
            onScreen = false; //configura para remover a bala da tela
        }
    }

    //desenha a bala de canhão na tela
    @Override
    public void draw(Canvas canvas) {
        canvas.drawCircle(shape.left+getRadius(), shape.top+getRadius(), getRadius(), paint);
    }
}
