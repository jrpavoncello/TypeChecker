class testForLoop {

	int i = 0;
	int j = 1;
	
	char name = 'c';
	char newName = 'k';
	
	float f = 0.0;
	bool b = true;

	void foo(){
		
	}
	void main( )
	{
		//correct 
		for(i = 0; i<10; i++){
			for( j = 12; j>0; j--){
			
			}
		}
		for( f = 10.45; f>(float)100; f = f + 19.0 - 9.0){	
		}
		for( b = true; b; b = false){	
		}
		for( name = 'c'; name<'L'; name = 'j'){	
		}
		
	}
}
