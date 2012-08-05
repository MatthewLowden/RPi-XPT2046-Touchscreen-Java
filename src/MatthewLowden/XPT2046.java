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

public class XPT2046 {

	private SPIManager _SPIManager;
	private ConversionSelect _ConversionSelect;

	XPT2046() {
		_SPIManager = new SPIManager();
		_ConversionSelect = ConversionSelect._12_BIT;
	}

	public int readX() {
		return readValue(ChannelSelect.X_POSITION);
	}

	public int readY() {
		return readValue(ChannelSelect.Y_POSITION);
	}

	public int readZ1() {
		return readValue(ChannelSelect.Z1_POSITION);
	}

	public int readZ2() {
		return readValue(ChannelSelect.Z2_POSITION);
	}

	public int readBatteryVoltage() {
		return readValue(ChannelSelect.BATTERY_VOLTAGE);
	}

	public int readTemperature0() {
		return readValue(ChannelSelect.TEMPERATURE_0);
	}

	public int readTemperature1() {
		return readValue(ChannelSelect.TEMPERATURE_1);
	}

	public int readAuxiliary() {
		return readValue(ChannelSelect.AUXILIARY);
	}

	public double readTouchPressure() {

		// Formula (option 1) according to the datasheet (12bit conversion)
		// RTouch = RX-Plate.(XPosition/4096).((Z1/Z2)-1)
		// Not sure of the correct value of RX-Plate.
		// Assuming the ratio is sufficient.
		// Empirically this function seems to yield a values in the range of 0.4
		// for a firm touch, and 1.75 for a light touch.

		int x = readX();
		int z1 = readZ1();
		int z2 = readZ2();
		int xDivisor = 4096;
		if (_ConversionSelect == ConversionSelect._8_BIT) {
			xDivisor = 256;
		}

		double result = ((double) x / (double) xDivisor)
				* (((double) z2 / (double) z1) - 1);
		return result;
	}

	public void setMode(ConversionSelect conversionSelect) {
		_ConversionSelect = conversionSelect;
	}

	private int readValue(ChannelSelect channelSelect) {
		byte[] request = new byte[1];
		request[0] = makeControlByte(channelSelect);

		_SPIManager.SPISelect();
		_SPIManager.SPISend(request);
		byte[] responseData = _SPIManager.SPIReceive(2);
		_SPIManager.SPIUnSelect();

		int retValue = 0;

		if (_ConversionSelect == ConversionSelect._12_BIT) {
			// 12 Bit conversion (bit pattern: 0XXXXXXX XXXXX000)
			retValue = responseData[0] << 5;
			int retValueLowBits = responseData[1];
			if (retValueLowBits < 0)
				retValueLowBits += 256;
			retValueLowBits >>= 3;
			retValue |= retValueLowBits;
		} else {
			// 8 Bit Conversion (bit pattern: 0XXXXXXX X0000000)
			retValue = responseData[0] << 1;
			if (0 != responseData[1]) {
				retValue += 1;
			}
		}

		return retValue;
	}

	private byte makeControlByte(ChannelSelect channelSelect) {
		byte controlByte = (byte) 0x80; // Start bit.
		controlByte |= channelSelect.toByte();
		controlByte |= _ConversionSelect.toByte();

		// @@TODO Other elements in control byte.

		return controlByte;
	}

	private enum ChannelSelect {
		X_POSITION(1, 0, 1), Y_POSITION(0, 0, 1), Z1_POSITION(0, 1, 1), Z2_POSITION(
				1, 0, 0), TEMPERATURE_0(0, 0, 0), TEMPERATURE_1(1, 1, 1), BATTERY_VOLTAGE(
				0, 1, 0), AUXILIARY(1, 1, 0);

		private byte _channelSelect;

		ChannelSelect(int A2, int A1, int A0) {
			_channelSelect = 0x00;
			if (A2 == 1) {
				_channelSelect |= 0x40;
			}
			if (A1 == 1) {
				_channelSelect |= 0x20;
			}
			if (A0 == 1) {
				_channelSelect |= 0x10;
			}
		}

		public byte toByte() {
			return _channelSelect;
		}
	};

	public enum ConversionSelect {
		_8_BIT(1), _12_BIT(0);

		private byte _conversionSelect;

		ConversionSelect(int mode) {
			_conversionSelect = 0x00;
			if (mode == 1) {
				_conversionSelect = 0x08;
			}
		}

		public byte toByte() {
			return _conversionSelect;
		}
	}

}
