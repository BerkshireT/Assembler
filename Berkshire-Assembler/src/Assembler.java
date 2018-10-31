// Tyler Berkshire
// CPS 250 Assembler

import java.util.Scanner;
import java.util.HashMap;
import java.io.*;

public class Assembler {
	public static void main(String[] args) {
		// Map for the opcodes
		HashMap<String, String> OpMap = new HashMap<>();
		OpMap.put("add", 	"000000");
		OpMap.put("lw", 	"100011");
		OpMap.put("sw", 	"101011");
		OpMap.put("slt", 	"101010");
		OpMap.put("bne", 	"000101");
		OpMap.put("j", 		"000010");
		OpMap.put("lui", 	"001111");
		OpMap.put("ori", 	"001101");
		
		// Map for registers
		HashMap<String, Integer> RegMap = new HashMap<>();
		RegMap.put("$zero", 0);
		RegMap.put("$t0", 	8);
		RegMap.put("$t1", 	9);
		RegMap.put("$t2", 	10);
		RegMap.put("$s0", 	16);
		RegMap.put("$s1", 	17);
		RegMap.put("$s2", 	18);
		RegMap.put("$sp", 	29);
		
		// Map for labels
		HashMap<String, String> LabMap = new HashMap<>();
		
		// Get file name from user
		Scanner ConScan = new Scanner(System.in);
		System.out.print("Please enter the name of the file to be converted: ");
		String fileName = ConScan.nextLine();
		ConScan.close();
		File f = new File(fileName);
		
		// Scanner for the Label Map
		Scanner LScan = null;
		try {
			LScan = new Scanner(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// Add all labels to the Label Map
		int currentLine = 1;		
		while (LScan.hasNext()) {
			String current = LScan.next();			
			if (current.contains(":")) {
				// Labels store the address of the line they're on
				LabMap.put(current.replaceAll(":", ""), decToBin(Integer.toString(currentLine), "label"));
				currentLine--;
			}
			currentLine++;
		}
		LScan.close();
		
		// Read the input file for instructions
		Scanner FScan = null;
		try {
			FScan = new Scanner(f);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// Array to hold the instructions
		String[] objectCode = new String[51];
		currentLine = 1;
		
		// Add OpCodes to the array
		while (FScan.hasNext()) {
			String current = FScan.next();
			// Label
			if (current.contains(":")) {
				// Don't increment the line count
				currentLine--;
			}
			// Add
			else if (current.equals("add")) {
				String dest = decToBin(RegMap.get(FScan.next().replaceAll(",", "")).toString(), "reg");			
				String source1 = decToBin(RegMap.get(FScan.next().replaceAll(",", "")).toString(), "reg");
				String source2 = decToBin(RegMap.get(FScan.next()).toString(), "reg");
				objectCode[currentLine] = OpMap.get("add") + dest + source1 + source2 + "00000" + "100000"; 
			}
			// LI
			else if (current.equals("li")) {
				String dest = decToBin(RegMap.get(FScan.next().replaceAll(",", "")).toString(), "reg");
				String imm = decToBin(FScan.next(), "imm");
				objectCode[currentLine] = OpMap.get("lui") + dest + dest + imm;
				currentLine++;
				objectCode[currentLine] = OpMap.get("ori") + dest + dest + imm;
			}
			// LW
			else if (current.equals("lw")) {
				String dest = decToBin(RegMap.get(FScan.next().replaceAll(",", "")).toString(), "reg");
				String offset = decToBin(FScan.next(), "imm");
				String source = decToBin(RegMap.get(FScan.next().replaceAll("\\(", "").replaceAll("\\)", "")).toString(), "reg");
				objectCode[currentLine] = OpMap.get("lw") + dest + source + offset;
			}
			// SW
			else if (current.equals("sw")) {
				String source = decToBin(RegMap.get(FScan.next().replaceAll(",", "")).toString(), "reg");
				String offset = decToBin(FScan.next(), "imm");
				String dest = decToBin(RegMap.get(FScan.next().replaceAll("\\(", "").replaceAll("\\)", "")).toString(), "reg");
				objectCode[currentLine] = OpMap.get("sw") + source + offset + dest;
			}
			// Move
			else if (current.equals("move")) {
				String dest = decToBin(RegMap.get(FScan.next().replaceAll(",", "")).toString(), "reg");
				String source = decToBin(RegMap.get(FScan.next().replaceAll(",", "")).toString(), "reg");
				String zero = decToBin("0", "reg");
				objectCode[currentLine] = OpMap.get("add") + dest + source + zero + "00000" + "100000";
			}
			// LA
			else if (current.equals("la")) {
				String dest = decToBin(RegMap.get(FScan.next().replaceAll(",", "")).toString(), "reg");
				String imm = decToBin(LabMap.get(FScan.next()), "imm");
				objectCode[currentLine] = OpMap.get("lui") + dest + dest + imm;
				currentLine++;
				objectCode[currentLine] = OpMap.get("ori") + dest + dest + imm;
			}
			// SLT
			else if (current.equals("slt")) {
				String dest = decToBin(RegMap.get(FScan.next().replaceAll(",", "")).toString(), "reg");
				String source1 = decToBin(RegMap.get(FScan.next().replaceAll(",", "")).toString(), "reg");
				String source2 = decToBin(RegMap.get(FScan.next()).toString(), "reg");
				objectCode[currentLine] = OpMap.get("slt") + dest + source1 + source2 + "00000" + "101010";
			}
			// BNE
			else if (current.equals("bne")) {
				String source1 = decToBin(RegMap.get(FScan.next().replaceAll(",", "")).toString(), "reg");
				String source2 = decToBin(RegMap.get(FScan.next().replaceAll(",", "")).toString(), "reg");
				String label = decToBin(LabMap.get(FScan.next()), "imm");
				objectCode[currentLine] = OpMap.get("bne") + source1 + source2 + label;
			}
			// J
			else if (current.equals("j")) {
				String label = LabMap.get(FScan.next());
				objectCode[currentLine] = OpMap.get("j") + label;
			}
			// .end
			else if (current.equals(".end")){
				break;
			}
			currentLine++;
		}
		FScan.close();
		
		// Convert binary codes to hex		
		String[] hexCode = new String[currentLine];		
		for (int i = 1; i < currentLine; i++) {
			hexCode[i] = binToHex(objectCode[i]);
		}
		for (int i = 1; i < currentLine; i++) {
				System.out.println("Line " + i + " is " + hexCode[i]);
			}
		
		// Write to output file
		PrintWriter writer;
		try {
			writer = new PrintWriter("output.txt", "UTF-8");
			for (int i = 1; i < currentLine; i++) {
				writer.println(hexCode[i]);
			}	 
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		System.out.println("Conversion complete!");
	}
	
	// Convert a decimal string to a binary string
	public static String decToBin(String dec, String type) {
		int decNum = Integer.parseInt(dec);
		String bin = Integer.toBinaryString(decNum);
		if (type == "reg") {
			// The number is a register address
			bin = signExt(bin, 5 - bin.length());
			if (bin.length() > 5) {
				bin = bin.substring(bin.length() - 5);
			}
		} else if (type == "imm") {
			// The number is a constant
			bin = signExt(bin, 16 - bin.length());
			if (bin.length() > 16) {
				bin = bin.substring(bin.length() - 16);
			}
		} else {
			// The number is a label address
			bin = signExt(bin, 26 - bin.length());
			if (bin.length() > 26) {
				bin = bin.substring(bin.length() - 26);
			}
		}		
		return bin;
	}
	
	// Convert binary to hex
	public static String binToHex(String bin) {
		Long decNum = Long.parseLong(bin, 2);
		String hexStr = Long.toString(decNum, 16);
		return hexStr;
	}
	
	// Sign extend binary bits
	public static String signExt(String bin, int val) {
		String ext = "";
		for (int i = 1; i <= val; i++) {
			ext = "0" + ext;
		}
		bin = ext + bin;
		return bin;
	}
}