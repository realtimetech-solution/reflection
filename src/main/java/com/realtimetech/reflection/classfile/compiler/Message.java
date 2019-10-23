package com.realtimetech.reflection.classfile.compiler;

public class Message {
	public static enum MessageType {
		ERROR, WARNING, NOTE
	}

	private MessageType type;

	private long line;

	private String message;

	public Message(MessageType type, long line, String message) {
		this.type = type;
		this.line = line;
		this.message = message;
	}

	public MessageType getType() {
		return type;
	}

	public long getLine() {
		return line;
	}

	public String getMessage() {
		return message;
	}
	
	@Override
	public String toString() {
		return type + " [ line=" + line + "\t  message=" + message + " ]";
	}
}
