package dev.eigenworks.computer.assembly;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import dev.eigenworks.computer.cpu.CpuInstruction;

/** Two-pass, dependency-free assembler for the frozen Eigen-8 instruction set. */
public final class Eigen8Assembler {
	private static final Map<String, CpuInstruction> SIMPLE_MNEMONICS = simpleMnemonics();

	public AssemblyResult assemble(String source) {
		if (source == null) {
			throw new NullPointerException("source");
		}
		String[] sourceLines = source.split("\\R", -1);
		List<AssemblyDiagnostic> diagnostics = new ArrayList<>();
		Map<String, Integer> symbols = new LinkedHashMap<>();
		List<ParsedLine> instructions = new ArrayList<>();
		int address = 0;

		for (int index = 0; index < sourceLines.length; index++) {
			int lineNumber = index + 1;
			String code = stripComment(sourceLines[index]).trim();
			if (code.isEmpty()) {
				continue;
			}
			try {
				if (isConstant(code)) {
					defineConstant(code, lineNumber, sourceLines[index], symbols);
					continue;
				}
				int colon = code.indexOf(':');
				if (colon >= 0) {
					String label = normalizedSymbol(code.substring(0, colon).trim());
					define(symbols, label, address);
					code = code.substring(colon + 1).trim();
					if (code.isEmpty()) {
						continue;
					}
				}
				ParsedLine parsed = parseInstruction(code, lineNumber, sourceLines[index], address);
				instructions.add(parsed);
				address += parsed.instruction().size();
				if (address > 65_536) {
					throw new AssemblyException("Program exceeds the 65536-byte address space");
				}
			} catch (AssemblyException exception) {
				diagnostics.add(new AssemblyDiagnostic(lineNumber, 1, exception.getMessage(), sourceLines[index]));
			}
		}

		if (!diagnostics.isEmpty()) {
			return new AssemblyResult(null, diagnostics);
		}

		ByteArrayOutputStream output = new ByteArrayOutputStream(address);
		Map<Integer, Integer> addressToLine = new HashMap<>();
		for (ParsedLine parsed : instructions) {
			try {
				addressToLine.put(parsed.address(), parsed.line());
				emit(parsed, symbols, output);
			} catch (AssemblyException exception) {
				diagnostics.add(new AssemblyDiagnostic(parsed.line(), 1, exception.getMessage(), parsed.sourceLine()));
			}
		}
		if (!diagnostics.isEmpty()) {
			return new AssemblyResult(null, diagnostics);
		}
		return new AssemblyResult(new AssembledProgram(output.toByteArray(), addressToLine, source), List.of());
	}

	private static ParsedLine parseInstruction(String code, int line, String sourceLine, int address) {
		String[] head = code.trim().split("\\s+", 2);
		String mnemonic = head[0].toUpperCase(Locale.ROOT);
		List<String> operands = splitOperands(head.length == 2 ? head[1] : "");
		CpuInstruction instruction;
		if (mnemonic.equals("LOAD")) {
			requireCount(mnemonic, operands, 2);
			instruction = isMemoryOperand(operands.get(1)) ? CpuInstruction.LOAD_MEMORY : CpuInstruction.LOAD_IMMEDIATE;
		} else {
			instruction = SIMPLE_MNEMONICS.get(mnemonic);
			if (instruction == null) {
				throw new AssemblyException("Unknown instruction '" + mnemonic + "'");
			}
			requireCount(mnemonic, operands, operandCount(instruction));
		}
		return new ParsedLine(line, sourceLine, address, instruction, operands);
	}

	private static void emit(ParsedLine parsed, Map<String, Integer> symbols, ByteArrayOutputStream output) {
		CpuInstruction instruction = parsed.instruction();
		List<String> operands = parsed.operands();
		output.write(instruction.opcode());
		switch (instruction.format()) {
			case NONE -> { }
			case REGISTER -> output.write(register(operands.get(0)));
			case TWO_REGISTERS -> {
				output.write(register(operands.get(0)));
				output.write(register(operands.get(1)));
			}
			case THREE_REGISTERS -> {
				output.write(register(operands.get(0)));
				output.write(register(operands.get(1)));
				output.write(register(operands.get(2)));
			}
			case REGISTER_IMMEDIATE8 -> {
				output.write(register(operands.get(0)));
				output.write(resolve(operands.get(1), symbols, 0xFF, "8-bit immediate"));
			}
			case REGISTER_ADDRESS16 -> {
				output.write(register(operands.get(0)));
				emitWord(output, resolve(memoryOperand(operands.get(1)), symbols, 0xFFFF, "address"));
			}
			case ADDRESS16_REGISTER -> {
				emitWord(output, resolve(memoryOperand(operands.get(0)), symbols, 0xFFFF, "address"));
				output.write(register(operands.get(1)));
			}
			case ADDRESS16 -> emitWord(output, resolve(operands.get(0), symbols, 0xFFFF, "address"));
			case REGISTER_PORT8 -> {
				output.write(register(operands.get(0)));
				output.write(resolve(operands.get(1), symbols, 0xFF, "I/O port"));
			}
			case PORT8_REGISTER -> {
				output.write(resolve(operands.get(0), symbols, 0xFF, "I/O port"));
				output.write(register(operands.get(1)));
			}
			case VECTOR8 -> output.write(resolve(operands.get(0), symbols, 0xFF, "interrupt vector"));
		}
	}

