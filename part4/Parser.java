/* *** This file is given as part of the programming assignment. *** */

public class Parser {


// tok is global to all these parsing methods;
// scan just calls the scanner's scan method and saves the result in tok.
private Token tok;     // the current token
private int block_ctr;
private void scan() {
        tok = scanner.scan();
}

private Scan scanner;
Parser(Scan scanner) {
        this.scanner = scanner;
        scan();
        program();
        if( tok.kind != TK.EOF )
                parse_error("junk after logical end of program");
}

// created a new stack to store tokens
// add Java keywords
private void program()
{
        MyStack table = new MyStack();

        System.out.printf("public class My_e2j{\n");
        System.out.printf("public static void main(String[] args){\n");
        block(table);
        System.out.printf("}\n}");
}

// numDeclared is the number of id declared in the declaration()
//    It is then used to update stackIndex, marking the next
//    starting index of next depth level
// Whatever is added to the stack in this block is popped off
//    after statement_list() is completed. Which we will also
//    decrement curDepth to reflect that the block has ended
private void block(MyStack table)
{
        int start = table.stackIndex[table.curDepth];
        int numDeclared = declaration_list(table, start);
        block_ctr++;
        table.stackIndex[++table.curDepth] = start + numDeclared;
        statement_list(table);

        for (int i = start; i < table.stackIndex[table.curDepth]; i++)
        {
          table.t.pop();
          table.storage.pop();
        }

        table.curDepth--;
}

// below checks whether tok is in first set of declaration.
// here, that's easy since there's only one token kind in the set.
// in other places, though, there might be more.
// so, you might want to write a general function to handle that.
private int declaration_list(MyStack table, int start)
{
        int numDeclared = 0;
        while( is(TK.DECLARE) ) {
                numDeclared += declaration(table, start);
        }

        return numDeclared;
}

// This checks if the token is in the stack or not in the stack
// If already stored in stack, checks if there is a redeclaration
// of the given token
// added Java keywords
private int declaration(MyStack table, int start)
{
        int numDeclared = 0;
        mustbe(TK.DECLARE);

        if (is(TK.ID))
        {
          if (table.t.search(tok.string) == -1) // not in stack
          {
            table.t.push(tok.string);
            table.storage.push((tok.string+Integer.toString(table.curDepth)));
            numDeclared += 1;
            System.out.printf("int x_"+ tok.string + Integer.toString(table.curDepth)+ ";\n");

          }
          else // already in stack
          {
            if (table.t.search(tok.string) > start)
              System.err.println("redeclaration of variable " + tok.string);
            else
            {
              table.t.push(tok.string);
              table.storage.push((tok.string+Integer.toString(table.curDepth)));
              numDeclared += 1;
              System.out.printf("int x_"+ tok.string + Integer.toString(table.curDepth)+ ";\n");
            }
          }

          scan();
        }
        else // error()
          mustbe(TK.ID);

        while( is(TK.COMMA) )
        {
                scan();

                if (is(TK.ID))
                {
                  if (table.t.search(tok.string) == -1) // not in stack
                  {
                    table.t.push(tok.string);
                    table.storage.push((tok.string+Integer.toString(table.curDepth)));
                    numDeclared += 1;
                    System.out.printf("int x_" + tok.string + Integer.toString(table.curDepth) + ";\n");
                  }
                  else // already in stack
                  {
                    System.err.println("redeclaration of variable " + tok.string);
                  }


                // System.out.print();
                 scan();
                }
                else // error()
                  mustbe(TK.ID);
        }

        System.out.printf(";\n");
        return numDeclared;
}

// calls statement() if isStatement
private void statement_list(MyStack table)
{
        while(isStatement())
                statement(table);
}

// checks if statement by calling other methods
private boolean isStatement()
{
        return isAssignment() || isPrint() || isDo2() || isIf2();
}

// Possible types of statements
private void statement(MyStack table)
{
        if (isAssignment())
                assignment(table);
        else if (isPrint())
                print(table);
        else if (isDo2())
                do2(table);
        else if (isIf2())
                if2(table);
}

// checks if correct token
private boolean isPrint()
{
        return is(TK.PRINT); // !
}

// prints
// added Java keywords
private void print(MyStack table)
{
        mustbe(TK.PRINT); // !
        System.out.printf("System.out.println(\"\" + ");
        expr(table);
        System.out.printf(");\n");
}

// checks if assignment
private boolean isAssignment()
{
        return isRef_id();
}

// determines assignment
// added Java keywords
private void assignment(MyStack table)
{
        ref_id(table);
        mustbe(TK.ASSIGN);
        System.out.printf(" = ");
        expr(table);
        System.out.printf(";\n");
}

// checks ref_id
private boolean isRef_id()
{
        return is(TK.TILDE) || is(TK.ID);
}

// To distinguish scoping vs. normal, num is initalized to -2.
// if it is not altered by the time it gets to is(TK.ID), it is
//    normal mode and simply check if it is numDeclared
// if it is altered:
//    1) num == -1   -   means its a global (~id)
//    2) num == 0+   -   means its in previous depth (~num id)
// which then searchUpper() will take care the rest
// deals with the difference between scopes to get the right scope
// added Java keywords
private void ref_id(MyStack table)
{

        int num = -2; // num stays -2 if it doesn't have ~
        int stackIndex = 0; // where token is in stack
        int tempDepth = table.curDepth; // temp size
        int diff = 0; // difference of depths for scope

        if (is(TK.TILDE)) // ~
        {
                scan();

                if (is(TK.NUM)) // '~''num''id'
                {
                        num = Integer.parseInt(tok.string);
                        scan();
                }
                else // '~''id'
                        num = -1;
        }

        if (is(TK.ID))
        {
          if (num == -2) // normal mode
          {
            stackIndex = table.t.search(tok.string);
            if (stackIndex == -1) // not in stack
            {
              System.err.println( tok.string + " is an undeclared variable on line " + tok.lineNumber );
              System.exit(1);
            }
            System.out.printf("x_" + (tok.string) + table.stackIndexToDepth(table.curDepth, tok.string)); //FIXME
          }
          else // scoping mode
          {
            table.searchUpper(tok.string, tok.lineNumber, num);

            //System.out.print("\n\nnum:"+ num+" curDepth: " + table.curDepth + "\n\n");
            if (num == 0)
            {
              num = Math.abs(1-num);

            } else if (num == 1)
            {
              num += 1;
              diff = 0;
            } else
            {
              num = Math.abs(1-num);
              diff = Math.abs(num-table.curDepth);

            }
            int val = table.curDepth-(num+diff);

            if (val < 0)
              val = 0;


            System.out.printf("x_" + tok.string + Integer.toString(val));
          }

          scan();
        }
        else // error()
          mustbe(TK.ID);
}

// checks if correct token
private boolean isDo2()
{
        return is(TK.DO); // <
}

// do statement
// added Java keywords
private void do2(MyStack table)
{
        mustbe(TK.DO); // <
        System.out.printf("while (");
        guarded_command(table);
        mustbe(TK.ENDDO); // >
}

// checks if correct token
private boolean isIf2()
{
        return is(TK.IF); // [
}

// determines if statement
// added Java keywords
private void if2(MyStack table)
{
        mustbe(TK.IF); // [
        System.out.printf("if (");
        guarded_command(table);

        while (is(TK.ELSEIF)) // |
        {
                System.out.printf("else if (");
                scan();
                guarded_command(table);
        }

        if(is(TK.ELSE)) // %
        {
                System.out.printf("else\n{\n");
                scan();
                block(table);
                System.out.printf("}\n");
        }

        mustbe(TK.ENDIF); // ]

}

// determines guarded commands
// added Java keywords
private void guarded_command(MyStack table)
{
        expr(table);
        mustbe(TK.THEN); // :
        System.out.printf(" <= 0)\n{\n");
        block(table);
        System.out.printf("}\n");
}

// allows for expressions
// added Java keywords
private void expr(MyStack table)
{
        System.out.printf("(");
        term(table);
        while (isAddop())
        {
                addop();
                term(table);
        }
        System.out.printf(")");
}

// determines term
private void term(MyStack table)
{
        factor(table);
        while (isMultop())
        {
                multop();
                factor(table);
        }
}

// allows for factors
// added Java keywords
private void factor(MyStack table)
{
        if (is(TK.LPAREN)) // (
        {
                System.out.printf("( ");
                scan();
                expr(table);
                mustbe(TK.RPAREN); // )
                System.out.printf(") ");
        }
        else if (isRef_id())
                ref_id(table);
        else if (is(TK.NUM))
        {
                System.out.printf(tok.string);
                scan();
        }
        else // error()
                mustbe(TK.LPAREN);
}

// checks if correct token
private boolean isAddop()
{
        return is(TK.PLUS) || is(TK.MINUS);
}

// allows for add operation
private void addop()
{
        if (is(TK.PLUS) || is(TK.MINUS))
        {
                System.out.printf(" " + tok.string + " ");
                scan();
        }
        else
                mustbe(TK.PLUS);
}

// checks if correct token
private boolean isMultop()
{
        return is(TK.TIMES) || is(TK.DIVIDE);
}

// allows for mult operation
private void multop()
{
        if (is(TK.TIMES) || is(TK.DIVIDE))
        {
                System.out.printf(" " + tok.string + " ");
                scan();
        }
        else
                mustbe(TK.TIMES);
}

// is current token what we want?
private boolean is(TK tk) {
        return tk == tok.kind;
}

// ensure current token is tk and skip over it.
private void mustbe(TK tk) {
        if( tok.kind != tk ) {
                System.err.println( "mustbe: want " + tk + ", got " +
                                    tok);
                parse_error( "missing token (mustbe)" );
        }
        scan();
}

private void parse_error(String msg) {
        System.err.println( "can't parse: line "
                            + tok.lineNumber + " " + msg );
        System.exit(1);
}
}
