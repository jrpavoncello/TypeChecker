class testTypeCase {
   int i = 0;
   
   char fooBar( ) {
   int j;
   int k;
   float fl;
   bool loop = false;
  
   for( j = 9; j>0; j--){
       fl=(float)0 + (float)j;
   }
   for(k = 0; k >= 9;  k = k + 2){
      fl=(float)1;
   }
   loop = (bool)k;
   while ((bool)k)
   {   }

   return (char)3;
   }

void main( )
{
    fooBar();
}

}
