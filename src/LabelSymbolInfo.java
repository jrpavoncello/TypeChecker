
public class LabelSymbolInfo extends SymbolInfo
{
	public boolean Visible;
	
	public LabelSymbolInfo(String id, Kinds k, Types t, boolean visible)
	{
		super(id, k, t, false);
		
		Visible = visible;
	}

	public LabelSymbolInfo(String id, int k, int t, boolean visible)
	{
		super(id, k, t, false);
		
		Visible = visible;
	}
}
