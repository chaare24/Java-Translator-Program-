/* *** This file is given as part of the programming assignment. *** */

import java.util.Stack;


public class Parser {


// tok is global to all these parsing methods;
// scan just calls the scanner's scan method and saves the result in tok.
private Token tok;     // the current token
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
private void program()
{
        MyStack table = new MyStack();
        block(table);
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
        table.stackIndex[++table.curDepth] = start + numDeclared;
        statement_list(table);

        for (int i = start; i < table.stackIndex[table.curDepth]; i++)
          table.t.pop();

        table.curDepth--;
}

// this checks whether tok is in first set of declaration.
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
private int declaration(MyStack table, int start)
{
        int numDeclared = 0;
        mustbe(TK.DECLARE);

        if (is(TK.ID))
        {
          if (table.t.search(tok.string) == -1) // not in stack
          {
            table.t.push(tok.string);
            numDeclared += 1;
          }
          else // already in stack
          {
            if (table.t.search(tok.string) > start)
              System.err.println("redeclaration of variable " + tok.string);
            else
            {
              table.t.push(tok.string);
              numDeclared += 1;
            }
          }

          scan();
        }
        else // not a TOK.ID
          mustbe(TK.ID);

        while( is(TK.COMMA) )
        {
                scan();

                if (is(TK.ID))
                {
                  if (table.t.search(tok.string) == -1) // not in stack
                  {
                    table.t.push(tok.string);
                    numDeclared += 1;
                  }
                  else // already in stack
                    System.err.println("redeclaration of variable " + tok.string);

                  scan();
                }
                else // not a TOK.ID
                  mustbe(TK.ID);
        }

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
private void print(MyStack table)
{
        mustbe(TK.PRINT); // !
        expr(table);
}

// checks if assignment
private boolean isAssignment()
{
        return isRef_id();
}

// determines assignment
private void assignment(MyStack table)
{
        ref_id(table);
        mustbe(TK.ASSIGN);
        expr(table);
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
private void ref_id(MyStack table)
{
        int num = -2; // num stays -2 if it doesn't have ~

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
            if (table.t.search(tok.string) == -1) // not in stack
            {
              System.err.println( tok.string + " is an undeclared variable on line " + tok.lineNumber );
              System.exit(1);
            }
          }
          else // scoping mode
            table.searchUpper(tok.string, tok.lineNumber, num);

          scan();
        }
        else // not a TOK.ID
          mustbe(TK.ID);
}

// checks if correct token
private boolean isDo2()
{
        return is(TK.DO); // <
}

// do statement
private void do2(MyStack table)
{
        mustbe(TK.DO); // <
        guarded_command(table);
        mustbe(TK.ENDDO); // >
}

// checks if correct token
private boolean isIf2()
{
        return is(TK.IF); // [
}

// determines if statement
private void if2(MyStack table)
{
        mustbe(TK.IF); // [
        guarded_command(table);
        while (is(TK.ELSEIF)) // |
        {
                scan();
                guarded_command(table);
        }
        if(is(TK.ELSE)) // %
        {
                scan();
                block(table);
        }
        mustbe(TK.ENDIF); // ]
}

// determines guarded commands
private void guarded_command(MyStack table)
{
        expr(table);
        mustbe(TK.THEN); // :
        block(table);
}

// allows for expressions
private void expr(MyStack table)
{
        term(table);
        while (isAddop())
        {
                addop();
                term(table);
        }
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
private void factor(MyStack table)
{
        if (is(TK.LPAREN)) // (
        {
                scan();
                expr(table);
                mustbe(TK.RPAREN); // )
        }
        else if (isRef_id())
                ref_id(table);
        else if (is(TK.NUM))
                scan();
        else
                mustbe(TK.LPAREN); // FIXME see addop
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
                scan();
        else
                mustbe(TK.PLUS); // FIXME: Idk if error message needs print "mustbe:... +/-"",
                                 //       but this should work fine
                                 // Fix would be throwing the cout in here
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
                scan();
        else
                mustbe(TK.TIMES); // FIXME: see addop, same issue
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
