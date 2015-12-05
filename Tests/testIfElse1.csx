class A {
void main() {
    bool x = true;  bool y = true;
    int i = 0;
   // TEST1: each IF has a corresponding ELSE
     if (x)  
         if (y)  i=2; 
         else   { i=3; } 
		 endif
     
     else 
         if (y) i=2;
         else i=3;
		 endif
     endif


  // TEST2: each IF has a corresponding ELSE
     if (x) 
         if (y) i=2;
         else   i=3;
		 endif
     else
         if (y) i=2;
         else i=3;
		 endif
	endif

  // TEST3: first nested IF does not have an ELSE
     if (x) {
         if (y) i=4; endif
     }
     else
         i=5;
	endif

} }
