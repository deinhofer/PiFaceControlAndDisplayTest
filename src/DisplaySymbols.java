/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author hammer
 */
public class DisplaySymbols {
     private static final byte bitmapPlay[] = {16,24,28,30,28,24,16,0};
     private static final byte bitmapStop[] = {0,31,31,31,31,31,0,0};
     private static final byte bitmapPause[] = {0,27,27,27,27,27,0,0};
     
     public enum Symbols {
         Play(0), Stop(1), Pause(2);
         
         private final int value;
         private Symbols(int value) {
             this.value = value;
         }
         /**
          * @return the value of the enum type
          */
         public int getValue() {
             return value;
         }
     }
     /**
      * Constructor of DisplaySymbols
      * saves the DisplaySymbols on the display controller
      * @param spi 
      */
     public DisplaySymbols(SPI spi) {
         spi.LCDStoreCustomBitmap(Symbols.Play.getValue(), bitmapPlay);
         spi.LCDStoreCustomBitmap(Symbols.Stop.getValue(), bitmapStop);
         spi.LCDStoreCustomBitmap(Symbols.Pause.getValue(), bitmapPause);
     }
}
