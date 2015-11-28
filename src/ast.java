abstract class ASTNode
{

	int linenum;
	int colnum;

	static int typeErrors = 0; // Total number of type errors found

	static void genIndent(int indent)
	{
		for (int i = 1; i <= indent; i++) {
			System.out.print("\t");
		}
	} // genIndent

	static void mustBe(boolean assertion)
	{
		if (!assertion) {
			throw new RuntimeException();
		}
	} // mustBe

	static void typeMustBe(int testType, int requiredType, String errorMsg)
	{
		if ((testType != Types.Error) && (testType != requiredType)) {
			System.out.println(errorMsg);
			typeErrors++;
		}
	} // typeMustBe

	String error()
	{
		return "Error (line " + linenum + "): ";
	} // error

	// We will run any character through this in order to escape any special
	// characters.
	String escapeCharacters(char c)
	{
		return escapeCharacters(c);
	}

	// We will run any string or character through this in order to escape any
	// special characters.
	String escapeCharacters(String toEscape)
	{
		return toEscape.replaceAll("\t", "\\t").replaceAll("\r", "\\r")
				.replaceAll("\n", "\\n");
	}

	public static SymbolTable st = new SymbolTable();

	ASTNode()
	{
		linenum = -1;
		colnum = -1;
	}

	ASTNode(int l, int c)
	{
		linenum = l;
		colnum = c;
	}

	boolean isNull()
	{
		return false;
	} // Is this node null?

	void Unparse(int indent)
	{
	}

	// This will normally need to be redefined in a subclass

	void checkTypes()
	{
	}
	// This will normally need to be redefined in a subclass
} // abstract class ASTNode

class nullNode extends ASTNode
{
	// This class definition probably doesn't need to be changed
	nullNode()
	{
		super();
	}

	boolean isNull()
	{
		return true;
	}

	void Unparse(int indent)
	{
		// no action
	}
} // class nullNode

class csxLiteNode extends ASTNode
{

	csxLiteNode(fieldDeclsNode decls, stmtsNode stmts, int line, int col)
	{
		super(line, col);
		fields = decls;
		progStmts = stmts;
	} // csxLiteNode

	void Unparse(int indent)
	{
		System.out.println(linenum + ":" + " {");
		fields.Unparse(1);
		progStmts.Unparse(1);
		System.out.println(linenum + ":" + " } EOF");
	} // Unparse

	void checkTypes()
	{
		fields.checkTypes();
		progStmts.checkTypes();
	} // checkTypes

	boolean isTypeCorrect()
	{
		st.openScope();
		checkTypes();
		return (typeErrors == 0);
	} // isTypeCorrect

	private final stmtsNode progStmts;
	private final fieldDeclsNode fields;
} // class csxLiteNode

class classNode extends ASTNode
{

	classNode(identNode id, memberDeclsNode memb, int line, int col,
			int closingLine)
			{
		super(line, col);
		className = id;
		members = memb;
		closingLineNum = closingLine;
			} // classNode

	private final identNode className;
	private final memberDeclsNode members;
	private int closingLineNum;

	void Unparse(int indent)
	{

		// Print like:
		// ##: class name {
		// members.Unparse
		// ##: } EOF
		System.out.print(linenum + ": ");
		genIndent(indent);
		System.out.print("class ");
		className.Unparse(0); // Print class name only, no indent
		System.out.println(" {");

		members.Unparse(indent + 1); // Print members with an additional indent

		System.out.print(closingLineNum + ": ");
		genIndent(indent);
		System.out.println("} EOF");
	}
} // class classNode

class memberDeclsNode extends ASTNode
{
	memberDeclsNode(fieldDeclsNode f, methodDeclsNode m, int line, int col)
	{
		super(line, col);
		fields = f;
		methods = m;
	}

	fieldDeclsNode fields;
	methodDeclsNode methods;

