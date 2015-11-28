import java.io.*;
import java.util.*;

class SymbolTable
{

	// Ref to the list and top scope
	LinkedList<HashMap<String, Symb>> scopeList;
	HashMap<String, Symb> currentScope;

	/* Initalize the List to track the scopes */
	SymbolTable()
	{
		scopeList = new LinkedList<HashMap<String, Symb>>();
	}
	// ///////////////////////////////////////////////////////////////////////////////////////////////

	/** Opens a new scope with an empty hash map and adds to the end of the list **/
	public void openScope()
	{
		currentScope = new HashMap<String, Symb>();
		scopeList.add(currentScope);
	}
	// ///////////////////////////////////////////////////////////////////////////////////////////////

	/** Closes the current scope, if it exists **/
	public void closeScope() throws EmptySTException
	{
		boolean check = scopeIsActive();
		if (!check) {
			throw new EmptySTException();
		} else {
			scopeList.remove(currentScope);
			currentScope.clear();
			currentScope = scopeList.peekLast(); // this will set currentScope
												 // to null if it's empty
												 // after
		}
	}

	/* Helper for close scope, checks for hashmap */
	/*
	 * So if current scope was set to null when closing the previous one, it
	 * returns false and throw an exception
	 */
	private boolean scopeIsActive()
	{
		return (currentScope != null);
	}
	// ///////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Inserts the given symb into the table and throws empty exception if it's
	 * null
	 */
	/* Uses same helper function as the close scope check */
	public void insert(Symb s) throws EmptySTException, DuplicateException
	{
		boolean check = scopeIsActive();
		if (!check) {
			throw new EmptySTException();
		} else {
			if (currentScope.containsKey(s.name())) { // current scope contains
													  // the value
				// Throw exception
				throw new DuplicateException();

			} else {
				// add the token
				currentScope.put(s.name(), s);
			}
		}
	}
	// ///////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Local Lookup checks if the current scope hash map contains the given
	 * string
	 */
	/* Uses same helper function as the close scope check */
	public Symb localLookup(String s)
	{

		boolean check = scopeIsActive();
		if (!check) {
			return null;
		} else {

			if (currentScope.containsKey(s)) {
				// Value is in current scope
				return currentScope.get(s);
			} else {
				// Value was not found
				return null;
			}
		}
	}
	// ///////////////////////////////////////////////////////////////////////////////////////////////

	/*
	 * Global Lookup checks if the current scope hash map contains the given
	 * string
	 */
	/*
	 * If it doesn't, it checks all the other scopes until it finds it or hits
	 * null
	 */
	public Symb globalLookup(String s)
	{

		/* Found in local scope, return */
		if (localLookup(s) != null) {
			return localLookup(s);
		} else {
			/* NOT Found in local scope, loop through others */
			HashMap<String, Symb> tmpMap;

			// go through list in reverse to find first scope that has the token
			for (int i = scopeList.size() - 1; i >= 0; i--) {

				tmpMap = scopeList.get(i);

				if (tmpMap.containsKey(s)) {
					// Value is in scope
					return tmpMap.get(s);
				}
			}

			// Exited loop without finding anything
			return null;
		}
	}
	// ///////////////////////////////////////////////////////////////////////////////////////////////

	/* returns a string representation of the symbolTable */

	public String toString()
	{

		String outputString = "";

		HashMap<String, Symb> tmpMap;

		// go through list in reverse to find first scope that has the token
		for (int i = scopeList.size() - 1; i >= 0; i--) {

			tmpMap = scopeList.get(i);
			outputString += tmpMap.toString();
			outputString += "\n";

		}

		return outputString; // change this
	}
	// ///////////////////////////////////////////////////////////////////////////////////////////////

	/* Dumps a string representation of the symbolTable to screen */

	void dump(PrintStream ps)
	{

		ps.print(toString());
		ps.flush();

	}
} // class SymbolTable
