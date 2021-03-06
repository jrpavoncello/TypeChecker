
abstract class ASTNode {

	int linenum;
	int colnum;

	static int typeErrors = 0; // Total number of type errors found
	static methodDeclNode currentMethod = null;

	static void genIndent(int indent) {
		for (int i = 1; i <= indent; i++) {
			System.out.print("\t");
		}
	} // genIndent

	static void assertTrue(boolean assertion, String errorMsg) {
		if (!assertion) {
			System.out.println(errorMsg);
			typeErrors++;
		}
	} // mustBe

	static boolean isCharacterArrayOrString(int type, int kind) {
		return ((kind == Kinds.Array || kind == Kinds.ArrayParm) && type == Types.Character)
				|| ((kind == Kinds.Var || kind == Kinds.ScalarParm) && type == Types.String);
	}

	static boolean areBothMatchingTypeArrays(int lhsType, int lhsKind, int rhsType, int rhsKind) {
		return (lhsKind == Kinds.Array || lhsKind == Kinds.ArrayParm)
				&& (rhsKind == Kinds.Array || rhsKind == Kinds.ArrayParm) && lhsType == rhsType;
	}

	static void assertAssignmentCompatible(exprNode lhs, exprNode rhs, String errorMsg) {
		// Don't print a type incompatible error message when the test type is
		// of type Error
		if (rhs.type.val != Types.Error) {
			boolean compatible = false;
			
			SymbolInfo lhsInfo = null;
			
			boolean isIndexed = false;
			
			if(lhs instanceof nameNode)
			{
				nameNode lhsName = (nameNode)lhs;
				
				lhsInfo = lhsName.varName.idinfo;
				
				isIndexed = lhsName.isIndexed();
			}
			else
			{
				if(lhs instanceof identNode)
				{
					identNode lhsIdent = (identNode)lhs;
					
					lhsInfo = lhsIdent.idinfo;
				}
			}

			// Can't assign to a constant
			if (!lhsInfo.constant) {
				
				// Start checking for array assignment compatibility
				// LHS can only be name node we are assigning an array
				if (rhs instanceof nameNode) {

					nameNode rhsName = (nameNode)rhs;
					
					// Make sure that the name node is not an array being indexed
					if (!isIndexed && !rhsName.isIndexed()) {
						SizedSymbolInfo lhsSizedInfo = null;
						SizedSymbolInfo rhsSizedInfo = null;

						boolean areTypeCompatibleArrays = areBothMatchingTypeArrays(lhs.type.val, lhs.kind.val, 
								rhs.type.val, rhs.kind.val);

						// If the LHS is a character array or string, excluding
						// string literals and constant character arrays
						// or we already know both sides are type and kind
						// compatible
						if (isCharacterArrayOrString(lhs.type.val, lhs.kind.val) || areTypeCompatibleArrays) {
							if (lhsInfo instanceof SizedSymbolInfo) {
								lhsSizedInfo = (SizedSymbolInfo) lhsInfo;
							}
						}

						// If the RHS is a character array or string, including
						// string literals
						// or we already know both sides are type and kind
						// compatible
						if (isCharacterArrayOrString(rhsName.type.val, rhsName.kind.val) || areTypeCompatibleArrays) {
							if (rhsName.varName.idinfo instanceof SizedSymbolInfo) {
								rhsSizedInfo = (SizedSymbolInfo) rhsName.varName.idinfo;
							}
						}

						// If we've gotten both the LHS and RHS infos,
						// indicating type and kind compatibility
						if (lhsSizedInfo != null && rhsSizedInfo != null) {
							// Size must be equal unless either the LHS or RHS
							// are method args, then we can't enforce the array
							// size
							if (lhsSizedInfo.Size == rhsSizedInfo.Size || lhs.kind.val == Kinds.ArrayParm
									|| rhs.kind.val == Kinds.ArrayParm) {
								compatible = true;
							}
						}
					}
				}

				// If no edge case determined the LHS and RHS were compatible
				if (!compatible) {
					// Check that the kinds are compatible
					if (((lhs.kind.val == Kinds.Array || lhs.kind.val == Kinds.ArrayParm)
							&& (rhs.kind.val == Kinds.Array || rhs.kind.val == Kinds.ArrayParm))
							|| ((lhs.kind.val == Kinds.Var || lhs.kind.val == Kinds.ScalarParm)
									&& (rhs.kind.val == Kinds.Var || rhs.kind.val == Kinds.ScalarParm
											|| rhs.kind.val == Kinds.Value))) {
						// Check that the types are exactly equivalent
						if (lhs.type.val == rhs.type.val) {
							compatible = true;
						}
					}
				}
			}

			if (!compatible) {
				System.out.println(errorMsg);
				typeErrors++;
			}
		}
	}

	static int assertArithmeticCompatible(int lhsKind, int lhsType, int rhsKind, int rhsType, String errorMsg) {
		int returnType = Types.Unknown;

		// Don't print a type incompatible error message when the test type is
		// of type Error, and also make sure the RHS actually returns something
		switch (rhsType) {
		case Types.Character:
		case Types.Integer:
		case Types.Real:
			boolean compatible = false;

			// Make sure the kinds are compatible
			if ((lhsKind == Kinds.Var || lhsKind == Kinds.ScalarParm || lhsKind == Kinds.Value)
					&& (rhsKind == Kinds.Var || rhsKind == Kinds.ScalarParm || rhsKind == Kinds.Value)) {
				// If the two types are either character or integer but are not
				// the same
				// this will return an integer
				if ((rhsType == Types.Character || rhsType == Types.Integer)
						&& (lhsType == Types.Character || lhsType == Types.Integer) && lhsType != rhsType) {
					returnType = Types.Integer;
					compatible = true;
				}

				// If the types match exactly
				if (lhsType == rhsType) {
					returnType = rhsType;
					compatible = true;
				}
			}

			if (!compatible) {
				System.out.println(errorMsg);
				typeErrors++;
			}
			break;
		default:
			System.out.println(errorMsg);
			typeErrors++;
			break;
		}

		return returnType;
	}

	static int assertBooleanCompatible(int lhsKind, int lhsType, int rhsKind, int rhsType, String errorMsg) {
		if (lhsType == Types.Boolean && rhsType == Types.Boolean
				&& (lhsKind == Kinds.Var || lhsKind == Kinds.ScalarParm || lhsKind == Kinds.Value)
				&& (rhsKind == Kinds.Var || rhsKind == Kinds.ScalarParm || rhsKind == Kinds.Value)) {
			return Types.Boolean;
		}

		System.out.println(errorMsg);
		typeErrors++;

		return Types.Error;
	}

	String error() {
		return "Error (line " + linenum + "): ";
	} // error

	// We will run any character through this in order to escape any special
	// characters.
	String escapeCharacters(char c) {
		return escapeCharacters(c);
	}

