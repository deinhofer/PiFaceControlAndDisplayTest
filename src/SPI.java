/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.IOException;
import static java.lang.Math.max;
import static java.lang.Math.min;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.spi.SpiChannel;
import com.pi4j.io.spi.SpiDevice;
import com.pi4j.io.spi.SpiFactory;
import com.pi4j.io.spi.SpiMode;
import com.pi4j.wiringpi.Spi;


/**
 *
 * @author hammer
 */
public class SPI {

    //SPI constants to connect to MCP23S17
    private static final int SPIChannel = 0;
    private static final int SPIChipselect = 1;
    private static final int SPISpeed = 10000000;
    private static final int SPIMode = 0;
    private static final int SPIBPW = 8;
    // SPI operations
    private static final byte WRITE_CMD = 0x40;
    private static final byte READ_CMD = 0x41;
    //Register addresses
    private static final byte IODIRA = 0x00; // I/O direction A
    private static final byte IODIRB = 0x01; // I/O direction B
    private static final byte IOCON = 0x0A; // I/O config
    private static final byte GPIOA = 0x12; // port A
    private static final byte GPIOB = 0x13; // port B
    private static final byte GPPUA = 0x0C; // port A pullups
    private static final byte GPPUB = 0x0D; // port B pullups
    private static final byte OUTPUT_PORT = GPIOB;
    private static final byte INPUT_PORT = GPIOA;
    private static final byte INPUT_PULLUPS = GPPUA;
    // LCD defines
    private static final byte PINEnable = 4;
    private static final byte PINBacklight = 7;
    private static final byte PINRS = 6;
    private static final byte PINRW = 5;
    
    // commands
    private static final byte LCD_CLEARDISPLAY = 0x01;
    private static final byte LCD_RETURNHOME = 0x02;
    private static final byte LCD_ENTRYMODESET = 0x04;
    private static final byte LCD_DISPLAYCONTROL = 0x08;
    private static final byte LCD_CURSORSHIFT = 0x10;
    private static final byte LCD_FUNCTIONSET = 0x20;
    private static final byte LCD_SETCGRAMADDR = 0x40;
    private static final byte LCD_SETDDRAMADDR = (byte) 0x80;
    private static final byte LCD_NEWLINE = (byte) 0xC0;
    // flags for display entry mode
    private static final byte LCD_ENTRYRIGHT = 0x00;
    private static final byte LCD_ENTRYLEFT = 0x02;
    private static final byte LCD_ENTRYSHIFTINCREMENT = 0x01;
    private static final byte LCD_ENTRYSHIFTDECREMENT = 0x00;
    // flags for display on/off control
    private static final byte LCD_DISPLAYON = 0x04;
    private static final byte LCD_DISPLAYOFF = 0x00;
    private static final byte LCD_CURSORON = 0x02;
    private static final byte LCD_CURSOROFF = 0x00;
    private static final byte LCD_BLINKON = 0x01;
    private static final byte LCD_BLINKOFF = 0x00;
    // flags for display/cursor shift
    private static final byte LCD_DISPLAYMOVE = 0x08;
    private static final byte LCD_CURSORMOVE = 0x00;
    private static final byte LCD_MOVERIGHT = 0x04;
    private static final byte LCD_MOVELEFT = 0x00;
    // flags for function set
    private static final byte LCD_8BITMODE = 0x10;
    private static final byte LCD_4BITMODE = 0x00;
    private static final byte LCD_2LINE = 0x08;
    private static final byte LCD_1LINE = 0x00;
    private static final byte LCD_5X10DOTS = 0x04;
    private static final byte LCD_5X8DOTS = 0x00;
    private static final int LCD_MAX_LINES = 2;
    private static final int LCD_WIDTH = 16;
    private static final int LCD_RAM_WIDTH = 80; // RAM is 80 wide, split over two lines
    private static final byte ROW_OFFSETS[] = {0, 0x40};
    
    private SpiDevice slave;
    private int wiringPiSpi=-1;
    private byte curFunctionSet = 0x0;
    private byte curDisplayControl = 0x0;
    private byte curAddress = 0x0;
    private byte curEntryMode = 0x0;

