class test {
//test prog for scanner & parser --
//may not type-check or execute correctly 

   int i = 0;
   
   void f( ) {
       i=i+1;
   };


   int funcshaun( ) {
   int j;
   int k;
   float notoptional;
   bool loop = false;
   for( j = 9; j>0; j--){
       // some junk and stuff
       notoptional=(float)0;
       
   }

 for(k = 0; k >= 9;  k = k + 2){
       // some junk and stuff
       notoptional=(float)1;
       
   }
   
   outer : while(loop)
   {
		inner : while(loop)
		{
			continue outer;
		}
		
		break inner;
   }
   
   read(j, k);
   
   print(j, k);
   
   return 3;
   }

void main( )
{

}

}