	// We will run any string or character through this in order to escape any
	// special characters.
	String escapeCharacters(String toEscape) {
		return toEscape.replaceAll("\t", "\\t").replaceAll("\r", "\\r").replaceAll("\n", "\\n");
	}

	public static SymbolTable st = new SymbolTable();

	ASTNode() {
		linenum = -1;
		colnum = -1;
	}

	ASTNode(int l, int c) {
		linenum = l;
		colnum = c;
	}

	boolean isNull() {
		return false;
	} // Is this node null?

	void Unparse(int indent) {
	}

	// Explicitly make children implement this to not miss anything by mistake
	abstract void checkTypes();

	// This will normally need to be redefined in a subclass
} // abstract class ASTNode

class nullNode extends ASTNode {
	// This class definition probably doesn't need to be changed
	nullNode() {
		super();
	}

	boolean isNull() {
		return true;
	}

	void Unparse(int indent) {
		// no action
	}

	void checkTypes() {
		// Null nodes are type correct by default (optional semi)
	}
} // class nullNode

class csxLiteNode extends ASTNode {

	csxLiteNode(fieldDeclsNode decls, stmtsNode stmts, int line, int col) {
		super(line, col);
		fields = decls;
		progStmts = stmts;
	} // csxLiteNode

	void Unparse(int indent) {
		System.out.println(linenum + ":" + " {");
		fields.Unparse(1);
		progStmts.Unparse(1);
		System.out.println(linenum + ":" + " } EOF");
	} // Unparse

	void checkTypes() {
		fields.checkTypes();
		progStmts.checkTypes();
	} // checkTypes

	boolean isTypeCorrect() {
		st.openScope();
		checkTypes();

		try {
			st.closeScope();
		} catch (EmptySTException e) {
			throw new RuntimeException("Tried to close a scope but no scope was available to close.");
		}
		
		return (typeErrors == 0);
	} // isTypeCorrect

	private final stmtsNode progStmts;
	private final fieldDeclsNode fields;
} // class csxLiteNode

class classNode extends ASTNode {
	classNode(identNode id, memberDeclsNode memb, int line, int col, int closingLine) {
		super(line, col);
		className = id;
		members = memb;
		closingLineNum = closingLine;
	} // classNode

	private final identNode className;
	private final memberDeclsNode members;
	private int closingLineNum;

	boolean isTypeCorrect() {
		checkTypes();
		return (typeErrors == 0);
	} // isTypeCorrect

	void Unparse(int indent) {
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

	void checkTypes() {
		st.openScope();
		members.checkTypes();

		try {
			st.closeScope();
		} catch (EmptySTException e) {
			throw new RuntimeException("Tried to close a scope but no scope was available to close.");
		}
	}
} // class classNode

class memberDeclsNode extends ASTNode {
	memberDeclsNode(fieldDeclsNode f, methodDeclsNode m, int line, int col) {
		super(line, col);
		fields = f;
		methods = m;
	}

	fieldDeclsNode fields;
	methodDeclsNode methods;

	// Print like:
	// fields.Unparse
	// methods.Unparse
	void Unparse(int indent) {
		fields.Unparse(indent);
		methods.Unparse(indent);
	}

	void checkTypes() {
		fields.checkTypes();
		methods.checkTypes();
	}
} // class memberDeclsNode

class fieldDeclsNode extends ASTNode {
	fieldDeclsNode() {
		super();
	}

	fieldDeclsNode(declNode d, fieldDeclsNode f, int line, int col) {
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
	void Unparse(int indent) {
		thisField.Unparse(indent);
		moreFields.Unparse(indent);
	}

	void checkTypes() {
		thisField.checkTypes();
		moreFields.checkTypes();
	}
} // class fieldDeclsNode

class nullFieldDeclsNode extends fieldDeclsNode {
	nullFieldDeclsNode() {
	}

	boolean isNull() {
		return true;
	}

	void Unparse(int indent) {
	}

	void checkTypes() {
		// No type checking necessary
	}
} // class nullFieldDeclsNode

// abstract superclass; only subclasses are actually created
abstract class declNode extends ASTNode {
	declNode() {
		super();
	}

	declNode(int l, int c) {
		super(l, c);
	}
} // class declNode

class varDeclNode extends declNode {
	varDeclNode(identNode id, typeNode t, exprNode e, int line, int col) {
		super(line, col);
		varName = id;
		varType = t;
		rhsExpr = e;
	}

	private final identNode varName;
	private final typeNode varType;
	private final exprNode rhsExpr;

	// Print like:
	// ##: type id = expression;
	void Unparse(int indent) {
		System.out.print(linenum + ": ");
		genIndent(indent);
		varType.Unparse(0);
		System.out.print(" ");
		varName.Unparse(0);
		System.out.print(" = ");
		rhsExpr.Unparse(0);
		System.out.println(";");
	}

	void checkTypes() {
		SymbolInfo info;
		// Make sure id is not already declared
		info = (SymbolInfo) st.localLookup(varName.idname);
		if (info == null) {
			info = new SymbolInfo(varName.idname, new Kinds(Kinds.Var), varType.type, false);

			// Type check the expression
			varName.checkTypes();
			rhsExpr.checkTypes();

			varName.idinfo = info;
			
			varName.type = info.type;
			varName.kind = info.kind;

			if(!(rhsExpr instanceof nullExprNode)){
				// Make sure that there's no type mismatch between typeNode and
				// initValue
				assertAssignmentCompatible(varName, rhsExpr, error() + "LHS and RHS are not compatible for assignment");
			}

			try {
				st.insert(info);
			} catch (DuplicateException d) {
				throw new RuntimeException("DuplicateException was thrown by st.insert, this \"can't happen\"");
			} catch (EmptySTException e) {
				throw new RuntimeException("EmptySTException was thrown by st.insert, this \"can't happen\"");
			}
		} else {
			System.out.println(error() + info.name() + " is already declared.");
			typeErrors++;
			varName.type = new Types(Types.Error);
		}
	}

} // class varDeclNode

class constDeclNode extends declNode {
	constDeclNode(identNode id, exprNode e, int line, int col) {
		super(line, col);
		constName = id;
		constValue = e;
	}

	private final identNode constName;
	private final exprNode constValue;

	// Print like:
	// ##: id = expression;
	void Unparse(int indent) {
		System.out.print(linenum + ": ");
		genIndent(indent);
		constName.Unparse(0);
		System.out.print(" = ");
		constValue.Unparse(0);
		System.out.println(";");
	}

