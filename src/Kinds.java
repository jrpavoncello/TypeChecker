class Kinds
{
	public static final int Var = 0;
	public static final int Value = 1;
	public static final int ScalarParm = 2;
	public static final int Array = 3;
	public static final int ArrayParm = 4;
	public static final int Method = 5;
	public static final int Label = 6;
	public static final int Unknown = 7;

	Kinds(int i)
	{
		val = i;
	}

	Kinds()
	{
		val = Unknown;
	}

	public String toString()
	{
		switch (val)
		{
			case 0:
				return "Var";
			case 1:
				return "Value";
			case 2:
				return "ScalarParm";
			case 3:
				return "Array";
			case 4:
				return "ArrayParm";
			case 5:
				return "Method";
			case 6:
				return "Label";
			case 7:
				return "Unknown";
			default:
				throw new RuntimeException();
		}
	}

	int val;
}