	private static boolean isConstant(String code) {
		String upper = code.toUpperCase(Locale.ROOT);
		return upper.startsWith(".EQU ") || upper.startsWith("CONST ");
	}

	private static void defineConstant(String code, int line, String sourceLine, Map<String, Integer> symbols) {
		String body = code.substring(code.indexOf(' ') + 1).trim();
		String[] parts = body.split("\\s*(?:,|=)\\s*", 2);
		if (parts.length != 2) {
			throw new AssemblyException("Constant syntax is .equ NAME, value");
		}
		String name = normalizedSymbol(parts[0]);
		int value = parseLiteral(parts[1]);
		if (value < 0 || value > 0xFFFF) {
			throw new AssemblyException("Constant is outside 0..65535");
		}
		define(symbols, name, value);
	}

	private static void define(Map<String, Integer> symbols, String name, int value) {
		if (symbols.putIfAbsent(name, value) != null) {
			throw new AssemblyException("Duplicate symbol '" + name + "'");
		}
	}

	private static int resolve(String token, Map<String, Integer> symbols, int maximum, String kind) {
		Integer value = symbols.get(token.trim().toUpperCase(Locale.ROOT));
		if (value == null) {
			try {
				value = parseLiteral(token);
			} catch (AssemblyException exception) {
				throw new AssemblyException("Unknown symbol '" + token.trim() + "'");
			}
		}
		if (value < 0 || value > maximum) {
			throw new AssemblyException(kind + " is outside 0.." + maximum + ": " + token.trim());
		}
		return value;
	}

	private static int parseLiteral(String token) {
		String value = token.trim().replace("_", "");
		try {
			if (value.startsWith("0x") || value.startsWith("0X")) {
				return Integer.parseInt(value.substring(2), 16);
			}
			if (value.startsWith("0b") || value.startsWith("0B")) {
				return Integer.parseInt(value.substring(2), 2);
			}
			return Integer.parseInt(value);
		} catch (NumberFormatException exception) {
			throw new AssemblyException("Invalid integer '" + token.trim() + "'");
		}
	}

	private static int register(String token) {
		String value = token.trim().toUpperCase(Locale.ROOT);
		if (value.length() == 2 && value.charAt(0) == 'R' && value.charAt(1) >= '0' && value.charAt(1) <= '7') {
			return value.charAt(1) - '0';
		}
		throw new AssemblyException("Expected register R0..R7, got '" + token.trim() + "'");
	}

	private static String normalizedSymbol(String value) {
		String symbol = value.trim().toUpperCase(Locale.ROOT);
		if (!symbol.matches("[A-Z_][A-Z0-9_]*")) {
			throw new AssemblyException("Invalid symbol '" + value + "'");
		}
		return symbol;
	}

	private static boolean isMemoryOperand(String operand) {
		String value = operand.trim();
		return value.startsWith("[") && value.endsWith("]");
	}

	private static String memoryOperand(String operand) {
		if (!isMemoryOperand(operand)) {
			throw new AssemblyException("Memory address must be enclosed in [brackets]");
		}
		return operand.trim().substring(1, operand.trim().length() - 1).trim();
	}

	private static List<String> splitOperands(String text) {
		if (text.isBlank()) {
			return List.of();
		}
		List<String> operands = new ArrayList<>();
		for (String operand : text.split(",", -1)) {
			if (operand.isBlank()) {
				throw new AssemblyException("Empty operand");
			}
			operands.add(operand.trim());
		}
		return List.copyOf(operands);
	}

	private static void requireCount(String mnemonic, List<String> operands, int expected) {
		if (operands.size() != expected) {
			throw new AssemblyException("%s expects %d operand%s, got %d".formatted(
					mnemonic, expected, expected == 1 ? "" : "s", operands.size()));
		}
	}

	private static int operandCount(CpuInstruction instruction) {
		return switch (instruction.format()) {
			case NONE -> 0;
			case REGISTER, ADDRESS16, VECTOR8 -> 1;
			case TWO_REGISTERS, REGISTER_IMMEDIATE8, REGISTER_ADDRESS16, ADDRESS16_REGISTER,
					REGISTER_PORT8, PORT8_REGISTER -> 2;
			case THREE_REGISTERS -> 3;
		};
	}

	private static String stripComment(String line) {
		int semicolon = line.indexOf(';');
		int hash = line.indexOf('#');
		int end = semicolon < 0 ? hash : hash < 0 ? semicolon : Math.min(semicolon, hash);
		return end < 0 ? line : line.substring(0, end);
	}

	private static void emitWord(ByteArrayOutputStream output, int value) {
		output.write((value >>> 8) & 0xFF);
		output.write(value & 0xFF);
	}

	private static Map<String, CpuInstruction> simpleMnemonics() {
		Map<String, CpuInstruction> result = new HashMap<>();
		for (CpuInstruction instruction : CpuInstruction.values()) {
			if (instruction != CpuInstruction.LOAD_MEMORY && instruction != CpuInstruction.LOAD_IMMEDIATE) {
				result.put(instruction.name(), instruction);
			}
		}
		return Map.copyOf(result);
	}

	private record ParsedLine(int line, String sourceLine, int address, CpuInstruction instruction,
			List<String> operands) { }

	private static final class AssemblyException extends RuntimeException {
		AssemblyException(String message) {
			super(message);
		}
	}
}
