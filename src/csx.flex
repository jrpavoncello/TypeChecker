import java_cup.runtime.*;

//Returned when reserved words, operators, or anything that we only care about what they are with no context are encountered
class CSXToken
{
	int linenum;
	int colnum;

	CSXToken()
	{

	}

	CSXToken(int line,int col)
	{
		linenum = line;
		colnum = col;
	}

	CSXToken(Position p)
	{
		linenum = p.linenum;
		colnum = p.colnum;

	}

}

//Returned when integer literals are encountered so that we can display the intValue in P2
class CSXIntLitToken extends CSXToken
{
	int intValue;
	CSXIntLitToken(int val, Position p)
	{
		super(p);
		intValue=val;
	}
}

//Returned when float literals are encountered so that we can display the floatValue in P2
class CSXFloatLitToken extends CSXToken
{
	float floatValue;
	CSXFloatLitToken(float floatValue, Position p)
	{
		super(p);
		this.floatValue = floatValue;
	}
}

//Returned when indentifier tokens are encountered so that we can display the name of the identifier in P2
class CSXIdentifierToken extends CSXToken
{
	String identifierValue;
	CSXIdentifierToken(String identifierValue, Position p)
	{
		super(p);
		this.identifierValue = identifierValue;
	}

}

//Returned when character literal are encountered so that we can display the charValue in P2
//This may look odd when displayed in the case of escaped characters, as we display them as is
class CSXCharLitToken extends CSXToken
{
	char charValue;
	CSXCharLitToken(char charValue, Position p)
	{
		super(p);
		this.charValue = charValue;
	}
}

//Returned when string literal are encountered so that we can display the stringValue in P2
class CSXStringLitToken extends CSXToken
{
	String stringValue;
	CSXStringLitToken(String stringValue, Position p)
	{
		super(p);
		this.stringValue = stringValue;
	}
}

//Returned when any errors are encountered so that we can display the error message in P2
class CSXErrorToken extends CSXToken
{
	String error;
	CSXErrorToken(String error, Position p)
	{
		super(p);
		this.error = error;
	}
}

// This class is used to track line and column numbers
// Feel free to change to extend it
class Position
{
	int  linenum; 			/* maintain this as line number current token was scanned on */
	int  colnum; 			/* maintain this as column number current token began at */
	int  line; 				/* maintain this as line number after scanning current token  */
	int  col; 				/* maintain this as column number after scanning current token  */
	Position()
	{
		linenum = 1;
		colnum = 1;
		line = 1;
		col = 1;
	}
	void setpos()
	{ // set starting position for current token
		linenum = line;
		colnum = col;
	}
} ;


%%

BLOCKCOMMENT = ##((#[^#])|[^#])*#?##
SINGLELINECOMMENT = [/][/].*([\n\r]|[\n])?

DIGIT=[0-9]
STRLIT = \"((\\[\\\"rnt])|[\040!#-\[\]-~])*\"
RUNSTRLIT = \"((\\[\\\"rnt]*)|[\040!#-\[\]-~])*

IDENTIFIER = (([a-zA-Z][_0-9]?)+([a-zA-Z]?[_0-9])*)+
ILLEGALIDENTIFIER = ((_([0-9]*[a-zA-Z]+[_0-9]?)+[_0-9]*)|([0-9]+[a-zA-Z]+[_0-9]?)+[_0-9]*)


FLOAT = [fF][lL][oO][aA][tT]
WHILE = [wW][hH][iI][lL][eE]
BOOL = [bB][oO][oO][lL]
CONTINUE = [cC][oO][nN][tT][iI][nN][uU][eE]
FALSE = [fF][aA][lL][sS][eE]
TRUE = [tT][rR][uU][eE]
VOID = [vV][oO][iI][dD]
PRINT = [pP][rR][iI][nN][tT]
IF = [iI][fF]
ENDIF = [eE][nN][dD][iI][fF]
FOR =  [Ff][Oo][Rr]

BREAK = [Bb][Rr][Ee][Aa][Kk]
CHAR = [Cc][Hh][Aa][Rr]
NEWLINE = \n|(\r\n)

