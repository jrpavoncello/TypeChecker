class testReadPrint {

	int i = 0;
	int j = 1;
	
	char name = 'c';
	char newName = 'k';
	
	float f = 0.0;
	bool b = true;
	
	
	void main( )
	{
		read(i);
		read(i, j);
		read(i, name, j, newName);
		
		read(name, f);
		read(b, f);
		read(newName);
			
		print(i);
		print(i, j);
		print(i, name, j, newName);
		
		print(name, f);
		print(b, f);
		print(newName);
			
	}

}