	void checkTypes() {
		// Get any errors even if the name is already declared
		constValue.checkTypes();

		SymbolInfo info = (SymbolInfo) st.localLookup(constName.idname);

		if (info == null) {
			info = new SymbolInfo(constName.idname, constValue.kind, constValue.type, true);

			constName.idinfo = info;
			
			constName.type = info.type;
			constName.kind = info.kind;

			try {
				st.insert(info);
			} catch (DuplicateException d) {
				throw new RuntimeException("DuplicateException was thrown by st.insert, this \"can't happen\"");
			} catch (EmptySTException e) {
				throw new RuntimeException("EmptySTException was thrown by st.insert, this \"can't happen\"");
			}
			
		} else {
			System.out.println(error() + info.name() + " is already declared.");
			typeErrors++;
			constName.type = new Types(Types.Error);
		}
	}
} // class constDeclNode

class arrayDeclNode extends declNode {
	arrayDeclNode(identNode id, typeNode t, intLitNode lit, int line, int col) {
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
	void Unparse(int indent) {
		System.out.print(linenum + ": ");
		genIndent(indent);
		elementType.Unparse(0);
		System.out.print(" ");
		arrayName.Unparse(0);
		System.out.print("[");
		arraySize.Unparse(0);
		System.out.println("];");
	}

	void checkTypes() {
		SymbolInfo info = (SymbolInfo) st.localLookup(arrayName.idname);

		if (info == null) {
			arraySize.checkTypes();

			info = new SizedSymbolInfo(arrayName.idname, Kinds.Array, elementType.type.val, arraySize.intval, false);

			try {
				st.insert(info);
			} catch (DuplicateException d) {
				throw new RuntimeException("DuplicateException was thrown by st.insert, this \"can't happen\"");
			} catch (EmptySTException e) {
				throw new RuntimeException("EmptySTException was thrown by st.insert, this \"can't happen\"");
			}

			arrayName.idinfo = info;
		} else {
			System.out.println(error() + info.name() + " is already declared.");
			typeErrors++;
			elementType.type = new Types(Types.Error);
		}
	}
} // class arrayDeclNode

abstract class typeNode extends ASTNode {
	typeNode(int l, int c, Types t) {
		super(l, c);
		type = t;
	}

	// Used for typechecking -- the type of this typeNode
	Types type;
} // class typeNode

class intTypeNode extends typeNode {
	intTypeNode(int line, int col) {
		super(line, col, new Types(Types.Integer));
	}

	// Just print the data type INT
	void Unparse(int indent) {
		System.out.print("INT");
	}

	void checkTypes() {
		// No type checking needed
	}
} // class intTypeNode

class floatTypeNode extends typeNode {
	floatTypeNode(int line, int col) {
		super(line, col, new Types(Types.Real));
	}

	// Just print the data type FLOAT
	void Unparse(int indent) {
		System.out.print("FLOAT");
	}

	void checkTypes() {
		// No type checking needed
	}
} // class floatTypeNode

class boolTypeNode extends typeNode {
	boolTypeNode(int line, int col) {
		super(line, col, new Types(Types.Boolean));
	}

	// Just print the data type BOOL
	void Unparse(int indent) {
		System.out.print("BOOL");
	}

	void checkTypes() {
		// No type checking needed
	}
} // class boolTypeNode

class charTypeNode extends typeNode {
	charTypeNode(int line, int col) {
		super(line, col, new Types(Types.Character));
	}

	// Just print the data type CHAR
	void Unparse(int indent) {
		System.out.print("CHAR");
	}

	void checkTypes() {
		// No type checking needed
	}
} // class charTypeNode

class voidTypeNode extends typeNode {
	voidTypeNode(int line, int col) {
		super(line, col, new Types(Types.Void));
	}

	// Just print the data type VOID
	void Unparse(int indent) {
		System.out.print("VOID");
	}

	void checkTypes() {
		// No type checking needed
	}
} // class voidTypeNode

class methodDeclsNode extends ASTNode {
	methodDeclsNode() {
		super();
	}

	methodDeclsNode(methodDeclNode m, methodDeclsNode ms, int line, int col) {
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
	void Unparse(int indent) {
		thisDecl.Unparse(indent);
		moreDecls.Unparse(indent);
	}

	void checkTypes() {
		thisDecl.checkTypes();
		
		if(moreDecls instanceof nullMethodDeclsNode)
		{
			boolean isMainMethod = thisDecl.info.name().equals("main");
			
			assertTrue(isMainMethod, 
					error() + "Last method declaration must be the main method");
			
			if(isMainMethod)
			{
				assertTrue(thisDecl.info.type.val == Types.Void, 
						error() + "main method must have return type of VOID.");
			}
		}
		
		moreDecls.checkTypes();
	}
} // class methodDeclsNode

class nullMethodDeclsNode extends methodDeclsNode {
	nullMethodDeclsNode() {
	}

	boolean isNull() {
		return true;
	}

	void Unparse(int indent) {
	}

	void checkTypes() {
		// Don't type check null
	}
} // class nullMethodDeclsNode

class methodDeclNode extends ASTNode {
	methodDeclNode(identNode id, argDeclsNode a, typeNode t, fieldDeclsNode f, stmtsNode s, int line, int col,
			int closingLine) {
		super(line, col);
		name = id;
		args = a;
		returnType = t;
		decls = f;
		stmts = s;
		closingLineNum = closingLine;
		info = null;
	}

	private final identNode name;
	private final argDeclsNode args;
	private final typeNode returnType;
	private final fieldDeclsNode decls;
	private final stmtsNode stmts;
	private int closingLineNum;
	public MethodSymbolInfo info;

	// Print like:
	// type id(args){
	// fieldDeclarations
	// statements
	// }
	void Unparse(int indent) {
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

	void checkTypes() {
		SymbolInfo info = (SymbolInfo) st.localLookup(name.idname);

		assertTrue(info == null, 
				error() + "ID " + name.idname + " was already declared.");

		if (info == null) {
			MethodSymbolInfo methodInfo = new MethodSymbolInfo(name.idname, returnType.type);

			try {
				st.insert(methodInfo);
			} catch (DuplicateException d) {
				throw new RuntimeException("DuplicateException was thrown by st.insert, this \"can't happen\"");
			} catch (EmptySTException e) {
				throw new RuntimeException("EmptySTException was thrown by st.insert, this \"can't happen\"");
			}

			currentMethod = this;
			
			this.info = methodInfo;

			st.openScope();

			args.checkTypes();

			decls.checkTypes();

			stmts.checkTypes();

			try {
				st.closeScope();
			} catch (EmptySTException e) {
				throw new RuntimeException("Tried to close a scope but no scope was available to close.");
			}
		}
	}
} // class methodDeclNode

// abstract superclass; only subclasses are actually created
abstract class argDeclNode extends ASTNode {
	argDeclNode() {
		super();
	}

	argDeclNode(int l, int c) {
		super(l, c);
	}
}

class argDeclsNode extends ASTNode {
	argDeclsNode() {
	}

