package org.teiohanson.synacor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TeleporterPuzzle {
	
	static final Logger logger = LogManager.getLogger(TeleporterPuzzle.class);
	
	short[][] memoizationArray = new short[32768][32768];
	int register8;

	public void solve() {
		int initialRegister1 = 4;
		int initialRegister2 = 1;
		for(; register8 < memoizationArray.length; register8++) {
			initializeMemoizationArray();
			if(testRegisterValue(initialRegister1, initialRegister2) == 6) {
				logger.info("Teleporter Register Value Found: " + register8);
				break;
			}
		}
	}
	
	private void initializeMemoizationArray() {
		for(int i = 0; i < memoizationArray.length; i++) {
			for(int j = 0; j < memoizationArray.length; j++) {
				memoizationArray[i][j] = -1;		// Initialize our array to a value outside of a possible return value.
			}
		}
	}
	
	/**
	 *  Reversed from the memory dump.
	 *  
	 *  19:07:20.860 [main] INFO  org.teiohanson.synacor.VirtualMachine - 5483: set 32768 4						register1 = 4
	 *  19:07:20.860 [main] INFO  org.teiohanson.synacor.VirtualMachine - 5486: set 32769 1						register2 = 1
	 *  19:07:20.860 [main] INFO  org.teiohanson.synacor.VirtualMachine - 5489: call 6027
 	 *  19:07:20.860 [main] INFO  org.teiohanson.synacor.VirtualMachine - 5491: eq 32769 32768 6				register1 == 6
     *  19:07:20.860 [main] INFO  org.teiohanson.synacor.VirtualMachine - 5495: jf 32769 5579
     *  
     *  Function(6027)
     *  19:07:20.874 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6027: jt 32768 6035					if(register1 == 0) continue, else goto 6035
	 *  19:07:20.874 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6030: add 32768 32769 1				register1 = register2 + 1
	 *  19:07:20.874 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6034: ret
	 *  19:07:20.874 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6035: jt 32769 6048					if(register2 == 0) continue, else goto 6048
	 *  19:07:20.874 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6038: add 32768 32768 32767			register1 -= 1
	 *  19:07:20.874 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6042: set 32769 32775					register2 == register 8
	 *  19:07:20.874 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6045: call 6027
	 *  19:07:20.874 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6047: ret
	 *  19:07:20.874 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6048: push 32768						push register1 to the stack before calling the function recursively.
	 *  19:07:20.874 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6050: add 32769 32769 32767			register2 -= 1
	 * 	19:07:20.874 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6054: call 6027
	 *  19:07:20.874 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6056: set 32769 32768					register2 = register1 (This is the return of the recursive call at 6054)
	 *  19:07:20.874 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6059: pop 32768						pop the previous value of register1 off the stack.
	 *  19:07:20.875 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6061: add 32768 32768 32767			register1 -= 1
	 *  19:07:20.875 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6065: call 6027
	 *  19:07:20.875 [main] INFO  org.teiohanson.synacor.VirtualMachine - 6067: ret
	 * 
	 *  private static int testRegisterValue(int register1, int register2, int register8) {
	 *  	if(register1 == 0) {
	 *			return register2 + 1;
	 *		} else if(register2 == 0) {
	 *			return testRegisterValue(register1 - 1, register8, register8);
	 *		} else {
	 * 			return testRegisterValue(register1 - 1, testRegisterValue(register1, register2 - 1, register8), register8);
	 *		}
	 *  }
	**/
	
	private int testRegisterValue(int register1, int register2) {
		if(memoizationArray[register1][register2] != -1) {
			return memoizationArray[register1][register2];
		} else if(register1 == 0) {
			memoizationArray[register1][register2] = (short) ((register2 + 1) % 32768);
		} else if(register2 == 0) {
			memoizationArray[register1][register2] = (short) testRegisterValue(((register1 - 1) % 32768), register8);
		} else {
			memoizationArray[register1][register2] = (short) testRegisterValue((register1 - 1 % 32768), testRegisterValue(register1, (register2 - 1 % 32768)));
		}
		return memoizationArray[register1][register2];
	}	
}