	// Print like:
	// fields.Unparse
	// methods.Unparse
	void Unparse(int indent)
	{
		fields.Unparse(indent);
		methods.Unparse(indent);
	}
} // class memberDeclsNode

class fieldDeclsNode extends ASTNode
{
	fieldDeclsNode()
	{
		super();
	}

	fieldDeclsNode(declNode d, fieldDeclsNode f, int line, int col)
	{
		super(line, col);
		thisField = d;
		moreFields = f;
	}

	static nullFieldDeclsNode NULL = new nullFieldDeclsNode();
	private declNode thisField;
	private fieldDeclsNode moreFields;

	// Print like:
	// thisField
	// nextField
	// nextField
	// ...
	// lastField
	void Unparse(int indent)
	{
		thisField.Unparse(indent);
		moreFields.Unparse(indent);
	}
} // class fieldDeclsNode

class nullFieldDeclsNode extends fieldDeclsNode
{
	nullFieldDeclsNode()
	{
	}

	boolean isNull()
	{
		return true;
	}

	void Unparse(int indent)
	{
	}
} // class nullFieldDeclsNode

// abstract superclass; only subclasses are actually created
abstract class declNode extends ASTNode
{
	declNode()
	{
		super();
	}

	declNode(int l, int c)
	{
		super(l, c);
	}
} // class declNode

class varDeclNode extends declNode
{
	varDeclNode(identNode id, typeNode t, exprNode e, int line, int col)
	{
		super(line, col);
		varName = id;
		varType = t;
		initValue = e;
	}

	private final identNode varName;
	private final typeNode varType;
	private final exprNode initValue;

	// Print like:
	// ##: type id = expression;
	void Unparse(int indent)
	{
		System.out.print(linenum + ": ");
		genIndent(indent);
		varType.Unparse(0);
		System.out.print(" ");
		varName.Unparse(0);
		System.out.print(" = ");
		initValue.Unparse(0);
		System.out.println(";");
	}
} // class varDeclNode

class constDeclNode extends declNode
{
	constDeclNode(identNode id, exprNode e, int line, int col)
	{
		super(line, col);
		constName = id;
		constValue = e;
	}

	private final identNode constName;
	private final exprNode constValue;

	// Print like:
	// ##: id = expression;
	void Unparse(int indent)
	{
		System.out.print(linenum + ": ");
		genIndent(indent);
		constName.Unparse(0);
		System.out.print(" = ");
		constValue.Unparse(0);
		System.out.println(";");
	}
} // class constDeclNode

class arrayDeclNode extends declNode
{
	arrayDeclNode(identNode id, typeNode t, intLitNode lit, int line, int col)
	{
		super(line, col);
		arrayName = id;
		elementType = t;
		arraySize = lit;
	}

	private final identNode arrayName;
	private final typeNode elementType;
	private final intLitNode arraySize;

	// Print like:
	// ##: type id[intlit];
	void Unparse(int indent)
	{
		System.out.print(linenum + ": ");
		genIndent(indent);
		elementType.Unparse(0);
		System.out.print(" ");
		arrayName.Unparse(0);
		System.out.print("[");
		arraySize.Unparse(0);
		System.out.println("];");
	}
} // class arrayDeclNode

abstract class typeNode extends ASTNode
{
	// abstract superclass; only subclasses are actually created
	typeNode()
	{
		super();
	}

	typeNode(int l, int c)
	{
		super(l, c);
	}

	static nullTypeNode NULL = new nullTypeNode();
} // class typeNode

class nullTypeNode extends typeNode
{
	nullTypeNode()
	{
	}

	boolean isNull()
	{
		return true;
	}

	void Unparse(int indent)
	{
	}
} // class nullTypeNode

class intTypeNode extends typeNode
{
	intTypeNode(int line, int col)
	{
		super(line, col);
	}

	// Just print the data type INT
	void Unparse(int indent)
	{
		System.out.print("INT");
	}
} // class intTypeNode

class floatTypeNode extends typeNode
{
	floatTypeNode(int line, int col)
	{
		super(line, col);
	}