	argDeclsNode(argDeclNode arg, argDeclsNode args, int line, int col) {
		super(line, col);
		thisDecl = arg;
		moreDecls = args;
	}

	static nullArgDeclsNode NULL = new nullArgDeclsNode();

	private argDeclNode thisDecl;
	private argDeclsNode moreDecls;

	// Print like:
	// thisArgDecl, nextArgDecl, nextArgDecl, ... , lastArgDecl
	void Unparse(int indent) {
		thisDecl.Unparse(0);

		// Make sure we're not at the end of the decls list
		if (!(moreDecls instanceof nullArgDeclsNode)) {
			System.out.print(", ");
		}

		moreDecls.Unparse(0);
	}

	void checkTypes() {
		thisDecl.checkTypes();
		moreDecls.checkTypes();
	}
} // class argDeclsNode

class nullArgDeclsNode extends argDeclsNode {
	nullArgDeclsNode() {
	}

	boolean isNull() {
		return true;
	}

	void Unparse(int indent) {
	}

	void checkTypes() {
		// Don't need to type check a null node
	}
} // class nullArgDeclsNode

class arrayArgDeclNode extends argDeclNode {
	arrayArgDeclNode(identNode id, typeNode t, int line, int col) {
		super(line, col);
		arrayName = id;
		elementType = t;
	}

	private final identNode arrayName;
	private final typeNode elementType;

	// Print like:
	// type id[]
	void Unparse(int indent) {
		elementType.Unparse(0);
		System.out.print(" ");
		arrayName.Unparse(0);
		System.out.print("[]");
	}

	void checkTypes() {
		SymbolInfo info = (SymbolInfo) st.localLookup(arrayName.idname);

		if (info == null) {
			info = new SizedSymbolInfo(arrayName.idname, Kinds.ArrayParm, elementType.type.val, 0, false);

			try {
				st.insert(info);
			} catch (DuplicateException d) {
				throw new RuntimeException("DuplicateException was thrown by st.insert, this \"can't happen\"");
			} catch (EmptySTException e) {
				throw new RuntimeException("EmptySTException was thrown by st.insert, this \"can't happen\"");
			}

			arrayName.idinfo = info;

			currentMethod.info.Arguments.add(info);
		} else {
			System.out.println(error() + info.name() + " is already declared.");
			typeErrors++;
			elementType.type = new Types(Types.Error);
		}
	}
} // class arrayArgDeclNode

class valArgDeclNode extends argDeclNode {
	valArgDeclNode(identNode id, typeNode t, int line, int col) {
		super(line, col);
		argName = id;
		argType = t;
	}

	private final identNode argName;
	private final typeNode argType;

	// Print like:
	// type id
	void Unparse(int indent) {
		argType.Unparse(0);
		System.out.print(" ");
		argName.Unparse(0);
	}

	void checkTypes() {
		SymbolInfo info;
		// Make sure id is not already declared
		info = (SymbolInfo) st.localLookup(argName.idname);
		if (info == null) {
			info = new SymbolInfo(argName.idname, new Kinds(Kinds.ScalarParm), argType.type, false);

			argType.checkTypes();
			argName.checkTypes();

			try {
				st.insert(info);
			} catch (DuplicateException d) {
				throw new RuntimeException("DuplicateException was thrown by st.insert, this \"can't happen\"");
			} catch (EmptySTException e) {
				throw new RuntimeException("EmptySTException was thrown by st.insert, this \"can't happen\"");
			}

			argName.idinfo = info;

			currentMethod.info.Arguments.add(info);
		} else {
			System.out.println(error() + info.name() + " is already declared.");
			typeErrors++;
			argName.type = new Types(Types.Error);
		}
	}
} // class valArgDeclNode

// abstract superclass; only subclasses are actually created
abstract class stmtNode extends ASTNode {
	stmtNode() {
		super();
	}

	stmtNode(int l, int c) {
		super(l, c);
	}

	static nullStmtNode NULL = new nullStmtNode();
}

class nullStmtNode extends stmtNode {
	nullStmtNode() {
	}

	boolean isNull() {
		return true;
	}

	void Unparse(int indent) {
	}

	void checkTypes() {
		// Do nothing, we don't need to type check a null node
	}
} // class nullStmtNode

class stmtsNode extends ASTNode {
	stmtsNode(stmtNode stmt, stmtsNode stmts, int line, int col) {
		super(line, col);
		thisStmt = stmt;
		moreStmts = stmts;
	}

	stmtsNode() {
	}

	// Print like:
	// thisStmt
	// nextStmt
	// nextStmt
	// ...
	// lastStmt
	void Unparse(int indent) {
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

	void checkTypes() {
		thisStmt.checkTypes();
		moreStmts.checkTypes();
	}

	static nullStmtsNode NULL = new nullStmtsNode();
	private stmtNode thisStmt;
	private stmtsNode moreStmts;
} // class stmtsNode

class nullStmtsNode extends stmtsNode {
	nullStmtsNode() {
	}

	boolean isNull() {
		return true;
	}

	void Unparse(int indent) {
	}

	void checkTypes() {
		// No type check needed
	}
} // class nullStmtsNode

class asgNode extends stmtNode {
	asgNode(nameNode n, exprNode e, int line, int col) {
		super(line, col);
		target = n;
		source = e;
	}

	// Print like:
	// ##: type id = expression;
	void Unparse(int indent) {
		target.Unparse(0);
		System.out.print(" = ");
		source.Unparse(0);
	}

	void checkTypes() {
		target.checkTypes();
		
		SymbolInfo info = (SymbolInfo) st.globalLookup(target.varName.idname);

		if (info != null) {
			source.checkTypes();

			// Make sure
			assertAssignmentCompatible(target, source, error() + "Both the left and right"
					+ " hand sides of an assignment must " + "have compatible types.");
		}
	}

