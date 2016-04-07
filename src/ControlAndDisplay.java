
public class ControlAndDisplay {

	public static void main(String[] args) throws InterruptedException {
		System.out.println("Starting Control & Display Test app...");
		
		SPI spi=new SPI();
		
		System.out.println("Opened device...");
		
		spi.LCDClear();
		spi.LCDDisplayOn(true);
		System.out.println("Display on...");
		spi.LCDBacklight(true);
		spi.LCDWrite("Hello");
		System.out.println("Wrote text...");
		
		while(true) {
			byte switchValues=spi.ReadSwitches();
			System.out.println("Switches value: "+switchValues);
			Thread.sleep(100);
		}
	

	}

}