	// Just print the data type FLOAT
	void Unparse(int indent)
	{
		System.out.print("FLOAT");
	}
} // class floatTypeNode

class boolTypeNode extends typeNode
{
	boolTypeNode(int line, int col)
	{
		super(line, col);
	}

	// Just print the data type BOOL
	void Unparse(int indent)
	{
		System.out.print("BOOL");
	}
} // class boolTypeNode

class charTypeNode extends typeNode
{
	charTypeNode(int line, int col)
	{
		super(line, col);
	}

	// Just print the data type CHAR
	void Unparse(int indent)
	{
		System.out.print("CHAR");
	}
} // class charTypeNode

class voidTypeNode extends typeNode
{
	voidTypeNode(int line, int col)
	{
		super(line, col);
	}

	// Just print the data type VOID
	void Unparse(int indent)
	{
		System.out.print("VOID");
	}
} // class voidTypeNode

class methodDeclsNode extends ASTNode
{
	methodDeclsNode()
	{
		super();
	}

	methodDeclsNode(methodDeclNode m, methodDeclsNode ms, int line, int col)
	{
		super(line, col);
		thisDecl = m;
		moreDecls = ms;
	}

	static nullMethodDeclsNode NULL = new nullMethodDeclsNode();
	private methodDeclNode thisDecl;
	private methodDeclsNode moreDecls;

	// Print like:
	// thisDeclaration
	// nextDeclaration
	// nextDeclaration
	// ...
	// lastDeclaration
	void Unparse(int indent)
	{
		thisDecl.Unparse(indent);
		moreDecls.Unparse(indent);
	}
} // class methodDeclsNode

class nullMethodDeclsNode extends methodDeclsNode
{
	nullMethodDeclsNode()
	{
	}

	boolean isNull()
	{
		return true;
	}

	void Unparse(int indent)
	{
	}
} // class nullMethodDeclsNode

class methodDeclNode extends ASTNode
{
	methodDeclNode(identNode id, argDeclsNode a, typeNode t, fieldDeclsNode f,
			stmtsNode s, int line, int col, int closingLine)
	{
		super(line, col);
		name = id;
		args = a;
		returnType = t;
		decls = f;
		stmts = s;
		closingLineNum = closingLine;
	}

	private final identNode name;
	private final argDeclsNode args;
	private final typeNode returnType;
	private final fieldDeclsNode decls;
	private final stmtsNode stmts;
	private int closingLineNum;

	// Print like:
	// type id(args){
	// fieldDeclarations
	// statements
	// }
	void Unparse(int indent)
	{
		System.out.print(linenum + ": ");
		genIndent(indent);
		returnType.Unparse(0);
		System.out.print(" ");
		name.Unparse(0);
		System.out.print("(");
		args.Unparse(0);
		System.out.println("){");
		decls.Unparse(indent + 1);
		stmts.Unparse(indent + 1);
		System.out.print(closingLineNum + ": ");
		genIndent(indent);
		System.out.println("}");
	}
} // class methodDeclNode

// abstract superclass; only subclasses are actually created
abstract class argDeclNode extends ASTNode
{
	argDeclNode()
	{
		super();
	}

	argDeclNode(int l, int c)
	{
		super(l, c);
	}
}

class argDeclsNode extends ASTNode
{
	argDeclsNode()
	{
	}

	argDeclsNode(argDeclNode arg, argDeclsNode args, int line, int col)
	{
		super(line, col);
		thisDecl = arg;
		moreDecls = args;
	}

	static nullArgDeclsNode NULL = new nullArgDeclsNode();

	private argDeclNode thisDecl;
	private argDeclsNode moreDecls;

	// Print like:
	// thisArgDecl, nextArgDecl, nextArgDecl, ... , lastArgDecl
	void Unparse(int indent)
	{
		thisDecl.Unparse(0);

		// Make sure we're not at the end of the decls list
		if (!(moreDecls instanceof nullArgDeclsNode)) {
			System.out.print(", ");
		}

		moreDecls.Unparse(0);
	}
} // class argDeclsNode

