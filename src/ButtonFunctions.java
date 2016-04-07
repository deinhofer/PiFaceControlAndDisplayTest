/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hammer
 */
public abstract class ButtonFunctions {

    private static final byte S0 = (byte) 0xfe;
    private static final byte S1 = (byte) 0xfd;
    private static final byte S2 = (byte) 0xfb;
    private static final byte S3 = (byte) 0xf7;
    private static final byte S4 = (byte) 0xef;
    private static final byte S5 = (byte) 0xdf;
    private static final byte S6 = (byte) 0xbf;
    private static final byte S7 = (byte) 0x7f;
    
    /**
     * reads the button value
     * @return the value of the buttons
     */
    public abstract byte Read();
    /**
     * button play was pressed
     */
    public abstract void Play();
    /**
     * button pause was pressed
     */
    public abstract void Pause();
    /**
     * button stop was pressed
     */
    public abstract void Stop();
    /**
     * button next was pressed
     */
    public abstract void Next();
    /**
     * button back was pressed
     */
    public abstract void Back();
    //public abstract void Select();
    
    /**
     * starts a new Thread which reads the buttons continously
     * calls the specific function if a button is pressed
     */
    public void ReadButtons(){
            @SuppressWarnings("Convert2Lambda")
            Runnable run = new Runnable() {
                @Override
                @SuppressWarnings("SleepWhileInLoop")
                public void run() {
                    byte data_old = (byte) 0xff;
                    while(true) {
                        byte data = Read();
                        if(data != data_old) {
                            switch(data) {
                                case S0:
                                    Play();
                                    break;
                                case S1:
                                    Pause();
                                    break;
                                case S2:
                                    Stop();
                                    break;
                                case S6:
                                    Back();
                                    break;
                                case S7:
                                    Next();
                                    break;
                                default :
                                    break;
                            }
                            data_old = data;
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            };
            Thread thread = new Thread(run);
            thread.setPriority(Thread.MAX_PRIORITY);
            thread.start();
        }
}