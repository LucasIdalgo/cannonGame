package br.com.progiv.cannongame;

//Barreira
public class Blocker extends GameElement{
    private int missPenalty; //penalidade por erro

    //construtor
    public Blocker(CannonView view, int color, int missPenalty, int x, int y, int width, int lenght, float velocity) {
        super(view, color, CannonView.BLOCKER_SOUND_ID, x, y, width, lenght, velocity);
        this.missPenalty=missPenalty;
    }

    public int getMissPenalty(){
        return this.missPenalty;
    }
}