class nullArgDeclsNode extends argDeclsNode
{
	nullArgDeclsNode()
	{
	}

	boolean isNull()
	{
		return true;
	}

	void Unparse(int indent)
	{
	}
} // class nullArgDeclsNode

class arrayArgDeclNode extends argDeclNode
{
	arrayArgDeclNode(identNode id, typeNode t, int line, int col)
	{
		super(line, col);
		argName = id;
		elementType = t;
	}

	private final identNode argName;
	private final typeNode elementType;

	// Print like:
	// type id[]
	void Unparse(int indent)
	{
		elementType.Unparse(0);
		System.out.print(" ");
		argName.Unparse(0);
		System.out.print("[]");
	}
} // class arrayArgDeclNode

class valArgDeclNode extends argDeclNode
{
	valArgDeclNode(identNode id, typeNode t, int line, int col)
	{
		super(line, col);
		argName = id;
		argType = t;
	}

	private final identNode argName;
	private final typeNode argType;

	// Print like:
	// type id
	void Unparse(int indent)
	{
		argType.Unparse(0);
		System.out.print(" ");
		argName.Unparse(0);
	}
} // class valArgDeclNode

// abstract superclass; only subclasses are actually created
abstract class stmtNode extends ASTNode
{
	stmtNode()
	{
		super();
	}

	stmtNode(int l, int c)
	{
		super(l, c);
	}

	static nullStmtNode NULL = new nullStmtNode();
}

class nullStmtNode extends stmtNode
{
	nullStmtNode()
	{
	}

	boolean isNull()
	{
		return true;
	}

	void Unparse(int indent)
	{
	}
} // class nullStmtNode

class stmtsNode extends ASTNode
{
	stmtsNode(stmtNode stmt, stmtsNode stmts, int line, int col)
	{
		super(line, col);
		thisStmt = stmt;
		moreStmts = stmts;
	}

	stmtsNode()
	{
	}

	// Print like:
	// thisStmt
	// nextStmt
	// nextStmt
	// ...
	// lastStmt
	void Unparse(int indent)
	{
		// Don't print the line number or indent on a block node since the first
		// thing it does
		// is print the linenum, indent, and LBRACE
		if (!(thisStmt instanceof blockNode)) {
			System.out.print(thisStmt.linenum + ":");
			genIndent(indent);
		}

		thisStmt.Unparse(indent);

		// If the thenPart is an ifThenNode, we don't want a semicolon to be
		// printed
		// or else we will have "endif;"
		if (!(thisStmt instanceof ifThenNode)) {
			System.out.print(";");
		}

		System.out.println();
		moreStmts.Unparse(indent);
	}

	static nullStmtsNode NULL = new nullStmtsNode();
	private stmtNode thisStmt;
	private stmtsNode moreStmts;
} // class stmtsNode

class nullStmtsNode extends stmtsNode
{
	nullStmtsNode()
	{
	}

	boolean isNull()
	{
		return true;
	}

	void Unparse(int indent)
	{
	}

} // class nullStmtsNode

class asgNode extends stmtNode
{
	asgNode(nameNode n, exprNode e, int line, int col)
	{
		super(line, col);
		target = n;
		source = e;
	}

	// Print like:
	// ##: type id = expression;
	void Unparse(int indent)
	{
		target.Unparse(0);
		System.out.print(" = ");
		source.Unparse(0);
	}

	private final nameNode target;
	private final exprNode source;
} // class asgNode

class ifThenNode extends stmtNode
{
	ifThenNode(exprNode e, stmtNode s1, stmtNode s2, int line, int col,
			int endifLineNum)
	{
		super(line, col);
		condition = e;
		thenPart = s1;
		elsePart = s2;
		this.endifLineNum = endifLineNum;
	}

	private final exprNode condition;
	private final stmtNode thenPart;
	private final stmtNode elsePart;
	private int endifLineNum;

