package org.teiohanson.synacor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VirtualMachine {
	
	static final Logger logger = LogManager.getLogger(VirtualMachine.class);
	
	public final static String CHALLENGE_FILE_PATH = "/opt/software/synacor/challenge.bin";
	public final static String CHALLENGE_INPUT_FILE_PATH = "/opt/software/synacor/challenge.dat";

	private List<Integer> vmMemory = new ArrayList<Integer>();
	private Stack<Integer> vmStack = new Stack<Integer>();
	private int[] vmRegisters = new int[8];
	
	private LinkedList<String> challengeInput = new LinkedList<String> ();
	
	private static final Map<Integer, String[]> opcodes = new HashMap<Integer, String[]>();
	static {
		opcodes.put(0, new String[]{"halt", "0"});
		opcodes.put(1, new String[]{"set", "2"});
		opcodes.put(2, new String[]{"push", "1"});
		opcodes.put(3, new String[]{"pop", "1"});
		opcodes.put(4, new String[]{"eq", "3"});
		opcodes.put(5, new String[]{"gt", "3"});
		opcodes.put(6, new String[]{"jmp", "1"});
		opcodes.put(7, new String[]{"jt", "2"});
		opcodes.put(8, new String[]{"jf", "2"});
		opcodes.put(9, new String[]{"add", "3"});
		opcodes.put(10, new String[]{"mult", "3"});
		opcodes.put(11, new String[]{"mod", "3"});
		opcodes.put(12, new String[]{"and", "3"});
		opcodes.put(13, new String[]{"or", "3"});
		opcodes.put(14, new String[]{"not", "2"});
		opcodes.put(15, new String[]{"rmem", "2"});
		opcodes.put(16, new String[]{"wmem", "2"});
		opcodes.put(17, new String[]{"call", "1"});
		opcodes.put(18, new String[]{"ret", "0"});
		opcodes.put(19, new String[]{"out", "1"});
		opcodes.put(20, new String[]{"in", "1"});
		opcodes.put(21, new String[]{"noop", "0"});
	} 
	
	public VirtualMachine() throws IOException {
		this.initializeMemory();
		challengeInput.addAll(Files.readAllLines(Paths.get(VirtualMachine.CHALLENGE_INPUT_FILE_PATH), Charset.defaultCharset()));
	}
	
	private void initializeMemory() {
		File challengeFile = new File(VirtualMachine.CHALLENGE_FILE_PATH);
		FileInputStream challengeInputStream = null;
		try {
			if(challengeFile.length() % 2 != 0) throw new IOException("File format is incorrect.");
			challengeInputStream = new FileInputStream(challengeFile);
			byte[] buffer = new byte[2];
			while(challengeInputStream.read(buffer) != -1) {	
				vmMemory.add((buffer[1] & 0xFF) << 8 | (buffer[0] & 0xFF));	// Convert little endian unsigned short to big endian signed integer.
			}
		} catch(IOException e) {
			logger.error("Error: " + e.getMessage(), e);
		} finally {
			if(challengeInputStream != null)
				try {
					challengeInputStream.close();
				} catch (IOException e) {
					logger.error("Error: " + e.getMessage(), e);
				}
		}
	}

	public void runVm() throws IOException {
		
		int executionPosition = 0;
		int currentOpcode;
		int[] parameters = new int[3];
		boolean halt = false;
	
		StringBuilder debugOutput = new StringBuilder();	
		String zorkCommand = null;
		
		while (!halt) {
			currentOpcode = vmMemory.get(executionPosition++);
		
			if(logger.isDebugEnabled()) {
				// output the register state
				for(int i = 0; i < vmRegisters.length; i++) {
					logger.debug("Register " + i + "(" + (i + 32768) +"): " + vmRegisters[i]);
				}
				
				// output the stack
				StringBuilder debugStack = new StringBuilder();
				for(int i = vmStack.size() - 1; i >= 0; i--) {
					if(debugStack.length() > 0) {
						debugStack.append(", " + vmStack.get(i));
					} else {
						debugStack.append(vmStack.get(i));
					}
				}
				logger.debug("Stack: " + debugStack.toString());
				
				// output the current call
				debugOutput.setLength(0);
				debugOutput.append(opcodes.get(currentOpcode)[0]);
			}
			
			for(int i = 0; i < Integer.valueOf(opcodes.get(currentOpcode)[1]); i++) {
				parameters[i] = vmMemory.get(executionPosition++);
				if(logger.isDebugEnabled()) debugOutput.append(" " + parameters[i]);
			}
			logger.debug(debugOutput.toString());

			switch(currentOpcode) {
				case 0:	// halt
					halt = true;
					break;
				case 1:	// set
					this.setRegister(parameters[0], parameters[1]);
					break;
				case 2:	// push
					vmStack.push(this.getRegister(parameters[0]));
					break;
				case 3:	//pop
					if(vmStack.empty()) throw new IOException("Attempting to pop from an empty stack: pop");
					this.setRegister(parameters[0], vmStack.pop());
					break;
				case 4:	// eq
					this.setRegister(parameters[0], (this.getRegister(parameters[1]) == this.getRegister(parameters[2])) ? 1 : 0);
					break;
				case 5:	// gt
					this.setRegister(parameters[0], (this.getRegister(parameters[1]) > this.getRegister(parameters[2])) ? 1 : 0);
					break;
				case 6:	// jmp
					executionPosition = this.getRegister(parameters[0]);
					break;
				case 7:	// jt
					if(this.getRegister(parameters[0]) != 0) executionPosition = this.getRegister(parameters[1]);
					break;
				case 8:	// jf
					if(this.getRegister(parameters[0]) == 0)  executionPosition = this.getRegister(parameters[1]);
					break;
				case 9:	// add
					this.setRegister(parameters[0], (this.getRegister(parameters[1]) + this.getRegister(parameters[2])) % 32768);
					break;
				case 10: // mult
					this.setRegister(parameters[0], (this.getRegister(parameters[1]) * this.getRegister(parameters[2])) % 32768);
					break;
				case 11: // mod
					this.setRegister(parameters[0], this.getRegister(parameters[1]) % this.getRegister(parameters[2]));
					break;
				case 12: // and
					this.setRegister(parameters[0], this.getRegister(parameters[1]) & this.getRegister(parameters[2]));
					break;
				case 13: // or
					this.setRegister(parameters[0], this.getRegister(parameters[1]) | this.getRegister(parameters[2]));
					break;
				case 14: // not
					this.setRegister(parameters[0], ~this.getRegister(parameters[1]) & 0x7FFF);
					break;
				case 15: // rmem
					this.setRegister(parameters[0], vmMemory.get(this.getRegister(parameters[1])));
					break;
				case 16: // wmem
					vmMemory.set(this.getRegister(parameters[0]), this.getRegister(parameters[1]));
					break;
				case 17: // call
					vmStack.push(executionPosition);
					executionPosition = this.getRegister(parameters[0]);
					break;
				case 18: // ret
					if(vmStack.empty()) throw new IOException("Attempting to pop from an empty stack: ret");
					executionPosition = this.getRegister(vmStack.pop());
					break;
				case 19: // out
					System.out.print((char) this.getRegister(parameters[0]));
					break;
				case 20: // in
					if(zorkCommand == null) {
						if(challengeInput.isEmpty() || (challengeInput.peek().equalsIgnoreCase("modify teleporter") && challengeInput.size() < 2)) {
							halt = true;
							System.out.println("exit");
							break;
						} else {
							zorkCommand = challengeInput.remove();
							System.out.println(zorkCommand);
						
							if(zorkCommand.equalsIgnoreCase("modify teleporter")) {
								this.setRegister(32775, 25734);
								zorkCommand = challengeInput.remove();
								System.out.println(zorkCommand);
							}
						}
					}
					
					if(zorkCommand.length() > 0) {
						this.setRegister(parameters[0], (int) zorkCommand.charAt(0));
						zorkCommand = zorkCommand.substring(1);
					} else {
						this.setRegister(parameters[0], (int) '\n');
						zorkCommand = null;
					}
					break;
				case 21: // noop
					break;
			}
		}
	}
	
	private int getRegister(int value) {
		if(value >= 32768 && value <= 32775) return vmRegisters[value % 32768];
		return value;
	}
	
	private void setRegister(int address, int value) {
		if(address >= 32768 && address <= 32775) vmRegisters[address % 32768] = this.getRegister(value);
	}
	
	public void bypassTeleporterCheck() {
		vmMemory.set(6027, 1);
		vmMemory.set(6028, 32768);
		vmMemory.set(6029, 6);
		vmMemory.set(6030, 18);
	}
	
	public void dumpMemory() {
		int executionPosition = 0;
		int currentPosition = 0;
		int currentOpcode;
		StringBuilder output = new StringBuilder();
		
		while(executionPosition < vmMemory.size()) {
			currentPosition = executionPosition;
			currentOpcode = vmMemory.get(executionPosition++);
			output.setLength(0);
			if(opcodes.get(currentOpcode) != null) {
				output.append(opcodes.get(currentOpcode)[0]);
				for(int i = 0; i < Integer.valueOf(opcodes.get(currentOpcode)[1]); i++) {
					output.append(" " + vmMemory.get(executionPosition++));
				}
			} else {
				output.append(currentOpcode);
			}
			logger.info(currentPosition + ": " + output.toString());
		}
	}
	
	public static void main(String[] args) {
		
		StrangeMonumentPuzzle.solve();
		
		VaultPuzzle vaultPuzzle = new VaultPuzzle();
		vaultPuzzle.solve();
		
		TeleporterPuzzle teleporterPuzzle = new TeleporterPuzzle();
		teleporterPuzzle.solve();
		
		VirtualMachine vm;
		try {
			vm = new VirtualMachine();
			vm.dumpMemory();
			vm.bypassTeleporterCheck();
			vm.runVm();
		} catch (IOException e) {
			logger.error("Error: " + e.getMessage(), e);
		}
	}
}
