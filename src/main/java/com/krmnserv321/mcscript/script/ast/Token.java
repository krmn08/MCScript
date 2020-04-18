package com.krmnserv321.mcscript.script.ast;

public class Token implements Cloneable {
    private String path = "";
    private int lineNumber;
    private final TokenType type;
    private final String literal;

    public Token(TokenType type, String literal) {
        this.type = type;
        this.literal = literal;
    }

    public Token(TokenType type, int lineNumber, String literal) {
        this.type = type;
        this.lineNumber = lineNumber;
        this.literal = literal;
    }

    public Token(TokenType type, String path, int lineNumber, String literal) {
        this.type = type;
        this.path = path;
        this.lineNumber = lineNumber;
        this.literal = literal;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public String toString() {
        return literal;
    }

    @Override
    public Token clone() {
        try {
            return (Token) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }
}