	// Print like:
	// ##: if(expr)
	// stmt
	// ##: else
	// stmt
	// ##: endif
	void Unparse(int indent)
	{
		System.out.print("if (");
		condition.Unparse(0);
		System.out.println(")");

		// Don't print the line number or indent on a block node since the first
		// thing it does
		// is print the linenum, indent, and LBRACE
		if (!(thenPart instanceof blockNode)) {
			System.out.print(thenPart.linenum + ":");
			genIndent(indent + 1);
		}

		thenPart.Unparse(indent + 1);

		// If the thenPart is an ifThenNode, we don't want a semicolon to be
		// printed
		// or else we will have "endif;"
		if (!(thenPart instanceof ifThenNode)) {
			System.out.print(";");
		}

		System.out.println();

		// Make sure we actually have an else part before printing
		if (!(elsePart instanceof nullStmtNode)) {
			System.out.print(linenum + ":");
			genIndent(indent);
			System.out.println("else");

			// Don't print the line number or indent on a block node since the
			// first thing it does
			// is print the linenum, indent, and LBRACE
			if (!(elsePart instanceof blockNode)) {
				System.out.print(elsePart.linenum + ":");
				genIndent(indent + 1);
			}

			elsePart.Unparse(indent + 1);

			// If the thenPart is an ifThenNode, we don't want a semicolon to be
			// printed
			// or else we will have "endif;"
			if (!(elsePart instanceof ifThenNode)) {
				System.out.print(";");
			}

			System.out.println();
		}

		System.out.print(endifLineNum + ": ");
		genIndent(indent);
		System.out.print("endif");
	}
} // class ifThenNode

class whileNode extends stmtNode
{
	whileNode(exprNode i, exprNode e, stmtNode s, int line, int col)
	{
		super(line, col);
		label = i;
		condition = e;
		loopBody = s;
	}

	private final exprNode label;
	private final exprNode condition;
	private final stmtNode loopBody;

	// Print like:
	// ##: label:while(expression)
	// stmt
	void Unparse(int indent)
	{
		label.Unparse(0);
		System.out.print(" : while (");
		condition.Unparse(0);
		System.out.println(")");

		// Don't print the line number or indent on a block node since the first
		// thing it does
		// is print the linenum, indent, and LBRACE
		if (!(loopBody instanceof blockNode)) {
			System.out.print(loopBody.linenum + ":");
			genIndent(indent + 1);
		}

		loopBody.Unparse(indent + 1);
	}
} // class whileNode

class forNode extends stmtNode
{

	forNode(identNode id, exprNode inita, exprNode e, stmtNode u, stmtNode s,
			int line, int col)
			{
		super(line, col);
		loopVar = id;
		initialization = inita;
		condition = e;
		update = u;
		loopBody = s;
			}

	private final identNode loopVar;
	private final exprNode initialization;
	private final exprNode condition;
	private final stmtNode update;
	private final stmtNode loopBody;

	// Print like:
	// ##: for (type id = expression; expression; assignment)
	// stmt
	void Unparse(int indent)
	{
		System.out.print("for (");
		loopVar.Unparse(0);
		System.out.print(" = ");
		initialization.Unparse(0);
		System.out.print("; ");
		condition.Unparse(0);
		System.out.print("; ");
		update.Unparse(0);
		System.out.println(")");
		loopBody.Unparse(indent + 1);
	}
}

class readNode extends stmtNode
{
	readNode()
	{
	}

	readNode(nameNode n, readNode rn, int line, int col)
	{
		super(line, col);
		targetVar = n;
		moreReads = rn;
	}

	static nullReadNode NULL = new nullReadNode();
	private nameNode targetVar;
	private readNode moreReads;

	void Unparse(int indent)
	{
		Unparse(indent, true);
	}

