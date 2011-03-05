package com.vectorpen.core.util;

import java.util.List;
import java.util.Vector;

public class GeneralPath {
    
    public static class Operation {
	public static enum OperationCode {
	    LINE_TO, MOVE_TO
		}
	
	public OperationCode op;
	public float x;
	public float y;
	
	public Operation(OperationCode op, float x, float y) {
	    this.op = op;
	    this.x = x;
	    this.y = y;
	}
    }

    private List<Operation> operations;

    public GeneralPath() {
	this.operations = new Vector<Operation>();
    }

    public void lineTo(float x, float y) {
	this.operations.add(
	     new Operation(
		Operation.OperationCode.LINE_TO,
			   x, y
			   )
	);
    }

    public void	moveTo(float x, float y) {
	this.operations.add(
	     new Operation(
		Operation.OperationCode.MOVE_TO,
			   x, y
			   )
	);
    }
}
