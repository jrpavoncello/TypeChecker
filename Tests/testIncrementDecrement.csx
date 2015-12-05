class testIncrementDecrement {

	int i = 0;
	int j = 1;
	
	char name = 'c';
	char newName = 'k';
	
	float f = 0.0;
	bool b = true;
	
	void main( )
	{
	// correct
		i++;
		++i;
		i--;
		--i;
	
	// incorrect
		f++;
		++name;
		b--;

	}

}