    /**
     * open a new SPI connection and initalize the SPI-controller
     */
    public SPI() {
 
        //SPIDeviceConfig spi0config = new  SPIDeviceConfig(SPIChannel, SPIChipselect, SPISpeed, SPIMode, SPIBPW, DeviceConfig.DEFAULT);
        try {
        	SpiDevice slave=SpiFactory.getInstance(SpiChannel.CS1, SPISpeed, SpiMode.MODE_0);
        	//int wiringPiSpi=Spi.wiringPiSPISetupMode(SPIChipselect, SPISpeed, SPIMode);
        	
        	//System.out.println("Opened SPI device: "+wiringPiSpi);
        	System.out.println("Opened SPI device: "+slave);

            //slave = (SPIDevice)DeviceManager.open(spi0config);
            
        	
            write(IOCON, 0x08); // enable hardware addressing
            write(IODIRA, 0xFF); // set port A as inputs
            write(GPPUA, 0xFF); // set port A pullups on
            write(IODIRB, 0x00); // set port B as outputs
            
            LCDinit();
            
        } catch (Exception ex) {
            Logger.getLogger(SPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void write(byte reg, int value){
        try {
            byte packet[] = new byte[3];
            packet[0] = WRITE_CMD; // address byte
            packet[1] = reg; // register byte
            packet[2] = (byte) value; // data byte
            ByteBuffer txBuf = ByteBuffer.wrap(packet);
            ByteBuffer rxBuf=ByteBuffer.wrap(new byte[]{0,0,0});

            //System.out.println("Wrote data "+txBuf.toString());
            
            rxBuf=slave.write(txBuf);        

            //slave.writeAndRead(txBuf,rxBuf);
            
            //Spi.wiringPiSPIDataRW(wiringPiSpi, packet);
        } catch (Exception ex) {
            
        }
    }
    
    private void writeBit(byte reg, byte pin, int value){
        try {
            byte port = read(reg);
            
            if(value == 1){
                port = (byte) (port | (1 << pin));
            } else if (value == 0) {
                port = (byte) (port & ~(1 << pin));
            } else {
                return;
            }
            
            byte packet[] = new byte[3];
            packet[0] = WRITE_CMD; // address byte
            packet[1] = reg; // register byte
            packet[2] = (byte) port; // data byte
            ByteBuffer txBuf = ByteBuffer.wrap(packet);
            ByteBuffer rxBuf=ByteBuffer.wrap(new byte[]{0,0,0});
            rxBuf=slave.write(txBuf);
            //slave.writeAndRead(txBuf,rxBuf);
            //Spi.wiringPiSPIDataRW(wiringPiSpi, packet);
        } catch (Exception ex) {
        }
    }
    
    private byte read(byte reg){
        try {
            byte packet[] = new byte[3];
            packet[0] = READ_CMD; // address byte
            packet[1] = reg; // register byte
            packet[2] = 0; // data byte
            ByteBuffer txBuf = ByteBuffer.wrap(packet);
            ByteBuffer rxBuf=ByteBuffer.wrap(new byte[]{0,0,0});
            //Will have to do 2 reads because of SPI synchronous bus behaviour.
            rxBuf=slave.write(txBuf);
            rxBuf=slave.write(txBuf);
            //slave.writeAndRead(txBuf,rxBuf);
            return rxBuf.get(2);
            
            /*
            Spi.wiringPiSPIDataRW(wiringPiSpi, packet);
            Spi.wiringPiSPIDataRW(wiringPiSpi, packet);
            
            return packet[2];
            */
        } catch (Exception ex) {
        }
        return 0;
    }
    /**
     * reads all switches nonblocking
     * @return the state of the button
     */
    public byte ReadSwitches() {
        return read(INPUT_PORT);
    }
    /**
     * reads one specific button nonblocking
     * @param switchNum which button should be read
     * @return the state of the button true if pressed
     */
    public boolean ReadSwitch(int switchNum) {
        return ((ReadSwitches() >> switchNum) & 0x1) == 0x1;
    }
    
    @SuppressWarnings("PointlessBitwiseExpression")
    private void LCDinit() throws InterruptedException{
    	System.out.println("in LCD init");
            Thread.sleep(15);
            write(OUTPUT_PORT, 0x3);
            LCDPulseEnable();
            Thread.sleep(5);
            write(OUTPUT_PORT, 0x3);
            LCDPulseEnable();
            Thread.sleep(1);
            write(OUTPUT_PORT, 0x3);
            LCDPulseEnable();
            write(OUTPUT_PORT, 0x2);
            LCDPulseEnable();
            
            curFunctionSet |= LCD_4BITMODE | LCD_2LINE | LCD_5X8DOTS;
            LCDSendCommand((byte) (LCD_FUNCTIONSET | curFunctionSet));
            
            curDisplayControl |= LCD_DISPLAYOFF | LCD_CURSOROFF | LCD_BLINKOFF;
            LCDSendCommand((byte) (LCD_DISPLAYCONTROL | curDisplayControl));
            
            LCDClear();
            
            curEntryMode |= LCD_ENTRYLEFT | LCD_ENTRYSHIFTDECREMENT;
            LCDSendCommand((byte) (LCD_ENTRYMODESET | curEntryMode));
            
            curDisplayControl |= LCD_DISPLAYON | LCD_CURSORON | LCD_BLINKON;
            LCDSendCommand((byte) (LCD_DISPLAYCONTROL | curDisplayControl));
            
            LCDBacklight(true);
            LCDSetCursorHome();
    }
    /**
     * writes a string to the display at the current cursor position
     * @param message which should be dislayed
     */
    public void LCDWrite(String message) {
        try {
            LCDSendCommand((byte) (LCD_SETDDRAMADDR | curAddress));
            for(char c : message.toCharArray()) {
                if(c == '\n')
                    LCDSetCursor(0, 1);
                else {
                    LCDSendData((byte)c);
                    curAddress++;
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(SPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * turns the display on or off
     * @param state 
     */
    public void LCDDisplayOn(boolean state) {
        try {
            if(state)
                curDisplayControl |= LCD_DISPLAYON;
            else
                curDisplayControl &= 0xff ^ LCD_DISPLAYON;
            LCDSendCommand((byte) (LCD_DISPLAYCONTROL | curDisplayControl));
        } catch (InterruptedException ex) {
            Logger.getLogger(SPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * turns the blincking of the cursor on or off
     * @param state 
     */
    public void LCDCursorBlinkOn(boolean state) {
        try {
            if(state)
                curDisplayControl |= LCD_BLINKON;
            else
                curDisplayControl &= 0xff ^ LCD_BLINKON;
            LCDSendCommand((byte) (LCD_DISPLAYCONTROL | curDisplayControl));
        } catch (InterruptedException ex) {
            Logger.getLogger(SPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * turns the cursor on or off
     * @param state 
     */
    public void LCDCursorOn(boolean state) {
        try {
            if(state)
                curDisplayControl |= LCD_CURSORON;
            else
                curDisplayControl &= 0xff ^ LCD_CURSORON;
            LCDSendCommand((byte) (LCD_DISPLAYCONTROL | curDisplayControl));
        } catch (InterruptedException ex) {
            Logger.getLogger(SPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * turns the backlight on or off
     * @param state 
     */
    public void LCDBacklight(boolean state){
        int bit = state ? 1 : 0;
        writeBit(OUTPUT_PORT, PINBacklight, bit);
    }
    /**
     * shift the text on the display leftwards 
     */
    public void LCDMoveLeft() {
        try {
            LCDSendCommand((byte) (LCD_CURSORSHIFT | LCD_DISPLAYMOVE | LCD_MOVELEFT));
        } catch (InterruptedException ex) {
            Logger.getLogger(SPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * shift the text on the display rightwards
     */
    public void LCDMoveRight() {
        try {
            LCDSendCommand((byte) (LCD_CURSORSHIFT | LCD_DISPLAYMOVE | LCD_MOVERIGHT));
        } catch (InterruptedException ex) {
            Logger.getLogger(SPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * writes a previously saved bitmap to the display
     * @param location position of the display memory
     */
    public void LCDWriteCustomBitmap(int location) {
        try {
            LCDSendCommand((byte) (LCD_SETDDRAMADDR | curAddress));
            LCDSendData((byte) location);
            curAddress++;
        } catch (InterruptedException ex) {
            Logger.getLogger(SPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * saves a custom bitmap on the display controller
     * @param location
     * @param bitmap 
     */
    public void LCDStoreCustomBitmap(int location, byte bitmap[]) {
        try {
            location &= 0x7; // we only have 8 locations 0-7
            LCDSendCommand((byte) (LCD_SETCGRAMADDR | (location << 3)));
            for (int i = 0; i < 8; i++)
                LCDSendData(bitmap[i]);
        } catch (InterruptedException ex) {
            Logger.getLogger(SPI.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    private void LCDPulseEnable() throws InterruptedException{
            writeBit(OUTPUT_PORT, PINEnable, 1);
            Thread.sleep(0, 1000);
            writeBit(OUTPUT_PORT, PINEnable, 0);
            Thread.sleep(0, 1000);
    }
    
    private void LCDSendCommand(byte command) throws InterruptedException{
            LCDSetRS(0);
            LCDSendByte(command);
            Thread.sleep(0, 40000);
    }
    
    private void LCDSendData(byte data) throws InterruptedException{
            LCDSetRS(1);
            LCDSendByte(data);
            Thread.sleep(0, 40000);
    }
    
    private void LCDSetRS(int state){
        writeBit(OUTPUT_PORT, PINRS , state);
    }
    
    private void LCDSendByte(byte value) throws InterruptedException{
        byte curState = read(OUTPUT_PORT);
        curState &= 0xf0;
        byte new_byte = (byte) (curState | ((value >> 4) & 0xf));
        write(OUTPUT_PORT, new_byte);
        LCDPulseEnable();
        new_byte = (byte) (curState | (value & 0xf));
        write(OUTPUT_PORT, new_byte);
        LCDPulseEnable();
    }
    /**
     * clears all rows on the display
     */
    public void LCDClear() {
        try {
            LCDSendCommand(LCD_CLEARDISPLAY);
            Thread.sleep(2, 600000);
            curAddress = 0;
        } catch (InterruptedException ex) {
            Logger.getLogger(SPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * clears only a specific row on the display
     * @param row 
     */
    public void LCDClear(int row) {
        if(row >= 0 && row <= 1) {
            LCDSetCursor(0, row);
            LCDWrite("                ");
            LCDSetCursor(0, row);
        }
    }
    /**
     * set the cursor to the homeposition
     */
    public void LCDSetCursorHome() {
        try {
            LCDSendCommand(LCD_RETURNHOME);
            Thread.sleep(2, 600000);
            curAddress = 0;
        } catch (InterruptedException ex) {
            Logger.getLogger(SPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * set the cursor on a specific position
     * @param col
     * @param row 
     */
    public void LCDSetCursor(int col, int row) {
        col = (byte) max(0, min(col, LCD_RAM_WIDTH/2)-1);
        row = (byte) max(0, min(row, LCD_MAX_LINES-1));
        LCDSetCursorAddress(ColRow2Address(col, row));
    }
    /**
     * set the cursor on a specific position
     * @param address 
     */
    public void LCDSetCursorAddress(byte address) {
        try {
            curAddress = (byte) (address % LCD_RAM_WIDTH);
            LCDSendCommand((byte) (LCD_SETDDRAMADDR | curAddress));
        } catch (InterruptedException ex) {
            Logger.getLogger(SPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private byte ColRow2Address(int col, int row){
        return (byte) (col + ROW_OFFSETS[row]);
    }
    /**
     * @return the current cursor address 
     */
    public byte getCurAddress() {
        return curAddress;
    }
}