	// Print like:
	// ##: READ (id1, id2, id3, ... , idN);
	void Unparse(int indent, boolean rootCalled)
	{
		// If it's the first node in the list, print the "READ (" part
		if (rootCalled) {
			System.out.print("READ (");
		}

		// Print the current node's expression
		targetVar.Unparse(0);

		// If this is the last node, the next readNode will be
		// an instance of nullReadNode
		if (moreReads instanceof nullReadNode) {
			// If so, print the closing ");"
			System.out.print(")");
		} else {
			// If not, print a separator comma
			System.out.print(", ");
			moreReads.Unparse(0, false);
		}
	}
} // class readNode

class nullReadNode extends readNode
{
	nullReadNode()
	{
	}

	boolean isNull()
	{
		return true;
	}

	void Unparse(int indent)
	{
	}
} // class nullReadNode

class printNode extends stmtNode
{
	printNode()
	{
	}

	printNode(exprNode val, printNode pn, int line, int col)
	{
		super(line, col);
		outputValue = val;
		morePrints = pn;
	}

	static nullPrintNode NULL = new nullPrintNode();

	private exprNode outputValue;
	private printNode morePrints;

	// Use a helper method to unparse the print statement because
	// the parser buids a linked list and we need to extra formatting
	// for the first, last and in between nodes
	void Unparse(int indent)
	{
		Unparse(indent, true);
	}

	// Print like:
	// PRINT (thisExpr, nextExpr, nextExpr, ... , lastExpr);
	void Unparse(int indent, boolean rootCalled)
	{
		// If it's the first node in the list, print the "PRINT (" part
		if (rootCalled) {
			System.out.print("PRINT (");
		}

		// Print the current node's expression
		outputValue.Unparse(0);

		// If this is the last node, the next printNode will be
		// an instance of nullPrintNode
		if (morePrints instanceof nullPrintNode) {
			// If so, print the closing ");"
			System.out.print(")");
		} else {
			// If not, print a separator comma
			System.out.print(", ");
			morePrints.Unparse(0, false);
		}
	}
} // class printNode

class nullPrintNode extends printNode
{
	nullPrintNode()
	{
	}

	boolean isNull()
	{
		return true;
	}

	void Unparse(int indent)
	{
	}
} // class nullprintNode

class callNode extends stmtNode
{
	callNode(identNode id, argsNode a, int line, int col)
	{
		super(line, col);
		methodName = id;
		args = a;
	}

	private final identNode methodName;
	private final argsNode args;

	// Print like:
	// ##: id(args);
	void Unparse(int indent)
	{
		methodName.Unparse(0);
		System.out.print(" (");

		args.Unparse(0);

		System.out.print(")");
	}
} // class callNode

class returnNode extends stmtNode
{
	returnNode(exprNode e, int line, int col)
	{
		super(line, col);
		returnVal = e;
	}

	private final exprNode returnVal;

	// Print like:
	// ##: return expression;
	void Unparse(int indent)
	{
		System.out.print("return ");

		returnVal.Unparse(0);
	}
} // class returnNode

class blockNode extends stmtNode
{
	blockNode(fieldDeclsNode f, stmtsNode s, int line, int col, int closingLine)
	{
		super(line, col);
		decls = f;
		stmts = s;
		closingLineNum = closingLine;
	}

	private final fieldDeclsNode decls;
	private final stmtsNode stmts;
	private int closingLineNum;

	// Print like:
	// {
	// fieldDeclarations
	// statements
	// }
	void Unparse(int indent)
	{
		System.out.print(linenum + ":");
		genIndent(indent);
		System.out.println("{");

		decls.Unparse(indent + 1);
		stmts.Unparse(indent + 1);

		System.out.print(closingLineNum + ":");
		genIndent(indent);
		System.out.print("}");

	}
} // class blockNode

class breakNode extends stmtNode
{
	breakNode(identNode i, int line, int col)
	{
		super(line, col);
		label = i;
	}

	private final identNode label;

	// Print like:
	// ##: break label;
	void Unparse(int indent)
	{
		System.out.print("break ");
		label.Unparse(0);
	}
} // class breakNode

