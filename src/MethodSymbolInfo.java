import java.util.ArrayList;
import java.util.List;

public class MethodSymbolInfo extends SymbolInfo
{
	public Types ReturnType;
	public List<SymbolInfo> Arguments;
	
	public MethodSymbolInfo(String id, Types t)
	{
		super(id, new Kinds(Kinds.Method), t, false);
		
		Arguments = new ArrayList<SymbolInfo>();
	}

	public MethodSymbolInfo(String id, int t)
	{
		super(id, Kinds.Method, t, false);
		
		Arguments = new ArrayList<SymbolInfo>();
	}
}
