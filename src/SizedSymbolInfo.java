public class SizedSymbolInfo extends SymbolInfo
{
	public int Size;
	
	public SizedSymbolInfo(String id, Kinds k, Types t, int size)
	{
		super(id, k, t);
		
		Size = size;
	}

	public SizedSymbolInfo(String id, int k, int t, int size)
	{
		super(id, k, t);
		
		Size = size;
	}
}
