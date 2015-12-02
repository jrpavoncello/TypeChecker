public class SizedSymbolInfo extends SymbolInfo
{
	public int Size;
	
	public SizedSymbolInfo(String id, Kinds k, Types t, int size, boolean constant)
	{
		super(id, k, t, constant);
		
		Size = size;
	}

	public SizedSymbolInfo(String id, int k, int t, int size, boolean constant)
	{
		super(id, k, t, constant);
		
		Size = size;
	}
}
