package com.vectorpen.ui.util;

import com.vectorpen.core.util.Color;
import com.vectorpen.core.util.GeneralPath;

public class AwtWrapper {
    public static java.awt.Color convertColor(Color c) {
	return new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue());
    }
    public static Color importColor(java.awt.Color c) {
	return new Color(c.getRed(), c.getGreen(), c.getBlue());
    }

    public static java.awt.geom.GeneralPath convertGeneralPath(GeneralPath p) {
	java.awt.geom.GeneralPath res = new java.awt.geom.GeneralPath();
	for (GeneralPath.Operation op : p.getOperations()) {
	    if (op.op == GeneralPath.Operation.OperationCode.LINE_TO)
		res.lineTo(op.x, op.y);
	    else if (op.op == GeneralPath.Operation.OperationCode.MOVE_TO)
		res.moveTo(op.x, op.y);
	}
	return res;
    }
}