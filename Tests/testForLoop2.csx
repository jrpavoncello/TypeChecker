class test {
int j = 0; int k = 0;
   void f(int i) {
 
   for( j = 9; j>0; j--){
       // some junk and stuff
      int  notoptional=0;
  
        for(k = 0; k >= 9;  k = k + 2){
         // some junk and stuff
          notoptional=0;
       
        }     
        
         for(k = 0; k >= 9;  k = k + (2+3)){
         // some junk and stuff
          notoptional=0;
       
        }     
     }  
   }
}