class continueNode extends stmtNode
{
	continueNode(identNode i, int line, int col)
	{
		super(line, col);
		label = i;
	}

	private final identNode label;

	// Print like:
	// ##: continue label;
	void Unparse(int indent)
	{
		System.out.print("continue ");
		label.Unparse(0);
	}
} // class continueNode

class argsNode extends ASTNode
{
	argsNode()
	{
	}

	argsNode(exprNode e, argsNode a, int line, int col)
	{
		super(line, col);
		argVal = e;
		moreArgs = a;
	}

	static nullArgsNode NULL = new nullArgsNode();
	private exprNode argVal;
	private argsNode moreArgs;

	// Print like:
	// thisExpression, nextExpression, ... , lastExpression
	void Unparse(int indent)
	{
		argVal.Unparse(0);

		// Make sure we're not at the end of the args list
		if (!(moreArgs instanceof nullArgsNode)) {
			System.out.print(", ");
		}

		moreArgs.Unparse(0);
	}
} // class argsNode

class nullArgsNode extends argsNode
{
	nullArgsNode()
	{
		// empty constructor
	}

	boolean isNull()
	{
		return true;
	}

	void Unparse(int indent)
	{
	}
} // class nullArgsNode

class strLitNode extends exprNode
{
	strLitNode(String stringval, int line, int col)
	{
		super(line, col);
		strval = stringval;
	}

	private final String strval;

	void Unparse(int indent)
	{
		System.out.print(escapeCharacters(strval));
	}
} // class strLitNode

// abstract superclass; only subclasses are actually created
abstract class exprNode extends ASTNode
{
	exprNode()
	{
		super();
	}

	exprNode(int l, int c)
	{
		super(l, c);
	}

	static nullExprNode NULL = new nullExprNode();
}

class nullExprNode extends exprNode
{
	nullExprNode()
	{
		super();
	}

	boolean isNull()
	{
		return true;
	}

	void Unparse(int indent)
	{
	}
} // class nullExprNode

class binaryOpNode extends exprNode
{
	binaryOpNode(exprNode e1, int op, exprNode e2, int line, int col)
	{
		super(line, col);
		operatorCode = op;
		leftOperand = e1;
		rightOperand = e2;
	}

	static void printOp(int op)
	{
		switch (op)
		{
		case sym.PLUS:
			System.out.print(" + ");
			break;
		case sym.MINUS:
			System.out.print(" - ");
			break;
		case sym.TIMES:
			System.out.print(" * ");
			break;
		case sym.SLASH:
			System.out.print(" / ");
			break;
		case sym.LT:
			System.out.print(" < ");
			break;
		case sym.GT:
			System.out.print(" > ");
			break;
		case sym.GEQ:
			System.out.print(" >= ");
			break;
		case sym.LEQ:
			System.out.print(" <= ");
			break;
		case sym.COR:
			System.out.print(" || ");
			break;
		case sym.CAND:
			System.out.print(" && ");
			break;

		default:
			throw new Error("printOp: case not found");
		}
	}

	void Unparse(int indent)
	{
		System.out.print("(");
		leftOperand.Unparse(0);
		printOp(operatorCode);
		rightOperand.Unparse(0);
		System.out.print(")");
	}

	private final exprNode leftOperand;
	private final exprNode rightOperand;
	private final int operatorCode; // Token code of the operator
} // class binaryOpNode

class unaryOpNode extends exprNode
{
	unaryOpNode(int op, exprNode e, int line, int col)
	{
		super(line, col);
		operand = e;
		operatorCode = op;
	}

	void Unparse(int indent)
	{
		if (operatorCode == sym.NOT) {
			System.out.print("!");
		}
		operand.Unparse(0);
	}

	private final exprNode operand;
	private final int operatorCode; // Token code of the operator
} // class unaryOpNode