	private final nameNode target;
	private final exprNode source;
} // class asgNode

class ifThenNode extends stmtNode {
	ifThenNode(exprNode e, stmtNode s1, stmtNode s2, int line, int col, int endifLineNum) {
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
	void Unparse(int indent) {
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

	void checkTypes() {
		condition.checkTypes();
		assertTrue(condition.type.val == Types.Boolean,
				error() + "The control expression of an if statement must be a boolean.");
		
		st.openScope();
		
		thenPart.checkTypes();

		try {
			st.closeScope();
		} catch (EmptySTException e) {
			throw new RuntimeException("Tried to close a scope but no scope was available to close.");
		}
		
		st.openScope();
		
		elsePart.checkTypes();
		
		try {
			st.closeScope();
		} catch (EmptySTException e) {
			throw new RuntimeException("Tried to close a scope but no scope was available to close.");
		}
	}

} // class ifThenNode

class whileNode extends stmtNode {
	whileNode(exprNode i, exprNode e, stmtNode s, int line, int col) {
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
	void Unparse(int indent) {
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

	void checkTypes() {
		label.checkTypes();

		identNode labelAsIdent = null;

		LabelSymbolInfo labelInfo = null;

		// Will be identNode if it's defined
		if (label instanceof identNode) {
			labelAsIdent = (identNode) label;

			SymbolInfo info = (SymbolInfo) st.localLookup(labelAsIdent.idname);

			assertTrue(info == null, error() + "Label: " + labelAsIdent.idname + " was already defined in this scope.");

			if (info == null) {
				labelInfo = new LabelSymbolInfo(labelAsIdent.idname, Kinds.Label, Types.Void, true);
			}

			try {
				st.insert(labelInfo);
			} catch (DuplicateException d) {
				throw new RuntimeException("DuplicateException was thrown by st.insert, this \"can't happen\"");
			} catch (EmptySTException e) {
				throw new RuntimeException("EmptySTException was thrown by st.insert, this \"can't happen\"");
			}
		}

		condition.checkTypes();

		assertTrue(condition.type.val == Types.Boolean,
				error() + "The control expression of a while loop must be a boolean.");

		st.openScope();
		
		loopBody.checkTypes();

		try {
			st.closeScope();
		} catch (EmptySTException e) {
			throw new RuntimeException("Tried to close a scope but no scope was available to close.");
		}
		
		if (labelInfo != null) {
			labelInfo.Visible = false;
		}
	}
} // class whileNode

class forNode extends stmtNode {
	forNode(identNode id, exprNode inita, exprNode e, stmtNode u, stmtNode s, int line, int col) {
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
	void Unparse(int indent) {
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

	void checkTypes() {
		loopVar.checkTypes();
		initialization.checkTypes();
		condition.checkTypes();
		
		assertTrue(condition.type.val == Types.Boolean,
				error() + "The control expression of a for loop must be a boolean.");
		
		update.checkTypes();
		
		st.openScope();
		
		loopBody.checkTypes();
		
		try {
			st.closeScope();
		} catch (EmptySTException e) {
			throw new RuntimeException("Tried to close a scope but no scope was available to close.");
		}
	}
}

class readNode extends stmtNode {
	readNode() {
	}

	readNode(nameNode n, readNode rn, int line, int col) {
		super(line, col);
		targetVar = n;
		moreReads = rn;
	}

	static nullReadNode NULL = new nullReadNode();
	private nameNode targetVar;
	private readNode moreReads;

	void Unparse(int indent) {
		Unparse(indent, true);
	}

	// Print like:
	// ##: READ (id1, id2, id3, ... , idN);
	void Unparse(int indent, boolean rootCalled) {
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

	void checkTypes() {
		targetVar.checkTypes();
		
		assertTrue((targetVar.type.val == Types.Integer || targetVar.type.val == Types.Character) &&
						(targetVar.kind.val == Kinds.Var || targetVar.kind.val == Kinds.ScalarParm), 
						error() + "Only integer or character variables or scalar parameters are allowed to be read into.");
		
		moreReads.checkTypes();
	}
} // class readNode

class nullReadNode extends readNode {
	nullReadNode() {
	}

	boolean isNull() {
		return true;
	}

	void Unparse(int indent) {
		// No type check needed
	}
	
	void checkTypes(){
		// Nothing to see here
	}
} // class nullReadNode

class printNode extends stmtNode {
	printNode() {
	}

	printNode(exprNode val, printNode pn, int line, int col) {
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
	void Unparse(int indent) {
		Unparse(indent, true);
	}

	// Print like:
	// PRINT (thisExpr, nextExpr, nextExpr, ... , lastExpr);
	void Unparse(int indent, boolean rootCalled) {
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

	void checkTypes() {
		outputValue.checkTypes();
		assertTrue(
				(
					(outputValue.type.val == Types.Boolean || 
						outputValue.type.val == Types.Character || 
						outputValue.type.val == Types.Integer || 
						outputValue.type.val == Types.Real ||
						outputValue.type.val == Types.String) 
					&&
						(outputValue.kind.val == Kinds.Value || 
						outputValue.kind.val == Kinds.Var || 
						outputValue.kind.val == Kinds.ScalarParm)
				)
				||
				(
					(outputValue.kind.val == Kinds.Array || 
						outputValue.kind.val == Kinds.ArrayParm) && 
					outputValue.type.val == Types.Character
				)
				, error() + 
				"The following are valid for printing: INT, BOOL, REAL, " +
				"CHAR, and STRING variables, values, and scalar parameters." +
				" CHAR arrays and array parameters.");
	}
} // class printNode

class nullPrintNode extends printNode {
	nullPrintNode() {
	}

	boolean isNull() {
		return true;
	}

	void Unparse(int indent) {
	}

	void checkTypes() {
		// No type check needed
	}
} // class nullprintNode

class callNode extends stmtNode {
	callNode(identNode id, argsNode a, int line, int col) {
		super(line, col);
		methodName = id;
		args = a;
	}

	private final identNode methodName;
	private final argsNode args;

	// Print like:
	// ##: id(args);
	void Unparse(int indent) {
		methodName.Unparse(0);
		System.out.print(" (");

		args.Unparse(0);

		System.out.print(")");
	}

	void checkTypes() {
		SymbolInfo info;
		// Make sure id is not already declared
		info = (SymbolInfo) st.globalLookup(methodName.idname);
		
		assertTrue(info != null, error() + methodName.idname + " is not declared.");

		if (info != null) {

			// Check the types of each argument, so that they evaluate their own types
			args.checkTypes();

			MethodSymbolInfo methodInfo = (MethodSymbolInfo) info;
			
			boolean argsCorrect = true;
			//The reference to the argsNode we should examine next
			argsNode currentArgsNode = args;

			// Traverse the arguments linked list and verify at each step of the way
			// that the called argument matches the declared method signature
			for (int i = 0; i < methodInfo.Arguments.size(); i++) {

				if (!(currentArgsNode instanceof nullArgsNode)) {

					exprNode calledArg = currentArgsNode.argVal;

					SymbolInfo signatureArg = methodInfo.Arguments.get(i);
					
					// Make sure that the types match exactly and that the called arg is a
					// value, scalar param, or variable. (since we can't return references)
					assertTrue(
						signatureArg.type.val == calledArg.type.val && 
						(calledArg.kind.val == Kinds.ScalarParm || 
							calledArg.kind.val == Kinds.Value || 
							calledArg.kind.val == Kinds.Var),
							error() + "Method call parameters did not match the method signature.");

					// Advance ptr to next element
					currentArgsNode = currentArgsNode.moreArgs;
					
					// If we are at the end of the called arguments list but we're not at
					// the end of the signature arguments list, the # of arguments differed
					if ((currentArgsNode instanceof nullArgsNode) &&
							(i < methodInfo.Arguments.size() - 1)) {
						
						// So fail because of it
						argsCorrect = false;
						System.out.println(error() + "Not enough arguments in the method call.");
						break;
					}
				}
			}
		
			// If we reached the end of the signature arguments and everything was correct
			// but we didn't reach the end of the called arguments, then the # of arguments differed
			if (argsCorrect && 
					!(currentArgsNode instanceof nullArgsNode)) {
				
				// So fail because of it
				System.out.println(error() + "There were too many arguments in the method call.");
			}

			methodName.idinfo = info;

		} else {
			methodName.type = new Types(Types.Error);
		}
	}
} // class callNode

class returnNode extends stmtNode {
	returnNode(exprNode e, int line, int col) {
		super(line, col);
		returnVal = e;
	}

	private final exprNode returnVal;

	// Print like:
	// ##: return expression;
	void Unparse(int indent) {
		System.out.print("return ");

		returnVal.Unparse(0);
	}

	void checkTypes() {
		returnVal.checkTypes();
		
		assertTrue(currentMethod.info.type.val == returnVal.type.val && 
				(returnVal.kind.val == Kinds.ScalarParm || returnVal.kind.val == Kinds.Value || returnVal.kind.val == Kinds.Value), 
				"Return type did not match the method signature return type.");
	}
} // class returnNode

class blockNode extends stmtNode {
	blockNode(fieldDeclsNode f, stmtsNode s, int line, int col, int closingLine) {
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
	void Unparse(int indent) {
		System.out.print(linenum + ":");
		genIndent(indent);
		System.out.println("{");
		
		st.openScope();

		decls.Unparse(indent + 1);
		stmts.Unparse(indent + 1);

		try {
			st.closeScope();
		} catch (EmptySTException e) {
			throw new RuntimeException("Tried to close a scope but no scope was available to close.");
		}

		System.out.print(closingLineNum + ":");
		genIndent(indent);
		System.out.print("}");

	}

	void checkTypes() {
		decls.checkTypes();
		stmts.checkTypes();
	}
} // class blockNode

class breakNode extends stmtNode {
	breakNode(identNode i, int line, int col) {
		super(line, col);
		label = i;
	}

	private final identNode label;

	// Print like:
	// ##: break label;
	void Unparse(int indent) {
		System.out.print("break ");
		label.Unparse(0);
	}

	void checkTypes() {
		label.checkTypes();
		
		SymbolInfo info = (LabelSymbolInfo) st.localLookup(label.idname);

		if (info != null) {
			LabelSymbolInfo labelInfo = (LabelSymbolInfo) info;

			assertTrue(labelInfo.Visible, error() + "Label: " + label.idname + " is no longer visible.");
		}
	}
} // class breakNode

class continueNode extends stmtNode {
	continueNode(identNode i, int line, int col) {
		super(line, col);
		label = i;
	}

	private final identNode label;

	// Print like:
	// ##: continue label;
	void Unparse(int indent) {
		System.out.print("continue ");
		label.Unparse(0);
	}

	void checkTypes() {
		label.checkTypes();
		
		SymbolInfo info = (LabelSymbolInfo) st.localLookup(label.idname);

		if (info != null) {
			LabelSymbolInfo labelInfo = (LabelSymbolInfo) info;

			assertTrue(labelInfo.Visible, error() + "Label: " + label.idname + " is no longer visible.");
		}
	}
} // class continueNode

class argsNode extends ASTNode {
	argsNode() {
	}

	argsNode(exprNode e, argsNode a, int line, int col) {
		super(line, col);
		argVal = e;
		moreArgs = a;
	}

	static nullArgsNode NULL = new nullArgsNode();
	public exprNode argVal;
	public argsNode moreArgs;

	// Print like:
	// thisExpression, nextExpression, ... , lastExpression
	void Unparse(int indent) {
		argVal.Unparse(0);

		// Make sure we're not at the end of the args list
		if (!(moreArgs instanceof nullArgsNode)) {
			System.out.print(", ");
		}

		moreArgs.Unparse(0);
	}

	void checkTypes() {
		argVal.checkTypes();
		moreArgs.checkTypes();
	}
} // class argsNode

class nullArgsNode extends argsNode {
	nullArgsNode() {
		// empty constructor
	}

	boolean isNull() {
		return true;
	}

	void Unparse(int indent) {
	}

	void checkTypes() {
		// No type check needed
	}
} // class nullArgsNode

class strLitNode extends exprNode {
	strLitNode(String stringval, int line, int col) {
		super(line, col);
		strval = stringval;
	}

	private final String strval;

	void Unparse(int indent) {
		System.out.print(escapeCharacters(strval));
	}

	void checkTypes() {
		// string lits are always correct
	}
} // class strLitNode

// abstract superclass; only subclasses are actually created
abstract class exprNode extends ASTNode {
	exprNode() {
		super();
	}

	exprNode(int l, int c) {
		super(l, c);
		type = new Types();
		kind = new Kinds();
	} // exprNode

	exprNode(int l, int c, Types t, Kinds k) {
		super(l, c);
		type = t;
		kind = k;
	} // exprNode

	static nullExprNode NULL = new nullExprNode();
	protected Types type; // Used for typechecking: the type of this node
	protected Kinds kind; // Used for typechecking: the kind of this node
}

class nullExprNode extends exprNode {
	nullExprNode() {
		super();
	}

	boolean isNull() {
		return true;
	}

	void Unparse(int indent) {
	}

	void checkTypes() {
		// No type check needed
	}
} // class nullExprNode

class binaryOpNode extends exprNode {
	binaryOpNode(exprNode e1, int op, exprNode e2, int line, int col) {
		super(line, col);
		operatorCode = op;
		leftOperand = e1;
		rightOperand = e2;
	}

	static String getOpString(int op) {
		switch (op) {
		case sym.PLUS:
			return "+";
		case sym.MINUS:
			return "-";
		case sym.TIMES:
			return "*";
		case sym.SLASH:
			return "/";
		case sym.LT:
			return "<";
		case sym.GT:
			return ">";
		case sym.GEQ:
			return ">=";
		case sym.LEQ:
			return "<=";
		case sym.COR:
			return "||";
		case sym.CAND:
			return "&&";

		default:
			throw new Error("printOp: case not found");
		}
	}

	void Unparse(int indent) {
		System.out.print("(");
		leftOperand.Unparse(0);
		System.out.print(" " + getOpString(operatorCode) + " ");
		rightOperand.Unparse(0);
		System.out.print(")");
	}

	void checkTypes() {
		leftOperand.checkTypes();
		rightOperand.checkTypes();

		int returnType = Types.Error;

		switch (operatorCode) {
		case sym.PLUS:
		case sym.MINUS:
		case sym.TIMES:
		case sym.SLASH:
		case sym.LT:
		case sym.GT:
		case sym.LEQ:
		case sym.GEQ:
		case sym.EQ:
		case sym.NOTEQ:
			returnType = assertArithmeticCompatible(leftOperand.kind.val, leftOperand.type.val, rightOperand.kind.val,
					rightOperand.type.val,
					error() + "Left and right operand are not compatible for operator: " + getOpString(operatorCode));
			
			if(returnType != Types.Unknown && returnType != Types.Error)
			{
				switch (operatorCode) {
					case sym.LT:
					case sym.GT:
					case sym.LEQ:
					case sym.GEQ:
					case sym.EQ:
					case sym.NOTEQ:
						returnType = Types.Boolean;
					break;
				}
			}
			break;
		case sym.CAND:
		case sym.COR:
			returnType = assertBooleanCompatible(
					leftOperand.kind.val,
					leftOperand.type.val,
					rightOperand.kind.val,
					rightOperand.type.val,
					error() + 
						"Left and right operands of the operator: " + 
						getOpString(operatorCode) + 
						" requires both sides to be of type boolean.");
			break;
		}

		kind = new Kinds(Kinds.Value);
		type = new Types(returnType);
	} // checkTypes

	private final exprNode leftOperand;
	private final exprNode rightOperand;
	private final int operatorCode; // Token code of the operator
} // class binaryOpNode

class unaryOpNode extends exprNode {
	unaryOpNode(int op, exprNode e, int line, int col) {
		super(line, col);
		operand = e;
		operatorCode = op;
	}

	void Unparse(int indent) {
		if (operatorCode == sym.NOT) {
			System.out.print("!");
		}
		operand.Unparse(0);
	}

	void checkTypes() {
		operand.checkTypes();

		if (operatorCode == sym.NOT) {
			assertTrue(operand.type.val == Types.Boolean && 
					(operand.kind.val == Kinds.ScalarParm || operand.kind.val == Kinds.Value || operand.kind.val == Kinds.Var)
					, error() + "NOT operator requires a boolean operand.");
			
			kind = new Kinds(Kinds.Value);
			type = new Types(Types.Boolean);
			
		} else if (operatorCode == sym.IDENTIFIER) {
			// it's used to hold an id
			
			kind = operand.kind;
			type = operand.type;
		} else {
			// invalid unary operator
			throw new UnsupportedOperationException(
					"! is the only unary operator" + "code of operator is : " + operatorCode);
		}
	}

	private final exprNode operand;
	private final int operatorCode; // Token code of the operator
} // class unaryOpNode

class castNode extends exprNode {
	castNode(typeNode t, exprNode e, int line, int col) {
		super(line, col);
		operand = e;
		resultType = t;
	}

	void Unparse(int indent) {
		System.out.println("(");
		resultType.Unparse(0);
		System.out.println(")");

		operand.Unparse(0);
	}

	void checkTypes() {
		// the project doc says :: Any expression (including variables,
		// constants and literals)
		// of type int, char or bool may be type-cast to an int, char, float or
		// bool value.
		// These are the only type casts allowed.
		assertTrue(resultType.type.val == Types.Real || 
				resultType.type.val == Types.Character || 
				resultType.type.val == Types.Integer || 
				resultType.type.val == Types.Boolean, 
				error() + "Only valid casts are to FLOAT, CHAR, INT, and BOOL.");

		assertTrue(operand.type.val == Types.Character || 
				operand.type.val == Types.Integer || 
				operand.type.val == Types.Boolean, 
				error() + "Only source types to cast from are CHAR, INT, and BOOL.");

		operand.checkTypes();
		
		type = resultType.type;
		kind = new Kinds(Kinds.Value);
	}

	private final exprNode operand;
	private final typeNode resultType;
} // class castNode

class fctCallNode extends exprNode {
	fctCallNode(identNode id, argsNode a, int line, int col) {
		super(line, col);
		methodName = id;
		methodArgs = a;
	}

	void Unparse(int indent) {
		methodName.Unparse(indent);
		System.out.print('(');
		methodArgs.Unparse(0);
		System.out.print(")");
	}

	void checkTypes() {
		SymbolInfo info;
		// Make sure id is not already declared
		info = (SymbolInfo) st.globalLookup(methodName.idname);
		
		//Make sure that the name of the method is declared already
		assertTrue(info != null, error() + methodName.idname + " is not declared.");

		if (info != null) {

			// Check the types of each argument, so that they evaluate their own types
			methodArgs.checkTypes();

			MethodSymbolInfo methodInfo = (MethodSymbolInfo) info;
			
			boolean argsCorrect = true;
			//The reference to the argsNode we should examine next
			argsNode currentArgsNode = methodArgs;

			// Traverse the arguments linked list and verify at each step of the way
			// that the called argument matches the declared method signature
			for (int i = 0; i < methodInfo.Arguments.size(); i++) {

				if (!(currentArgsNode instanceof nullArgsNode)) {

					exprNode calledArg = currentArgsNode.argVal;

					SymbolInfo signatureArg = methodInfo.Arguments.get(i);
					
					// Make sure that the types match exactly and that the called arg is a
					// value, scalar param, or variable. (since we can't return references)
					assertTrue(
						signatureArg.type.val == calledArg.type.val && 
						(calledArg.kind.val == Kinds.ScalarParm || 
							calledArg.kind.val == Kinds.Value || 
							calledArg.kind.val == Kinds.Var),
							error() + "Function call parameters did not match the method signature.");

					// Advance ptr to next element
					currentArgsNode = currentArgsNode.moreArgs;
					
					// If we are at the end of the called arguments list but we're not at
					// the end of the signature arguments list, the # of arguments differed
					if ((currentArgsNode instanceof nullArgsNode) &&
							(i < methodInfo.Arguments.size() - 1)) {
						
						// So fail because of it
						argsCorrect = false;
						System.out.println(error() + "Not enough arguments in the function call.");
						break;
					}
				}
			}
			
			// If we reached the end of the signature arguments and everything was correct
			// but we didn't reach the end of the called arguments, then the # of arguments differed
			if (argsCorrect && 
					!(currentArgsNode instanceof nullArgsNode)) {
				
				// So fail because of it
				System.out.println(error() + "There were too many arguments in the function call.");
			}

			methodName.idinfo = info;
			
			type = info.type;
			kind = new Kinds(Kinds.Value);

		} else {
			methodName.type = new Types(Types.Error);
		}
	}

	private final identNode methodName;
	private final argsNode methodArgs;
} // class fctCallNode

class identNode extends exprNode {
	identNode(String identname, int line, int col) {
		super(line, col, new Types(Types.Unknown), new Kinds(Kinds.Var));
		idname = identname;
		nullFlag = false;
	}

	identNode(boolean flag) {
		super(0, 0, new Types(Types.Unknown), new Kinds(Kinds.Var));
		idname = "";
		nullFlag = flag;
	} // identNode

	void Unparse(int indent) {
		System.out.print(idname);
	}

	void checkTypes() {
		// identNode is always type correct
	}

	public String idname;
	public SymbolInfo idinfo; // symbol table entry for this ident
	private final boolean nullFlag;
} // class identNode

class nameNode extends exprNode {
	nameNode(identNode id, exprNode expr, int line, int col) {
		super(line, col);
		varName = id;
		indexExpr = expr;
	}

	void Unparse(int indent) {
		varName.Unparse(indent);
	}

	void checkTypes() {
		
		varName.checkTypes();
		indexExpr.checkTypes();

		SymbolInfo info = (SymbolInfo)st.globalLookup(varName.idname);
		
		// Make sure the id is defined, a nameNode means it's referencing the id
		assertTrue(info != null,
				error() + "ID " + varName.idname + " was referenced but was not yet declared.");
		
		if(info != null)
		{
			// If the name is being indexed as in id[#], then the kind should not be
			// the kind of the name, it should be variable since each element of an
			// array is a variable in this language
			if(isIndexed())
			{
				kind = new Kinds(Kinds.Var);
			}
			else
			{
				// Otherwise, just set it to the kind that we got from the symbol table
				kind = info.kind;
			}
			
			// Type is always the type we got from the symbol table
			type = info.type;
			
			// Shortcut to the symbol info
			varName.idinfo = info;
		}
	}

	boolean isIndexed() {
		return !(indexExpr instanceof nullExprNode);
	}

	public final identNode varName;
	private final exprNode indexExpr;
} // class nameNode

class intLitNode extends exprNode {
	intLitNode(int val, int line, int col) {
		super(line, col, new Types(Types.Integer), new Kinds(Kinds.Value));
		intval = val;
	}

	void Unparse(int indent) {
		System.out.print(intval);
	}

	void checkTypes() {
		// All int lits are automatically type-correct
	}

	public final int intval;
} // class intLitNode

class floatLitNode extends exprNode {
	floatLitNode(float val, int line, int col) {
		super(line, col, new Types(Types.Real), new Kinds(Kinds.Value));
		floatval = val;
	}

	void Unparse(int indent) {
		System.out.print(floatval);
	}

	void checkTypes() {
		// All float lits are automatically type-correct
	}

	private final float floatval;
} // class floatLitNode

class charLitNode extends exprNode {
	charLitNode(char val, int line, int col) {
		super(line, col, new Types(Types.Character), new Kinds(Kinds.Value));
		charval = val;
	}

	void Unparse(int indent) {
		String tmp = escapeCharacters(charval);
		System.out.print(tmp);
	}

	void checkTypes() {
		// All char lits are automatically type-correct
	}

	private final char charval;
} // class charLitNode

class trueNode extends exprNode {
	trueNode(int line, int col) {
		super(line, col, new Types(Types.Boolean), new Kinds(Kinds.Value));
	}

	void Unparse(int indent) {
		System.out.print("True");
	}

	void checkTypes() {
		// All true lits are automatically type-correct
	}
} // class trueNode

class falseNode extends exprNode {
	falseNode(int line, int col) {
		super(line, col, new Types(Types.Boolean), new Kinds(Kinds.Value));
	}

	void Unparse(int indent) {
		System.out.print("False");
	}

	void checkTypes() {
		// False literals are always correct
	}
} // class falseNode

class preIncrStmtNode extends stmtNode {
	preIncrStmtNode(nameNode id, int line, int col) {
		super(line, col);

		targetID = id;
	}

	void Unparse(int indent) {
		genIndent(indent);
		System.out.print("++");
		targetID.Unparse(indent);
	}

	void checkTypes() {
		targetID.checkTypes();

		//Make sure that the id is defined
		SymbolInfo info = (SymbolInfo) st.globalLookup(targetID.varName.idname);

		if (info != null) {
			//If so, make sure that it's an integer that is either a scalar param or variable
			assertTrue(
				info.type.val == Types.Integer &&
					(info.kind.val == Kinds.ScalarParm || info.kind.val == Kinds.Var), 
				error() + "Increment statements can only be applied to parameter or variable integers");
		}
	}

	private nameNode targetID;
} // class preIncrStmtNode

class postIncrStmtNode extends stmtNode {
	postIncrStmtNode(nameNode id, int line, int col) {
		super(line, col);

		targetID = id;
	}

	void Unparse(int indent) {
		targetID.Unparse(indent);
		System.out.print("++");
	}

	void checkTypes() {
		targetID.checkTypes();

		//Make sure that the id is defined
		SymbolInfo info = (SymbolInfo) st.globalLookup(targetID.varName.idname);

		if (info != null) {
			//If so, make sure that it's an integer that is either a scalar param or variable
			assertTrue(
				info.type.val == Types.Integer &&
					(info.kind.val == Kinds.ScalarParm || info.kind.val == Kinds.Var), 
				error() + "Increment statements can only be applied to parameter or variable integers");
		}
	}

	private nameNode targetID;
} // class postIncrStmtNode

class preDecStmtNode extends stmtNode {
	preDecStmtNode(nameNode id, int line, int col) {
		super(line, col);

		targetID = id;
	}

	void Unparse(int indent) {
		System.out.print("--");
		targetID.Unparse(indent);
	}

	void checkTypes() {
		targetID.checkTypes();
		
		//Make sure that the id is defined
		SymbolInfo info = (SymbolInfo) st.globalLookup(targetID.varName.idname);

		if (info != null) {
			//If so, make sure that it's an integer that is either a scalar param or variable
			assertTrue(
				info.type.val == Types.Integer &&
					(info.kind.val == Kinds.ScalarParm || info.kind.val == Kinds.Var), 
				error() + "Decrement statements can only be applied to parameter or variable integers");
		}
	}

	private nameNode targetID;
} // class preDecStmtNode

class postDecStmtNode extends stmtNode {
	postDecStmtNode(nameNode id, int line, int col) {
		super(line, col);

		targetID = id;
	}

	void Unparse(int indent) {
		targetID.Unparse(indent);
		System.out.print("--");
	}

	void checkTypes() {
		targetID.checkTypes();

		//Make sure that the id is defined
		SymbolInfo info = (SymbolInfo) st.globalLookup(targetID.varName.idname);

		if (info != null) {
			//If so, make sure that it's an integer that is either a scalar param or variable
			assertTrue(
				info.type.val == Types.Integer &&
					(info.kind.val == Kinds.ScalarParm || info.kind.val == Kinds.Var), 
				error() + "Decrement statements can only be applied to parameter or variable integers");
		}
	}

	private nameNode targetID;
} // class postDecStmtNode