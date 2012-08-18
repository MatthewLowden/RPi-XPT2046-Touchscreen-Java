//   Copyright 2012 Matthew Lowden
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package MatthewLowden;

import be.doubleyouit.raspberry.gpio.Boardpin;
import be.doubleyouit.raspberry.gpio.Direction;
import be.doubleyouit.raspberry.gpio.GpioGateway;
import be.doubleyouit.raspberry.gpio.impl.GpioGatewayImpl;

public class SPIManager {

	private GpioGateway gpio;

	private static final Boardpin SPI_MOSI = Boardpin.PIN19_GPIO10;
	private static final Boardpin SPI_MISO = Boardpin.PIN21_GPIO9;
	private static final Boardpin SPI_SCLK = Boardpin.PIN23_GPIO11;
	private static final Boardpin SPI_CE0 = Boardpin.PIN24_GPIO8;

	SPIManager() {
		gpio = null;
		// Register Shutdown hook.
		Runtime.getRuntime().addShutdownHook(new Thread(new Shutdown()));
	}

	public void SPISelect() {
		if (null == gpio)
			setUpGPIO();

		// Bring the select line low.
		gpio.setValue(SPI_CE0, false);
	}

	public void SPIUnSelect() {
		if (null == gpio)
			setUpGPIO();

		// Bring the select line high.
		gpio.setValue(SPI_CE0, true);
	}
	
	public void SPIPulseClock() {
		gpio.setValue(SPI_SCLK, true);
		gpio.setValue(SPI_SCLK, false);
	}

	public void SPISend(byte[] data) {
		if (null == gpio)
			setUpGPIO();

		// Send MSB->LSB order.
		
		boolean currentMOSIstate = false;

		for (int i = 0; i < data.length; i++) {
			byte byteToSend = data[i];
			for (int j = 0; j < 8; j++) {

				boolean desiredState = false;
				if ((byteToSend & 0x80) > 0)
					desiredState = true;
				
				if (desiredState == true && currentMOSIstate == false) {
					gpio.setValue(SPI_MOSI, true);
					currentMOSIstate = true;
				} else if (desiredState == false && currentMOSIstate == true) {
					gpio.setValue(SPI_MOSI, false);
					currentMOSIstate = false;
				}
				
				// Pulse the clock.
				SPIPulseClock();

				// Shift to the next bit.
				byteToSend <<= 1;
			}
		}
		if (currentMOSIstate == true)
		{
			gpio.setValue(SPI_MOSI, false);
		}
	}

	public byte[] SPIReceive(int numBits) {
		if (null == gpio)
			setUpGPIO();
		
		int numBytes = (numBits + 7) / 8;

		byte[] buffer = new byte[numBytes];

		// Array is filled in received byte order.
		// Any padding bits are the least significant bits, of the last byte.

		int currentBit = 0;
		for (int i = 0; i < numBytes; i++) {
			byte receiveByte = 0x00;
			for (int j = 0; j < 8; j++) {
				// Shift to the next bit.
				receiveByte <<= 1;

				// Skip padding bits
				currentBit++;				
				if (currentBit > numBits)
					continue;

				// Set the clock high.
				gpio.setValue(SPI_SCLK, true);

				// Read the value.
				boolean bit = gpio.getValue(SPI_MISO);

				// Set the clock low.
				gpio.setValue(SPI_SCLK, false);

				// Set the received bit.
				if (bit) {
					receiveByte |= 1;
				}
				
			}
			buffer[i] = receiveByte;
		}

		return buffer;
	}

	private void setUpGPIO() {

		// Set up GPIO
		gpio = new GpioGatewayImpl();

		gpio.export(SPI_MOSI);
		gpio.setDirection(SPI_MOSI, Direction.OUT);

		gpio.export(SPI_MISO);
		gpio.setDirection(SPI_MISO, Direction.IN);

		gpio.export(SPI_SCLK);
		gpio.setDirection(SPI_SCLK, Direction.OUT);
		gpio.setValue(SPI_SCLK, false);

		gpio.export(SPI_CE0);
		gpio.setDirection(SPI_CE0, Direction.OUT);
		gpio.setValue(SPI_CE0, true);

	}

	public class Shutdown implements Runnable {

		// This Runnable is called on shutdown to ensure the GPIO pin is
		// released.

		@Override
		public void run() {
			shutDownGPIO();

			// Output a CR so that the command prompt is in the regular place on
			// exit.
			System.out.println();
		}
	}

	private void shutDownGPIO() {
		if (null != gpio) {
			gpio.unexport(SPI_CE0);
			gpio.unexport(SPI_MISO);
			gpio.unexport(SPI_MOSI);
			gpio.unexport(SPI_SCLK);
		}
	}

}
