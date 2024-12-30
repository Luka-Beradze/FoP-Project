# FoP-Project

### Members of the group:
- Luka Beradze
- Giorgi Kveliashvili
- Tsotne Tsutskiridze
- Ana Kakhidze
  
    
__User Guide available at the [bottom](#user-guide) of this README file.__

### Brief Description of How Our Interpreter Works:  
User feeds text file consisting of Ruby code to the Interpreter. Our program reads this file and executes code line by line, storing variables in a key-value Map. For validating code line correctness and Syntax Error detection we used Regular Expression (regex), "https://regex101.com/" helped greatly. Our program assures that scoping of while, if, and else works properly by creating additional key-value Maps for inner scope variables and updating outer scope variables after leaving the scope.

# Our Ruby Interpreter Guide and Ruby language subset we used.
1. Variable Assignment.
   - Variable assignment supports arithmetic operations (+, -, *, /, %) and compound assignment (+=, -=, *=, /=, %=).
   - You can either assign a number or a boolean.
   - Variable names in Ruby and, likewise, our program can only start with lowercase letters or "_", and contain any letter, digit, or _.
   - You can easily assign a *variable* like so:  
   ```
   a = 5  
   a += 10
   b = a - 9
   boolean_example = true
   c = (a + b) / 3
   ```
2. To print out a value/number, use **puts** keyword, as in Ruby.  
   ```  
   puts 5
   puts c
   puts boolean_example 
   ```
   > $ 5  
   > $ 7  
   > $ true
    
3. **if/else** statements work, don't forget to end the scope using **end** keyword. if condition along with comparators, also support boolean variable and raw boolean.
   ```
   if a < b  
     c -= b
   else
     c += b
   end

   puts c
   ```
   > $ 13
4. For iterations use **while** loop, which also ends with **end** keyword. while loop condition along with comparators, also support boolean variable and raw boolean.
   ```
   b = a + 4
   while a < b
     a += 4
   end

   puts a
   ```
   > $ 19
5. Our Interpreter detects Syntax Errors.
     
Tip: make sure that scope indentation is two spaces:  
```
outer_scope
  inner_scope
outer_scope
```

# User Guide
To use our Ruby Interpreter: Run the Interpreter.java file; it'll prompt u for an address of a file; paste the address of a text file containing Ruby code in the terminal and press Enter.  
Example:
> ` Enter an address of a Ruby algorithm text file: `
> 
> ` Algorithms/SumOfN.txt `
  
We *recommend* using **(tester should use)** our algorithm text files from src/Algorithms folder containing 10 Ruby Algorithms (given by university in the project guide) that we implemented for **Testing**.
   