CHARLIT = ['](([\\][ntr\'\"\\])|[\040-&(-\[\]-~])[']
RUNCHARLIT = ['](([\\][ntr\'\"\\])|[\040-&(-\[\]-~])*
RETURN = [Rr][Ee][Tt][Uu][Rr][Nn]
CLASS = [Cc][Ll][Aa][Ss][Ss]
INT = [Ii][Nn][Tt]
READ = [Rr][Ee][Aa][Dd]
ELSE = [Ee][Ll][Ss][Ee]
CONST = [Cc][Oo][Nn][Ss][Tt]

RESERVED_WORD = {FLOAT}|{WHILE}|{BOOL}|{CONTINUE}|{FALSE}|{TRUE}|{VOID}|{PRINT}|{BREAK}|{CHAR}|{CLASS}|{RETURN}|{INT}|{READ}|{ELSE}|{CONST}|{IF}|{ENDIF}|{FOR}

%states FoundIdentifier
%xstates FoundIdentifierMatch

%type Symbol

%eofval{
	//When jFlex see the EOF token, return back an EOF token so we can stop scanning
	return new Symbol(sym.EOF, new CSXToken(0,0));
%eofval}

%{
Position Pos = new Position();
%}

%%
/***********************************************************************
Tokens for the CSX language are defined here using regular expressions
************************************************************************/

"||"
{
	// Match || (boolean or) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.COR,
			new CSXToken(Pos));
}

"&&"
{
	// Match && (boolean and) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.CAND,
			new CSXToken(Pos));
}

"=="
{
	// Match == (equality comparison) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.EQ,
			new CSXToken(Pos));
}

"<="
{
	// Match <= (less than or equal to) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.LEQ, new CSXToken(Pos));
}

">="
{
	// Match >= (greater than or equal to) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.GEQ, new CSXToken(Pos));
}

"!="
{
	// Match != (not equal) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.NOTEQ, new CSXToken(Pos));
}

<FoundIdentifier> "++"
{
	//If we find an identifier without a paired increment or decrement, go into the FoundIdentifier state
	//so that we know we found an identifier that is eligible for a pair if one of those operators is scanned next
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.INC, new CSXToken(Pos));
}

"++" / {RESERVED_WORD}
{
	//If we find an increment, and we look ahead and find a reserved word, return an error token so that we don't mistake
	//the reserved word as an identifier in the rule below
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.error,
			new CSXErrorToken("Found reserved word after \"++\" operator without a matching identifier", Pos));
}

"++" / {IDENTIFIER}
{
	//If we find an increment, and we lookahead and find an identifier, go into an exclusive state to indicate that we found 
	//an identifier ahead and we don't want to match the regular identifier rule and be put into "FoundIdentifier" state
	yybegin(FoundIdentifierMatch);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.INC, new CSXToken(Pos));
}

"++"
{
	//If we find an increment any states to differentiate its context, such as a look ahead or that we
	//found an identifier before it, then fail because it doesn't have a matching identifier
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.error,
			new CSXErrorToken("Could not find matching identifier for \"++\" operator", Pos));
}

<FoundIdentifier> "--"
{
	// Duplicate approach as ++, see ++ for details
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.DEC, new CSXToken(Pos));
}

"--" / {RESERVED_WORD}
{
	// Duplicate approach as ++, see ++ for details
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.error,
			new CSXErrorToken("Found reserved word after \"--\" operator without a matching identifier", Pos));
}

"--" / {IDENTIFIER}
{
	// Duplicate approach as ++, see ++ for details
	yybegin(FoundIdentifierMatch);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.DEC, new CSXToken(Pos));
}

"--"
{
	// Duplicate approach as ++, see ++ for details
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.error,
			new CSXErrorToken("Could not find matching identifier for \"--\" operator", Pos));
}

">"
{
	// Match on > (greater than) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.GT, new CSXToken(Pos));
}

"<"
{
	// Match on < (less than) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.LT, new CSXToken(Pos));
}

"*"
{
	// Match on * (multiplication) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.TIMES, new CSXToken(Pos));
}

"="
{
	// Match on = (assignment) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.ASG, new CSXToken(Pos));
}

"+"
{
	// Match on + (addition) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.PLUS, new CSXToken(Pos));
}

"-"
{
	// Match on - (subtraction) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.MINUS, new CSXToken(Pos));
}

"!"
{
	// Match on ! (boolean not) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.NOT, new CSXToken(Pos));
}

";"
{
	// Match on ; (semicolon/statement termination)
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.SEMI, new CSXToken(Pos));
}

":"
{
	// Match on : (colon/seperator)
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.COLON, new CSXToken(Pos));
}

","
{
	// Match on , (comma/seperator)
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.COMMA, new CSXToken(Pos));
}

"{"
{
	// Match on { (begin scope) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.LBRACE, new CSXToken(Pos));
}

