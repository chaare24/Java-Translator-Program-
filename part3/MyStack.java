import java.util.Iterator;
import java.util.Stack;

public class MyStack
{
public Stack<String> t;
public int stackIndex[];
public int curDepth;

// Stack store all the ids
// Array stackIndex's index is based off of depth
//    which stackIndex[0] = 0 means depth of 0 starts at stack index of 0
// curDepth marks the current depth level
MyStack()
{
        t = new Stack<>();
        stackIndex = new int[1000];
        curDepth = 0;
        stackIndex[curDepth] = 0;
} // constructor

// Using num passed in as parameter, determine the depth the stack
//    should start searching. depth + 1 will be where it should end.
// Then, using stackIndex, we can find the starting stackIndex for
//    the specificied depth (if the iterator is done searching and
//    the value is not found, error message)
// *also error if num is greater than our curDepth
public void searchUpper(String tok_string, int tok_lineNumber, int num)
{
        Iterator<String> itr = t.iterator();
        int depth = curDepth - 1, counter = 0;
        String temp;

        if (num >= curDepth)
        {
                System.err.println( "no such variable ~" + num + tok_string
                                    + " on line " + tok_lineNumber);
                System.exit(1);
        }
        else if ( num == -1 )
        {
                depth = 0;
        }
        else
                depth -= num;

        while (itr.hasNext())
        {
                temp = itr.next();

                if (counter >= stackIndex[depth + 1])
                        break;

                if (counter >= stackIndex[depth])
                {
                        if (temp.equals(tok_string))
                                return;
                }

                counter++;
        }

        if (num == -1)
                System.err.println( "no such variable ~" + tok_string
                                    + " on line " + tok_lineNumber);
        else
                System.err.println( "no such variable ~" + num + tok_string
                                    + " on line " + tok_lineNumber);
        System.exit(1);
}
}
