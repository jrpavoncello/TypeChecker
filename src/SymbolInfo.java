/**************************************************
 * class used to hold information associated w/ Symbs (which are stored in
 * SymbolTables)
 * 
 ****************************************************/

class SymbolInfo extends Symb
{
	public boolean constant;
	public Kinds kind;
	public Types type;

	public SymbolInfo(String id, Kinds k, Types t, boolean constant)
	{
		super(id);
		kind = k;
		type = t;
		this.constant = constant;
	}

	public SymbolInfo(String id, int k, int t, boolean constant)
	{
		super(id);
		kind = new Kinds(k);
		type = new Types(t);
		this.constant = constant;
	}

	public String toString()
	{
		return "(" + name() + ": kind=" + kind + ", type=" + type + ")";
	}
}