"}"
{
	// Match on } (close scope) operator
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.RBRACE, new CSXToken(Pos));
}

"["
{
	// Match on [ (open bracket)
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.LBRACKET, new CSXToken(Pos));
}

"]"
{
	// Match on ] (closed bracket)
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.RBRACKET, new CSXToken(Pos));
}

"/"
{
	// Match on / (slash)
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.SLASH, new CSXToken(Pos));
}

"("
{
	// Match on ( (open parentheses)
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.LPAREN, new CSXToken(Pos));
}

")"
{
	// Match on ) (close parentheses)
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.RPAREN, new CSXToken(Pos));
}

{SINGLELINECOMMENT}
{
	// Match on a single line comment, does not include the newline so no need to increment the row
	yybegin(YYINITIAL);
	String comment = yytext();
    //System.out.println("Line Comment: " + comment);
    Pos.setpos();
    Pos.col += comment.length();
    Pos.line ++;
}

{BLOCKCOMMENT}
{
	// Match on a block comment that can span multiple lines or just a single line
	yybegin(YYINITIAL);
	Pos.setpos();
	
	// Parse all the scanned text for any newline characters, if found increment the line and reset the column
	// If we don't find one, just increment the column
    int rowsSkipped = 0;
    String parseString = yytext();
	for(int i = 0; i < parseString.length(); i++)
	{
		Pos.col++;
		if(parseString.charAt(i) == '\n')
		{
			Pos.col=1;
			Pos.line++;
		}
	}
	
	//Print out the comment for debug purposes
    //System.out.println("Block Comment: " + parseString);
}

{CHARLIT}
{
	// Match any character literal
	yybegin(YYINITIAL);
	Pos.setpos();
	String charString = yytext();
	Pos.col += charString.length();

	//Find any escaped characters and parse them
	char parsedChar;
	switch(charString)
	{
		case "'\\n'":
			parsedChar = '\n';
			break;
		case "'\\r'":
			parsedChar = '\r';
			break;
		case "'\\t'":
			parsedChar = '\t';
			break;
		case "'\\\\'":
			parsedChar = '\\';
			break;
		case "'\\''":
			parsedChar = '\'';
			break;
		default:
			//If not an escaped character, just return the one character between the single quotes
			parsedChar = charString.charAt(1);
			break;
	}
	
	return new Symbol(sym.CHARLIT,
			new CSXCharLitToken(parsedChar, Pos));
}

{RUNCHARLIT}
{
	// Match any runaway character literals
	yybegin(YYINITIAL);
	Pos.setpos();
	
	//Get text, increase column to length of text
	//Rule does not include newline, so no line increment
	String parsed = yytext();
	Pos.col += parsed.length();
	return new Symbol(sym.error,
			new CSXErrorToken("Runaway character found: " + parsed, Pos));
}

([~]?{DIGIT}+\.{DIGIT}*)|([~]?{DIGIT}*\.{DIGIT}+)
{
	yybegin(YYINITIAL);
	Pos.setpos();
	String parsedString = yytext();
	Pos.col += parsedString.length();
	
	//If a tilde was found in parse string, this is supposed to be a negative number.
	//Replace the tilde with a java recognizable symbol for negation
	parsedString = parsedString.replace('~', '-');
	
	//Float.Parse() will return back negative or positive infinity on overflow
    float parsedFloat = Float.parseFloat(parsedString);
    if(parsedFloat == Float.NEGATIVE_INFINITY || parsedFloat == Float.POSITIVE_INFINITY)
    {
		//Print error, but return Float.MAX_VALUE
		System.out.println("Float Overflow Error");

		return new Symbol(sym.FLOATLIT,
				new CSXFloatLitToken(Float.MAX_VALUE, Pos));
    }
    else
    {
    	//No overflow? Return a FloatLitToken with the parsed float as the value
		return new Symbol(sym.FLOATLIT,
			new CSXFloatLitToken(parsedFloat, Pos));
	}
}

[~]?{DIGIT}+
{
	yybegin(YYINITIAL);
	Pos.setpos();
	
	String parsedString = yytext();
	Pos.col += parsedString.length();
	
	//Same idea as float, replace tilde with java recognizable negation symbol
	parsedString = parsedString.replace('~', '-');

	try{
		//If Integer.parseInt() throws a number format exception, because our rule only finds
		//a valid integer format, the exception must be due to an overflow error.
		//If not, return an INTLITToken with that integer as the value
		return new Symbol(sym.INTLIT,
				new CSXIntLitToken(Integer.parseInt(parsedString), Pos));

	} catch (NumberFormatException e) {
		
		//Print the error and return an IntLitToken with Integer.MAX_VALUE as the value
		System.out.println("Overflow Error");
		System.out.println(e.getMessage());

		return new Symbol(sym.INTLIT,
				new CSXIntLitToken(Integer.MAX_VALUE, Pos));
	}
}

