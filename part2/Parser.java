/* *** This file is given as part of the programming assignment. *** */

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

private void program() {
        block();
}

private void block(){
        declaration_list();
        statement_list();
}

private void declaration_list() {
        // below checks whether tok is in first set of declaration.
        // here, that's easy since there's only one token kind in the set.
        // in other places, though, there might be more.
        // so, you might want to write a general function to handle that.
        while( is(TK.DECLARE) ) {
                declaration();
        }
}

private void declaration() {
        mustbe(TK.DECLARE);
        mustbe(TK.ID);
        while( is(TK.COMMA) ) {
                scan();
                mustbe(TK.ID);
        }
}

// calls statement() if isStatement
private void statement_list() {
        while(isStatement())
                statement();
}

// checks if statement by calling other methods
private boolean isStatement()
{
        return isAssignment() || isPrint() || isDo2() || isIf2();
}

// Possible types of statements
private void statement()
{
        if (isAssignment())
                assignment();
        else if (isPrint())
                print();
        else if (isDo2())
                do2();
        else if (isIf2())
                if2();
}

// checks if correct token
private boolean isPrint()
{
        return is(TK.PRINT); // !
}

// prints
private void print()
{
        mustbe(TK.PRINT); // !
        expr();
}

// checks if assignment
private boolean isAssignment()
{
        return isRef_id();
}

// determines assignment
private void assignment()
{
        ref_id();
        mustbe(TK.ASSIGN);
        expr();
}

// checks ref_id
private boolean isRef_id()
{
        return is(TK.TILDE) || is(TK.ID);
}

private void ref_id()
{
        if (is(TK.TILDE)) // ~
        {
                scan();

                if (is(TK.NUM))
                        scan();
        }

        mustbe(TK.ID);
}

// checks if correct token
private boolean isDo2()
{
        return is(TK.DO); // <
}

// do statement
private void do2()
{
        mustbe(TK.DO); // <
        guarded_command();
        mustbe(TK.ENDDO); // >
}

// checks if correct token
private boolean isIf2()
{
        return is(TK.IF); // [
}

// determines if statement
private void if2()
{
        mustbe(TK.IF); // [
        guarded_command();
        while (is(TK.ELSEIF)) // |
        {
                scan();
                guarded_command();
        }
        if(is(TK.ELSE)) // %
        {
                scan();
                block();
        }
        mustbe(TK.ENDIF); // ]
}

// determines guarded commands
private void guarded_command()
{
        expr();
        mustbe(TK.THEN); // :
        block();
}

// allows for expressions
private void expr()
{
        term();
        while (isAddop())
        {
                addop();
                term();
        }
}

// determines term
private void term()
{
        factor();
        while (isMultop())
        {
                multop();
                factor();
        }
}

// allows for factors
private void factor()
{
        if (is(TK.LPAREN)) // (
        {
                scan();
                expr();
                mustbe(TK.RPAREN); // )
        }
        else if (isRef_id())
                ref_id();
        else if (is(TK.NUM))
                scan();
        else
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
                scan();
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
