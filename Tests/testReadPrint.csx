class testReadPrint {

	int i = 0;
	int j = 1;
	
	char name = 'c';
	char newName = 'k';
	
	float f = 0.0;
	bool b = true;

	void main( )
	{
		// correct
		read (i);
		read (i, j);
		read (i, name, j, newName);
		// incorrect
		read (name, f);
		read (b, f);
		read (newName);
		
		//correct	
		print (i);
		print (i, j);
		print (i, name, j, newName);
		
		// correct
		print (name, f);
		print (b, f);
		print (newName);
		
		print(foo(i));
		
		print(bar());
	}
	
	

}