{STRLIT}
{
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();

	return new Symbol(sym.STRLIT,
			new CSXStringLitToken(yytext(), Pos));
}

{RUNSTRLIT}
{
	yybegin(YYINITIAL);
	Pos.setpos();
	
	//Any runaway strings will not contain the newline, so no need to increment line.
	String parsed = yytext();
	Pos.col += parsed.length();
	return new Symbol(sym.error,
			new CSXErrorToken("Runaway string found: " + parsed, Pos));
}

" "
{
	//Space character
	Pos.setpos();
	yybegin(YYINITIAL);
	Pos.col += 1;
}

\t
{
	//Tab non-printable character
	Pos.setpos();
    yybegin(YYINITIAL);
    Pos.col += 1;

}
{NEWLINE}
{
	//Newline non-printable character including \n and \r\n
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.line += 1;
	Pos.col = 1;
}

{BREAK}
{
	//Reserved word BREAK, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_BREAK,
			new CSXToken(Pos));
}

{CHAR}
{
	//Reserved word CHAR, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_CHAR,
			new CSXToken(Pos));
}


{RETURN}
{
	//Reserved word RETURN, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_RETURN,
			new CSXToken(Pos));
}


{CLASS}
{
	//Reserved word CLASS, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_CLASS,
			new CSXToken(Pos));
}


{INT}
{
	//Reserved word INT, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_INT,
			new CSXToken(Pos));
}

{READ}
{
	//Reserved word READ, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_READ,
			new CSXToken(Pos));
}

{ELSE}
{
	//Reserved word ELSE, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_ELSE,
			new CSXToken(Pos));
}

{CONST}
{
	//Reserved word CONST, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_CONST,
			new CSXToken(Pos));
}


{FLOAT}
{
	//Reserved word FLOAT, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_FLOAT,
			new CSXToken(Pos));
}

{WHILE}
{
	//Reserved word WHILE, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_WHILE,
			new CSXToken(Pos));
}

{BOOL}
{
	//Reserved word BOOL, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_BOOL,
			new CSXToken(Pos));
}

{CONTINUE}
{
	//Reserved word CONTINUE, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_CONTINUE,
			new CSXToken(Pos));
}

{FALSE}
{
	//Reserved word FALSE, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_FALSE,
			new CSXToken(Pos));
}

{TRUE}
{
	//Reserved word TRUE, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_TRUE,
			new CSXToken(Pos));
}

{VOID}
{
	//Reserved word VOID, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_VOID,
			new CSXToken(Pos));
}

{PRINT}
{
	//Reserved word PRINT, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_PRINT,
			new CSXToken(Pos));
}

{IF}
{
	//Reserved word IF, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_IF,
			new CSXToken(Pos));
}

{ENDIF}
{
	//Reserved word ENDIF, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_ENDIF,
			new CSXToken(Pos));
}

{FOR}
{
	//Reserved word FOR, case insensitive
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.rw_FOR,
			new CSXToken(Pos));
}

<FoundIdentifierMatch> {IDENTIFIER}
{
	//If we found an identifier match already 
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();

	return new Symbol(sym.IDENTIFIER,
			new CSXIdentifierToken(yytext(), Pos));
}

{IDENTIFIER}
{
	//When we encounter an identifier with this rule, it means we didn't find any increment or decrement
	//operators before it, so we will enter a state so that we don't error if one follows immediately after
	yybegin(FoundIdentifier);
	Pos.setpos();
	Pos.col += yytext().length();

	return new Symbol(sym.IDENTIFIER,
			new CSXIdentifierToken(yytext(), Pos));
}

{ILLEGALIDENTIFIER}
{
	//When an identifier is not caught by the identifier rule above, but it is by this rule, then it must be close
	//to an identifier, but start with a number or underscore
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.error,
	new CSXErrorToken("Found invalid Identifier: " + yytext(), Pos));
}

[^\Z]
{
	//Catch anything not caught by any rules above except for EOF and return an error
	yybegin(YYINITIAL);
	Pos.setpos();
	Pos.col += yytext().length();
	return new Symbol(sym.error,
			new CSXErrorToken("Found invalid token: " + yytext(), Pos));
}
