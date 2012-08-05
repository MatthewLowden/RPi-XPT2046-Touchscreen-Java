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

public class TouchMyPi {

	// Java proof of concept for interfacing an XPT2046 Touch Screen Controller
	// to a Raspberry Pi using SPI (via bit-banged GPIO).

	// This Java implementation is of little practical use as the SPI clock
	// frequency achieved with this method is 20-40 Hz, and so it takes
	// approximately 10s to execute each loop (reading all values).

	// The XPT2046 touch screen controller should be able to work with an SPI
	// clock up to 2MHz.

	// This sample uses the SPI pins on the Raspberry Pi Expansion header.
	// (With the intention that no wiring changes should be required to use SPI
	// drivers, rather than bit-banged GPIO).

	// See /docs/TouchScreen Connection Diagram.png for wiring used.

	// More information on Raspberry Pi GPIO can be found here:
	//    http://elinux.org/RPi_Low-level_peripherals

	public TouchMyPi() {
	}

	private void monitorTouchPanel() throws InterruptedException {

		XPT2046 xpt2046 = new XPT2046();

		// Simply loop forever, checking the state.

		while (true) {
			long time = System.currentTimeMillis();
			int x = xpt2046.readX();
			System.out.println("X: " + Integer.toString(x));
			int y = xpt2046.readY();
			System.out.println("Y: " + Integer.toString(y));
			int z1 = xpt2046.readZ1();
			System.out.println("Z1: " + Integer.toString(z1));
			int z2 = xpt2046.readZ2();
			System.out.println("Z2: " + Integer.toString(z2));
			double pressure = xpt2046.readTouchPressure();
			System.out.println("Pressure:" + Double.toString(pressure));
			int t0 = xpt2046.readTemperature0();
			System.out.println("Temp0: " + Integer.toString(t0));
			int t1 = xpt2046.readTemperature1();
			System.out.println("Temp1: " + Integer.toString(t1));
			int vbatt = xpt2046.readBatteryVoltage();
			System.out.println("VBatt: " + Integer.toString(vbatt));
			int aux = xpt2046.readAuxiliary();
			System.out.println("Aux: " + Integer.toString(aux));
			System.out.println("Total Sample Time (ms): "
					+ Long.toString(System.currentTimeMillis() - time));
		}
	}

	public static void main(String[] args) {

		TouchMyPi touchMyPi = new TouchMyPi();
		try {
			touchMyPi.monitorTouchPanel();
		} catch (InterruptedException e) {
		}
	}
}
