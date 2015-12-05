class testFunctionCall {

	int i = 0;
	int j [10];
	
	char name = 'c';
	char newName [10];
	
	float f = 0.0;
	bool b [10] ;
	
	int foo(int f, float k, char n){
		return foo(1,2.3,name);
	}
	float bar(bool con, char ar){
		return bar(con, name);
	}
	void main( )
	{
		foo(i, 0.0, 'K');
		bar(true, 'c');

		foo(0.0, 1, 'K');
		bar(true);


	}

}