class castNode extends exprNode
{
	castNode(typeNode t, exprNode e, int line, int col)
	{
		super(line, col);
		operand = e;
		resultType = t;
	}

	void Unparse(int indent)
	{
		System.out.println("(");
		resultType.Unparse(0);
		System.out.println(")");

		operand.Unparse(0);
	}

	private final exprNode operand;
	private final typeNode resultType;
} // class castNode

class fctCallNode extends exprNode
{
	fctCallNode(identNode id, argsNode a, int line, int col)
	{
		super(line, col);
		methodName = id;
		methodArgs = a;
	}

	void Unparse(int indent)
	{
		methodName.Unparse(indent);
		System.out.print('(');
		methodArgs.Unparse(0);
		System.out.print(")");
	}

	private final identNode methodName;
	private final argsNode methodArgs;
} // class fctCallNode

class identNode extends exprNode
{
	identNode(String identname, int line, int col)
	{
		super(line, col);
		idname = identname;
	}

	void Unparse(int indent)
	{
		System.out.print(idname);
	}

	private final String idname;
} // class identNode

class nameNode extends exprNode
{
	nameNode(identNode id, exprNode expr, int line, int col)
	{
		super(line, col);
		varName = id;
		subscriptVal = expr;
	}

	void Unparse(int indent)
	{
		varName.Unparse(indent); // Subscripts not allowed in CSX Lite
	}

	public final identNode varName;
	private final exprNode subscriptVal;
} // class nameNode

class intLitNode extends exprNode
{
	intLitNode(int val, int line, int col)
	{
		super(line, col);
		intval = val;
	}

	void Unparse(int indent)
	{
		System.out.print(intval);
	}

	private final int intval;
} // class intLitNode

class floatLitNode extends exprNode
{
	floatLitNode(float val, int line, int col)
	{
		super(line, col);
		floatval = val;
	}

	void Unparse(int indent)
	{
		System.out.print(floatval);
	}

	private final float floatval;
} // class floatLitNode

class charLitNode extends exprNode
{
	charLitNode(char val, int line, int col)
	{
		super(line, col);
		charval = val;
	}

	void Unparse(int indent)
	{
		String tmp = escapeCharacters(charval);
		System.out.print(tmp);
	}

	private final char charval;
} // class charLitNode

class trueNode extends exprNode
{
	trueNode(int line, int col)
	{
		super(line, col);
	}

	void Unparse(int indent)
	{
		System.out.print("True");
	}
} // class trueNode

class falseNode extends exprNode
{
	falseNode(int line, int col)
	{
		super(line, col);
	}

	void Unparse(int indent)
	{
		System.out.print("False");
	}
} // class falseNode

class preIncrStmtNode extends stmtNode
{
	preIncrStmtNode(nameNode id, int line, int col)
	{
		super(line, col);

		targetID = id;
	}

	void Unparse(int indent)
	{
		genIndent(indent);
		System.out.print("++");
		targetID.Unparse(indent);
	}

	private nameNode targetID;
} // class preIncrStmtNode

class postIncrStmtNode extends stmtNode
{
	postIncrStmtNode(nameNode id, int line, int col)
	{
		super(line, col);

		targetID = id;
	}

	void Unparse(int indent)
	{
		targetID.Unparse(indent);
		System.out.print("++");
	}

	private nameNode targetID;
} // class postIncrStmtNode

class preDecStmtNode extends stmtNode
{
	preDecStmtNode(nameNode id, int line, int col)
	{
		super(line, col);

		targetID = id;
	}

	void Unparse(int indent)
	{
		System.out.print("--");
		targetID.Unparse(indent);
	}

	private nameNode targetID;
} // class preDecStmtNode

class postDecStmtNode extends stmtNode
{
	postDecStmtNode(nameNode id, int line, int col)
	{
		super(line, col);

		targetID = id;
	}

	void Unparse(int indent)
	{
		targetID.Unparse(indent);
		System.out.print("--");
	}

	private nameNode targetID;
} // class postDecStmtNode 